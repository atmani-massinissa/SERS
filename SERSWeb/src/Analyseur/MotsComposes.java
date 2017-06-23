package Analyseur;

// ajouter la mÃ©thode qui trouve les mots composÃ©s Ã  l'aide des tableaux de l'objet parser s'ils ne sont pas trouvÃ©s
//avec findMC() et appeler cette mÃ©thode dans le constructeur

import java.io.*;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.*;

import org.wikiutils.StringUtils;

import RequeterRezo.RequeterRezo;
import RequeterRezo.Mot;
import RequeterRezo.RequeterRezo.TupleRelationTerme;

public class MotsComposes extends TextClass {

	ArrayList<String> motsTrouves;
	HashSet<String> wordList;
	HashSet<String> nonExistingWords;
	Parser pr = null;
	String ressourcePath;

	public MotsComposes(String ressourcePath) throws IOException {
		this.ressourcePath = ressourcePath;
		oldText = new String();
		newText = new String();
		motsTrouves = new ArrayList<String>();
		wordList = new HashSet<String>();
		nonExistingWords = new HashSet<String>();

	}

	public MotsComposes(TextClass p, String ressourcePath) throws Exception {
		this.ressourcePath = ressourcePath;
		this.oldText = new String(p.newText);
		wordList = new HashSet<String>();
		nonExistingWords = new HashSet<String>();
		//createWordList(ressourcePath+"jdm-mc.txt");
		motsTrouves = new ArrayList<String>();
		if (p instanceof Parser) {
			pr = (Parser) p;
			for (String str : pr.linksWiki) {
				//////system.out.println("LinkWiki : "+str);
				if (str.contains(" ")) {
					motsTrouves.add(str);
					if (!lookUp(str))
						nonExistingWords.add(str);
					this.oldText = this.oldText.replace(str, str.replace(" ", "_"));
				}
			}
			for (String str : pr.bold) {
				//system.out.println("BoldWiki : "+str);
				if (str.contains(" ")) {
					motsTrouves.add(str);
					if (!lookUp(str))
						nonExistingWords.add(str);
					this.oldText = this.oldText.replace(str, str.replace(" ", "_"));
				}
			}
							
		}
		
		this.newText = findMC();
		//Création de nouveaux mots composés
		apostrF();
		nomAdj();
		//noms_particuliers();
		mots_particuliers();

		if (pr != null)
			System.out.println("mots ajoutés "+nonExistingWords);
			addWordsToFile();
		// //system.out.println(" Analyser mot composes "+this.newText);
	}

