package com.xxdhy.service.impl;


import java.util.ArrayList;
import java.util.List;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.xxdhy.common.Const;
import com.xxdhy.service.ICategoryService;
import com.xxdhy.util.PropertiesUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.xxdhy.common.ResponseCode;
import com.xxdhy.common.ServerResponse;
import com.xxdhy.dao.CategoryMapper;
import com.xxdhy.dao.ProductMapper;
import com.xxdhy.pojo.Category;
import com.xxdhy.pojo.Product;
import com.xxdhy.service.IProductService;
import com.xxdhy.util.DateTimeUtil;

import com.xxdhy.vo.ProductDetailVo;
import com.xxdhy.vo.ProductListVo;

@Service
public class ProductServiceImpl implements IProductService{
      
	  @Autowired
	  private ProductMapper productMapper;
	  @Autowired
	  private CategoryMapper categoryMapper;
	  @Autowired
	  private ICategoryService iCategoryService;


	  public ServerResponse saveOrUpdateProduct(Product product) {
		  if(product!=null) {
			  
			  if(StringUtils.isNotBlank(product.getSubImages())) {
				  String[] subImageArray=product.getSubImages().split(",");
				  if(subImageArray.length>0) {
					  product.setMainImage(subImageArray[0]);
				  }
				  
			  }
			    
			  if(product.getId()!=null) {
				int rowCount= productMapper.updateByPrimaryKey(product);
				if(rowCount>0) {
					return ServerResponse.createBySuccess("更新产品成功");
				}
				 return ServerResponse.createByErrorMessage("更新产品失败");
				 
				 
			  }else {
				  int rowCount= productMapper.insert(product);
				  if(rowCount>0) {
					  return ServerResponse.createBySuccessMessage("新增成品成功");
				  }
				  return ServerResponse.createByErrorMessage("更新产品失败");
			  }
		}
		  return ServerResponse.createByErrorMessage("新增或更新产品参数不正确");
		  }
	  
	  
	  public ServerResponse<String>setSaleStatus(Integer productId,Integer status){
		  if(productId==null||status== null) {
			  return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
		  }
		  Product product=new Product();
		  product.setId(productId);
		  product.setStatus(status);
		  int rowCount =productMapper.updateByPrimaryKeySelective(product);
		  if(rowCount>0) {
			  return ServerResponse.createBySuccessMessage("修改产品销售状态成功");
		  }
		  return ServerResponse.createByErrorMessage("修改产品销售状态失败");
	  }


	@Override
	public ServerResponse<ProductDetailVo> manageProductDetail(Integer productId) {
		// TODO 自动生成的方法存根
		 if(productId==null) {
			  return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
		  }
		 Product product=productMapper.selectByPrimaryKey(productId);
		 if(product==null) {
			 return ServerResponse.createByErrorMessage("产品已下架或不存在");
		 }
		 //VO对象-value object
		 //pojo->bo()
		  ProductDetailVo productDetailVo=assembleProductDetailVo(product);
		 
		return ServerResponse.createBySuccess(productDetailVo);
	}


	  private ProductDetailVo assembleProductDetailVo(Product product) {
		  ProductDetailVo productDetailVo=new ProductDetailVo();
		  productDetailVo.setId(product.getId());
		  productDetailVo.setSubtitle(product.getSubtitle());
		  productDetailVo.setPrice(product.getPrice());
		  productDetailVo.setMainImage(product.getMainImage());
		  productDetailVo.setSubImages(product.getSubImages());
		  productDetailVo.setCategoryId(product.getCategoryId());
		  productDetailVo.setDetail(product.getDetail());
		  productDetailVo.setName(product.getName());
		  productDetailVo.setStatus(product.getStatus());
		  productDetailVo.setStock(product.getStock());
		  
		 productDetailVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix","http://img.xxdhymmall.com/"));
		  
		  Category category=categoryMapper.selectByPrimaryKey(product.getCategoryId());
		  
		  if(category==null) {
			  productDetailVo.setParentCategoryId(0);//默认根节点			  
		  }else {
			  productDetailVo.setParentCategoryId(category.getParentId());
		  }
		   productDetailVo.setCreateTiem(DateTimeUtil.dateToStr(product.getCreateTime()));
	       productDetailVo.setUpdateTime(DateTimeUtil.dateToStr(product.getUpdateTime()));
		   return productDetailVo;
	  }
	  
	  
	  /**
	   *    后台商品列表动态功能开发
	   */
	  
	  public ServerResponse<PageInfo> getProductList(int pageNum,int pageSize) {
		  //srartPage--start
		  //填充自己的sql查询逻辑
		  //pageHelper-收尾
		  
	     PageHelper.startPage(pageNum,pageSize);//(需要在maven注入依赖)
		  List<Product> productList=productMapper.selectList();

		  List<ProductListVo> pListVo= Lists.newArrayList();
		  for(Product p:productList) {
			  ProductListVo productListVo=assembleProductListVo(p);
			  pListVo.add(productListVo);
		  }

		  PageInfo pageResult=new PageInfo(productList);//放入pageHelper中自动封装
		  pageResult.setList(pListVo);
		  return ServerResponse.createBySuccess(pageResult);
	  }
	  
