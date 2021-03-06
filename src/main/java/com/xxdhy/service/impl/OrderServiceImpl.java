package com.xxdhy.service.impl;

import com.alipay.api.AlipayResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.demo.trade.config.Configs;
import com.alipay.demo.trade.model.ExtendParams;
import com.alipay.demo.trade.model.GoodsDetail;
import com.alipay.demo.trade.model.builder.AlipayTradePrecreateRequestBuilder;
import com.alipay.demo.trade.model.result.AlipayF2FPrecreateResult;
import com.alipay.demo.trade.service.AlipayTradeService;
import com.alipay.demo.trade.service.impl.AlipayTradeServiceImpl;
import com.alipay.demo.trade.utils.ZxingUtils;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.xxdhy.common.Const;
import com.xxdhy.common.ServerResponse;
import com.xxdhy.dao.*;
import com.xxdhy.pojo.*;
import com.xxdhy.service.IOrderService;

import com.xxdhy.util.BigDecimalUtil;
import com.xxdhy.util.DateTimeUtil;
import com.xxdhy.util.FTPUtil;
import com.xxdhy.util.PropertiesUtil;
import com.xxdhy.vo.OrderItemVo;
import com.xxdhy.vo.OrderProductVo;
import com.xxdhy.vo.OrderVo;
import com.xxdhy.vo.ShippingVo;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;


@Service
public class OrderServiceImpl implements IOrderService {

     private static final Logger logger= LoggerFactory.getLogger(OrderServiceImpl.class);

     @Autowired
     private OrderMapper orderMapper;
     @Autowired
     private OrderItemMapper orderItemMapper;
     @Autowired
     private PayInfoMapper payInfoMapper;
     @Autowired
     private CartMapper cartMapper;
     @Autowired
     private ProductMapper productMapper;
     @Autowired
     private ShippingMapper shippingMapper;


