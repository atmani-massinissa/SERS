package Analyseur;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

public class Lemmatisation extends TextClass {

	// String oldText;
	// String newText;
	String ressourcePath;
	static HashMap<String, ArrayList<String>> map;

	public Lemmatisation(String ressourcePath) throws IOException {
		this.ressourcePath=ressourcePath;
		createDico(ressourcePath+"dico.txt");
		oldText = new String();
		newText = new String();
		
	}

	public Lemmatisation(TextClass tc, String ressourcePath) throws Exception {
		this.ressourcePath=ressourcePath;
		oldText = new String(tc.newText);
		try {
			createDico(ressourcePath+"dico.txt");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		newText = lemmatizeText();
	}

	public static void createDico(String filePath) throws IOException {
		map = new HashMap<String, ArrayList<String>>();
		String line;
		BufferedReader reader = Files.newBufferedReader(Paths.get(filePath), StandardCharsets.UTF_8);
		while ((line = reader.readLine()) != null) {
			String[] parts = line.split("	");
			if (parts.length >= 2) {
				String key = parts[0];
				String value = parts[1];

				String deux = parts[1] + "	" + parts[2];

				if (map.keySet().contains(parts[0])) {
					map.get(parts[0]).add(deux);
				} else {
					ArrayList<String> values = new ArrayList<String>();
					values.add(deux);
					map.put(key, values);
				}

			} else {
				//System.out.println(line);
			}
		}
	}



	/*
	 * 
	 * -----------------------------------Lemmatiseurs---------------------------------------------
	 * 
	 */
	public String lemmatizeText() throws Exception {
		//Lemmatise texte de l'objet en entier
		String str = new String(oldText);
		String[] s = str.split("\\s|[,.?!:;\\(\\)]+");
		ArrayList<String> list = new ArrayList<String>(Arrays.asList(s));
		list.removeAll(Arrays.asList("", null," "));
		String res = new String();
		str = new String();
		for (int i=0; i<list.size();i++) {
			if(!isAdv(list.get(i)) ){
				str=str+list.get(i)+" ";
			}
		}
		s = str.split("\\s|[,.?!:;\\(\\)]+");
		list = new ArrayList<String>(Arrays.asList(s));
		str = new String();
		for ( int i=0; i<list.size();i++) {
			if(iscON(list.get(i))){
					if(list.get(i).equalsIgnoreCase("ou") || list.get(i).equalsIgnoreCase("et") )
						str=str+list.get(i)+" ";
			}
			else {
				str=str+list.get(i)+" ";
			}
		}
		str = remSuccVerbs (str); // remove successive verbs
		s = str.split("\\s|[,.?!:;\\(\\)]+");
		for (int i=0; i<s.length;i++) {
				res=res+lemmatize(s[i])+" ";
		}

		res = lemmatizeArticles(res);
		res=res.replaceAll("\\s+", " ");
		res=res.replaceAll(" une | des | le | la | les | l' | du ", " un ");
		return res;
	}
	public String lemmatizeCOORD(String res){
		res = res.replaceAll("\\s+", " ");
		res = res.replaceAll(" une | des | le | la | les | l' | du ", " un ");
		return res;
	}
	public String lemmatizeArticles(String res){
		//Remplace les articles par "un"
		res = res.replaceAll("\\s+", " ");
		res = res.replaceAll(" une | des | le | la | les | l' | du ", " un ");
		return res;
	}

	public String lemmatizeText(String str) throws Exception {
		//Lemmatise texte en entrée en entier
		String[] s = str.split("\\s|[,.?!:;\\(\\)]+");
		String res = new String();
		for (int i = 0; i < s.length; i++) {
			if (!isAdv(s[i]))
				res = res + lemmatize(s[i]) + " ";

		}
		res = lemmatizeArticles(res);
		return res;
	}

	public String lemmatizeTextLine(String str) throws Exception {
		str = remSuccVerbs (str);
		String[] s = str.split("\\s|[.?!;\\(\\)]+");
		String res = new String();
		for (int i = 0; i < s.length; i++) {
			if (!isAdv(s[i]))
				res = res + lemmatize(s[i]) + " ";

		}
		res = lemmatizeArticles(res);
		return res;
	}

	public String lemmatizeTextPostMc(String str) throws Exception {
		String[] s = str.split("\\s|[,.?!:;\\(\\)]+");
		String res = new String();
		for (int i = 0; i < s.length; i++) {
			if (!isAdv(s[i]))
				res = res + lemmatize(s[i]) + " ";

		}
		res = lemmatizeArticles(res);
		return res;
	}
	
	public String lemmatize(String mot) {
		//Met les verbes à l'infinitif 
		if (mot.toLowerCase().startsWith("s'") || mot.toLowerCase().startsWith("m'") || mot.toLowerCase().startsWith("t'")) {
			mot = mot.substring(2);
		}
		ArrayList<String> tab = map.get(mot);
		if (tab != null) {
			if (tab.size() == 1) {
				if (tab.get(0).split("	")[1].substring(0, 3).equals("Ver"))
					return tab.get(0).split("\\s")[0];
			}
		}
		return mot;
	}
	/*
	 * --------------------------------------Utilitaires-------------------------------------------
	 */
	public int howManyLemmes(String mot) {
		int count = 0;
		if (map.keySet().contains(mot))
			for (String key : map.keySet()) {
				if (key.equals(mot))
					for (String str : map.get(mot)) {
						count++;
					}
			}
		return count;
	}

	public String getLemme(String mot) {
		return ((map.get(mot).get(0)).split("\\s"))[0];
	}

	/*
	 * public void getLemmes(String mot) { if (!map.keySet().contains(mot))
	 * String res = mot;ystem.out.println("Le mot n'existe pas"); else { for
	 * (String key : map.keySet()){ if(key.equals(mot)) for (String str :
	 * map.get(mot)){ String[] tab = str.split("	"); //return tab[0];
	 * System.out.println("La source du mot "+mot+" est "+tab[0]); } } } }
	 */

	// isPluriel isMasculin isFeminin isNom isVerb isAdj isAmbigu (plus d'une
	// correspondance)
	// getLemme("bois") ==> boire
	// getPos("bois") ==> verb ou nom ou adj pro det
	// getLemme("tapis","Nom") ==> tapis getLemme("tapis","Ver") ==> tapir
	// penser Ã dÃ©clencher des exceptions dans les cas ambigus
	// pour spliter sur plusieurs caractÃ¨res split(":|+") "Nom:Mas+SG" ==>
	// [0]="Nom" [1]="Mas" [2]="SG"

	public String detecter(String mot, String s) throws Exception {
		if (!map.keySet().contains(mot))
			throw new Exception("Le mot n'existe pas");
		else {
			for (String key : map.keySet()) {
				if (key.equals(mot))
					for (String str : map.get(mot)) {
						String[] tab = str.split("	");
						if (tab[1].contains(s))
							return tab[0];
					}
			}
		}
		return null;
	}

	public String getPos(String mot) {
		if (map.keySet().contains(mot)) {
			for (String str : map.get(mot)) {
				String[] tab = str.split("	");
				return tab[1].substring(0, 3);
			}
		}
		return null;
	}

	public boolean isPluriel(String mot) throws Exception {
		if (!map.keySet().contains(mot))
			throw new Exception("Le mot n'existe pas");
		else {
			for (String str : map.get(mot)) {
				String[] tab = str.split("	");
				if (tab[1].contains("PL"))
					return true;
			}
		}
		return false;
	}

	public boolean isAdj(String mot) throws Exception {
		if (!map.keySet().contains(mot))
			throw new Exception("Le mot n'existe pas");
		else {
			for (String str : map.get(mot)) {
				String[] tab = str.split("	");
				if (tab[1].contains("Adj"))
					return true;
			}
		}
		return false;
	}

	public boolean isAdv(String mot) throws Exception {

		if (!map.keySet().contains(mot))
			return false;
		else {
			for (String str : map.get(mot)) {
				//System.out.println("is Adv "+str);
				String[] tab = str.split("	");
				if (tab[1].contains("Adv")){
					//System.out.println("tab[1] "+tab[1]);
					return true;
					}
			}
		}
		return false;
	}

	public String remAdv(String mot) throws Exception {

		String[] tab = mot.split(" ");
		for (String str : tab) {
			if (isAdv(str))
				mot = mot.replace(str, "");
		}

		return mot;
	}

	public boolean isVerb(String mot) throws Exception {
		if (!map.keySet().contains(mot))
			return false;
		else {
			for (String str : map.get(mot)) {
				String[] tab = str.split("	");
				if (tab[1].contains("Ver"))
					return true;
			}
		}
		
		return false;
	}

	public boolean isMasculin(String mot) throws Exception {
		if (!map.keySet().contains(mot))
			return false;
		else {
			for (String str : map.get(mot)) {
				String[] tab = str.split("	");
				if (tab[1].contains("Mas"))
					return true;
			}
		}
		return false;
	}

	public boolean isNom(String mot) {
		if (!map.keySet().contains(mot))
			return false;
		else {
			for (String str : map.get(mot)) {
				String[] tab = str.split("	");
				if (tab[1].contains("Nom"))
					return true;
			}
		}
		return false;
	}
	
	public boolean isDet(String mot) {
		if (!map.keySet().contains(mot))
			return false;
		else {
			for (String str : map.get(mot)) {
				String[] tab = str.split("	");
				if (tab[1].contains("Det"))
					return true;
			}
		}
		return false;
	}
	
	public boolean iscON(String mot) throws Exception{
		if (!map.keySet().contains(mot)) 
			return false/*System.out.println("")*/;
		else {
			for (String str : map.get(mot)){
				String[] tab = str.split("	");
				if (tab[1].contains("Con")) 
					return true;
			}
		}
		return false;
}
	
	public boolean isPre(String mot) {
		if (!map.keySet().contains(mot))
			return false;
		else {
			for (String str : map.get(mot)) {
				String[] tab = str.split("	");
				if (tab[1].contains("Pre"))
					return true;
			}
		}
		return false;
	}
	public boolean isPro(String mot) {
		if (!map.keySet().contains(mot))
			return false;
		else {
			for (String str : map.get(mot)) {
				String[] tab = str.split("	");
				if (tab[1].contains("Pre"))
					return true;
			}
		}
		return false;
	}
	
	public boolean isFeminin(String mot) throws Exception {
		if (!map.keySet().contains(mot))
			throw new Exception("Le mot n'existe pas");
		else {
			for (String str : map.get(mot)) {
				String[] tab = str.split("	");
				if (tab[1].contains("Fem"))
					return true;
			}
		}
		return false;
	}
	
	public String remSuccVerbs(String str) throws Exception{
		String[] s = str.split("\\s|[.?!;\\(\\)]+");
		ArrayList<String> list = new ArrayList<String>(Arrays.asList(s));
		list.removeAll(Arrays.asList("", null," "));
		String res = new String();
		int i=0;
		while(i<list.size()){
			int k=0;
			if(howManyLemmes(list.get(i))==1){
					while(isVerb(list.get(i))){
						k++;
						i++;
					}
			}
			else {
				if(Arrays.asList(new String[]{"peut","peuvent","pouvoir","être","doivent"}).contains(list.get(i))){
					k++;
					i++;
					while(isVerb(list.get(i))){
						k++;
						i++;
					}
				}
				else{
					i++;
					
				}
			}
			if(k>1){
				for(int j=(i-2); (j>=(i-k)) && (j<list.size());j--){
					list.remove(j);
					}
				}
			i++;
			}
		 	res = String.join(" ", list);	
		 	return res;		
	}
	
	

	public static void main(String[] args) throws Exception {

		Lemmatisation lm = new Lemmatisation("");

		File file = new File("Text.txt");
		FileInputStream fis = new FileInputStream(file);
		byte[] data = new byte[(int) file.length()];
		fis.read(data);
		fis.close();
		String str = new String(data, "UTF-8");
		str = str.replaceAll("'", "' ");
		System.out.println(lm.lemmatizeTextPostMc(str));

	}

}
