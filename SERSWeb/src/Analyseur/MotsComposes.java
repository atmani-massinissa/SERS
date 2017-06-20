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
		createWordList(ressourcePath+"jdm-mc.txt");

	}

	public MotsComposes(TextClass p, String ressourcePath) throws IOException {
		this.ressourcePath = ressourcePath;
		this.oldText = new String(p.newText);
		wordList = new HashSet<String>();
		nonExistingWords = new HashSet<String>();
		createWordList(ressourcePath+"jdm-mc.txt");
		motsTrouves = new ArrayList<String>();
		if (p instanceof Parser) {
			pr = (Parser) p;
			for (String str : pr.linksWiki) {
				System.out.println("LinkWiki : "+str);
				if (str.contains(" ")) {
					motsTrouves.add(str);
					if (!lookUp(str))
						nonExistingWords.add(str);
					this.oldText = this.oldText.replace(str, str.replace(" ", "_"));
				}
			}
			for (String str : pr.bold) {
				System.out.println("BoldWiki : "+str);
				if (str.contains(" ")) {
					motsTrouves.add(str);
					if (!lookUp(str))
						nonExistingWords.add(str);
					this.oldText = this.oldText.replace(str, str.replace(" ", "_"));
				}
			}
							
		}
		
		this.newText = findMC();
		
		for (String str : mots_particuliers()) {
			if (str.contains(" ")) {
				this.newText = this.newText.replace(str, str.replace(" ", "_"));
			}
		}

		if (pr != null)
			addWordsToFile();
		// System.out.println(" Analyser mot composes "+this.newText);
	}

	public void addWordsToFile(HashSet<String> nonExisting) throws IOException {
		FileWriter f = new FileWriter(ressourcePath+"jdm-mc.txt", true);
		BufferedWriter bw = new BufferedWriter(f);
		String outS = new String();
		for (String s : nonExisting) {
			outS = outS + "\n" + s + ";";
		}
		bw.write(outS);
		bw.close();
		f.close();
	}

	public void addWordsToFile() throws IOException {
		FileWriter f = new FileWriter(ressourcePath+"jdm-mc.txt", true);
		BufferedWriter bw = new BufferedWriter(f);
		String outS = new String();
		for (String s : nonExistingWords) {
			outS = outS + "\n" + s + ";";
		}
		bw.write(outS);
		bw.close();
		f.close();
	}

	public void createWordList(String filePath) throws IOException {

		String line;
		BufferedReader reader = Files.newBufferedReader(Paths.get(filePath), StandardCharsets.UTF_8);
		while ((line = reader.readLine()) != null) {
			wordList.add(line.substring(0, line.length() - 1));
		}
	}

	public static Mot requeterRezo(String s) {

		RequeterRezo r = new RequeterRezo();
		Mot m = null;
		try {
			m = r.requete(s);
			// System.out.println(m);
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


	public void lookUp2(String word) throws FileNotFoundException {
		for (String string : wordList) {
			if (string.startsWith(word)) {
				System.out.println(string);
			}
		}
	}
	
	public String findMC() throws FileNotFoundException {

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
			// System.out.println(phrase[j]);
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
						compound_word_underscore = compound_word_underscore.replaceAll("\\s", "_");
//						System.out.println("Mot avant remplacement: "+chaine_mots_compose);
//						System.out.println("Mot après remplacement: "+compound_word_underscore);
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
		 * System.out.println("***************"+s5);
		 */
		return str;
	}
	public HashSet<String> mots_particuliers() throws IOException {
		HashSet<String> mots_composes = new HashSet<String>();
		String str = new String(this.newText);
		String[] s = str.split("\\s|[,.?!:;\\(\\)]+");
		String res = new String();
		for (int i = 0; i < s.length; i++) {
			
				if (Arrays.asList(new String[] {"degré","insuffisance","gain","carence","manque","excès","baisse","taux","diminution","perte","niveaux","augmentation","absence","montée","déficience"}).contains(s[i].toLowerCase())) {
					if (Arrays.asList(new String[] {"du","en","de","d'"}).contains(s[i+1].toLowerCase())) {
						if (Arrays.asList(new String[] {"du","en","de","d'"}).contains(s[i+3].toLowerCase())) {
							mots_composes.add(s[i]+" "+s[i+1]+" "+s[i+2]+" "+s[i+3]+" "+s[i+4]);
						}
						else {
							mots_composes.add(s[i]+" "+s[i+1]+" "+s[i+2]);
						}
					}
				}
				
			}
		HashSet<String> noms_composes = new HashSet<String>();
		
		Lemmatisation abu = new Lemmatisation(ressourcePath);
		
		for (int i = 0; i < s.length; i++) {
			if (abu.isNom(s[i].toLowerCase()) || s[i].contains("_")) {
				if (Arrays.asList(new String[] {"du","en","de","d'"}).contains(s[i+1].toLowerCase())) {
					if (Arrays.asList(new String[] {"du","en","de","d'"}).contains(s[i+3].toLowerCase())) {
						noms_composes.add(s[i]+" "+s[i+1]+" "+s[i+2]+" "+s[i+3]+" "+s[i+4]);
					}
					else {
						noms_composes.add(s[i]+" "+s[i+1]+" "+s[i+2]);
					}
				}
			}
			
		}
		System.out.println("TEST NOUVELLE FONCTION : "+mots_composes);
		System.out.println("TEST NOUVELLE FONCTIONNALITé: "+noms_composes);
		mots_composes.addAll(noms_composes);
		return mots_composes;
//		HashSet<String> nonExisting = new HashSet<String>();
//		for (String mot : mots_composes) {
//			if (!lookUp(mot)) {
//				nonExisting.add(mot);
//			}
//		}
//		try {
//			addWordsToFile(nonExisting);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
	
	public String replace_article(String compound_word_underscore, String str){

		if (compound_word_underscore.endsWith("_de") || compound_word_underscore.endsWith("_du") || compound_word_underscore.endsWith("_en")) {
			String regexp = compound_word_underscore+"(\\s)(.{3})"; 
			Pattern ExpReg = Pattern.compile(regexp);
			Matcher matcher = ExpReg.matcher(str);
			while (matcher.find()) {
				str = str.replace(matcher.group(), matcher.group().replace(matcher.group(1), "_"));
				if (matcher.group(2).startsWith("la ")) {
					System.out.println("ARTICLE "+matcher.group().replace(matcher.group(1), "_").replace(matcher.group(2), matcher.group(2).replaceFirst("\\s", "_"))); 
					str = str.replace(matcher.group().replace(" ", "_"),matcher.group().replace(" ", "_").replace(matcher.group(2), matcher.group(2).replaceFirst("\\s", "_")));
				}
				System.out.println("FIN AVEC 'de' "+matcher.group().replace(matcher.group(1), "_"));
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
		// System.out.println(phrase[j]);
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
		 * System.out.println("***************"+s5);
		 */
		return str;
	}

	public static void main(String[] args) throws Exception {
		System.out.println((new MotsComposes("")).findMcLine("$x se trouvent souvent localisées au niveau de la $y"));
		System.out.println();
	}
}