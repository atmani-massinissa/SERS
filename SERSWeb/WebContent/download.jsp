<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"
    import = "java.net.URLDecoder"
import = "java.net.URLEncoder"
%>
<%    
  String filename = request.getParameter("filename");   
  String filepath = request.getParameter("filepath");
  filename = URLDecoder.decode(filename, "UTF-8");
   response.setContentType("APPLICATION/OCTET-STREAM");   
   response.setHeader("Content-Disposition","attachment; filename=\"" + filename + "\"");   
  
  java.io.FileInputStream fileInputStream=new java.io.FileInputStream(filepath + filename);  
            
  int i; 
  out.clear();
  while ((i=fileInputStream.read()) != -1) {  
    out.write(i);   
  }   
  fileInputStream.close();   
%>   
