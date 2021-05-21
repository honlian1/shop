package com.xxdhy.util;

import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class FTPUtil {

       private static final Logger logger= LoggerFactory.getLogger(FTPUtil.class);

      private static String ftpIp=PropertiesUtil.getProperty("ftp.server.ip");
      private static String ftpUser=PropertiesUtil.getProperty("ftp.user");
      private static String ftpPass=PropertiesUtil.getProperty("ftp.pass");

      private String ip;
      private int port;
      private String user;
      private String pwd;
      private FTPClient ftpClient;

    public FTPUtil(String ip, int port, String user, String pwd) {
        this.ip = ip;
        this.port = port;
        this.user = user;
        this.pwd = pwd;
    }

    public static boolean uploadFile(List<File> fileList) throws IOException {
           //port:端口
           FTPUtil ftpUtil=new FTPUtil(ftpIp,21,ftpUser,ftpPass);
           logger.info("开始连接FTP服务器");
           boolean result=ftpUtil.uploadFile("img",fileList);
           logger.info("开始连接ftp服务器，结束上传，上传结果:{}");

           return result;
    }
    public boolean uploadFile(String remotePath,List<File> fileList) throws IOException {
        boolean uploaded=true;
        FileInputStream fis=null;
        //连接FTP服务器
        if(connectServer(this.getIp(),this.port,this.user,this.pwd)){
            try {
                ftpClient.changeWorkingDirectory(remotePath);
                ftpClient.setBufferSize(1024);
                ftpClient.setControlEncoding("UTF-8");
                //设置文件类型   binary adj.二元的
                ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
                //开始上传
                ftpClient.enterLocalPassiveMode();
                for(File fileItem:fileList){
                     fis = new FileInputStream(fileItem);
                     ftpClient.storeFile(fileItem.getName(),fis);
                }
            } catch (IOException e) {
                logger.error("上传文件 异常,",e);
                uploaded=false;
                e.printStackTrace();
            } finally{
                fis.close();
                ftpClient.disconnect();
            }
        }
          return uploaded;

    }

    /**
     *             创建FTP的连接
     * @param ip  地址
     * @param port 端口
     * @param user  用户名
     * @param pwd  密码
     * @return
     */
    public boolean connectServer(String ip,int port,String user,String pwd){

        boolean isSuccess=false;
        ftpClient =new FTPClient();
        try {
            ftpClient.connect(ip);
            isSuccess=ftpClient.login(user,pwd);

        } catch (IOException e) {
            logger.error("连接FTP服务器异常",e);
       }
        return isSuccess;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public FTPClient getFtpClient() {
        return ftpClient;
    }

    public void setFtpClient(FTPClient ftpClient) {
        this.ftpClient = ftpClient;
    }
}
