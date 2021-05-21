package com.xxdhy.controller;

import java.util.HashMap;

import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.xxdhy.common.Const;
import com.xxdhy.common.ResponseCode;
import com.xxdhy.common.ServerResponse;
import com.xxdhy.pojo.User;
import com.xxdhy.service.IUserService;


@Controller
@RequestMapping("user")
public class UserController {
     
	  @Resource
	  private IUserService iUserService;    

	  /**
	   *    用户登录
	   * @param username
	   * @param password
	   * @param session
	   * @return
	   */
	  @ResponseBody
	  @RequestMapping(value="login",method=RequestMethod.POST)
	  public ServerResponse<User> login(String username,String password,HttpSession session ) {
	  		  
		  ServerResponse<User> response=iUserService.login(username, password);
		  
		  if(response.isSuccess()) {
			  session.setAttribute(Const.CURRENT_USER, response.getData());
		  }
		return response;
		  
	  }
	  
	  /**
	   *    登出功能
	   */
	  
	  @ResponseBody
	  @RequestMapping(value="logout",method=RequestMethod.GET)
	  public ServerResponse<String> logout(HttpSession session){
		  session.removeAttribute(Const.CURRENT_USER);
		  
		  return ServerResponse.createBySuccess();
		  
	  }
	  
	  /**
	   *   注册功能
	   * @param user
	   * @return
	   */
	  @ResponseBody
	  @RequestMapping(value="register.do",method=RequestMethod.POST)
	  public ServerResponse<String> register(User user){
		  
		  return iUserService.register(user);
		  
	  }
	  /**
	   *    检查用户名或邮箱是否已存在
	   *    (用来注册的时候来校验)
	   * @param str
	   * @param type
	   * @return
	   */
	  
	  @ResponseBody
	  @RequestMapping(value="check_valid.do",method=RequestMethod.POST)
	  public ServerResponse<String> checkValid(String str,String type){
		  return iUserService.checkValid(str, type);
	  }
	  
       /**
        *    获取当前用户信息
        * @param session
        * @return
        */
	  @ResponseBody
	  @RequestMapping(value="get_user_info.do",method=RequestMethod.POST)
	  public ServerResponse<User> getUserInfo(HttpSession session){
		  User user=(User) session.getAttribute(Const.CURRENT_USER);
		  if(user!=null) {
			  return ServerResponse.createBySuccess(user);
		  }
		  return ServerResponse.createByErrorMessage("用户未登录，无法获取当前用户的信息");
		  
	  }
	  
	  /**
	   *   
	   *  找回密码问题
	   */
	  
	  @ResponseBody
	  @RequestMapping(value="forget_get_question.do",method=RequestMethod.POST)
	  public ServerResponse<String> forgetGetQuestion(String username){
		return iUserService.selectQuestion(username);		  
	  }
	  
	  /**
	   *  校验问题答案是否正确
	   */
	  @ResponseBody
	  @RequestMapping(value="forget_check_answer.do",method=RequestMethod.POST)
	  public ServerResponse<String> forgetCheckAnswer(String username,String question,String answer){
		  
		  return iUserService.checkAnswer(username, question, answer);
		  
	  }
	  
       /**
        *    未能登录下的重置密码	  
        * @param username
        * @param passwordNew
        * @param forgetToken
        */
	  @ResponseBody
	  @RequestMapping(value="forget_reset_password.do",method=RequestMethod.POST)
	  public ServerResponse<String> forgetRestPassword(String username,String passwordNew,String forgetToken){
		  
		return iUserService.forgetRestPassword(username, passwordNew, forgetToken);
	  }
	  
	  /**
	   *    session:判断登录状态
	   *    登录状态下的重置密码
	   */
	  @ResponseBody
	  @RequestMapping(value="reset_password.do",method=RequestMethod.POST)
	  public ServerResponse<String> resetPassword(HttpSession session,String passwordOld,String passwordNew){
		  
		   User user=(User)session.getAttribute(Const.CURRENT_USER);
		   if(user==null) {
			   return ServerResponse.createByErrorMessage("用户未登录");
		   }
		   
		   return iUserService.resetPassword(passwordOld, passwordNew, user);
	  }
	  /**
	   *   更新用户信息
	   *   (只有登录状态下才能更新用户信息)
	   *  
	   */
	  @ResponseBody
	  @RequestMapping(value="update_information.do",method=RequestMethod.POST)
	   public ServerResponse<User> update_information(HttpSession session,User user){
		   User currentUser=(User)session.getAttribute(Const.CURRENT_USER);
		   if(currentUser==null) {
			   return ServerResponse.createByErrorMessage("用户未登录");
		   }
		   //前台传过来的user是没有id值的
		   user.setId(currentUser.getId());
		   user.setUsername(currentUser.getUsername());
		   ServerResponse<User> response=iUserService.updateInfomation(user);
		   if(response.isSuccess()) {
			   session.setAttribute(Const.CURRENT_USER, response.getData());
		   }
		   return response;
	   }
  
	  /**
	   *   未登录状态下获取用户信息  
	   */
	  @ResponseBody
	  @RequestMapping(value="get_information.do",method=RequestMethod.POST)
	   public ServerResponse<User> get_information(HttpSession session){
		   User currentUser =(User)session.getAttribute(Const.CURRENT_USER);
		   if(currentUser==null) {
			   return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "未登录，需要强制登录");
		   }
		   
		   return iUserService.getInformation(currentUser.getId());
	   }
}
