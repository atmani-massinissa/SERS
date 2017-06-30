package Analyseur;

// ajouter la méthode qui trouve les mots composés à l'aide des tableaux de l'objet parser s'ils ne sont pas trouvés
//avec findMC() et appeler cette méthode dans le constructeur

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
	HashMap<String,String> wordListMap;
	HashSet<String> nonExistingWords;
	Parser pr = null;
	String ressourcePath;
	Analyseur analyseur;
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
		apostrFs();
		quantifAdj();
		AdjNom();
		nomAdj();
		//apostrFs();
		noms_particuliers();
		//nomS_particuliers();
		mots_particuliers();

		if (pr != null)
			System.out.println("mots ajout�s "+nonExistingWords);
			addWordsToFile();
		// //system.out.println(" Analyser mot composes "+this.newText);
	}


	public void addWordsToFile() throws IOException 
	{
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
		 new FileOutputStream("C:\\Users\\user\\workspace\\SERSWeb\\WebContent\\WEB-INF\\"+"jdm-mc.txt",true), "UTF-8"));
		BufferedWriter bw2 = new BufferedWriter(new OutputStreamWriter(
		 new FileOutputStream("C:\\Users\\user\\workspace\\SERSWeb\\WebContent\\WEB-INF\\"+"OurJdm-mc.txt",true), "UTF-8"));
		String outS = new String();
		for (String s : nonExistingWords){
			if(!lookUp(s)){
							outS = outS +s+ ";  Source :  "+analyseur.getTitle()+ ";\n";
			}
		}
		bw.append(outS);
		bw2.write(outS);
		bw.close();
		bw2.close();	
	}
	public void addWordsToFile(HashSet<String> nonExisting) throws IOException 
	{
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
		 new FileOutputStream("C:\\Users\\user\\workspace\\SERSWeb\\WebContent\\WEB-INF\\"+"jdm-mc.txt",true), "UTF-8"));
		BufferedWriter bw2 = new BufferedWriter(new OutputStreamWriter(
		 new FileOutputStream("C:\\Users\\user\\workspace\\SERSWeb\\WebContent\\WEB-INF\\"+"OurJdm-mc.txt",true), "UTF-8"));
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
			createWordList(ressourcePath+"jdm-mc.txt");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String str = (new String(oldText));
		String[] avoid = { "du", "la", "des", "les", "un", "une", "par", "elle", "il", "mais", "ou", "est", "et",
				"donc", "or", "ni", "car", "", "_", "de", " ", "�", "dans", "dont" };
		String[] phrase = str.split("[.|;|,|==|\\n|?|!|:|(|)|\\[|\\]|�|�|�|�|\"]+");
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
					 || s[i+j].endsWith("i�me"); j++) {
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
		System.out.println("TEST adj_noms: "+adj_nom_composes);
		for (String mot : adj_nom_composes) {		
			this.newText = this.newText.replace(mot, mot.replace(" ", "_"));
			if (!lookUp(mot.replace("_", " ").trim().toLowerCase())) {
				nonExistingWords.add(mot.trim().replace("_", " "));
				this.wordListMap.put(mot.trim().replace("_", " ")," Nom:Ajout� ");
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
				if (Arrays.asList(new String[] {"non","tr�s","extr�mement","hautement","sensiblement","peu","moyennement","moins", "plus"}).contains(s[i].trim().toLowerCase())) {
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
		System.out.println("TEST quantif_adj: "+quantifAdj);
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
				if (s[i].equals("syst�me_s�rotoninergique")) {
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
		System.out.println("TEST noms_adj: "+noms_adj_composes);
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
			
				if (Arrays.asList(new String[] {"mesure","abus","risque","pr�sence","p�re","m�re","s�ur","soeur","fr�re","fils","fille","compl�ment","capable","capables","degr�","insuffisance","gain","carence","manque","exc�s","baisse","taux","diminution","perte","niveaux","niveau","augmentation","absence","mont�e","d�ficience"}).contains(s[i].trim().toLowerCase())) {
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
		
		System.out.println("TEST mots particuliers : "+mots_composes);
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
						System.out.println("JE SUIS "+s[i]+"ET JE SUIS A LA POSITION i");
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
		
		System.out.println("TEST nomS particuliers : "+mots_composes);
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
		str = str.trim();
		String[] s = str.split("\\s|[,.?!:;\\(\\)]+");
		Lemmatisation abu = new Lemmatisation(this.ressourcePath);
		for (int i = 0; i < s.length; i++) {
			s[i] = s[i].trim();
			if ((abu.isNom(s[i].toLowerCase())) || s[i].contains("_")){
				if (!s[i].equals(new String("partie")) && !(Arrays.asList(new String[] {
						"chez","bien_que","est","la","les","le","un","une","en","pour","si","plus","avoir","fois","celui",
						"celle","celui-ci","celle-ci","terme","travers","sein",
						"pas","�tre","but","quelque","quelques","par","dans","soit","autres","sur","a","autres","particulier",
						"d'","l'","du","en","de","dans","le","la","une","un","leurs","cette","ce","sa","au"}).
						contains(s[i].toLowerCase())
						&&(!abu.isPro(s[i].toLowerCase()))
						&&(!abu.isPre(s[i].toLowerCase())) 
						&&(!abu.iscON(s[i].toLowerCase()))
						&&(!s[i].contains(new String("que")))
						&&(!s[i].startsWith(new String("en_")))
						&&(!s[i].startsWith(new String("en ")))
						&&(!s[i].startsWith(new String("ont_")))
						&&(!s[i].startsWith(new String("a_")))
						&&(!s[i].startsWith(new String("chez_")))
						&&(!s[i].startsWith(new String("au_")))
						&&(!s[i].startsWith(new String("pour_")))
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

						)) {
					String mot= new String();
					int k=0;
					if(abu.isVerb(s[i])){
						//System.out.println("s[i] is verb ="+s[i]);

					}
					else {
						//System.out.println("s[i] ="+s[i]);
						}
					
					if(s[i].equals(new String("pour"))){
						//System.out.println(" equals ");
					}
					while (s[i].equals(new String(""))){
						//System.out.println("yabenaamiiiiiiiiiiiiiii "+i);
						i++;
					}
					while(i+1<s.length && 
							Arrays.asList(new String[] {"d'","l'","du","en","de","dans","le","la","une","un"})
							.contains(s[i+1].toLowerCase())){
								mot = mot + s[i] + " " +s[i+1]+ " ";
								i =i +2;
								k = k+2;
						}
					mot = mot.trim();
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
							else if(Arrays.asList(new String[] {"les","d'","du","de","dans","l'","la","le","en"}).
									contains(s[i].toLowerCase())){	
								
							 if(l[k-1].toLowerCase().equals(new String("de"))){						
									if(abu.isVerb(s[i+1].toLowerCase())){
										mot = null;
									}
									else{
										mot = mot +" "+ s[i]+ " "+ s[i+1];
									}
								}
								else {
									mot = mot +" "+ s[i]+ " "+ s[i+1];
								}
							}
							else if(Arrays.asList(new String[] {"pas","plus","le","une","un","leur","leurs",
									"cette","ce","sa","les","eux","tels","ces","cette","ceux"}).
									contains(s[i].toLowerCase())){
								mot = null;
							}
							else if(s[i].toLowerCase().equals(new String("certains"))||s[i].toLowerCase().equals(new String("certaines"))){
								mot = mot + " "+s[i]+ " "+ s[i+1];
							}
							else if(!s[i].toLowerCase().equals(new String("son")) && !s[i].toLowerCase().equals(new String("d'autres")) )
									{
									mot = mot + " "+ s[i];}
	
							}						
							else if(l[k-1].toLowerCase().equals(new String("en"))){
								if(s[i].toLowerCase().contains("_")|| abu.isNomBis(s[i].toLowerCase())!=0){									
									if(Arrays.asList(new String[] {"_qui","eux","_que","quelque","lui-m�me","quelques","eux"
											,"janvier","fevrier","mars","avril","mai","juin","juillet","aout","septembre","octobre","novembre","decembre"}).contains(s[i].toLowerCase())
											){
										mot =null;
									}							
									else mot = mot + " "+s[i];
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
							if(	l[1].toLowerCase().equals(new String("dans") )
								||l[1].toLowerCase().equals(new String("le") )
								||l[1].toLowerCase().equals(new String("la") )
								||l[1].toLowerCase().equals(new String("les"))
								||l[1].toLowerCase().equals(new String("l'") )
								||l[1].toLowerCase().equals(new String("un") )
								||l[1].toLowerCase().equals(new String("une"))
								||l[1].toLowerCase().equals(new String("sur"))
								)
							mot = null;
							
							/*if(l.length>2){
								if(l[2].toLowerCase().equals(new String("un"))
								|| l[2].toLowerCase().equals(new String("une"))){
									mot = null;

								}
							}*/
					}		
					if (mot != null && !mot.equals("")) {
						mot = apostrFj(mot);
						noms_composes.add(mot.trim());
					}
				}
			}
		}
		
		System.out.println("TEST noms particuliers : "+noms_composes);
		for (String mot : noms_composes) {
			if (mot.contains(" ")) {
				this.newText = this.newText.replace(mot, mot.replace(" ", "_"));
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
				"donc", "or", "ni", "car", "", "_", "de", " ", "�", "dans", "dont", "celui", "que", "ce", "qui" };
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
			if (chaine_mots_compose.contains("esp�rance de vie")) {
				System.out.println("MOT : "+chaine_mots_compose+" LOOKUP VALUE "+found);
			}
			if (found) {
				if (chaine_mots_compose.contains("esp�rance de vie")) {
					
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