  public ServerResponse getOrderCartProduct(Integer userId){
      OrderProductVo orderProductVo= new OrderProductVo();
      //???????????????????????????

      List<Cart> cartList =cartMapper.selectCheckedCartByUserId(userId);
      ServerResponse serverResponse=this.getCartOrderItem(userId,cartList);
      if(!serverResponse.isSuccess()){
          return serverResponse;
      }
      List<OrderItem> orderItemList = (List<OrderItem>)serverResponse.getData();

      List<OrderItemVo> orderItemVoList=Lists.newArrayList();

      BigDecimal payment = new BigDecimal("0");
      for (OrderItem orderItem : orderItemList){
          payment = BigDecimalUtil.add(payment.doubleValue(),orderItem.getTotalPrice().doubleValue());
          orderItemVoList.add(assembleOrderItemVo(orderItem));
      }
      orderProductVo.setProductTotalPrice(payment);
      orderProductVo.setOrderItemVoList(orderItemVoList);
      orderProductVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));

      return ServerResponse.createBySuccess(orderProductVo);
  }

    /**
     *     ????????????n
     */
    public ServerResponse<String> cancel(Integer userId,Long orderNo){
         Order order =orderMapper.selectByUserIdAndOrderNo(userId, orderNo);
         //??????
         if(order==null){
             return ServerResponse.createByErrorMessage("???????????????????????????");
         }
         if(order.getStatus() != Const.OrderStatusEnum.NO_PAY.getCode()){
                   return ServerResponse.createByErrorMessage("??????????????????????????????");
         }
         Order updateOrder = new Order();
         /*
         *    ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
         * */
         updateOrder.setId(order.getId());
         updateOrder.setStatus(Const.OrderStatusEnum.CANCELED.getCode());

         int row = orderMapper.updateByPrimaryKeySelective(updateOrder);
         if (row>0) {
             return ServerResponse.createBySuccess();
         }
         return ServerResponse.createByError();
     }

     public ServerResponse createOrder(Integer userId,Integer shippingId){

         //???????????????????????????
         List<Cart> cartList = cartMapper.selectCheckedCartByUserId(userId);

         //???????????????????????????
         ServerResponse serverResponse = this.getCartOrderItem(userId,cartList);
         if(!serverResponse.isSuccess()){
             return serverResponse;
         }
          List<OrderItem> orderItemList=(List<OrderItem>)serverResponse.getData();
         BigDecimal payment =this.getCartOrderItem(orderItemList);

         //????????????
         Order order =this.assembleOrder(userId,shippingId,payment);
         if(order ==null){
             return ServerResponse.createByErrorMessage("??????????????????");
         }
         if(CollectionUtils.isEmpty(orderItemList)){
             return ServerResponse.createByErrorMessage("???????????????");
         }
         for(OrderItem orderItem:orderItemList){
             orderItem.setOrderNo(order.getOrderNo());
         }
         //mybatis ????????????
         orderItemMapper.batchInsert(orderItemList);

         //?????????????????????????????????????????????
         this.reduceProductStock(orderItemList);
         //?????????????????????
         this.cleanCart(cartList);

         //?????????????????????
         OrderVo orderVo =assembleOrderVo(order,orderItemList);
         return ServerResponse.createBySuccess(orderVo);
     }

     private OrderVo assembleOrderVo(Order order,List<OrderItem> orderItemList){
         OrderVo orderVo=new OrderVo();
         orderVo.setOrderNo(order.getOrderNo());
         orderVo.setPayment(order.getPayment());
         orderVo.setPaymentType(order.getPaymentType());
         orderVo.setPaymentTypeDesc(Const.PaymentTypeEnum.codeOf(order.getPaymentType()).getValue());

         orderVo.setPostage(order.getPostage());
         orderVo.setStatus(order.getStatus());
         orderVo.setStatusDesc(Const.OrderStatusEnum.codeOf(order.getStatus()).getValue());
         Shipping shipping = shippingMapper.selectByPrimaryKey(order.getShippingId());
         if(shipping !=null){
              orderVo.setReceiverName(shipping.getReceiverName());
              orderVo.setShippingVo(assembleShippingVo(shipping));
              orderVo.setShippingId(order.getShippingId());
         }

         orderVo.setPaymentTime(DateTimeUtil.dateToStr(order.getPaymentTime()));
         orderVo.setCloseTime(DateTimeUtil.dateToStr(order.getCloseTime()));
         orderVo.setCreateTime(DateTimeUtil.dateToStr(order.getCreateTime()));
         orderVo.setEndTime(DateTimeUtil.dateToStr(order.getEndTime()));
         orderVo.setSendTime(DateTimeUtil.dateToStr(order.getSendTime()));

         orderVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));

         List<OrderItemVo> orderItemVoList=Lists.newArrayList();
         for(OrderItem orderItem : orderItemList){
             OrderItemVo orderItemVo=assembleOrderItemVo(orderItem);
             orderItemVoList.add(orderItemVo);
         }
         orderVo.setOrderItemVoList(orderItemVoList);
         return orderVo;
     }

     private OrderItemVo assembleOrderItemVo(OrderItem orderItem){
         OrderItemVo orderItemVo = new OrderItemVo();
         orderItemVo.setOrderNo(orderItem.getOrderNo());
         orderItemVo.setCurrentUnitPrice(orderItem.getCurrentUnitPrice());
         orderItemVo.setTotalPrice(orderItem.getTotalPrice());
         orderItemVo.setQuantity(orderItem.getQuantity());
         orderItemVo.setProductImage(orderItem.getProductImage());
         orderItemVo.setProductName(orderItem.getProductName());
         orderItemVo.setProductId(orderItem.getProductId());

         orderItemVo.setCreateTime(DateTimeUtil.dateToStr(orderItem.getCreateTime()));
        return orderItemVo;
     }

      //?????? shippingVO
      private ShippingVo assembleShippingVo(Shipping shipping){
         ShippingVo shippingVo=new ShippingVo();
         shippingVo.setReceiverAddress(shipping.getReceiverAddress());
         shippingVo.setReceiverCity(shipping.getReceiverCity());
         shippingVo.setReceiverDistrict(shipping.getReceiverDistrict());
         shippingVo.setReceiverMoblie(shipping.getReceiverMoblie());
         shippingVo.setReceiverZip(shipping.getReceiverZip());
         shippingVo.setReceiverProvince(shipping.getReceiverProvince());
         shippingVo.setReceiverPhone(shipping.getReceiverPhone());
         shippingVo.setReceiverName(shipping.getReceiverName());

         return shippingVo;
      }

     //??????????????????
     private  void reduceProductStock(List<OrderItem> orderItemList){
          for(OrderItem orderItem:orderItemList){
              Product product = productMapper.selectByPrimaryKey(orderItem.getProductId());
              product.setStock(product.getStock()-orderItem.getQuantity());
              productMapper.updateByPrimaryKeySelective(product);
         }
     }
    //???????????????
      private void cleanCart(List<Cart> cartList){
         for(Cart cart : cartList){
             cartMapper.deleteByPrimaryKey(cart.getId());
         }
     }

     private Order assembleOrder(Integer userId,Integer shippingId,BigDecimal payment){
         Order order = new Order();
         long orderNo = this.generateOrderNo();
         order.setOrderNo(orderNo);
         order.setStatus(Const.OrderStatusEnum.NO_PAY.getCode());
         order.setPostage(0);//??????
         order.setPaymentType(Const.PaymentTypeEnum.ONLINE_PAY.getCode());
         order.setPayment(payment);

         order.setUserId(userId);
         order.setShippingId(shippingId);
         //??????????????????
         //??????????????????
         int rowCount = orderMapper.insert(order);
         if(rowCount > 0){
             return order;
         }
        return null;
     }

     //?????????????????????????????????
     private long generateOrderNo(){
           long currentTime =System.currentTimeMillis();
           return currentTime+ new Random().nextInt(100);
     }

     private BigDecimal getCartOrderItem(List<OrderItem> orderItemList){
            BigDecimal payment = new BigDecimal("0");
            for(OrderItem orderItem:orderItemList){
             payment =BigDecimalUtil.add(payment.doubleValue(),orderItem.getTotalPrice().doubleValue());
            }
            return payment;
     }

     private ServerResponse getCartOrderItem(Integer userId,List<Cart> cartList){
        List<OrderItem> orderItemList=Lists.newArrayList();
        if(CollectionUtils.isEmpty(cartList)){
            return ServerResponse.createByErrorMessage("???????????????");
        }
        //?????????????????????????????????????????????????????????
         for(Cart cartItem : cartList){
             OrderItem orderItem=new OrderItem();
             Product product=productMapper.selectByPrimaryKey(cartItem.getProductId());
             if(Const.ProductStatusEnum.ON_SALE.getCode()!=product.getStatus()){
                 return ServerResponse.createByErrorMessage("??????????????????????????????");
             }
             //????????????
             if(cartItem.getQuantitiy()>product.getStock()){
                 return ServerResponse.createByErrorMessage("??????"+product.getName()+"????????????");
             }
             orderItem.setUserId(userId);
             orderItem.setProductId(product.getId());
             orderItem.setProductName(product.getName());
             orderItem.setProductImage(product.getMainImage());
             orderItem.setQuantity(cartItem.getQuantitiy());
             orderItem.setCurrentUnitPrice(product.getPrice());
             orderItem.setTotalPrice(BigDecimalUtil.mul(product.getPrice().doubleValue(),cartItem.getQuantitiy()));
             orderItemList.add(orderItem);
         }
         return ServerResponse.createBySuccess(orderItemList);
     }


    /**
     *    ??????????????????
     */
    public ServerResponse<OrderVo> getOrderDetail(Integer userId,Long orderNo){
        Order order = orderMapper.selectByUserIdAndOrderNo(userId, orderNo);
        if(order!=null){
            List<OrderItem> orderItemList=orderItemMapper.getByOrderNoUserId(orderNo, userId);
            OrderVo orderVo=assembleOrderVo(order,orderItemList);
            return ServerResponse.createBySuccess(orderVo);
        }
        return ServerResponse.createByErrorMessage("?????????????????????");
    }

    /**
     *
     *   ????????????????????????
     */
     public ServerResponse<PageInfo> getOrderList(Integer userId,int pageNum,int pageSize){
         PageHelper.startPage(pageNum,pageSize);
         List<Order> orderList=orderMapper.selectByUserId(userId);
         List<OrderVo> orderVoList = assembleOrderVoList(orderList,userId);
         PageInfo pageResult = new PageInfo(orderVoList);
         return ServerResponse.createBySuccess(pageResult);
     }

     private List<OrderVo> assembleOrderVoList(List<Order> orderList,Integer userId){
         List<OrderVo> orderVoList= Lists.newArrayList();
         for (Order order:orderList){
             List<OrderItem> orderItemList=Lists.newArrayList();
             if(userId == null){
                 //todo ?????????????????????????????????????????????userid
                 orderItemList =orderItemMapper.getByOrderNo(order.getOrderNo());

             }else{
                orderItemList= orderItemMapper.getByOrderNoUserId(order.getOrderNo(),userId);
             }
             OrderVo orderVo=assembleOrderVo(order,orderItemList);
             orderVoList.add(orderVo);
         }
         return orderVoList;
     }




     //backend
        public ServerResponse<PageInfo> manageList(int pageNum,int pageSize){
              PageHelper.startPage(pageNum,pageSize);
              List<Order> orderList =orderMapper.selectAllOrder();
              List<OrderVo> orderVoList=this.assembleOrderVoList(orderList,null);
              PageInfo pageResult = new PageInfo(orderList);
              pageResult.setList(orderVoList);
              return ServerResponse.createBySuccess(pageResult);

     }
      public ServerResponse<OrderVo> manageDetail(Long orderNo){
         Order order = orderMapper.selectByOrderNo(orderNo);
         if(order!=null){
             List<OrderItem> orderItemList=orderItemMapper.getByOrderNo(orderNo);
             OrderVo orderVo = assembleOrderVo(order,orderItemList);
             return ServerResponse.createBySuccess(orderVo);
         }
         return ServerResponse.createByErrorMessage("???????????????");
    }

    public ServerResponse<PageInfo> managerSearch(Long orderNo,int pageNum,int pageSize){
        PageHelper.startPage(pageNum,pageSize);
        Order order = orderMapper.selectByOrderNo(orderNo);
        if(order!=null){
            List<OrderItem> orderItemList=orderItemMapper.getByOrderNo(orderNo);
            OrderVo orderVo = assembleOrderVo(order,orderItemList);

            PageInfo pageResult = new PageInfo(Lists.newArrayList(order));
            pageResult.setList(Lists.newArrayList(orderVo));
            return ServerResponse.createBySuccess(pageResult);
        }
        return ServerResponse.createByErrorMessage("???????????????");
    }

     public ServerResponse<String> manageSendGoods(Long orderNo){
           Order order= orderMapper.selectByOrderNo(orderNo);
           if(order!=null){
               if(order.getStatus() == Const.OrderStatusEnum.PAID.getCode()){
                  order.setStatus(Const.OrderStatusEnum.SHIPPED.getCode());
                  order.setSendTime(new Date());
                  orderMapper.updateByPrimaryKeySelective(order);
                  return ServerResponse.createBySuccess("????????????");
               }
           }
         return ServerResponse.createByErrorMessage("???????????????");
     }





     public ServerResponse pay(Long orderNo,Integer userId,String path)  {
         Map<String,String> resultMap= Maps.newHashMap();
         Order order=orderMapper.selectByUserIdAndOrderNo(userId, orderNo);
         if(order==null){
             return ServerResponse.createByErrorMessage("?????????????????????");
         }
         resultMap.put("orderNo",String.valueOf(order.getOrderNo()));

         // (??????) ?????????????????????????????????????????????64????????????????????????????????????????????????????????????
         // ????????????????????????????????????????????????????????????sequence?????????
         String outTradeNo =order.getOrderNo().toString();

         // (??????) ?????????????????????????????????????????????????????????xxx??????xxx??????????????????????????????
         String subject = new StringBuilder().append("Youlammall????????????,?????????:").append(outTradeNo).toString();

         // (??????) ?????????????????????????????????????????????1??????
         // ???????????????????????????????????????,????????????????????????,???????????????????????????,???????????????????????????:?????????????????????=??????????????????+????????????????????????
         String totalAmount = order.getPayment().toString();

         // (??????) ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
         // ?????????????????????,?????????????????????????????????,??????????????????,???????????????????????????????????????-??????????????????
         String undiscountableAmount = "0";

         // ?????????????????????ID???????????????????????????????????????????????????????????????????????????(?????????sellerId????????????????????????)
         // ??????????????????????????????????????????????????????????????????PID????????????appid?????????PID
         String sellerId = "";

         // ?????????????????????????????????????????????????????????????????????????????????"????????????2??????15.00???"
         String body =new StringBuilder().append("??????").append(outTradeNo).append("???????????????").append(totalAmount).append("???").toString();

         // ??????????????????????????????????????????????????????????????????????????????
         String operatorId = "test_operator_id";

         // (??????) ???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
         String storeId = "test_store_id";

         // ????????????????????????????????????????????????????????????????????????(??????setSysServiceProviderId??????)???????????????????????????????????????
         ExtendParams extendParams = new ExtendParams();
         extendParams.setSysServiceProviderId("2088100200300400500");

         // ????????????????????????120??????
         String timeoutExpress = "120m";

         // ?????????????????????????????????????????????????????????
         List<GoodsDetail> goodsDetailList = new ArrayList<GoodsDetail>();

         List<OrderItem> orderItemList =orderItemMapper.getByOrderNoUserId(orderNo,userId);
         for(OrderItem orderItem:orderItemList){
             GoodsDetail goods1 = GoodsDetail.newInstance(orderItem.getProductId().toString(),orderItem.getProductName(),
                     BigDecimalUtil.mul(orderItem.getCurrentUnitPrice().doubleValue(),new Double(100)).longValue(),
                     orderItem.getQuantity());
             goodsDetailList.add(goods1);
         }


         // ????????????????????????builder?????????????????????
         AlipayTradePrecreateRequestBuilder builder = new AlipayTradePrecreateRequestBuilder()
                 .setSubject(subject).setTotalAmount(totalAmount).setOutTradeNo(outTradeNo)
                 .setUndiscountableAmount(undiscountableAmount).setSellerId(sellerId).setBody(body)
                 .setOperatorId(operatorId).setStoreId(storeId).setExtendParams(extendParams)
                 .setTimeoutExpress(timeoutExpress)
                 .setNotifyUrl(PropertiesUtil.getProperty("alipay.callback.url"))//???????????????????????????????????????????????????????????????http??????,??????????????????
                 .setGoodsDetailList(goodsDetailList);

         Configs.init("zfbinfo.properties");

         /** ??????Configs?????????????????????
          *  AlipayTradeService???????????????????????????????????????????????????????????????new
          */

         AlipayTradeService tradeService = new AlipayTradeServiceImpl.ClientBuilder().build();
         AlipayF2FPrecreateResult result = tradeService.tradePrecreate(builder);
         switch (result.getTradeStatus()) {
             case SUCCESS:
                logger.info("????????????????????????: )");

                 AlipayTradePrecreateResponse response = result.getResponse();
                 dumpResponse(response);//???????????????

                 File folder = new File(path);
                 if(!folder.exists()){
                     folder.setWritable(true);
                     folder.mkdirs();
                 }


                 // ???????????????????????????????????????
                 //?????????????????????
                 String qrPath = String.format(path+"/qr-%s.png",
                         response.getOutTradeNo());
                 String qrFileName=String.format("qr-%s.png",response.getOutTradeNo());
                 ZxingUtils.getQRCodeImge(response.getQrCode(), 256, qrPath);

                 File targetFile = new File(path,qrFileName);

                 try {
                     FTPUtil.uploadFile(Lists.newArrayList(targetFile));
                 } catch (IOException e) {
                     logger.error("?????????????????????",e);
                 }

                 logger.info("qrpath:" + qrPath);
                 String qrUrl=PropertiesUtil.getProperty("ftp.server.http.prefix")+targetFile.getName();
                 resultMap.put("qrUrl",qrUrl);
                 //                ZxingUtils.getQRCodeImge(response.getQrCode(), 256, filePath);
                 return ServerResponse.createBySuccess(resultMap);

             case FAILED:
                 logger.error("????????????????????????!!!");
                return  ServerResponse.createByErrorMessage("????????????????????????!!!");

             case UNKNOWN:
                 logger.error("????????????????????????????????????!!!");
                 return  ServerResponse.createByErrorMessage("????????????????????????????????????!!!");

             default:
                 logger.error("?????????????????????????????????????????????!!!");
                 return  ServerResponse.createByErrorMessage("?????????????????????????????????????????????!!!");
         }

     }
     //?????????????????????
    private void dumpResponse(AlipayResponse response) {
        if (response != null) {
            logger.info(String.format("code:%s, msg:%s", response.getCode(), response.getMsg()));
            if (StringUtils.isNotEmpty(response.getSubCode())) {
                logger.info(String.format("subCode:%s, subMsg:%s", response.getSubCode(),
                        response.getSubMsg()));
            }
            logger.info("body:" + response.getBody());
        }
    }

    public ServerResponse aliCallback(Map<String,String> params) {
          Long orderNo = Long.parseLong(params.get("out_trade_no"));
          String tradeNo = params.get("trade_no");
          String tradeStatus = params.get("trade_status");
          Order order=orderMapper.selectByOrderNo(orderNo);
          if(order==null){
              return ServerResponse.createByErrorMessage("??????????????????????????????");
          }
          if(order.getStatus()>= Const.OrderStatusEnum.PAID.getCode()){
              return ServerResponse.createBySuccessMessage("?????????????????????");
          }
          if(Const.AlipayCallback.TRADE_STATUS_TRADE_SUCCESS.equals(tradeStatus)){
              try {
                  order.setPaymentTime(DateTimeUtil.strToDate(params.get("gmt_payment")));
              } catch (ParseException e) {
                  e.printStackTrace();
              }
              order.setStatus(Const.OrderStatusEnum.PAID.getCode());//????????????
              //??????????????????????????????
              orderMapper.updateByPrimaryKeySelective(order);
          }

          PayInfo payInfo = new PayInfo();
          payInfo.setUserId(order.getUserId());
          payInfo.setOrderNo(order.getOrderNo());
          payInfo.setPayPlatform(Const.PayPlatformEnum.AlIPAY.getCode());
          payInfo.setPlatformNumber(tradeNo);
          payInfo.setPlatformStatus(tradeStatus);

          payInfoMapper.insert(payInfo);

          return ServerResponse.createBySuccess();
    }

    public ServerResponse queryOrderPayStatus(Integer userId,Long orderNo){
         Order order = orderMapper.selectByUserIdAndOrderNo(userId,orderNo);
         if(order==null){
             return ServerResponse.createByErrorMessage("?????????????????????");
         }
         if(order.getStatus()>=Const.OrderStatusEnum.PAID.getCode()){
             return ServerResponse.createBySuccess();
         }
         return ServerResponse.createByError();
    }




}
