package com.xxdhy.controller;

import com.xxdhy.common.Const;
import com.xxdhy.common.ResponseCode;
import com.xxdhy.common.ServerResponse;
import com.xxdhy.pojo.User;
import com.xxdhy.service.ICartService;
import com.xxdhy.vo.CartVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/cart/")
public class CartController {

        @Autowired
        private ICartService cartService;

        @ResponseBody
        @RequestMapping("add.do")
        public ServerResponse<CartVo> add(HttpSession session, Integer productId, Integer count){

            User user=(User)session.getAttribute(Const.CURRENT_USER);
            if(user==null){
                return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请先登录");
            }
            return cartService.add(user.getId(),productId,count);
        }

    @ResponseBody
    @RequestMapping("update.do")
    public ServerResponse<CartVo> update(HttpSession session, Integer productId, Integer count){

        User user=(User)session.getAttribute(Const.CURRENT_USER);
        if(user==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请先登录");
        }
       return cartService.update(user.getId(),productId,count);
    }
    /*
        删除购物车中指定的产品
        参数 ：字符串类型的  productId，与前端约定，选择多个产品，则用","分隔开
     */
    @ResponseBody
    @RequestMapping("delete_product.do")
    public ServerResponse<CartVo> deleteProduct(HttpSession session,String productIds){

        User user=(User)session.getAttribute(Const.CURRENT_USER);
        if(user==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请先登录");
        }
        return cartService.deleteProduct(user.getId(),productIds);
    }

    @ResponseBody
    @RequestMapping("list.do")
    public ServerResponse<CartVo> list(HttpSession session){

        User user=(User)session.getAttribute(Const.CURRENT_USER);
        if(user==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请先登录");
        }
        return cartService.list(user.getId());
    }

    // 全选   将该名用户下的购物车中的checked全部改成 1
    @ResponseBody
    @RequestMapping("select_all.do")
    public ServerResponse<CartVo> selectAll(HttpSession session){
        User user=(User)session.getAttribute(Const.CURRENT_USER);
        if(user==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请先登录");
        }
        return cartService.selectOrUnselect(user.getId(),null,Const.Cart.CHECKED);
    }
    //全反选  将该名用户下的购物车中的checked全部改成 0
    @ResponseBody
    @RequestMapping("un_select_all.do")
    public ServerResponse<CartVo> unSelectAll(HttpSession session){
        User user=(User)session.getAttribute(Const.CURRENT_USER);
        if(user==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请先登录");
        }
        return cartService.selectOrUnselect(user.getId(),null,Const.Cart.UN_CHECKED);
    }
    //单独选
    @ResponseBody
    @RequestMapping("select.do")
    public ServerResponse<CartVo> select(HttpSession session,Integer productId){
        User user=(User)session.getAttribute(Const.CURRENT_USER);
        if(user==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请先登录");
        }
        return cartService.selectOrUnselect(user.getId(),productId,Const.Cart.CHECKED);
    }
    //单独反选
    @ResponseBody
    @RequestMapping("un_select.do")
    public ServerResponse<CartVo> unSelect(HttpSession session,Integer productId){
        User user=(User)session.getAttribute(Const.CURRENT_USER);
        if(user==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请先登录");
        }
        return cartService.selectOrUnselect(user.getId(),productId,Const.Cart.UN_CHECKED);
    }

    //查询当前用户的购物车里面的产品数量，如果一个产品有10个，那么数量就是10
    @ResponseBody
    @RequestMapping("get_cart_product_count.do")
    public ServerResponse<Integer> getCartProductCount(HttpSession session){
        User user=(User)session.getAttribute(Const.CURRENT_USER);
        if(user==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请先登录");
        }
        return cartService.getCartProductCount(user.getId());
    }



}
