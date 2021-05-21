package com.xxdhy.service;

import com.xxdhy.common.ServerResponse;
import com.xxdhy.vo.CartVo;

public interface ICartService {
     ServerResponse<CartVo> add(Integer userId, Integer productId, Integer count);

     ServerResponse<CartVo> update(Integer userId,Integer productId,Integer count);

     ServerResponse<CartVo> deleteProduct(Integer userId, String productIds);

     ServerResponse<CartVo> list(Integer id);

     ServerResponse<CartVo> selectOrUnselect(Integer userId,Integer productId,Integer checked);

     ServerResponse getCartProductCount(Integer userId);
}
