package com.xxdhy.controller.backend;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.xxdhy.common.Const;
import com.xxdhy.common.ServerResponse;
import com.xxdhy.pojo.User;
import com.xxdhy.service.IUserService;

@Controller
@RequestMapping("manager/user")
public class UserMangeController {
    
	@Resource
	private IUserService iUserService;
	
	
	@ResponseBody
	@RequestMapping(value="login.do",method=RequestMethod.POST)
	public ServerResponse<User> login(String username,String password,HttpSession session){
		
		ServerResponse<User> response=iUserService.login(username, password);
		if(response.isSuccess()) {
			User user=response.getData();
			if(user.getRole()==Const.Role.ROLE_ADMIN) {
				//说明登录的是管理员
				session.setAttribute(Const.CURRENT_USER, user);
				return response;
			}else {
				return ServerResponse.createByErrorMessage("不是管理员无法登录");
			}
		}
		return response;
	}
}
