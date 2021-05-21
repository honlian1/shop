package com.xxdhy.util;

public class Result {
     
	  private Object data;
	  private String message;
	  private Integer code;//100正常
	   
	  
	public Result() {
		super();
	}
	public Result(Object data, String message, Integer code) {
		super();
		this.data = data;
		this.message = message;
		this.code = code;
	}
	public Object getData() {
		return data;
	}
	public void setData(Object data) {
		this.data = data;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public Integer getCode() {
		return code;
	}
	public void setCode(Integer code) {
		this.code = code;
	}
	  
	  
	  
}
