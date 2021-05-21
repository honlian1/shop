package com.xxdhy.test;

import org.junit.Test;

import java.math.BigDecimal;

public class BigDecimalTest {

     @Test
     public  void test01(){
         System.out.println(0.01+0.2);
         System.out.println(0.03-0.02);
         System.out.println(4.015*100);
         System.out.println(123.3/100);
     }

     @Test
     public void test02(){

         BigDecimal b1=new BigDecimal("0.21");
         BigDecimal b2=new BigDecimal("0.33");
         System.out.println(b1.add(b2));
     }
}
