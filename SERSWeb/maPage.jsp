<%@ page import = "Analyseur.*" 
import = "org.wikipedia.Wiki"
import = "java.util.regex.Matcher"
import = "java.util.regex.Pattern"
import = "java.net.URLDecoder"
%>
<%@page contentType="text/html; charset=iso-8859-1" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Web Analyser</title>
</head>
<body>
<div style="margin: 0 auto; width: 43%"><h2>Démo du mécanisme d'extraction</h2></div>
<div style="margin: 0 auto; width: 50%">
  <br><br><br>
<form action="maPage.jsp" accept-charset="UTF-8">
  Entrez page à analyser :<br><br>
  <textarea rows="1" cols="60" name="inputText"><%if (request.getParameter("inputText")==null){%>https://fr.wikipedia.org/wiki/Dépression_(psychiatrie)
<% } 
   else
   		{
	   		out.println(request.getParameter("inputText"));
   		}
%></textarea>
 Veuillez saisir le seul minimal des relation a éxtraire :<br><br>
<input name="threshold" value="0.0"><%if (request.getParameter("threshold")==null){%>0.0
<% } 
   else
   		{
	   		out.println(request.getParameter("threshold"));
   		}
%></input>
  <br><br><br>
  <div style="margin: 0 auto; width: 70%">
  <div style="float:left">
 Prétraitements:<br>
 Lemmatisation<BR>
 Mots composés<BR>
</div>
<div style="float:right">
<br>
<input type="radio" name="id" value="Lemmatisation"><BR>
<input type="radio" name="id" value=".NET"> <BR>
  </div>
  <div style="float:right">
<br>
<input type="radio" name="id2" value="Lemmatisation"><BR>
<input type="radio" name="id2" value=".NET"> <BR>
  </div>
   <br><br><br>
   <br><br><br>
  <input type="submit" style="border:gray; color:black;" value="Extraire relations sémantiques">
  </div>
  <br><br>

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
				//out.print("1" +matcher.group(2));
				String text= frWiki.getPageText(""+matcher.group(2));
				if (text != null) {
					analyseurDeTest.setText(text);
					analyseurDeTest.setTitle(matcher.group(2));
					//analyseurDeTest.setText(text);
					//analyseurDeTest.setText(frWiki.getPageText("Dépression_(psychiatrie)"));
					//out.print(""Texte en wikiCode"+" +text);
					analyseurDeTest.analyserParMcLem(Double.parseDouble(request.getParameter("threshold")));
 					analyseurDeTest.displayResults(out);
 					out.print("Texte prétraité"+analyseurDeTest.getText());
 					analyseurDeTest.writeResults();
				}
				else 
				{
					%>La page <%out.print(request.getParameter("inputText"));%> est introuvable sur Wikipédia Fr!
					<%
					
				}
			}
			else 
			{
				%>Le nom de l'article n'a pas été séparé du lien : <%out.print(request.getParameter("inputText"));%> 
				<%
				
			}
			
		}
%>  

</div>
</body>
</html>