package com.xxdhy.service;

import com.github.pagehelper.PageInfo;
import com.xxdhy.common.ServerResponse;
import com.xxdhy.pojo.Product;
import com.xxdhy.vo.ProductDetailVo;

public interface IProductService {
   
	ServerResponse saveOrUpdateProduct(Product product);

	ServerResponse setSaleStatus(Integer productId, Integer status);

	ServerResponse manageProductDetail(Integer productId);
	
	ServerResponse getProductList(int pageNum, int pageSize);

	ServerResponse<PageInfo> searchProduct(String productName, Integer productId, int pageNum, int pageSize);

	ServerResponse<ProductDetailVo> getProductDetail(Integer productId);

	ServerResponse<PageInfo> getProductByKeywordCategory(String keyword,Integer categoryId,int pageNum,int pageSize,String orderBy);
}