	public void addWordsToFile(HashSet<String> nonExisting) throws IOException {
		//FileWriter f = new FileWriter("C:\\Users\\user\\workspace\\SERSWeb\\WebContent\\WEB-INF\\"+"jdm-mc.txt", true);
		  
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
			    new FileOutputStream("C:\\Users\\user\\workspace\\SERSWeb\\WebContent\\WEB-INF\\"+"jdm-mc.txt",true), "UTF-8"));
		String outS = new String();
		for (String s : nonExisting) {
			outS = outS + "\n" + s + ";";
		}
		bw.append(outS);
		bw.close();
		//f.close();
	}

	public void addWordsToFile() throws IOException {
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
			    new FileOutputStream("C:\\Users\\user\\workspace\\SERSWeb\\WebContent\\WEB-INF\\"+"jdm-mc.txt",true), "UTF-8"));
		String outS = new String();
		for (String s : nonExistingWords) {
			outS = outS + "\n" + s + ";";
		}
		bw.write(outS);
		bw.close();
		//f.close();
	}

	public void createWordList(String filePath) throws IOException {

		String line;
		//BufferedReader reader = Files.newBufferedReader(Paths.get(filePath), StandardCharsets.UTF_8);
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath),"utf-8"));
		while ((line = reader.readLine()) != null) {
			wordList.add(line.substring(0, line.length() - 1));
		}
	}

	public static Mot requeterRezo(String s) {

		RequeterRezo r = new RequeterRezo();
		Mot m = null;
		try {
			m = r.requete(s);
			// //system.out.println(m);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return m;
	}

	public Boolean lookUp(String word) throws FileNotFoundException {
		return (wordList.contains(word));
	}	
	public String findMC() throws FileNotFoundException {
		try {
			createWordList(ressourcePath+"jdm-mc.txt");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String str = (new String(oldText));
		String[] avoid = { "du", "la", "des", "les", "un", "une", "par", "elle", "il", "mais", "ou", "est", "et",
				"donc", "or", "ni", "car", "", "_", "de", " ", "à", "dans", "dont" };
		String[] phrase = str.split("[.|;|,|==|\\n|?|!|:|(|)|\\[|\\]|«|»|“|”|\"]+");
		for (int j = 0; j < phrase.length; j++) {
			int i = 0;
			// if (phrase[j].charAt(phrase[j].length())==" ";
			String[] s = phrase[j].split(" ");
			int mindecalage = Math.max(s.length, 2);
			int decalage = Math.min(8, mindecalage);
			Boolean found = false;
			// //system.out.println(phrase[j]);
			while (s.length > i) {
				String chaine_mots_compose = new String();
				while ((s.length > i) && Arrays.asList(avoid).contains(s[i].toLowerCase())) {
					i++;
				}
				int fenetre = i;
				int k = 0;
				while (k < decalage && k + fenetre < s.length) {
					if (!s[k + fenetre].equals("")) {
						chaine_mots_compose = chaine_mots_compose + s[k + fenetre] + " ";
					}
					k++;
				}
				// chaine_mots_compose = chaine_mots_compose.trim();
				if (chaine_mots_compose.length() > 0)
					chaine_mots_compose = chaine_mots_compose.substring(0, chaine_mots_compose.length() - 1);
				if (motsTrouves.contains(chaine_mots_compose)) {
					i = i + (chaine_mots_compose.split("\\s")).length;
					decalage = Math.min(8, mindecalage);
					String compound_word_underscore = new String(chaine_mots_compose);
					
					compound_word_underscore = compound_word_underscore.replaceAll("\\s|[  ]", "_");
					
				}

				else {
					// chaine_mots_compose = chaine_mots_compose.trim();
					found = lookUp(chaine_mots_compose.replace("l' ", "l'").replace("s' ", "s'"));
					if (found) {
						motsTrouves.add(chaine_mots_compose.trim());
						String compound_word_underscore = new String(chaine_mots_compose);
						for (String b : motsTrouves) {
							if (chaine_mots_compose.contains(b) && chaine_mots_compose.length() > b.length()) {
								String c = b.replace(" ", "_");
								chaine_mots_compose = chaine_mots_compose.replace(b, c);
							}
						}
						i = i + (chaine_mots_compose.split("\\s")).length;
						decalage = Math.min(8, mindecalage);
						str = str.replace("L' ", "L'");
						str = str.replace("S' ", "S'");
						str = str.replace("D' ", "D'");
						str = str.replace("l' ", "l'");
						str = str.replace("s' ", "s'");
						str = str.replace("d' ", "d'");
						compound_word_underscore = compound_word_underscore.replace("L' ", "L'");
						compound_word_underscore = compound_word_underscore.replace("S' ", "S'");
						compound_word_underscore = compound_word_underscore.replace("D' ", "D'");
						compound_word_underscore = compound_word_underscore.replace("l' ", "l'");
						compound_word_underscore = compound_word_underscore.replace("s' ", "s'");
						compound_word_underscore = compound_word_underscore.replace("d' ", "d'");
						compound_word_underscore = compound_word_underscore.replaceAll("\\s", "_");
//						//system.out.println("Mot avant remplacement: "+chaine_mots_compose);
//						//system.out.println("Mot après remplacement: "+compound_word_underscore);
						str = str.replace(chaine_mots_compose, compound_word_underscore);

						//Mots composés se terminant par des articles sont rallongés

						str = new String(replace_article(compound_word_underscore, str));
						
					} else if (decalage == 2) {
						i++;
						decalage = Math.min(8, mindecalage);
					} else
						decalage--;
				}
			}
		}
		/*
		 * for(String s5 : motsTrouves)
		 * //system.out.println("***************"+s5);
		 */
		return str;
	}
	public void apostrF(){
		newText = newText.replace("d'", "d' ");
		newText = newText.replace("l'", "l' ");
		newText = newText.replace("s'", "s' ");
		newText = newText.replace("D'", "D' ");
		newText = newText.replace("L'", "L' ");
		newText = newText.replace("S'", "S' ");
	}
	public HashSet<String> nomAdj() throws Exception {
		HashSet<String> noms_adj_composes = new HashSet<String>();
		Lemmatisation abu = new Lemmatisation(ressourcePath);
		String str = new String(this.newText);
		String[] s = str.split("\\s|[,.?!:;\\(\\)]+");
		
		for (int i = 0; i < s.length; i++) {
			String mot="";
			if (s[i].contains("_")||(abu.isNom(s[i].toLowerCase()) && abu.howManyLemmes(s[i].toLowerCase()) == 1)) {
				if (!s[i+1].equals("est") && (abu.isAdj(s[i+1].toLowerCase())) || s[i+1].endsWith("ique") ) {
					mot=s[i]+" "+s[i+1];
				}
				if (mot.matches(".+\\s.+")) {
					mot = mot.replace("L' ", "L'");
					mot = mot.replace("S' ", "S'");
					mot = mot.replace("D' ", "D'");
					mot = mot.replace("l' ", "l'");
					mot = mot.replace("s' ", "s'");
					mot = mot.replace("d' ", "d'");
					noms_adj_composes.add(mot.trim());
				}
			}
		}
		System.out.println("TEST noms_adj: "+noms_adj_composes);
		for (String mot : noms_adj_composes) {		
			this.newText = this.newText.replace(mot, mot.replace(" ", "_"));
			if (!lookUp(mot.replace("_", " ").trim())) {
				nonExistingWords.add(mot.replace("_", " "));
				if (mot.contains(" ")) {
					this.newText = this.newText.replace(mot, mot.replace(" ", "_"));
				}
			}
		}
		
		this.newText =  new String(replace_article(noms_adj_composes, this.newText));
		return noms_adj_composes;
		
	}
	public HashSet<String> mots_particuliers() throws Exception {
		HashSet<String> mots_composes = new HashSet<String>();
		String str = new String(this.newText);
		String[] s = str.split("\\s|[,.?!:;\\(\\)]+");
		String res = new String();	
		for (int i = 0; i < s.length; i++) {
			
				if (Arrays.asList(new String[] {"complément","capable","capables","degré","insuffisance","gain","carence","manque","excès","baisse","taux","diminution","perte","niveaux","niveau","augmentation","absence","montée","déficience"}).contains(s[i].toLowerCase())) {
					String mot="";
					if (Arrays.asList(new String[] {"d'","l'","du","en","de","dans","le","la","une","un","leurs"}).contains(s[i+1].toLowerCase())) {
						int k = 2;
						int p = 0;
						for (int j = 0; j <= k; j++) {
							mot = mot+s[i+j]+" ";
							if (Arrays.asList(new String[] {"d'","l'","du","en","de","dans","le","la","une","un","leurs"}).contains(s[i+k+1].toLowerCase())) {
								k=k+2;
							}
							if (j==k && Arrays.asList(new String[] {"d'","l'","du","en","de","dans","le","la","une","un","leurs"}).contains(s[i+j].toLowerCase()))  {
								k++;
							}
						}
					
						if (mot != null) {
							mot = mot.replace("L' ", "L'");
							mot = mot.replace("S' ", "S'");
							mot = mot.replace("D' ", "D'");
							mot = mot.replace("l' ", "l'");
							mot = mot.replace("s' ", "s'");
							mot = mot.replace("d' ", "d'");
							mots_composes.add(mot.trim());
						}
		
					}
				}
				
			}
		
		System.out.println("TEST mots particuliers : "+mots_composes);
		for (String mot : mots_composes) {
			if (mot.contains(" ")) {
				this.newText = this.newText.replace(mot, mot.replace(" ", "_"));
			}
			if (!lookUp(mot.trim())) {
				if (mot.contains("_")) {
					nonExistingWords.add(mot.replace("_", " "));
				}
				
			
			}
		}
		return mots_composes;
	}
		
	public HashSet<String> noms_composes() {

//		for (int i = 0; i < s.length; i++) {
//			if ((abu.isNom(s[i].toLowerCase()) && abu.howManyLemmes(s[i].toLowerCase()) == 1) || s[i].contains("_")) {
//			  if (!s[i].equals("partie")) {
//				String mot="";
//				if (Arrays.asList(new String[] {"d'","l'","du","en","de","dans","le","la","une","un","leurs"}).contains(s[i+1].toLowerCase())) {
//					int k = 2;
//					for (int j = 0; j <= k; j++) {
//						mot = mot+s[i+j]+" ";
//						System.out.println("PAS WAZZZAAAAAAAAA "+mot+" et "+s[i+j]+" "+k);
//						if (Arrays.asList(new String[] {"d'","l'","du","en","de","dans","le","la","une","un","leurs"}).contains(s[i+k+1].toLowerCase())) {
//							k=k+2;	
//							System.out.println("WAZZZAAAAAAAAA "+mot+" et "+s[i+j+1]+" "+k);
//						}
//						if (j==k && (Arrays.asList(new String[] {"d'","l'","du","en","de","dans","le","la","une","un","leurs","cette","ce","sa"}).contains(s[i+j].toLowerCase()) || isNumeric(s[i+j])))  {
//							k++;
//						}
//						
//						}
//				
//					if (mot != null) {
//						noms_composes.add(mot.trim());
//					}
//
//				}
//			  }
//		    }
//		}
//		for (int i = 0; i < s.length; i++) {
//			if (abu.isNom(s[i].toLowerCase()) || s[i].contains("_")) {
//				int k = 0;
//				if (!s[i].equals("partie")) {
//					if (i+1 < s.length) {
//						if (Arrays.asList(new String[] {"d'","l'","du","en","de","dans","le","la","une","un","leurs"}).contains(s[i+1].toLowerCase())) {
//							String nom ="";
//							if (i+3 < s.length) {
//								if (Arrays.asList(new String[] {"d'","l'","du","en","de","dans","le","la","une","un","leurs"}).contains(s[i+3].toLowerCase())) {
//									k = 4;
//									if (i+4 < s.length) {
//										//nom = (s[i]+" "+s[i+1]+" "+s[i+2]+" "+s[i+3]+" "+s[i+4]);
//									}
//									
//								}
//							}
//							else {
//								k = 2;
//								//nom = (s[i]+" "+s[i+1]+" "+s[i+2]);
//							}
//							if (i+k < s.length && k > 0) {
//								for (int j = 0; j <= k; j++) {
//									nom=nom+s[i+j]+" ";
//									if (j==k && i+k+1 < s.length && Arrays.asList(new String[] {"d'","l'","du","en","de","dans","le","la","une","un","leurs","leur"}).contains(s[i+j].toLowerCase()))  {
//										k++;
//									}
//								}
//								
//							}
////							nom = nom.replace("L' ", "L'");
////							nom = nom.replace("S' ", "S'");
////							nom = nom.replace("D' ", "D'");
////							nom = nom.replace("l' ", "l'");
////							nom = nom.replace("s' ", "s'");
////							nom = nom.replace("d' ", "d'");
//							noms_composes.add(nom.trim());
//							
//						}
//					}
//				
//				}
//				
//			}
//			
//		}
		
		
		//System.out.println("Non existing : "+nonExistingWords);
	
		return null;
	}	
	public static boolean isNumeric(String str)  
	{  
	  try  
	  {  
	    double d = Double.parseDouble(str);  
	  }  
	  catch(NumberFormatException nfe)  
	  {  
	    return false;  
	  }  
	  return true;  
	}
	public String replace_article(HashSet<String> mots_composes, String str){
		for (String compound_word: mots_composes) {
			if (compound_word.endsWith("_un") || compound_word.endsWith("_une") || compound_word.endsWith("_sur") || compound_word.endsWith("_aucun") || compound_word.endsWith("_aucune")) {
				String regexp = compound_word+"(\\s)(.{3})"; 
				Pattern ExpReg = Pattern.compile(regexp);
				Matcher matcher = ExpReg.matcher(str);
				while (matcher.find()) {
					str = str.replace(matcher.group(), matcher.group().replace(matcher.group(1), "_"));
					if (matcher.group(2).startsWith("la ")) { 
						str = str.replace(matcher.group().replace(" ", "_"),matcher.group().replace(" ", "_").replace(matcher.group(2), matcher.group(2).replaceFirst("\\s", "_")));
					}
				}
				
			}
		}
		
		return str;
	}
	
	public String replace_article(String compound_word_underscore, String str){

		if (compound_word_underscore.endsWith("_de") || compound_word_underscore.endsWith("_du") || compound_word_underscore.endsWith("_en") || compound_word_underscore.endsWith("_l'")) {
			String regexp = compound_word_underscore+"(\\s)(.{3})"; 
			Pattern ExpReg = Pattern.compile(regexp);
			Matcher matcher = ExpReg.matcher(str);
			while (matcher.find()) {
				str = str.replace(matcher.group(), matcher.group().replace(matcher.group(1), "_"));
				if (matcher.group(2).startsWith("la ")) { 
					str = str.replace(matcher.group().replace(" ", "_"),matcher.group().replace(" ", "_").replace(matcher.group(2), matcher.group(2).replaceFirst("\\s", "_")));
				}
			}
			
		}
		return str;
	}

	public String findMcLine(String str) throws FileNotFoundException {

		String[] avoid = { "du", "la", "des", "les", "un", "une", "par", "elle", "il", "mais", "ou", "est", "et",
				"donc", "or", "ni", "car", "", "_", "de", " ", "à", "dans", "dont", "celui", "que", "ce", "qui" };
		int i = 0;
		String[] s = str.split(" ");
		int mindecalage = Math.max(s.length, 2);
		int decalage = Math.min(8, mindecalage);
		Boolean found = false;
		// //system.out.println(phrase[j]);
		while (s.length > i) {
			String chaine_mots_compose = new String();
			while ((s.length > i) && Arrays.asList(avoid).contains(s[i].toLowerCase())) {
				i++;
			}
			int fenetre = i;
			int k = 0;
			while (k < decalage && k + fenetre < s.length) {
				if (!s[k + fenetre].equals("")) {
					chaine_mots_compose = chaine_mots_compose + s[k + fenetre] + " ";
				}
				k++;
			}
			// chaine_mots_compose = chaine_mots_compose.trim();
			if (chaine_mots_compose.length() > 0)
				chaine_mots_compose = chaine_mots_compose.substring(0, chaine_mots_compose.length() - 1);
			// chaine_mots_compose = chaine_mots_compose.trim();
			found = lookUp(chaine_mots_compose.replace("l' ", "l'").replace("s' ", "s'"));
			if (chaine_mots_compose.contains("espérance de vie")) {
				System.out.println("MOT : "+chaine_mots_compose+" LOOKUP VALUE "+found);
			}
			if (found) {
				if (chaine_mots_compose.contains("espérance de vie")) {
					
				}
				motsTrouves.add(chaine_mots_compose.trim());
				String compound_word_underscore = new String(chaine_mots_compose);
				for (String b : motsTrouves) {
					if (chaine_mots_compose.contains(b) && chaine_mots_compose.length() > b.length()) {
						String c = b.replace(" ", "_");
						chaine_mots_compose = chaine_mots_compose.replace(b, c);
					}
				}
				i = i + (chaine_mots_compose.split("\\s")).length;
				decalage = Math.min(8, mindecalage);
				
				compound_word_underscore = compound_word_underscore.replaceAll("\\s", "_");
				str = str.replace(chaine_mots_compose, compound_word_underscore);
			} else if (decalage == 2) {
				i++;
				decalage = Math.min(8, mindecalage);
			} else
				decalage--;

		}
		/*
		 * for(String s5 : motsTrouves)
		 * //system.out.println("***************"+s5);
		 */
		return str;
	}

	public static void main(String[] args) throws Exception {
		//system.out.println((new MotsComposes("")).findMcLine("$x se trouvent souvent localisées au niveau de la $y"));
		//system.out.println();
	}
}