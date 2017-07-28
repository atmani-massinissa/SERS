<%@page contentType="text/html; charset=iso-8859-1" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>
<body>
<br><br>
------------------------------------------------------------------------------------------------
<br><br>
<br><br>
Fichiers : 
<br><br>
Patrons : WEB-INF/classes/PatternsParMcLem.txt
<br>
Dico d'ABU :  WEB-INF/classes/dico.txt
<br>
Termes composés jdm-mc.txt : WEB-INF/classes/jdm-mc.txt
<br>
Termes composés CONSTRUITS : WEB-INF/classes/OurJdm-mc.txt
<br>
Résultats : WEB-INF/classes/Results/NomDeL'article.txt
<br><br>
--------------------------------------------------------------------------------------------------------
<br><br>
<br><br>
Fichier des mots composés construits : OurJdm-mc.txt
<br><br>
Le fichier OurJdm contient les mots composés construits par le programme.
<br>
Une ligne du fichier renseigne un mot construit, elle est de la forme suivante : 
<br>
 

Mot composé;Catégorie grammaticale;Fonction;Source :NomDeL’article

<br><br>
<br><br>
*-Fonction : 
<br><br>
<br><br>
Afin de lier les mots entre eux plusieurs fonctions ont été mises en place.
<br>
Ayant pour but d’alimenter jeuxdemots, chacune de ces fonctions référence, pour chaque terme construit, ses composants présents dans jeuxdemots.
<br><br>
<br>
+-Caractérisation(x,y) : Le mot est créé grâce aux fonctions nomAdj & adjNom, une relation de caractérisation est donc créée entre le nom et l’adjectif, en s’assurant que ces derniers sont contenus dans jeuxdemots.
<br>
Exemple : dépressions postnatales; Nom:Ajouté ;Caractérisation(dépressions, postnatales);  Source :  Grossesse;
<br><br>
<br>
+-Schéma1(x,y) : La fonction impliquée dans les mots ayant la valeur schéma1 est un processus cherchant à lier les expression débutant par une certaine liste de mot :
<br>Baisse %DET% %Nom% 
<br>Augmentation %DET% %Nom%
<br>Risque %DET% %Nom%
<br>Mesure %DET% %Nom%
<br>...<br><br>
Exemple : Carence en aspirine; Nom:Ajouté ;Schéma1(aspirine,carence);  Source :  Dépression_(psychiatrie);
<br><br>
<br>

+-Schéma2(x,y): Fonction regroupant les mots sous la forme suivante : 
 %NOM% %DET% %NOM%
<br>
Exemple : complications de la grossesse; Nom:Ajouté ;Schéma1(complications,grossesse);  Source :  Grossesse;
<br><br>
<br>
+-advDadj : Génère les adjectifs de forme : Très dangereux, plus efficace….
<br>

</body>
</html>