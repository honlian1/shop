package com.xxdhy.vo;

import java.math.BigDecimal;

public class CartProductVo {
  
	//结合子产品和购物车的一个抽象对象
	  
	private Integer id;   //购物车id
	private Integer userId;  //用户id
	private Integer productId;//产品id
	private String mainImage;//产品主图
	private Integer quantity;//购物车中此商品的数量
	private String productName;//   产品名称

	public String getMainImage() {
		return mainImage;
	}

	public void setMainImage(String mainImage) {
		this.mainImage = mainImage;
	}

	private String productSbutitle; //产品副标题
	private BigDecimal productPrice;//产品价格
	private Integer productStatus;//产品状态
	private BigDecimal productTotalPrice;// 产品总价
	private Integer productStock;//产品库存
	private Integer productChecked;//此商品是否勾选
	
	private String limitQuantity;//限制数量的一个返回结果

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public Integer getProductId() {
		return productId;
	}

	public void setProductId(Integer productId) {
		this.productId = productId;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public String getProductSbutitle() {
		return productSbutitle;
	}

	public void setProductSbutitle(String productSbutitle) {
		this.productSbutitle = productSbutitle;
	}

	public BigDecimal getProductPrice() {
		return productPrice;
	}

	public void setProductPrice(BigDecimal productPrice) {
		this.productPrice = productPrice;
	}

	public Integer getProductStatus() {
		return productStatus;
	}

	public void setProductStatus(Integer productStatus) {
		this.productStatus = productStatus;
	}

	public BigDecimal getProductTotalPrice() {
		return productTotalPrice;
	}

	public void setProductTotalPrice(BigDecimal productTotalPrice) {
		this.productTotalPrice = productTotalPrice;
	}

	public Integer getProductStock() {
		return productStock;
	}

	public void setProductStock(Integer productStock) {
		this.productStock = productStock;
	}

	public Integer getProductChecked() {
		return productChecked;
	}

	public void setProductChecked(Integer productChecked) {
		this.productChecked = productChecked;
	}

	public String getLimitQuantity() {
		return limitQuantity;
	}

	public void setLimitQuantity(String limitQuantity) {
		this.limitQuantity = limitQuantity;
	}
	
	
}