	  private ProductListVo assembleProductListVo(Product product) {
		  ProductListVo productListVo=new ProductListVo();
		  productListVo.setId(product.getId());
		  productListVo.setName(product.getName());
		  productListVo.setPrice(product.getPrice());
		  productListVo.setCategoryId(product.getCategoryId());
		  productListVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix","http://img.xxdhymmall.com/"));
		  productListVo.setMainImage(product.getMainImage());
		  productListVo.setSubtitle(product.getSubtitle());
		  productListVo.setStatus(product.getStatus());
		  
		  return productListVo;
		  
	  }
	  
	 public ServerResponse<PageInfo> searchProduct(String productName,Integer productId,int pageNum,int pageSize){
	      PageHelper.startPage(pageNum,pageSize);
	      if(StringUtils.isNotBlank(productName)){
	        productName=new StringBuilder().append("%").append(productName).append("%").toString();	        
	      }
	      List<Product> productList =productMapper.selectByNameAndProductId(productName,productId);
	      List<ProductListVo> pListVo=Lists.newArrayList();
		  for(Product p:productList) {
			  ProductListVo productListVo=assembleProductListVo(p);
			  pListVo.add(productListVo);
		  }
		 PageInfo pageResult=new PageInfo(productList);//放入pageHelper中自动封装
		 pageResult.setList(pListVo);
		 return ServerResponse.createBySuccess(pageResult);
	  
	  }



	  public ServerResponse<ProductDetailVo> getProductDetail(Integer productId){
		  if(productId==null) {
			  return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
		  }
		  Product product=productMapper.selectByPrimaryKey(productId);
		  if(product==null) {
			  return ServerResponse.createByErrorMessage("产品已下架或不存在");
		  }
		  //VO对象-value object
		  //pojo->bo()
		  if(product.getStatus()!= Const.ProductStatusEnum.ON_SALE.getCode()){
		  	return ServerResponse.createByErrorMessage("产品已下架或删除");
		  }
		  ProductDetailVo productDetailVo=assembleProductDetailVo(product);

		  return ServerResponse.createBySuccess(productDetailVo);
	  }

	/**
	 *   根据关键字和 分类id搜索的方法
	 * @param keyword
	 * @param categoryId
	 * @param pageNum
	 * @param pageSize
	 * @param orderBy
	 * @return
	 */
	  public ServerResponse<PageInfo> getProductByKeywordCategory(String keyword,Integer categoryId,int pageNum,int pageSize,String orderBy){
	  	if(StringUtils.isBlank(keyword)&&categoryId==null){
	  		return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
		}
	  	List<Integer> categoryIdList=new ArrayList<Integer>();
	  	if(categoryId!=null){
	  		Category category=categoryMapper.selectByPrimaryKey(categoryId);
	  		if(category==null&&StringUtils.isBlank(keyword)){
               //没有该分类，并且还没有关键字，这个时候返回一个空的结果集，不报错
				PageHelper.startPage(pageNum,pageSize);
				List<ProductListVo> productListVoList=Lists.newArrayList();
				PageInfo pageInfo=new PageInfo(productListVoList);
				return ServerResponse.createBySuccess(pageInfo);
			}
	  		categoryIdList =iCategoryService.selectCategoryAndChildrenById(category.getId()).getData();
		}
	  	if(StringUtils.isNotBlank(keyword)){
	  		keyword = new StringBuilder().append("%").append(keyword).append("%").toString();
		}
	  	PageHelper.startPage(pageNum,pageSize);
	  	//排序处理
		  //动态排序   order  n.排序、顺序、次序、条理
		  if(StringUtils.isNotBlank(orderBy)){
             if(Const.ProductListOrderBy.PRICE_ASC_DESC.contains(orderBy)){
                String[]  orderByArray=orderBy.split("_");
                PageHelper.orderBy(orderByArray[0]+" "+orderByArray[1]);
			 }
		  }
		  //假设keyword和cateIdList其中之一没有传或者为空，但是我们已经定义了 ",所以在传到mapper的时候需要重新给它赋值为null
		  List<Product> productList=productMapper.selectByNameAndCategoryIds(StringUtils.isBlank(keyword)?null:keyword,categoryIdList.size()==0?null:categoryIdList);

		  List<ProductListVo> productListVoLists=Lists.newArrayList();
		  for (Product product:productList){
		  	ProductListVo productListVo=assembleProductListVo(product);
		  	productListVoLists.add(productListVo);
		  }
		  //分页
		  PageInfo pageInfo=new PageInfo(productList);
		  pageInfo.setList(productListVoLists);
		  return ServerResponse.createBySuccess(pageInfo);
	  }
}