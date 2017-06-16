<%@ page import = "Analyseur.*" 
import = "org.wikipedia.Wiki"
import = "java.util.regex.Matcher"
import = "java.util.regex.Pattern"
%>
<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=US-ASCII">
<title>Web Analyser</title>
</head>
<body>
<div style="margin: 0 auto; width: 43%"><h2>Démo du mécanisme d'extraction</h2></div>
<div style="margin: 0 auto; width: 50%">
  <br><br><br>
<form action="maPage.jsp">
  Entrez page à analyser :<br><br>
  <textarea rows="1" cols="50" name="inputText"><%if (request.getParameter("inputText")==null){%>
<% } 
   else
   		{
	   		out.println(request.getParameter("inputText"));
   		}
%></textarea>
  <br><br><br>
  <div style="margin: 0 auto; width: 50%">
  <input type="submit" style="border:gray; color:black;" value="Extraire relations sémantiques">
  </div>
  </form> 
  <br><br><br>
  
<%
		if (request.getParameter("inputText")!=null)
		{	
			
			if (Relation.types_de_relations.isEmpty()){
				
				Principale.fetchPatrons (session.getServletContext().getRealPath("/WEB-INF/PatternsParMcLem.txt"));
			
			}
			Wiki frWiki = new Wiki("fr.wikipedia.org");
			frWiki.setMaxLag(-1);
			frWiki.getPageText("");
			String regexp="(.+)[/](.+)$";
			Pattern ExpReg = Pattern.compile(regexp);
			Matcher matcher = ExpReg.matcher(request.getParameter("inputText"));
			if (matcher.find()) {
				Analyseur analyseurDeTest=new Analyseur(session.getServletContext().getRealPath("/WEB-INF/"));
				String text = frWiki.getPageText(""+matcher.group(2));
				if (text != null) {
					analyseurDeTest.setText(text);
					out.print("1" +text);
					//analyseurDeTest.analyserParMcLem();
 					//analyseurDeTest.displayResults(out);
				}
				else 
				{
					%>La page <%out.print(request.getParameter("inputText"));%> est introuvable sur Wikipédia Fr!
					<%
					
				}
			}
			else 
			{
				%>La page <%out.print(request.getParameter("inputText"));%> est introuvable !
				<%
				
			}
			
		}
%>  

</div>
</body>
</html>