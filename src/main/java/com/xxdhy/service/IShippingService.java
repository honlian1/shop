package com.xxdhy.service;

import com.github.pagehelper.PageInfo;
import com.xxdhy.common.ServerResponse;
import com.xxdhy.pojo.Shipping;

public interface IShippingService {

    ServerResponse add(Integer userId, Shipping shipping);

    ServerResponse<String> del(Integer userId, Integer shippingId);

    ServerResponse<Shipping> select(Integer userId, Integer shippingId);

    ServerResponse<String> update(Integer userId,Shipping shipping);

    ServerResponse<PageInfo> list(Integer userId, int pageNum, int pageSize);
}
