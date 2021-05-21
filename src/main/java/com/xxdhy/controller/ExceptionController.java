package com.xxdhy.controller;


import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import com.xxdhy.util.Result;

/**
 *   异常处理类
 * @author User
 *
 */
@ControllerAdvice
public class ExceptionController {
     /**
      *    捕获算数异常
      *    标准
      *    
      * @param e
      * @return
      */
	@ResponseBody
	@ExceptionHandler(value=ArithmeticException.class)//需要捕获的异常
	 public Object handleArithmeitc(ArithmeticException e) {
		 
		 e.printStackTrace();//打印堆栈异常信息
		 System.out.println("出现算数异常");
		 return new Result(null,"请求异常:"+e,101);
	 }
	
	/**
	 *    捕获所有的异常
	 * @param e
	 * @return
	 */
	@ResponseBody
	@ExceptionHandler(value=Exception.class)//捕获所有的异常
	 public Object handleException(Exception e) {
		 
		 e.printStackTrace();//打印堆栈异常信息
		
		 return new Result(null,"请求异常:"+e,101);
	 }
	
	
	
	
	
	
}
