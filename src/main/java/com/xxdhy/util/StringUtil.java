package com.xxdhy.util;

public class StringUtil {
   
	 public static Boolean isNotBlank(String str) {
		 
		 if(str==" ") {
			 return false;
		 }else if(str==""){
			 return false;
		 }else if(str==null) {
			 return false;
		 }
		 
		 return true;
	 }
}
