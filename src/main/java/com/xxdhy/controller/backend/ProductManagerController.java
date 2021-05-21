package com.xxdhy.controller.backend;

import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import com.xxdhy.common.Const;
import com.xxdhy.common.ResponseCode;
import com.xxdhy.common.ServerResponse;
import com.xxdhy.pojo.Product;
import com.xxdhy.pojo.User;
import com.xxdhy.service.IFileService;
import com.xxdhy.service.IProductService;
import com.xxdhy.service.IUserService;
import com.xxdhy.util.PropertiesUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;

/*
     这里的操作全部需要判断身份验证
 */
@Controller
@RequestMapping("/manager/product")
public class ProductManagerController {
          @Autowired
          private IUserService iUserService;
          @Autowired
          private IProductService iProductService;
          @Autowired
          private IFileService iFileService;

        @RequestMapping("save.do")
        @ResponseBody
       public ServerResponse productSave(HttpSession session, Product product){
              User user=(User)session.getAttribute(Const.CURRENT_USER);
              if(user==null){
                  return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请登陆管理员");
              }
              if(iUserService.checkadminRole(user).isSuccess()){
                  //填充添加产品的业务逻辑
                  return iProductService.saveOrUpdateProduct(product);
              }else{
                  return ServerResponse.createByErrorMessage("无权限操作");
              }
       }

    @RequestMapping("set_sale_status.do")
    @ResponseBody
    public ServerResponse setSaleStatus(HttpSession session, Integer productId,Integer status){
        User user=(User)session.getAttribute(Const.CURRENT_USER);
        if(user==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请登陆管理员");
        }
        if(iUserService.checkadminRole(user).isSuccess()){
            //修改产品状态信息
            return iProductService.setSaleStatus(productId,status);
        }else{
            return ServerResponse.createByErrorMessage("无权限操作");
        }
    }

    @RequestMapping("detail.do")
    @ResponseBody
    public ServerResponse getDetail(HttpSession session, Integer productId){
        User user=(User)session.getAttribute(Const.CURRENT_USER);
        if(user==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请登陆管理员");
        }
        if(iUserService.checkadminRole(user).isSuccess()){
            //获取产品详细信息
            return iProductService.manageProductDetail(productId);
        }else{
            return ServerResponse.createByErrorMessage("无权限操作");
        }
    }

    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse getList(HttpSession session, @RequestParam(value = "pageNum",defaultValue = "1") int pageNum, @RequestParam(value = "pageSize",defaultValue = "10") int pageSize){
        User user=(User)session.getAttribute(Const.CURRENT_USER);
        if(user==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请登陆管理员");
        }
        if(iUserService.checkadminRole(user).isSuccess()){
            //获取产品列表信息（动态分页）
          return iProductService.getProductList(pageNum,pageSize);
        }else{
            return ServerResponse.createByErrorMessage("无权限操作");
        }
    }

    /**
     *
       商品搜索的接口
     */
    @RequestMapping("search.do")
    @ResponseBody
    public ServerResponse<PageInfo> productSearch(HttpSession session, String productName , Integer productId,@RequestParam(value = "pageNum",defaultValue = "1") int pageNum, @RequestParam(value = "pageSize",defaultValue = "10") int pageSize){
        User user=(User)session.getAttribute(Const.CURRENT_USER);
        if(user==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请登陆管理员");
        }
        if(iUserService.checkadminRole(user).isSuccess()){
            return iProductService.searchProduct(productName,productId,pageNum,pageSize);
        }else{
            return ServerResponse.createByErrorMessage("无权限操作");
        }
    }
    /**
     *     文件上传
     */
        @RequestMapping("upload.do")
        @ResponseBody
        public ServerResponse upload(HttpSession session, @RequestParam(value = "upload_file",required = false) MultipartFile file, HttpServletRequest request ){

            User user=(User)session.getAttribute(Const.CURRENT_USER);
            if(user==null){
                return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请登陆管理员");
            }
            if(iUserService.checkadminRole(user).isSuccess()){
                //获取servlet上下文 通用 真实路径
                String path=request.getSession().getServletContext().getRealPath("upload");
                String targetFileName=iFileService.upload(file,path);
                String url= PropertiesUtil.getProperty("ftp.server.http.prefix")+targetFileName;

                Map fileMap= Maps.newHashMap();
                fileMap.put("uri",targetFileName);
                fileMap.put("url",url);
                return ServerResponse.createBySuccess(fileMap);
            }else{
                return ServerResponse.createByErrorMessage("无权限操作");
            }
        }
    @RequestMapping("richtext_img_upload.do")
    @ResponseBody
    public Map richtextImgUpload(HttpSession session, @RequestParam(value = "upload_file",required = false) MultipartFile file, HttpServletRequest request, HttpServletResponse response){
          Map resultMap=Maps.newHashMap();
        User user=(User)session.getAttribute(Const.CURRENT_USER);
        if(user==null){
           resultMap.put("success",false);
           resultMap.put("mas","请登录管理员");
        }
        //f=富文本中对于返回值有自己的要求，我们使用的是simditor的要求进行返回
       /* {
            "success":true/false,
                "msg":"error message",#optional
            "file_path":"[real file path]"
        }*/
        if(iUserService.checkadminRole(user).isSuccess()){
            //获取servlet上下文 通用 真实路径
            String path=request.getSession().getServletContext().getRealPath("upload");
            String targetFileName=iFileService.upload(file,path);
          if(StringUtils.isBlank(targetFileName)){
              resultMap.put("success",false);
              resultMap.put("mas","上传失败");
              return resultMap;
          }
            String url= PropertiesUtil.getProperty("ftp.server.http.prefix")+targetFileName;
            resultMap.put("success",true);
            resultMap.put("mas","上传成功");
            resultMap.put("file_path",url);
             //修改与前端的约定
            response.addHeader("Access-Control-Allow-Headers","X-File-Name");
           return resultMap;
        }else{
            resultMap.put("success",false);
            resultMap.put("mas","无权限操作");
            return resultMap;
        }
    }
}
