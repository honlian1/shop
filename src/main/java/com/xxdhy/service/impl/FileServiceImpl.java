package com.xxdhy.service.impl;

import com.google.common.collect.Lists;
import com.xxdhy.service.IFileService;
import com.xxdhy.util.FTPUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
public class FileServiceImpl implements IFileService {

       private Logger logger= LoggerFactory.getLogger(FileServiceImpl.class);

      public String upload(MultipartFile file,String path) {
          //  extension n.扩展    original adj.初次的 原来的
            String fileName=file.getOriginalFilename();
          //扩展名
          //abc.jpg
          //   思考？？？？奇怪为什么这个点不要，后又把点和扩展名加回去
          //    应该是老师的问题，这里的lasIndexOf可以不＋1 后面的直接UUID加fileExtension
          String fileExtensionName=fileName.substring(fileName.lastIndexOf(".")+1);
          String uploadFileName= UUID.randomUUID().toString()+"."+fileExtensionName;
          logger.info("开始上传文件！！！上传文件的文件名：{},上传的路径：{},新文件名{}",fileName,path,uploadFileName);
          //文件流
          File fileDir=new File(path);
          //判断文件的路径是否存在   若不存在，由自己创建
          if(!fileDir.exists()){
           fileDir.setWritable(true);
           fileDir.mkdirs();
          }
          File targetFile = new File(path,uploadFileName);

          try {
              file.transferTo(targetFile);
              //文件已经上传成功
              //todo 将targetFile上传到我们的FTP服务器上'

              FTPUtil.uploadFile(Lists.newArrayList(targetFile));
              //已经上传到ftp服务器上
              //todo 上传完之后，删除upload下面的文件
             targetFile.delete();

          } catch (IOException e) {
              logger.error("上传文件异常",e);
          }

          return null;
      }

    public static void main(String[] args) {
           String file="abc.jpg";
        System.out.println(file.substring(file.lastIndexOf(".")));
    }
}
