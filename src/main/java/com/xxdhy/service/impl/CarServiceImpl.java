package com.xxdhy.service.impl;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.xxdhy.common.Const;
import com.xxdhy.common.ResponseCode;
import com.xxdhy.common.ServerResponse;
import com.xxdhy.dao.CartMapper;
import com.xxdhy.dao.ProductMapper;
import com.xxdhy.pojo.Cart;
import com.xxdhy.pojo.Product;
import com.xxdhy.service.ICartService;
import com.xxdhy.util.BigDecimalUtil;
import com.xxdhy.util.PropertiesUtil;
import com.xxdhy.vo.CartProductVo;
import com.xxdhy.vo.CartVo;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import java.util.List;

@Service
public class CarServiceImpl implements ICartService {

            @Autowired
           private CartMapper cartMapper;
            @Autowired
            private ProductMapper productMapper;
         /*
             添加产品至购物车的方法
          */
         public ServerResponse<CartVo> add(Integer userId,Integer productId,Integer count){
             if(productId==null|| count==null){
                 return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
             }
             Cart cart =cartMapper.selectByUserIdAndProductId(userId,productId);
             if(cart==null){
                 Cart cartItem=new Cart();
                 cartItem.setUserId(userId);
                 cartItem.setProductId(productId);
                 cartItem.setQuantity(count);
                 cartItem.setChecked(Const.Cart.CHECKED);//默认为选中状态
                 cartMapper.insert(cartItem);
             }else{
                 //这个产品已经在购物车里
                 //如果产品已存在，数量相加
                 count=cart.getQuantitiy()+count;
                 cart.setQuantity(count);
                 cartMapper.updateByPrimaryKey(cart);
             }
             return this.list(userId);
         }
  /*
       更新购物车中的产品数量
   */
         public ServerResponse<CartVo> update(Integer userId,Integer productId,Integer count){
             if(productId==null|| count==null){
                 return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
             }
            //先找到该用户选择修改产品数量的购物车
            Cart cart =cartMapper.selectByUserIdAndProductId(userId, productId);
            if(cart != null){
                cart.setQuantity(count);
            }
            cartMapper.updateByPrimaryKeySelective(cart);
             return this.list(userId);
         }

    @Override
    public ServerResponse<CartVo> deleteProduct(Integer userId, String productIds) {
        List<String> productList=Splitter.on(",").splitToList(productIds);
        //判断集合
        if(CollectionUtils.isEmpty(productList)){
            return  ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        cartMapper.deleteByUserIdProductIds(userId,productList);
        return this.list(userId);
    }

    @Override
    public ServerResponse<CartVo> list(Integer id) {
        CartVo cartVo=this.getCartVoLimit(id);
        return ServerResponse.createBySuccess(cartVo);
    }

    //
        private CartVo getCartVoLimit(Integer userId){
             CartVo cartVO=new CartVo();
             List<Cart> cartList=cartMapper.selectList(userId);//老师的是selectCartByUserId;
             List<CartProductVo> cartProductVoList= Lists.newArrayList();

             BigDecimal cartTotalPrice=new BigDecimal("0");//数字计算的时候如何避免丢失精度

            if(CollectionUtils.isNotEmpty(cartList)){
                //增强for循环
                for(Cart cartItem : cartList) {
                    CartProductVo cartProductVo = new CartProductVo();
                    cartProductVo.setId(cartItem.getId());//购物车id
                    cartProductVo.setProductId(cartItem.getProductId());//商品id
                    cartProductVo.setUserId(cartItem.getUserId());//用户id

                    Product product = productMapper.selectByPrimaryKey(cartItem.getProductId());
                    if (product != null) {
                        cartProductVo.setProductName(product.getName());//名称
                        cartProductVo.setProductSbutitle(product.getSubtitle());//副标题
                        cartProductVo.setProductPrice(product.getPrice());//单价
                        cartProductVo.setProductStock(product.getStock());//库存
                        cartProductVo.setProductStatus(product.getStatus());//状态
                        cartProductVo.setMainImage(product.getMainImage());//主图

                        //若购物车的选择的数量大于商品的库存
                        int buyLimitCount = 0;
                        if (product.getStock() >= cartItem.getQuantitiy()) {
                            //库存充足
                            buyLimitCount = cartItem.getQuantitiy();
                            cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_SUCCESS);
                        } else {
                            //库存不足
                            buyLimitCount = product.getStock();
                            cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_FAIL);
                            //购物车中更新有效库存
                            Cart carForQuantity = new Cart();
                            carForQuantity.setId(cartItem.getId());
                            carForQuantity.setQuantity(buyLimitCount);
                            //更新该购物车中的产品数量
                            cartMapper.updateByPrimaryKey(carForQuantity);
                        }
                        cartProductVo.setQuantity(buyLimitCount);
                        //计算总价
                        cartProductVo.setProductTotalPrice(BigDecimalUtil.mul(product.getPrice().doubleValue(), cartProductVo.getQuantity().doubleValue()));
                        cartProductVo.setProductChecked(cartItem.getChecked());
                    }

                    if (cartItem.getChecked()==Const.Cart.CHECKED){
                        //若商品已被勾选，则将改产品的总价加入到购物车总价当中
                        cartTotalPrice = BigDecimalUtil.add(cartTotalPrice.doubleValue(),cartProductVo.getProductTotalPrice().doubleValue());
                    }
                    cartProductVoList.add(cartProductVo);
                }

            }
            cartVO.setCartTotalPrice(cartTotalPrice);
            cartVO.setCartProductVoList(cartProductVoList);
            cartVO.setAllChecked(this.getAllCheckedStatus(userId));
            cartVO.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
             return cartVO;
        }

        //获取该用户购物车中选中的商品
        private boolean getAllCheckedStatus(Integer userId){
             if(userId==null){
                 return false;
             }//如果有未勾选就，就不是全选
             return cartMapper.selectCartProductCheckedStatusByUserId(userId) == 0;
        }

     public ServerResponse<CartVo> selectOrUnselect(Integer userId,Integer productId,Integer checked){
        cartMapper.checkedOrUncheckedProduct(userId,productId,checked);
        return this.list(userId);
     }

      public ServerResponse getCartProductCount(Integer userId){
             if(userId==null){
                 return ServerResponse.createBySuccess(0);
             }
             return ServerResponse.createBySuccess(cartMapper.selectCartProductCount(userId));
      }




}
