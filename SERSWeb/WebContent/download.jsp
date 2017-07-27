<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%    
  String filename = request.getParameter("filename");   
  String filepath = request.getParameter("filepath");
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
