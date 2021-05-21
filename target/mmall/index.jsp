<%@ page language="java"  contentType="text/html; charset=UTF-8" %>

<html>
<meta http-equiv="Access-Control-Allow-Origin" content="*" />
<body>
<h2>Hello World!</h2>



springmvc上传文件
<form name="form1" action="/manager/product/upload.do" method="post" enctype="multipart/form-data">
    <input type="file" name="upload_file" />
    <input type="submit" value="springmvc上传文件" />
</form>
<a href="tset01.html">adawd</a>
<form action="/manager/user/login.do" method="post">
    <input type="text" name="username">
    <input type="text" name="password">
    <input type="submit" value="登陆啦~">
</form>
富文本图片上传文件
<form name="form2" action="/manager/product/richtext_img_upload.do" method="post" enctype="multipart/form-data">
    <input type="file" name="upload_file" />
    <input type="submit" value="富文本图片上传文件" />
</form>

</body>
</html>