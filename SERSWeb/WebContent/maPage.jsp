<%@ page import = "Analyseur.*" 
import = "org.wikipedia.Wiki"
import = "java.util.regex.Matcher"
import = "java.util.regex.Pattern"
import = "java.net.URLDecoder"
import = "java.net.URLEncoder"
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
 <br><br>------------------------------------------------Paramétrage-----------------------------------------------<br><br>
 <br><br>Saisir le seuil confiance minimal :<br><br>
<input name="threshold" value="0.2">
  <br><br><br>
  <br><br><br>
  <input type="submit" style="border:gray; color:black;" value="Extraire relations sémantiques">
 <br><br>-------------------------------------------------------------------------------------------------------------<br><br>


  </form> 
  <br><br><br>
  
<%
		if (request.getParameter("inputText")!=null)
		{	
			
			if (Relation.types_de_relations.isEmpty()){
				
				Principale.fetchPatrons (session.getServletContext().getRealPath("/WEB-INF/classes/PatternsParMcLem.txt"));
			
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
					analyseurDeTest.setTitle(matcher.group(2).toString());
					//analyseurDeTest.setText(text);
					//analyseurDeTest.setText(frWiki.getPageText("Dépression_(psychiatrie)"));
					//out.print(""Texte en wikiCode"+" +text);
					analyseurDeTest.analyserParMcLem(Double.parseDouble(request.getParameter("threshold")));
					%>
					<br><br>
					<a href="download.jsp?filepath=<%=session.getServletContext().getRealPath("/WEB-INF/classes/")%>&filename=PatternsParMcLem.txt">Fichier des patterns lémmatisé</a>
					<br><br>
					<a href="download.jsp?filepath=<%=session.getServletContext().getRealPath("/WEB-INF/classes/")%>&filename=OurJdm-mc.txt">Fichier des mot composés extraits de l'article Wikipédia</a>
					<br><br>
					<a href="download.jsp?filepath=<%=session.getServletContext().getRealPath("/WEB-INF/classes/results/")%>&filename=<%=URLEncoder.encode(analyseurDeTest.getTitle()+"_Results.txt","UTF-8")%>">Fichier des relations extraites</a>
					<br><br>
 					<%
 					analyseurDeTest.displayResults(out);
 					%>
 					<br><br>---------------------------------Relations tirées des mots composés-------------------------------<br><br>
 					<%
 					//analyseurDeTest.displayResultsComposes(out);
 					%>
 					<br><br>-----------------------------------------------------------------------------------------------------<br><br>
 					<br><br>------------------------------------------------Texte-----------------------------------------------<br><br>
 					<%out.print("Texte prétraité"+analyseurDeTest.getText());
 					%><br><br>-----------------------------------------------------------------------------------------------------<br><br>
 					<%analyseurDeTest.writeResults();
				}
				else 
				{
					%>La page <%out.print(request.getParameter("inputText"));%> est introuvable sur Wikipédia Fr!
					<%
					
				}
			}
			
		}
%>  

</div>
</body>
</html>