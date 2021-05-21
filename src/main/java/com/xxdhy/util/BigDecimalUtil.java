package com.xxdhy.util;

import java.math.BigDecimal;

public class BigDecimalUtil {

      private BigDecimalUtil(){}

       //加法
      public static BigDecimal add(double d1,double d2){

          BigDecimal b1=new BigDecimal(Double.toString(d1));
          BigDecimal b2=new BigDecimal(Double.toString(d2));
          return b1.add(b2);
      }

      //减法
      public static BigDecimal sub(double d1,double d2){

          BigDecimal b1=new BigDecimal(Double.toString(d1));
          BigDecimal b2=new BigDecimal(Double.toString(d2));
          return b1.subtract(b2);
      }

      //乘法
    public  static BigDecimal mul(double d1,double d2){
        BigDecimal b1=new BigDecimal(Double.toString(d1));
        BigDecimal b2=new BigDecimal(Double.toString(d2));
        return b1.multiply(b2);
    }
    //除法
    public  static BigDecimal div(double d1,double d2){
        BigDecimal b1=new BigDecimal(Double.toString(d1));
        BigDecimal b2=new BigDecimal(Double.toString(d2));
        return b1.divide(b2,2,BigDecimal.ROUND_HALF_UP);//4舍5入保留两位小数
    }
}
