package com.xxdhy.service.impl;

import java.util.UUID;

import javax.annotation.Resource;


import com.xxdhy.common.TokenCache;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.xxdhy.common.Const;
import com.xxdhy.common.ServerResponse;
import com.xxdhy.dao.UserMapper;
import com.xxdhy.pojo.User;
import com.xxdhy.service.IUserService;
import com.xxdhy.util.MD5Util;
import com.xxdhy.util.StringUtil;

@Service
public class UserServiceImpl implements IUserService {

	@Resource
	private UserMapper userMapper;

	@Override
	public ServerResponse<User> login(String username, String password) {
		int resultCount = userMapper.checkUsername(username);
		if (resultCount == 0) {
			return ServerResponse.createByErrorMessage("用户名不存在");
		}

		// 密码登录MD5
		String mad5Password = MD5Util.MD5EncodeUtf8(password);

		User user = userMapper.selectLogin(username, mad5Password);
		if (user == null) {
			return ServerResponse.createByErrorMessage("密码错误");
		}
		 user.setPassword(org.apache.commons.lang3.StringUtils.EMPTY);

		return ServerResponse.createBySuccess("登录成功", user);

	}

	public ServerResponse<String> register(User user) {

		 ServerResponse<String> validResponse=this.checkValid(user.getUsername(), Const.USERNAME);
		 if(!validResponse.isSuccess()) {
			 return validResponse;
		 }
		 validResponse=this.checkValid(user.getEmail(), Const.EMAIL);
		 if(!validResponse.isSuccess()) {
			 return validResponse;
		 }
	
		user.setRole(Const.Role.ROLE_CUSTOMER);
		// MD5加密
		user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));

	int	resultCount = userMapper.insert(user);

		if (resultCount == 0) {
			return ServerResponse.createByErrorMessage("注册失败");

		}
		return ServerResponse.createBySuccessMessage("注册成功");

	}

	/**
	 *   校验用户名和邮箱是否存在 
	 * @param str
	 * @param type
	 * @return
	 */
	
	public ServerResponse<String> checkValid(String str, String type) {

		if (StringUtil.isNotBlank(type)) {
			// 开始校验
			if (Const.USERNAME.equals(type)) {
				int resultCount = userMapper.checkUsername(str);
				if (resultCount > 0) {
					return ServerResponse.createByErrorMessage("用户名已存在");
				}
			}
			if (Const.EMAIL.equals(type)) {
				int resultCount = userMapper.checkEmail(str);
				if (resultCount > 0) {
					return ServerResponse.createByErrorMessage("Email已存在");
				}
			}
			return ServerResponse.createBySuccessMessage("校验成功");
		} else {
			return ServerResponse.createByErrorMessage("参数错误");
		}
		
	}
	
	public ServerResponse selectQuestion(String username) {
		
		ServerResponse validResponse=this.checkValid(username, Const.USERNAME);
		if(validResponse.isSuccess()) {
			return ServerResponse.createByErrorMessage("用户不存在");
		}
		String question=userMapper.selectQuestionByUsername(username);
		if(StringUtil.isNotBlank(question)) {
			return ServerResponse.createBySuccessMessage(question);
		}
		return ServerResponse.createByErrorMessage("找回密码的问题为空");
	}
	
	
  
	  public ServerResponse<String> checkAnswer(String username,String question,String answer){
		  
		  int resultCount =userMapper.checkAnswer(username,question,answer);
		  if(resultCount>0) {
			  //说明及问题答案是这个用户的，并且是正确的
			  
			  /**
			   *    使用本地缓存检查问题的接口，需要回看五章 5-4
			   */
			  String forgetToken=UUID.randomUUID().toString();
			  TokenCache.setKey(TokenCache.TOKEN_PREFIX+username,forgetToken);
			  return ServerResponse.createBySuccess(forgetToken);
		  }
		  return ServerResponse.createByErrorMessage("问题的答案错误");
		 		  
	  }
	  /**
	   * 
	          【因为缺少jar包】不完整的重置密码
	   */
	public  ServerResponse<String> forgetRestPassword(String username,String passwordNew,String forgetToken){
		  if(!StringUtil.isNotBlank(forgetToken)) {
			  return ServerResponse.createBySuccessMessage("参数错误，token需要传递");
		  }
		  ServerResponse validResponse=this.checkValid(username, Const.USERNAME);
			if(validResponse.isSuccess()) {
				return ServerResponse.createByErrorMessage("用户不存在");
			}
			//获取缓存中的token值
		    //  token应该做成常量
		   String token=TokenCache.getKey(TokenCache.TOKEN_PREFIX+username);
			if(StringUtils.isBlank(token)){
                  return ServerResponse.createByErrorMessage("token无效或者过期");
			}

			if(StringUtils.equals(forgetToken,token)){
				String md5Password=MD5Util.MD5EncodeUtf8(passwordNew);
				int rowCount =userMapper.updatePasswordByUsername(username,md5Password);
				if(rowCount>0) {
					return ServerResponse.createBySuccessMessage("修改密码成功");
				}
				}else {
				return ServerResponse.createByErrorMessage("token错误,请重新获取重置密码的token");
			}
		   return ServerResponse.createBySuccessMessage("修改密码失败");
	  }
	  
	public ServerResponse<String> resetPassword(String passwordOld,String passwordNew,User user){
		//防止横向越权，要校验一下这个用户的旧密码，一定要指定是这个用户，因为会查询一个count(1),
		//如果不指定id，那么结果是true，count>0;
		int resultCount=userMapper.checkPassword(MD5Util.MD5EncodeUtf8(passwordOld),user.getId());
		if(resultCount==0) {
			return ServerResponse.createByErrorMessage("旧密码错误");
			
		}
		user.setPassword(MD5Util.MD5EncodeUtf8(passwordNew));
		//int updateCount =userMapper.updatePasswordByUsername(user.getUsername(),user.getPassword());
		int updateCount=userMapper.updateByPrimaryKeySelective(user);// 更新数据
		if(updateCount>0) {
			return ServerResponse.createBySuccessMessage("密码更新成功");
		}
		return ServerResponse.createBySuccessMessage("密码更新失败");
	}
     
	
	/**
	 *    更新用户个人信息
	 * @param user
	 * @return
	 */
	
	public ServerResponse<User> updateInfomation(User user){
		//username是不能被更新的
		//email也要进行一个校验，校验新的email是不是已经存在，并且存在的email如果相同的话，不能是我们当前这个用户的
	    int resultCount=userMapper.checkEmailByUserId(user.getEmail(),user.getId());
		if(resultCount>0) {
			return ServerResponse.createByErrorMessage("email已经存在，请更换eamil");
		}
		
		User updateUser=new User();
		updateUser.setId(user.getId());
		updateUser.setUsername(user.getUsername());
		updateUser.setEmail(user.getEmail());
		updateUser.setPhone(user.getPhone());
		updateUser.setQuestion(user.getQuestion());
		updateUser.setAnswer(user.getAnswer());


		/*
		 *    updateByPrimaryKeySelective 只有属性不为空的时候才去更新数据表中的数据
		 */
		int updateCount=userMapper.updateByPrimaryKeySelective(updateUser);
		if(updateCount>0) {
			return ServerResponse.createBySuccess("更新个人信息成功",updateUser);
		}
		return ServerResponse.createByErrorMessage("更新个人信息失败");
	}
	
	
	 public ServerResponse<User> getInformation(Integer userId){
		 User user=userMapper.selectByPrimaryKey(userId);
		 
		 if(user==null) {
			 return ServerResponse.createByErrorMessage("找不到当前用户");
		 }
		 user.setPassword(" ");
		 return ServerResponse.createBySuccess(user);
	 }
	
	 /**
	  *   校验是否为管理员
	  * @param user
	  * @return
	  */
	 public ServerResponse checkadminRole(User user) {
		 if(user!=null&&user.getRole()==Const.Role.ROLE_ADMIN) {
			 return ServerResponse.createBySuccess();
		 }
		 return ServerResponse.createByError();
	 }
	
}
