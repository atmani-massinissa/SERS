package Analyseur;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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
	MotsComposes mc ;
	Path dicoPath;


	public Lemmatisation(String ressourcePath) throws IOException {
		this.ressourcePath=ressourcePath;
		createDico(ressourcePath+"dico.txt");
		oldText = new String();
		newText = new String();
		
	}

	public Lemmatisation(TextClass tc, String ressourcePath) throws Exception {
		this.ressourcePath=ressourcePath;
		oldText = new String(tc.newText);
		mc = null;
		if (tc instanceof MotsComposes) {
			this.mc = (MotsComposes) tc;
		}
		dicoPath =null;
		try {
			dicoPath = Paths.get(MotsComposes.class.getResource("/dico.txt").toURI());

		} catch (URISyntaxException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			createDico(dicoPath.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		newText = lemmatizeText();
	}
	
	public Lemmatisation(TextClass tc, String ressourcePath,TextClass tc2) throws Exception {
		this.ressourcePath=ressourcePath;
		oldText = new String(tc.newText);
		mc = null;
		if (tc instanceof MotsComposes) {
			this.mc = (MotsComposes) tc;
		}
		try {
			createDico(ressourcePath+"dico.txt");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
				////System.out.println(line);
			}
		}
	}



	/*
	 * 
	 * -----------------------------------Lemmatiseurs---------------------------------------------
	 * 
	 */
	public void advSupp() {
		for (String mot : this.mc.motsTrouves) {
			if (this.mc.wordListMap.keySet().contains(mot)) {
				String categorie = this.mc.wordListMap.get(mot);
				if (!categorie.contains("Nom") && !categorie.contains("GN") && categorie.contains("Modifier")) {
					this.oldText = this.oldText.replace(" "+mot.replace(" ","_")+" ", " ");
				}
			}
		}
	}
	public void removeCompAdv() {
		for (String mot : this.mc.motsTrouves) {
			if (this.mc.wordListMap.keySet().contains(mot)) {
				String categorie = this.mc.wordListMap.get(mot);
				if ( !categorie.contains("Adj") &&!categorie.contains("Nom") && !categorie.contains("GN") && categorie.contains("Adv")) {
					this.oldText = this.oldText.replace(" "+mot.replace(" ","_")+" ", " ");
				}		
			}
		}
	}
	public void removeCompCon() {
		for (String mot : this.mc.motsTrouves) {
			if(mot.contains("tand"))
				////System.out.println("//////"+mot);
			if (this.mc.wordListMap.keySet().contains(mot)) {
				////System.out.println("\\\\\\"+mot);

				String categorie = this.mc.wordListMap.get(mot);
				if ( categorie.contains("Con")) {
					////System.out.println("-------------------\\\\\\"+mot);

					this.oldText = this.oldText.replace(" "+mot.replace(" ","_")+" ", " ");
				}		
			}
		}
	}
	
	public void remBothAdv() throws Exception{
		removeCompAdv();
		String str = new String(oldText);
		String[] s = str.split("\\s|[,.?!:;\\(\\)]+");
		ArrayList<String> list = new ArrayList<String>(Arrays.asList(s));
		String res = new String();
		for (int i=0; i<list.size();i++) {
				if(isAdv(list.get(i))){
					this.oldText = this.oldText.replace(" "+list.get(i)+" ", " ");
				}
		}
	}
	
	public void remCon() throws Exception{
		removeCompCon();
		String str = new String(oldText);
		String[] s = str.split("\\s|[,.?!:;\\(\\)]+");
		ArrayList<String> list = new ArrayList<String>(Arrays.asList(s));
		String res = new String();
		for (int i=0; i<list.size();i++) {
				if(iscON(list.get(i)) && !list.get(i).equalsIgnoreCase("ou") && !list.get(i).equalsIgnoreCase("et") ){
					this.oldText = this.oldText.replace(" "+list.get(i)+" ", " ");
				}
			}
	}
	public String lemmatizeText() throws Exception {
		advSupp();
		remBothAdv();
		remCon();
		String str = new String(oldText);
		str = str.replace(" peut ", " ");
		str = str.replace(" peuvent ", " ");
		str = str.replace(" d 'autres ", " d'autres  ");
		str = str.replace(" d' autres ", " d'autres ");
		//str = str.replace(" par ", " ");
		str = str.replace(" qu'", " ");
		str = str.replace(" que ", " ");
		str = str.replace(" au_quotidien ", " ");

		/*String[] s = str.split("\\s|[,.?!:;\\(\\)]+");
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
		}*/
		String res = new String();
		str = remSuccVerbs (str); // remove successive verbs
		String[] s = str.split("\\s|[,.?!:;\\(\\)]+");
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
		//Lemmatise texte en entrÃ©e en entier
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
		//Met les verbes Ã  l'infinitif 
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
			for (String str : map.get(mot)) {
				count++;
			}
		return count;
	}
	
	public int howManyLemmesComp(String mot) {
		int count = 0;
		mot = mot.replace("_", " ").trim();
		if (this.mc.wordListMap.keySet().contains(mot)) {
			String categorie = this.mc.wordListMap.get(mot);
			count = categorie.length();
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
	 * //System.out.println("La source du mot "+mot+" est "+tab[0]); } } } }
	 */

	// isPluriel isMasculin isFeminin isNom isVerb isAdj isAmbigu (plus d'une
	// correspondance)
	// getLemme("bois") ==> boire
	// getPos("bois") ==> verb ou nom ou adj pro det
	// getLemme("tapis","Nom") ==> tapis getLemme("tapis","Ver") ==> tapir
	// penser Ãƒ dÃƒÂ©clencher des exceptions dans les cas ambigus
	// pour spliter sur plusieurs caractÃƒÂ¨res split(":|+") "Nom:Mas+SG" ==>
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
			return false;
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
				String[] tab = str.split("	");
				if (tab[1].contains("Adv")){
					return (!isAdj(mot));
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
	
	public String remAdv() throws Exception {

		String[] tab = this.oldText.split(" ");
		for (String str : tab) {
			if (isAdv(str))
				this.oldText = this.oldText.replace(" "+str+" ", " ");
		}
		return this.oldText;
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

	public boolean isPos(String mot, String pos) {
		if (!map.keySet().contains(mot))
			return false;
		else {
				for (String str : map.get(mot)) {
					String[] tab = str.split("	");
						if (tab[1].contains(pos))
						{
							return true;
						}
				}
			}
		
		return false;
	}
	public boolean isPosComp(String mot, String pos) {
		if (!this.mc.wordListMap.keySet().contains(mot.trim().toLowerCase().replace("_", " ")))
			return false;
		else {
			if (mot.trim().toLowerCase().equals("système_sérotoninergique")) {
				////System.out.println("BZZZZZZZZZZ "+this.mc.wordListMap.get(mot.trim().toLowerCase().replace("_", " ")));
				////System.out.println("BZZZZZZZZZZ "+this.mc.wordListMap.get(mot.trim().toLowerCase().replace("_", " ")).contains(pos));
			}	
			if (this.mc.wordListMap.get(mot.trim().toLowerCase().replace("_", " ")).contains(pos)){
					////System.out.println("XXXXXXXXXXXXXXX"+this.mc.wordListMap.get(mot));
					return true;
				}
			}
		
		return false;
	}
	
	public boolean isAdvComp(String mot) {
		if (!this.mc.wordListMap.keySet().contains(mot))
			return false;
		else {
				if (this.mc.wordListMap.get(mot).contains("Adv") ){
					////System.out.println("XXXXXXXXXXXXXXX"+this.mc.wordListMap.get(mot));
					return true;
				}
			}
		
		return false;
	}
	public boolean isVerComp(String mot) {
		if (!this.mc.wordListMap.keySet().contains(mot))
			return false;
		else {
				if (this.mc.wordListMap.get(mot).contains("Ver") ){
					return true;
				}
			}
		
		return false;
	}
	public boolean isNomComp(String mot) {
		if (!this.mc.wordListMap.keySet().contains(mot))
			return false;
		else {
				if (this.mc.wordListMap.get(mot).contains("Nom") || this.mc.wordListMap.get(mot).contains("GN")){
					////System.out.println("XXXXXXXXXXXXXXX"+this.mc.wordListMap.get(mot));
					return true;
				}
			}
		
		return false;
	}
	public boolean isNom(String mot) {
		if(mot.startsWith("l'")){
			//System.out.println("s[i] = "+mot.substring(2, mot.length()));
			mot = mot.substring(2, mot.length());
		}
		if (!map.keySet().contains(mot))
			return false;
		else {
			for (String str : map.get(mot)) {
				String[] tab = str.split("	");
				if (tab[1].contains("Nom")){
					////System.out.println("XXXXXXXXXXXXXXX"+tab[1]);
					return true;
				}
			}
		}
		return false;
	}
	
	public int isNomBis(String mot) {
		if(mot.startsWith("l'")){
			//System.out.println("s[i] = "+mot.substring(2, mot.length()));
			mot = mot.substring(2, mot.length());
		}
		if (!map.keySet().contains(mot))
			return -1;
		else {
			for (String str : map.get(mot)) {
				String[] tab = str.split("	");
				if (tab[1].contains("Nom"))
					return 1;
			}
		}
		return 0;
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
			return false/*//System.out.println("")*/;
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
				if (tab[1].contains("Pro"))
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
			String v = new String(list.get(i));
			v = v.replace("_", " ").trim();
			////System.out.println("v ="+v);
			if(howManyLemmes(v)==1 || v.equals(new String("a")) || isVerComp(v)){
				////System.out.println("v =+"+v);

					while(isVerb(v) || isVerComp(v) ||  v.equals(new String("a"))){
					//	//System.out.println("v =++"+v);

						k++;
						i++;
						v = new String(list.get(i));
						v = v.replace("_", " ");
					}
			}
			else if(i+1<list.size()) {
				//System.out.println("v =++*"+v);
			//	i++;
			//	v = new String(list.get(i));
			//	v = v.replace("_", " ");
			}
			if(k>1){
				for(int j=(i-2); (j>=(i-k)) && (j<list.size());j--){
				//	//System.out.println("remove =++*"+list.get(j));

					list.remove(j);
					}
				}
			i++;
			}
		 	res = String.join(" ", list);	
		 	return res;		
	}
	
	

	public static void main(String[] args) throws Exception {

		Lemmatisation lm = new Lemmatisation("D:\\apache-tomcat-8.0.32\\wtpwebapps\\SERSWeb\\WEB-INF\\classes\\");
		System.out.println(" lm "+lm.howManyLemmes("risque"));

		/*File file = new File("Text.txt");
		FileInputStream fis = new FileInputStream(file);
		byte[] data = new byte[(int) file.length()];
		fis.read(data);
		fis.close();
		String str = new String(data, "UTF-8");
		str = str.replaceAll("'", "' ");
		//System.out.println(lm.lemmatizeTextPostMc(str));*/

	}

}
