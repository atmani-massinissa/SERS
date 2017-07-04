package Analyseur;

// ajouter la méthode qui trouve les mots composés à l'aide des tableaux de l'objet parser s'ils ne sont pas trouvés
//avec findMC() et appeler cette méthode dans le constructeur

import java.io.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.*;

import javax.servlet.jsp.PageContext;

import org.wikiutils.StringUtils;

import RequeterRezo.RequeterRezo;
import RequeterRezo.Mot;
import RequeterRezo.RequeterRezo.TupleRelationTerme;

public class MotsComposes extends TextClass {

	ArrayList<String> motsTrouves;
	HashSet<String> wordList;
	HashMap<String,String> wordListMap;
	HashSet<String> nonExistingWords;
	Parser pr = null;
	String ressourcePath;
	Analyseur analyseur;
	Path jdm_mcPath;
	Path our_jdm_mcPath;
	
	public MotsComposes(String ressourcePath) throws IOException {
		this.ressourcePath = ressourcePath;
		oldText = new String();
		newText = new String();
		motsTrouves = new ArrayList<String>();
		wordList = new HashSet<String>();
		wordListMap = new HashMap<String,String>();
		nonExistingWords = new HashSet<String>();
		

	}

	public MotsComposes(TextClass p, String ressourcePath, Analyseur analyseur) throws Exception {
		this.ressourcePath = ressourcePath;
		this.oldText = new String(p.newText);
		this.wordList = new HashSet<String>();
		this.wordListMap = new HashMap<String,String>();
		this.nonExistingWords = new HashSet<String>();
		this.analyseur = analyseur;
		our_jdm_mcPath = null;
		jdm_mcPath =null;
		try {
			jdm_mcPath = Paths.get(MotsComposes.class.getResource("/jdm-mc.txt").toURI());
			our_jdm_mcPath = Paths.get(MotsComposes.class.getResource("/OurJdm-mc.txt").toURI());

		} catch (URISyntaxException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		//createWordList(ressourcePath+"jdm-mc.txt");
		motsTrouves = new ArrayList<String>();
		if (p instanceof Parser) {
			pr = (Parser) p;
			for (String str : pr.linksWiki) {
				//////system.out.println("LinkWiki : "+str);
				if (str.contains(" ")) {
					motsTrouves.add(str.trim().toLowerCase());
					if (!lookUp(str))
						nonExistingWords.add(str);
					this.oldText = this.oldText.replace(str, str.replace(" ", "_"));
				}
			}
			for (String str : pr.bold) {
				//system.out.println("BoldWiki : "+str);
				if (str.contains(" ")) {
					motsTrouves.add(str.trim().toLowerCase());
					if (!lookUp(str.trim().toLowerCase()))
						nonExistingWords.add(str.trim().toLowerCase());
					this.oldText = this.oldText.replace(str, str.replace(" ", "_"));
				}
			}
							
		}
		
		this.newText = findMC();
		//Cr�ation de nouveaux mots compos�s
		//apostrFs();
		quantifAdj();
		AdjNom();
		nomAdj();
		//apostrFs();
		noms_particuliers();
		//nomS_particuliers();
		mots_particuliers();

		if (pr != null)
			//System.out.println("mots ajoutés "+nonExistingWords);
			addWordsToFile();
		// //system.out.println(" Analyser mot composes "+this.newText);
	}


	public void addWordsToFile() throws IOException 
	{	
		
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
		 new FileOutputStream(jdm_mcPath.toString(),true), "UTF-8"));
		BufferedWriter bw2 = new BufferedWriter(new OutputStreamWriter(
		 new FileOutputStream(our_jdm_mcPath.toString(),true), "UTF-8"));
		String outS = new String();
		for (String s : nonExistingWords){
			if(!lookUp(s)){
							outS = outS +s+ ";  Source :  "+analyseur.getTitle()+ ";\n";
			}
		}
		outS = outS + "############################;  Source :  "+analyseur.getTitle()+ ";\n";
		bw.append(outS);
		bw2.write(outS);
		bw.close();
		bw2.close();	
	}
	public void addWordsToFile(HashSet<String> nonExisting) throws IOException 
	{
		
		//System.out.println("ééééé "+MotsComposes.class.getResource("jdm-mc.txt"));

		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
		 new FileOutputStream(jdm_mcPath.toString(),true), "UTF-8"));
		BufferedWriter bw2 = new BufferedWriter(new OutputStreamWriter(
		 new FileOutputStream(our_jdm_mcPath.toString(),true), "UTF-8"));
		String outS = new String();
		for (String s : nonExisting){
			if(!lookUp(s)){
							outS = outS +s+ ";  Source :  "+analyseur.getTitle()+ ";\n";
			}
		}
		bw.append(outS);
		bw2.write(outS);
		bw.close();
		bw2.close();	
	}

	public void createWordList(String filePath) throws IOException {

		String line;
		//BufferedReader reader = Files.newBufferedReader(Paths.get(filePath), StandardCharsets.UTF_8);
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath),"utf-8"));
		while ((line = reader.readLine()) != null) {
			wordList.add(line.split(";")[0].trim().toLowerCase());
			if (line.contains(";")) {
				String[] tab = line.split(";");
				String categorie="";
				for (int i = 1; i < tab.length; i++) {
					if (tab[i].contains(":")) {
						categorie= categorie+tab[i]+" / ";
					}				
				}
				wordListMap.put(tab[0].trim().toLowerCase(),categorie);
			}
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
		return wordList.contains(word.trim().toLowerCase());
	}	
	
	public Boolean lookUpBis(String word) throws FileNotFoundException {
		return (wordList.contains(word.toLowerCase()));
	}
	public String findMC() throws FileNotFoundException {
		try {
			createWordList(jdm_mcPath.toString());
			//System.out.println("#################"+lookUp(new String("############################")));
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
						compound_word_underscore = compound_word_underscore.replace("L' ", "L'");
						compound_word_underscore = compound_word_underscore.replace("S' ", "S'");
						compound_word_underscore = compound_word_underscore.replace("D' ", "D'");
						compound_word_underscore = compound_word_underscore.replace("l' ", "l'");
						compound_word_underscore = compound_word_underscore.replace("s' ", "s'");
						compound_word_underscore = compound_word_underscore.replace("d' ", "d'");
						compound_word_underscore = compound_word_underscore.replaceAll("\\s", "_");
//						//system.out.println("Mot avant remplacement: "+chaine_mots_compose);
//						//system.out.println("Mot apr�s remplacement: "+compound_word_underscore);
						str = str.replace(chaine_mots_compose, compound_word_underscore);

						//Mots compos�s se terminant par des articles sont rallong�s

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
	public HashSet<String > AdjNom() throws Exception{
		HashSet<String> adj_nom_composes = new HashSet<String>();
		Lemmatisation abu = new Lemmatisation(this,this.ressourcePath);
		String[] s = this.newText.split("\\s|[,.?!:;\\(\\)]+");
		
		for (int i = 0; i < s.length; i++) {
			String mot="";
			int j =0;
			for (j = 0; i+j < s.length-1 &&	(Arrays.asList(new String[]{
					  "faible","petit","petite"
					 ,"autre", "beau", "bonne","bon", "bel", "belle"
					 ,"grand", "gros", "grosses", "haut", "jeune", "mauvais", "mauvaise"
					 ,"petit", "vieux", "vilain","premier","premiere", "nouveau",
					 "faibles","petits","petites"
					 ,"autres", "beaux", "bonnes","bons", "bels", "belles"
					 ,"grands", "gros", "grosses", "hauts", "jeunes", "mauvais", "mauvaises"
					 ,"petits", "vieux", "vilains","premiers","premieres", "nouveaux"}).contains(s[i+j]))
					 || s[i+j].endsWith("ième"); j++) {
				mot=mot+s[i+j]+" ";
			}
			if (abu.isPosComp(s[i+j].trim().toLowerCase(),"Nom")||(abu.isNom(s[i+j].toLowerCase()) && abu.howManyLemmes(s[i].toLowerCase()) == 1)) {		
				mot +=s[i+j]+" ";
				if (mot.matches(".+\\s.+")) {
					mot = apostrFj(mot);
					adj_nom_composes.add(mot.trim());
				}
			}
		}
		//System.out.println("TEST adj_noms: "+adj_nom_composes);
		for (String mot : adj_nom_composes) {		
			this.newText = this.newText.replace(mot, mot.replace(" ", "_"));
			if (!lookUp(mot.replace("_", " ").trim().toLowerCase())) {
				nonExistingWords.add(mot.trim().replace("_", " "));
				this.wordListMap.put(mot.trim().replace("_", " ")," Nom:Ajouté ");
				if (mot.contains(" ")) {
					this.newText = this.newText.replace(mot, mot.replace(" ", "_"));
				}
			}
		}
		
		this.newText =  new String(replace_article(adj_nom_composes, this.newText));
		return adj_nom_composes;
	
	}
	public HashSet<String > quantifAdj() throws Exception{
		HashSet<String> quantifAdj = new HashSet<String>();
		Lemmatisation abu = new Lemmatisation(this, this.ressourcePath);
		String[] s = this.newText.split("\\s|[,.?!:;\\(\\)]+");
		for (int i = 0; i < s.length; i++) 
			{
				String mot="";
				if (Arrays.asList(new String[] {"non","trés","extr�mement","hautement","sensiblement","peu","moyennement","moins", "plus"}).contains(s[i].trim().toLowerCase())) {
					mot = s[i]+" ";
					if( i+1 < s.length-1 &&	(!abu.isPos(s[i+1].toLowerCase(),"PP") || (abu.isPos((s[i+1].trim().toLowerCase()),"Adj") && s[i+1].trim().toLowerCase().endsWith("ie")) ) && (s[i+1].trim().toLowerCase().endsWith("ique") || s[i+1].trim().toLowerCase().endsWith("iques") || abu.isPosComp((s[i+1].trim().toLowerCase()),"Adj") || abu.isAdj(s[i+1].trim().toLowerCase())) ) {
						mot=mot+s[i+1]+" ";
					}
				}
				if (mot.matches(".+\\s.+")) {
					mot = apostrFj(mot);
					quantifAdj.add(mot.trim().toLowerCase());
				}
				
		
			}
		//System.out.println("TEST quantif_adj: "+quantifAdj);
		for (String mot : quantifAdj) {		
			this.newText = this.newText.replace(mot, mot.replace(" ", "_"));
			if (!lookUp(mot.replace("_", " ").trim().toLowerCase())) {
				nonExistingWords.add(mot.trim().toLowerCase());
				this.wordListMap.put(mot.trim().replace("_", " ")," Adj:Ajout� ");
				if (mot.trim().contains(" ")) {
					this.newText = this.newText.replace(mot.trim(), mot.trim().replace(" ", "_"));
				}
			}
		}
		
		this.newText =  new String(replace_article(quantifAdj, this.newText));
		return quantifAdj;
	}
	public HashSet<String> nomAdj() throws Exception {
		HashSet<String> noms_adj_composes = new HashSet<String>();
		Lemmatisation abu = new Lemmatisation(this,this.ressourcePath);
		String[] s = this.newText.split("\\s|[,.?!:;\\(\\)]+");
		
		for (int i = 0; i < s.length; i++) {
			String mot="";
			if (abu.isPosComp(s[i].trim().toLowerCase(),"Nom")||/*s[i].contains("_")||*/(abu.isNom(s[i].toLowerCase()) && abu.howManyLemmes(s[i].toLowerCase()) == 1)) {
			//if (s[i].contains("_")||(abu.isNom(s[i].toLowerCase()) && abu.howManyLemmes(s[i].toLowerCase()) == 1)) {		
				mot +=s[i]+" ";
				if (s[i].equals("système_sérotoninergique")) {
						System.out.println("mot : "+s[i+1]+" condition : "+(/*!abu.isPos(s[i+1].toLowerCase(),"PP"))); ||*/(abu.isPos((s[i+1].trim().toLowerCase()),"Adj")))); //&&s[i+1].trim().toLowerCase().endsWith("ie")));
						System.out.println("mot : "+s[i+2]+" condition : "+(/*!abu.isPos(s[i+1].toLowerCase(),"PP"))); ||*/(abu.isPos((s[i+1].trim().toLowerCase()),"Adj")))); //&&s[i+1].trim().toLowerCase().endsWith("ie")));
						System.out.println("mot : "+s[i+3]+" condition : "+(/*!abu.isPos(s[i+1].toLowerCase(),"PP"))); ||*/(abu.isPos((s[i+1].trim().toLowerCase()),"Adj")))); //&&s[i+1].trim().toLowerCase().endsWith("ie")));
				}
					for (int j = 1; i+j < s.length-1 &&	(!abu.isPos(s[i+j].toLowerCase(),"PP") || (abu.isPos((s[i+j].trim().toLowerCase()),"Adj") && s[i+j].trim().toLowerCase().endsWith("ie")) ) && (s[i+j].trim().toLowerCase().endsWith("ique") || s[i+j].trim().toLowerCase().endsWith("iques") || abu.isPosComp((s[i+j].trim().toLowerCase()),"Adj") || abu.isAdj(s[i+j].trim().toLowerCase())) ; j++) {
						mot=mot+s[i+j]+" ";
					}
					
				
				if (mot.matches(".+\\s.+")) {
					mot = apostrFj(mot);
					noms_adj_composes.add(mot.trim());
				}
			}
		}
		//System.out.println("TEST noms_adj: "+noms_adj_composes);
		for (String mot : noms_adj_composes) {		
			this.newText = this.newText.replace(mot, mot.replace(" ", "_"));
			if (!lookUp(mot.replace("_", " ").trim().toLowerCase())) {
				nonExistingWords.add(mot.trim().replace("_", " "));
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
		for (int i = 0; i < s.length; i++) {
			
				if (Arrays.asList(new String[] {"somme","mesure","abus","risque","présence","père","mère","s.ur","soeur","frère","fils","fille","complément","capable","capables","degré","insuffisance","gain","carence","manque","excés","baisse","taux","diminution","perte","niveaux","niveau","augmentation","absence","montée","déficience"}).contains(s[i].trim().toLowerCase())) {
					String mot="";
					if (Arrays.asList(new String[] {"d'","l'","du","en","de","dans","le","la","une","un","leurs","cette"}).contains(s[i+1].trim().toLowerCase())) {
						int k = 2;
						int p = 0;
						for (int j = 0; j <= k; j++) {
							mot = mot+s[i+j]+" ";
							if (Arrays.asList(new String[] {"d'","l'","du","en","de","dans","le","la","une","un","leurs","cette"}).contains(s[i+k+1].trim().toLowerCase())) {
								k=k+2;
							}
							if (j==k && Arrays.asList(new String[] {"presque","d'","l'","du","en","de","dans","le","la","une","un","leurs","cette"}).contains(s[i+j].trim().toLowerCase()))  {
								k++;
							}
						}
					
						if (mot != null) {
							mot = apostrFj(mot);
							mots_composes.add(mot.trim().toLowerCase());
						}
		
					}
				}
				
			}
		
		//System.out.println("TEST mots particuliers : "+mots_composes);
		for (String mot : mots_composes) {
			if (mot.contains(" ")) {
				this.newText = this.newText.replace(mot, mot.replace(" ", "_"));
			}
			if (!lookUp(mot.replace("_", " ").trim().toLowerCase())) {
				if (mot.contains("_")) {
					nonExistingWords.add(mot.trim().replace("_", " "));
				}
				else{
					nonExistingWords.add(mot.trim());
				}
			}
		}
		return mots_composes;
	}
	public HashSet<String> backupnomS_particuliers() throws Exception {
		HashSet<String> mots_composes = new HashSet<String>();
		Lemmatisation abu = new Lemmatisation(this.ressourcePath);
		String str = new String(this.newText);
		String[] s = str.split("\\s|[,.?!:;\\(\\)]+");
		for (int i = 0; i < s.length; i++) {
			
				if (abu.isNom(s[i].toLowerCase()) && !abu.isVerb(s[i].toLowerCase())) {
					//System.out.println("BAAAAAAAAAAAAAAAAAA"+s[i]);
					if (s[i].equals("")) {
						//System.out.println("JE SUIS "+s[i]+"ET JE SUIS A LA POSITION i");
					}
					String mot="";
					if (!Arrays.asList(new String[] {"pour","le","a","par"}).contains(s[i].toLowerCase())) {
						if (Arrays.asList(new String[] {"d'","l'","du","en","de","dans","le","la","une","un","leurs","cette"}).contains(s[i+1].toLowerCase())) {
							if (s[i+1].equals("")) {
								//System.out.println("JE SUIS "+s[i]+"ET JE SUIS A LA POSITION i+1");
							}
							int k = 2;
							for (int j = 0; j <= k; j++) {
								mot = mot+s[i+j]+" ";
								if (Arrays.asList(new String[] {"d'","l'","du","en","de","dans","le","les","la","une","un","leurs","cette"}).contains(s[i+k+1].toLowerCase())) {
									if (s[i].equals("")) {
										//System.out.println("JE SUIS "+s[i]+"ET JE SUIS A LA POSITION i+k+1");
									}
									k=k+2;
								}
								if (j==k && Arrays.asList(new String[] {"plus","d'","l'","du","en","de","dans","les","le","la","une","un","leurs","cette"}).contains(s[i+j].toLowerCase()) || isNumeric(s[i+j]) )  {
									if (s[i].equals("")) {
										//System.out.println("JE SUIS "+s[i]+"ET JE SUIS A LA POSITION i+j");
									}
									k++;
								}
							}
							int limite=5;
							if (Arrays.asList(new String[] {"d'","l'","du","en","de","dans","le","les","la","une","un","leurs","cette"}).contains(mot.toLowerCase())) {
								limite++;
							}
							if (mot != null && mot.split(" ").length < limite) {
								mot = apostrFj(mot);
								mots_composes.add(mot.trim());
							}
			
						}
			
					}

				}
				
			}
		
		//System.out.println("TEST nomS particuliers : "+mots_composes);
		for (String mot : mots_composes) {
			if (mot.contains(" ")) {
				this.newText = this.newText.replace(mot, mot.replace(" ", "_"));
			}
//			if (!lookUp(mot.trim())) {
//				if (mot.contains("_")) {
//					nonExistingWords.add(mot.replace("_", " "));
//				}
//				else{
//				nonExistingWords.add(mot);
//				}
//			}
		}
		return mots_composes;
	}
	
	public HashSet<String> noms_particuliers() throws Exception {
		HashSet<String> noms_composes = new HashSet<String>();
		String str = new String(this.newText);
		List<String> list =  Arrays.asList(new String[] {
				"chez","bien_que","est","la","les","le","un","une","en","pour","si","plus","avoir","fois","celui","celui_",
				"celle","celui-ci","celle-ci","terme","travers","sein","celui","celle","leur","moins","a",
				"pas","�tre","but","quelque","quelques","par","dans","soit","autres","sur","a","autres","particulier",
				"d'","l'","du","en","de","dans","le","la","une","un","leurs","cette","ce","sa","au","chez_","bien_que_","est_","la_","les_","le_","un_","une_","en_","pour_","si_","plus_","avoir_","fois_","celui_",
				"celle_","celui-ci_","celle-ci_","terme_","travers_","sein_","celui_","celle_","leur_","moins_",
				   "pas_","être_","but_","quelque_","quelques_","par_","dans_","soit_","autres_","sur_","a_","autres_","particulier_",
					"d'_","l'_","du_","en_","de_","dans_","le_","la_","une_","un_","leurs_","cette_","ce_","sa_","au_","à","lors"});
		str = str.trim();
		String[] s = str.split("\\s|[,.?!:;\\(\\)]+");
		Lemmatisation abu = new Lemmatisation(this,this.ressourcePath);
		//System.out.println("+++++++++++contains++++++++++++++++"+nonExistingWords.toString());

		for (int i = 0; i < s.length; i++) {
			System.out.println("++++++++++++++++++++++++++++here "+s[i]);
			//System.out.println("++++++++++++++++++++++++++++here trim"+s[i].trim().replace("_", " "));

			//System.out.println("contains++++++++++++++++"+nonExistingWords.contains(new String(s[i].trim().replace("_", " "))));

			if ((abu.isNom(s[i].toLowerCase())) || abu.isPosComp(s[i], "Nom") 
					|| s[i].toLowerCase().endsWith(new String("ité")) ||
					nonExistingWords.contains(new String(s[i].trim().replace("_", " ")))){
				s[i] = s[i].trim().toLowerCase();
				if ((!s[i].equals(new String("partie"))) 
						&& (!list.contains(s[i].toLowerCase()))
						&&(!abu.isPro(s[i].toLowerCase()))
						&&(!abu.isPre(s[i].toLowerCase())) 
						&&(!abu.iscON(s[i].toLowerCase()))
						&&(!s[i].startsWith(new String("partie_")))
						&&(!s[i].startsWith(new String("en_")))
						&&(!s[i].startsWith(new String("en ")))
						&&(!s[i].startsWith(new String("ont_")))
						&&(!s[i].startsWith(new String("a_")))
						&&(!s[i].startsWith(new String("chez_")))
						&&(!s[i].startsWith(new String("au_")))
						&&(!s[i].startsWith(new String("chaque_")))
						&&(!s[i].startsWith(new String("pour_")))
						&&(!s[i].endsWith(new String("_en")))
						&&(!s[i].endsWith(new String("en_")))
						&&(!s[i].endsWith(new String("_ont")))
						&&(!s[i].endsWith(new String("_a")))
						&&(!s[i].endsWith(new String("_chez")))
						&&(!s[i].endsWith(new String("_chaque")))
						&&(!s[i].endsWith(new String("_au")))
						&&(!s[i].endsWith(new String("_pour")))
						&&(!s[i].startsWith(new String("lors_")))
						&&(!s[i].endsWith(new String("_lors")))
						&&(!s[i].startsWith(new String("à_")))
						&&(!s[i].startsWith(new String("en ")))
						&&(!s[i].startsWith(new String("ont ")))
						&&(!s[i].startsWith(new String("a ")))
						&&(!s[i].startsWith(new String("au ")))
						&&(!s[i].startsWith(new String("chez ")))
						&&(!s[i].startsWith(new String("pour ")))
						&&(!s[i].endsWith(new String("_que")))
						&&(!s[i].equals(new String(" ")))
						&&(!s[i].equals(new String("")))
						&&(!s[i].equals(new String("pour")))
						&&(!s[i].equals(new String("par")))

						) {
					String mot= new String();
					int k=0;
					int last=0;				
					while (s[i].equals(new String(""))){
						//System.out.println("yabenaamiiiiiiiiiiiiiii "+i);
						i++;
					}
					System.out.println("-------------------------here 2"+s[i]);

					while(i+1<s.length && 
							Arrays.asList(new String[] {"d'","l'","du","en","de","dans","le","la","une","un","des"})
							.contains(s[i+1].toLowerCase())){
								mot = mot + s[i] + " " +s[i+1]+ " ";
								i =i +2;
								k = k+2;
						}
					
					mot = mot.trim();
					System.out.println("the word ="+mot);

					String[] l = mot.split(" ");
					if(l.length>1 ){
						if(Arrays.asList(new String[] {"le","une","un","leurs","cette","ce","sa","les","eux","tels","ces","cette"})
								.contains(l[k-1].toLowerCase())){
							mot = null;
						}
						else if(Arrays.asList(new String[] {"d'","du","de","dans","l'","la"}).contains(l[k-1].toLowerCase())) {
							if(isNumeric(s[i])){
								mot = null;
							}
							else if(Arrays.asList(new String[] {"tels"}).contains(s[i].toLowerCase())){
								mot = null;
							}
							else if(Arrays.asList(new String[] {"les","d'","du","de","dans","l'","la","le","en","une","un"}).
									contains(s[i].toLowerCase()) 
									|| s[i].endsWith(new String("_l'"))
									|| s[i].endsWith(new String("_d'"))
									|| s[i].endsWith(new String("_de"))
									|| s[i].endsWith(new String("_du"))
									|| s[i].endsWith(new String("_dans"))){	

							 if(l[k-1].toLowerCase().equals(new String("de"))){	
									if(abu.isVerb(s[i+1].toLowerCase())){
										mot = null;
									}
									else{
										mot = mot +" "+ s[i]+ " "+ s[i+1];
										last =i+2;
									}
								}
								else {
									mot = mot +" "+ s[i]+ " "+ s[i+1];
									last = i+2;
								}
							}
							else if(Arrays.asList(new String[] {"pas","plus","le","leur","leurs",
									"cette","ce","sa","les","eux","tels","ces","cette","ceux"}).
									contains(s[i].toLowerCase())){
								mot = null;
							}
							else if(s[i].toLowerCase().equals(new String("certains"))||s[i].toLowerCase().equals(new String("certaines"))){
								mot = mot + " "+s[i]+ " "+ s[i+1];
								last = i+2;
							}
							else if(!s[i].toLowerCase().equals(new String("son")) && !s[i].toLowerCase().equals(new String("d'autres")) )
									{
									mot = mot + " "+ s[i];
									last = i+1;
									}
	
							}						
							else if(l[k-1].toLowerCase().equals(new String("en"))){
								if(abu.isPosComp(s[i].toLowerCase(), "Nom")|| abu.isNomBis(s[i].toLowerCase())!=0){									
									if(Arrays.asList(new String[] {"_qui","eux","_que","quelque","lui-meme","quelques","eux"
											,"janvier","fevrier","mars","avril","mai","juin","juillet","aout","septembre","octobre","novembre","decembre"}).contains(s[i].toLowerCase())
											){
										mot =null;
									}							
									else {
										mot = mot + " "+s[i];
										last = i+1;
									}
								}
								else{
									mot = null;
								}
							}
							else{ 
								mot =null;
							}
							if(mot!=null){
								if(mot.endsWith(new String("d' au"))){
								mot.replace("d' au", "");
							}
							
							}
							
							//System.out.println("mot2 ="+mot);
							if(	l[1].toLowerCase().equals(new String("dans") )
								||l[1].toLowerCase().equals(new String("le") )
								||l[1].toLowerCase().equals(new String("la") )
								||l[1].toLowerCase().equals(new String("les"))
								||l[1].toLowerCase().equals(new String("l'") )
								||l[1].toLowerCase().equals(new String("un") )
								||l[1].toLowerCase().equals(new String("une"))
								||l[1].toLowerCase().equals(new String("sur"))
								)
							{mot = null;}
							//System.out.println("mot3 ="+mot);

							
							/*if(l.length>2){
								if(l[2].toLowerCase().equals(new String("un"))
								|| l[2].toLowerCase().equals(new String("une"))){
									mot = null;

								}
							}*/
					}	
					
					if (mot != null && !mot.equals("")) {
						//System.out.println("mot4 ="+mot);
						if( mot.endsWith(new String("_l'"))
								|| mot.endsWith(new String("_d'"))
								|| mot.endsWith(new String("_de"))
								|| mot.endsWith(new String("_du"))
								|| mot.endsWith(new String("_dans"))){
									if(last!=0){
										mot = mot + " "+ s[last];
									}
								}
						//System.out.println("mot5 ="+mot);
						//apostrFj(mot);
						noms_composes.add(mot.trim());
					}
				}
			}
		}
		
		//System.out.println("TEST noms particuliers : "+noms_composes);
		for (String mot : noms_composes) {
			if (mot.contains(" ")) {
				//System.out.println("mot contains esp : "+mot);
				//System.out.println("text contains mot : "+newText.contains(new String(" "+mot)));
				String ss = mot.replace(" ", "_");
				//System.out.println("ss : "+ss);
				this.newText = this.newText.replace(mot,ss);
			}
			if (!lookUp(mot.replace("_", " ").trim().toLowerCase())) {
				if (mot.contains("_")) {
					nonExistingWords.add(mot.replace("_", " "));
				}
			}
		}
		return noms_composes;
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
			if (compound_word.endsWith("_du") || compound_word.endsWith("_de") || compound_word.endsWith("_l'") || compound_word.endsWith("_d'") || compound_word.endsWith("_un") || compound_word.endsWith("_une") || compound_word.endsWith("_sur") || compound_word.endsWith("_aucun") || compound_word.endsWith("_aucune")) {
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

		if (compound_word_underscore.endsWith("_de") || compound_word_underscore.endsWith("_du") || compound_word_underscore.endsWith("_l'") ||compound_word_underscore.endsWith("_d'") || compound_word_underscore.endsWith("_un") || compound_word_underscore.endsWith("_une") || compound_word_underscore.endsWith("_sur") || compound_word_underscore.endsWith("_aucun") || compound_word_underscore.endsWith("_aucune")) {
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
				//System.out.println("MOT : "+chaine_mots_compose+" LOOKUP VALUE "+found);
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
		//system.out.println((new MotsComposes("")).findMcLine("$x se trouvent souvent localis�es au niveau de la $y"));
		//system.out.println();
	}
}