package com.xxdhy.controller.backend;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.xxdhy.common.Const;
import com.xxdhy.common.ResponseCode;
import com.xxdhy.common.ServerResponse;
import com.xxdhy.pojo.User;
import com.xxdhy.service.ICategoryService;
import com.xxdhy.service.IUserService;

@Controller
@RequestMapping("manager/category")
public class CategoryManageController {
	
	
	@Resource
	private IUserService iUserService;
	
	@Autowired
	private ICategoryService iCategoryService;
        
	
	    @RequestMapping("add_category.do")
	    @ResponseBody
	   public ServerResponse addCategory(HttpSession session,String categoryName,@RequestParam(value="parentId",defaultValue="0")Integer parentId) {
		
		   User user=(User)session.getAttribute(Const.CURRENT_USER);
		   
		   if(user==null) {
			   return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请登录");
		   }
		   //校验一下是否是管理员
		   user.setPassword(" ");
		   if(iUserService.checkadminRole(user).isSuccess()) {
			   //是管理员
			   //增加处理分类的逻辑
			 return iCategoryService.addCategory(categoryName, parentId);
		   }else {
			   return ServerResponse.createByErrorMessage("无权限操作，需要管理员权限");
		   }
		   		  
	   }
	    
	    
	    @ResponseBody
	    @RequestMapping("set_category_name.do")
	    public ServerResponse setCategoryName(HttpSession session,Integer categoryId,String categoryName) {
	    	
	    	User user=(User)session.getAttribute(Const.CURRENT_USER);
	    	if(user==null) {
	    		return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请登录");
	    	}
	    	if(iUserService.checkadminRole(user).isSuccess()) {
	    		//更新category
	    		return iCategoryService.updateCategoryName(categoryId, categoryName);
	    		
	    	}else {
	    		return ServerResponse.createByErrorMessage("无权限操作，需要管理员权限");
	    	}
	    }
	    /**
	     *   查询子节点的category信息，并且不递归，保持平级
	     * @param session
	     * @param categoryId
	     * @return
	     */

	    @ResponseBody
	    @RequestMapping("get_category.do")
	    public ServerResponse getChildrenParallelCategory(HttpSession session,@RequestParam(value="categoryId",defaultValue="0")Integer categoryId) {
	    	
	    	User user=(User)session.getAttribute(Const.CURRENT_USER);
	    	if(user==null) {
	    		return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请登录");
	    	}
	    	if(iUserService.checkadminRole(user).isSuccess()) {  		
	    		return iCategoryService.getChildrenParallelCategory(categoryId);
	    		
	    	}else {
	    		return ServerResponse.createByErrorMessage("无权限操作，需要管理员权限");
	    	}
			
	    }
	    
	    
	    /**
	     *    查询当前节点的id和递归子节点的id
	     * @param session
	     * @param categoryId
	     * @return
	     */
	    @ResponseBody
	    @RequestMapping("get_deep_category.do")
	    public ServerResponse getCategoryAndDeepChildrenCategory(HttpSession session,@RequestParam(value="categoryId",defaultValue="0")Integer categoryId) {
	    	User user=(User)session.getAttribute(Const.CURRENT_USER);
	    	if(user==null) {
	    		return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请登录");
	    	}
	    	if(iUserService.checkadminRole(user).isSuccess()) {
	    	  // 0->1000-10000
	    		return iCategoryService.selectCategoryAndChildrenById(categoryId);
	    		
	    	}else {
	    		return ServerResponse.createByErrorMessage("无权限操作，需要管理员权限");
	    	}
			
	    }
	  
	    
	    
	    
	    
	    
	    }
	    
	    

