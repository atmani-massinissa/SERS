package Analyseur;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Principale {
	public static void main(String[] args) throws Exception {
		createPatternVersions("Patterns.txt");

		/*fetchPatrons ("PatternsParMc.txt");
		Analyseur analyseurDeTest=new Analyseur("Dépression (psychiatrie).txt");
		analyseurDeTest.analyserParMc();
		analyseurDeTest.displayResults();*/

		/*fetchPatrons ("PatternsParLem.txt");
		Analyseur analyseurDeTest=new Analyseur("Dépression (psychiatrie).txt");
		analyseurDeTest.analyserParLem();
		analyseurDeTest.displayResults();*/

		fetchPatrons ("PatternsParMcLem.txt");
	    //Analyseur analyseurDeTest=new Analyseur("Dépression (psychiatrie).txt");
	    Analyseur analyseurDeTest=new Analyseur("D:\\TER\\Articles\\Articles\\G\\Grossesse.txt");
		analyseurDeTest.analyserParMcLem();
		analyseurDeTest.displayResults();
		
		/*fetchPatrons ("PatternsParLemMc.txt");
		Analyseur analyseurDeTest=new Analyseur("Dépression (psychiatrie).txt");
		analyseurDeTest.analyserParLemMc();
		analyseurDeTest.displayResults();*/
	}

	public static void fetchPatrons (String filePath) throws IOException{

		BufferedReader buffer = Files.newBufferedReader(Paths.get(filePath), StandardCharsets.UTF_8);
		String tmp;
		String tmc = "none";
		String tmpComp;
		String type = null;
		String patron;
		int nbrTerms = 0;
		String carAccentues="œ,àâäçèéêëîïôöùûüÀÂÄÇÈÉÊËÎÏÔÖÙÛÜ\\-";
		Pattern ExpRegPatron = Pattern.compile("\\s([A-Za-z\\s_'$"+carAccentues+"]+)\\s([$].*)");
		Pattern ExpRegType = Pattern.compile("([A-Za-z/"+carAccentues+"]+) [:]");
		Pattern ExpRegNbrTerms = Pattern.compile("\\$[A-Za-z]");
		Matcher matcherPatron ;
		Matcher matcherType ;
		Matcher matcherNbrTerms ;
		while ((tmpComp=buffer.readLine()) != null) {
			if (tmpComp.contains("-->")) {
				tmp=tmpComp.split("-->")[0].trim();
				tmc=tmpComp.split("-->")[1].trim();
			}
			else {
				tmp=tmpComp;
				tmc="none";
			}
			matcherPatron = ExpRegPatron.matcher(tmp);
			matcherType = ExpRegType.matcher(tmp);
			matcherNbrTerms = ExpRegNbrTerms.matcher(tmp);
			if (matcherType.find()){
				type=matcherType.group(1);
				//System.out.println("Type de relation ajouté: "+type);
				if (!Relation.types_de_relations.contains(type)) {
					Relation.types_de_relations.add(type);
					Relation.typePatrons.put(type, new ArrayList<String>());
				}
			}
			if (matcherPatron.find()){
				nbrTerms = 0;
				patron=matcherPatron.group(1);
				while (matcherNbrTerms.find()) {
					nbrTerms++;
				}
				if (!Relation.types_de_relations.contains(type)) {
					System.err.println("Type de relation inconnu pour le patron : "+patron);
				}
				else if (!Relation.typePatrons.get(type).contains(patron)) {
					patron = patron.replaceAll("\\s\\$[A-Za-z]+\\s", "\\$");
					if (matcherPatron.group(2).contains("$Post")) {
						patron += "$Post";
						nbrTerms--;
					}
					Relation.typePatrons.get(type).add(patron);
					Relation.patronNbrTerms.put(patron,new Integer (nbrTerms));
					if (!tmc.equals("none")) {
						Relation.patronConstraint.put(type+" : "+patron,tmc);
					}

				}
				else {
					//System.out.println("Patron déja définit ---> " +patron);
				}

			}

		}

		//System.out.println("Types de relations définis: "+Relation.types_de_relations);
		//System.out.println("Patrons définis: "+Relation.typePatrons.values());
		//System.out.println("Patrons avec contraintes: "+Relation.patronConstraint);

	}

	public static void createPatternVersions (String filePath) throws Exception {
		BufferedReader buffer = Files.newBufferedReader(Paths.get(filePath), StandardCharsets.UTF_8);
		MotsComposes mc = new MotsComposes();
		Lemmatisation lm = new Lemmatisation();
		String tmp;
		ArrayList<String> parMcS = new ArrayList<String>();
		ArrayList<String> parLemS = new ArrayList<String>();
		ArrayList<String> parMcLemS = new ArrayList<String>();
		ArrayList<String> parLemMcS = new ArrayList<String>();
		String parMc = new String();
		String parLem = new String ();
		String parMcLem = new String ();
		String parLemMc = new String ();
		while ((tmp=buffer.readLine()) != null) {
			if (tmp.trim().endsWith(":")){
				parMcS.add('\n'+tmp+'\n');
				parLemS.add('\n'+tmp+'\n');
				parMcLemS.add('\n'+tmp+'\n');
				parLemMcS.add('\n'+tmp+'\n');
			}
			else if (tmp.trim().length()>0) {
				if(!parMcS.contains(mc.findMcLine(tmp)+'\n'))
					parMcS.add(mc.findMcLine(tmp)+'\n');
				if(!parLemS.contains(lm.lemmatizeTextLine(tmp)+'\n'))
					parLemS.add(lm.lemmatizeTextLine(tmp)+'\n');
				if(!parMcLemS.contains(lm.lemmatizeTextLine((mc.findMcLine(tmp)))+'\n'))
					parMcLemS.add(lm.lemmatizeTextLine((mc.findMcLine(tmp)))+'\n');
				if(!parLemMcS.contains(mc.findMcLine(lm.lemmatizeTextLine(tmp))+'\n'))
					parLemMcS.add(mc.findMcLine(lm.lemmatizeTextLine(tmp))+'\n');
			}
		}
		for (String s : parMcS)
			parMc+=s;
		for (String s : parLemS)
			parLem+=s;
		for (String s : parMcLemS)
			parMcLem+=s;
		for (String s : parLemMcS)
			parLemMc+=s;
		try(PrintWriter out = new PrintWriter("PatternsParMc.txt")  ){
			out.println(parMc);
		}
		try(PrintWriter out = new PrintWriter("PatternsParLem.txt")  ){
			out.println(parLem);
		}
		try(PrintWriter out = new PrintWriter("PatternsParMcLem.txt")  ){
			out.println(parMcLem);
		}
		try(PrintWriter out = new PrintWriter("PatternsParLemMc.txt")  ){
			out.println(parLemMc);
		}
	}

}



