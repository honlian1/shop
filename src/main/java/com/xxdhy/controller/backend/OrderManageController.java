package com.xxdhy.controller.backend;

import com.github.pagehelper.PageInfo;
import com.xxdhy.common.Const;
import com.xxdhy.common.ResponseCode;
import com.xxdhy.common.ServerResponse;
import com.xxdhy.pojo.User;
import com.xxdhy.service.IOrderService;
import com.xxdhy.service.IUserService;
import com.xxdhy.vo.OrderVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/manager/order")
public class OrderManageController  {

       @Autowired
       private IUserService iUserService;

       @Autowired
       private IOrderService iOrderService;

       @ResponseBody
       @RequestMapping("list.do")
      public ServerResponse<PageInfo> orderList(HttpSession session, @RequestParam(value = "pageSize",defaultValue = "10") int pageSize, @RequestParam(value = "pageNum",defaultValue = "1") int pageNum){

           User user =(User)session.getAttribute(Const.CURRENT_USER);
           if(user==null){
               return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登陆，请登陆管理员");
           }
           if(iUserService.checkadminRole(user).isSuccess()){
               //填充产品的业务逻辑
               return iOrderService.manageList(pageNum, pageSize);
           }
           else{
               return ServerResponse.createByErrorMessage("无权限操作");
           }

      }

    @ResponseBody
    @RequestMapping("detail.do")
    public ServerResponse<OrderVo> orderDetail(HttpSession session, Long orderNo){
        User user =(User)session.getAttribute(Const.CURRENT_USER);
        if(user==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登陆，请登陆管理员");
        }
        if(iUserService.checkadminRole(user).isSuccess()){
            //填充产品的业务逻辑
            return iOrderService.manageDetail(orderNo);
        }
        else{
            return ServerResponse.createByErrorMessage("无权限操作");
        }

    }
    @ResponseBody
    @RequestMapping("search.do")
    public ServerResponse<PageInfo> orderSearch(HttpSession session, Long orderNo, @RequestParam(value = "pageSize",defaultValue = "10") int pageSize, @RequestParam(value = "pageNum",defaultValue = "1") int pageNum)
        {
        User user =(User)session.getAttribute(Const.CURRENT_USER);
        if(user==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登陆，请登陆管理员");
        }
        if(iUserService.checkadminRole(user).isSuccess()){
            //填充产品的业务逻辑
            return iOrderService.managerSearch(orderNo, pageNum, pageSize);
        }
        else{
            return ServerResponse.createByErrorMessage("无权限操作");
        }

    }

    /**
     *   ——————————————————————发货
     * @param session
     * @param orderNo
     * @return
     */
    @ResponseBody
    @RequestMapping("send_good.do")
    public ServerResponse<String> orderSendGoods(HttpSession session, Long orderNo) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登陆，请登陆管理员");
        }
        if (iUserService.checkadminRole(user).isSuccess()) {
            //填充产品的业务逻辑
            return iOrderService.manageSendGoods(orderNo);
        } else {
            return ServerResponse.createByErrorMessage("无权限操作");
        }
    }
}
