package com.xxdhy.service;

import java.util.List;

import com.xxdhy.common.ServerResponse;
import com.xxdhy.pojo.Category;

public interface ICategoryService {
      
	 ServerResponse addCategory(String categoryName, Integer parentId);
	 
	 ServerResponse updateCategoryName(Integer categoryId, String categoryName);
	 
	 ServerResponse<List<Category>> getChildrenParallelCategory(Integer categoryId);
	 
	 ServerResponse<List<Integer>> selectCategoryAndChildrenById(Integer categoryId);

}
