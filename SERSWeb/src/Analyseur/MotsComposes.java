package Analyseur;

// ajouter la méthode qui trouve les mots composés à l'aide des tableaux de l'objet parser s'ils ne sont pas trouvés
//avec findMC() et appeler cette méthode dans le constructeur

import java.io.*;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.net.URISyntaxException;
import java.util.*;
import java.util.regex.*;
import RequeterRezo.RequeterRezo;
import RequeterRezo.Mot;


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
				////////System.out.println("LinkWiki : "+str);
				if (str.contains(" ")) {
					motsTrouves.add(str.trim().toLowerCase());
					if (!lookUp(str))
						nonExistingWords.add(str);
					this.oldText = this.oldText.replace(str, str.replace(" ", "_"));
				}
			}
			for (String str : pr.bold) {
				////System.out.println("BoldWiki : "+str);
				if (str.contains(" ")) {
					motsTrouves.add(str.trim().toLowerCase());
					if (!lookUp(str.trim().toLowerCase()))
						nonExistingWords.add(str.trim().toLowerCase());
					this.oldText = this.oldText.replace(str, str.replace(" ", "_"));
				}
			}
							
		}
		apostrEsOld();
		this.newText = findMC();
		//Cr�ation de nouveaux mots compos�s
		//apostrFs();
		NumericSentence();
		
		quantifAdj();
		quantifNom();
		AdjNom();
		
		nomAdj();
		
		//apostrFs();
		noms_particuliers();
		
		//AdjNom();
		//nomAdj();
		//nomS_particuliers();
		
		//System.out.println("le text est "+this.newText);

		
		mots_particuliers();
		
		//System.out.println("le text2 est "+this.newText);

		
		//this.newText = replace_article2(this.newText);
		


		this.newText =  new String(replace_article(nonExistingWords, this.newText));
		this.newText =  new String(incomplete_words(this.newText));
		this.newText = new String(complete_sur(this.newText));
		this.newText = new String(complete_et(this.newText));


		
		//System.out.println("le text3 est "+this.newText);

		if (pr != null) 
			addWordsToFile();
		for (String sf : wordListMap.keySet()){
			System.out.println(" worListMap<"+sf+"> : "+wordListMap.get(sf));
		}
			
		//System.out.println("mots ajoutés "+nonExistingWords);
			apostrFs();
		// ////System.out.println(" Analyser mot composes "+this.newText);
			
	}


	public void addWordsToFile() throws IOException 
	{
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
		 new FileOutputStream(jdm_mcPath.toString(),true), "UTF-8"));
		BufferedWriter bw2 = new BufferedWriter(new OutputStreamWriter(
		 new FileOutputStream(our_jdm_mcPath.toString(),true), "UTF-8"));
		String outS = new String();
		for (String s : nonExistingWords){
			s = s.trim().toLowerCase().replace("_", " ");
			if(!lookUp(s)){
							outS = outS +s.replace("_", " ")+ ";"+wordListMap.get(s)+";  Source :  "+analyseur.getTitle()+ ";\n";
							System.out.println(s.replace("_", " ")+ ";"+wordListMap.get(s)+";  Source :  "+analyseur.getTitle()+ ";\n");

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
			if (line.contains(";")) {
				String[] tab = line.split(";");
				String categorie="";
				for (int i = 1; i < tab.length; i++) {
					if (tab[i].contains(":")) {
						categorie= categorie+tab[i]+" / ";
					}				
				}
				if (!isRemovable(categorie)) {
					wordList.add(line.split(";")[0].trim().toLowerCase());
					wordListMap.put(tab[0].trim().toLowerCase(),categorie);
				}
				//wordListMap.put(tab[0].trim().toLowerCase(),categorie);
			}
		}
	}

	public static Mot requeterRezo(String s) {

		RequeterRezo r = new RequeterRezo();
		Mot m = null;
		try {
			m = r.requete(s);
			// ////System.out.println(m);
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
			// ////System.out.println(phrase[j]);
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
					found = lookUp(apostrFj(chaine_mots_compose));
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
						apostrFj(compound_word_underscore);
						compound_word_underscore = compound_word_underscore.replaceAll("\\s", "_");
//						////System.out.println("Mot avant remplacement: "+chaine_mots_compose);
//						////System.out.println("Mot apr�s remplacement: "+compound_word_underscore);
						str = str.replace(chaine_mots_compose, compound_word_underscore);

						//Mots compos�s se terminant par des articles sont rallong�s

						//str = new String(replace_article(compound_word_underscore, str));
						
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
		 * ////System.out.println("***************"+s5);
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
					 ,"petit", "vieux", "vilain","premier","première", "nouveau"
					 ,"faibles","petits","petites"
					 ,"autres", "beaux", "bonnes","bons", "bels", "belles"
					 ,"grands", "gros", "grosses", "hauts", "jeunes", "mauvais", "mauvaises"
					 ,"petits", "vieux", "vilains","premiers","premieres", "nouveaux"}).contains(s[i+j]) || abu.isPosComp(s[i+j], "Adj:Ajouté"))
					 || s[i+j].endsWith("ième"); j++) {
				mot=mot+s[i+j]+" ";
			}
			if (abu.isPosComp(s[i+j].trim().toLowerCase(),"Nom")||(abu.isNom(s[i+j].toLowerCase()) && abu.howManyLemmes(s[i].toLowerCase()) == 1)) {		
				mot +=s[i+j]+" ";
				if (mot.trim().matches(".+\\s.+")) {
					mot = apostrFj(mot);
					adj_nom_composes.add(mot.trim());
				}
			}
		}
		//System.out.println("TEST adj_noms: "+adj_nom_composes);
		for (String mot : adj_nom_composes) {		
			if (!lookUp(mot.replace("_", " ").trim().toLowerCase())) {
				nonExistingWords.add(mot.trim().toLowerCase());
				this.wordListMap.put(mot.trim().toLowerCase().replace("_", " ")," Nom:Ajouté ");
			}
		}
		
		this.newText =  new String(replace_article(adj_nom_composes, this.newText));
		return adj_nom_composes;
	
	}
	public boolean isNumericWord(String word)
		{
		String[] numericWords = new String[] {"deux","trois","quatre","cinq","six",
				"sept","huit","neuf","dix","onze","douze",
				"treize","quatorze","quinze","seize","vingt","trente","quarante","cinquante","soixante","cents","cent","mille"};
		if (Arrays.asList(numericWords).contains(word)) {
			return true;
		}
		return false;
				
					
				
			
	}
	public HashSet<String> NumericSentence() throws FileNotFoundException{
		String[] numericEndWords = new String[] {"fois","pourcent","%"};
		String[] s = this.newText.split("\\s|[,.?!:;\\(\\)]+");
		HashSet<String> NumericPhrase = new HashSet<String>();
		for (int j = 0; j < s.length; j++) {
			String mot="";
			int i;
			for (i = 0; j+i < s.length && isNumericWord(s[j+i]); i++) 
				{
					mot+=s[j+i]+" ";
				}
			if (Arrays.asList(numericEndWords).contains(s[j+i].toLowerCase()) && mot.endsWith("ième")) {
				mot+=s[j+i];
			}
			j=j+mot.length();
			NumericPhrase.add(mot.trim());
		}
		System.out.println("TEST NumericSentence: "+NumericPhrase);
		for (String mot : NumericPhrase) {		
			if (!lookUp(mot.replace("_", " ").trim().toLowerCase())) {
				nonExistingWords.add(mot.trim().toLowerCase());
				this.wordListMap.put(mot.trim().toLowerCase().replace("_", " ")," quantif:Ajouté ");
			}
		}
		
		this.newText =  new String(replace_article(NumericPhrase, this.newText));
		return NumericPhrase;
		
	}
	public HashSet<String > quantifAdj() throws Exception{
		HashSet<String> quantifAdj = new HashSet<String>();
		Lemmatisation abu = new Lemmatisation(this, this.ressourcePath);
		String[] s = this.newText.split("\\s|[,.?!:;\\(\\)]+");
		for (int i = 0; i < s.length; i++) 
			{
				String mot="";
				if (Arrays.asList(new String[] {"non","très","extrèmement","hautement","sensiblement","peu","moyennement","moins", "plus"}).contains(s[i].trim().toLowerCase()) || abu.isPosComp(s[i], "quantif") ) {
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
			if (!lookUp(mot.replace("_", " ").trim().toLowerCase())) {
				nonExistingWords.add(mot.trim().toLowerCase());
				this.wordListMap.put(mot.trim().toLowerCase().replace("_", " ")," Adj:Ajouté ");
			}
		}
		
		this.newText =  new String(replace_article(quantifAdj, this.newText));
		return quantifAdj;
	}
	public HashSet<String > quantifNom() throws Exception{
		HashSet<String> quantifNom = new HashSet<String>();
		Lemmatisation abu = new Lemmatisation(this, this.ressourcePath);
		String[] s = this.newText.split("\\s|[,.?!:;\\(\\)]+");
		for (int i = 0; i < s.length; i++) 
			{
				String mot="";
				if (abu.isPosComp(s[i], "quantif") ) {
					mot = s[i]+" ";
					if(abu.isPosComp(s[i+1].trim().toLowerCase(),"Nom") || (abu.isPos(s[i+1].toLowerCase(),"Nom") && !s[i+1].toLowerCase().equals("est") && !abu.isPos(s[i+1].toLowerCase(),"Det")))  {
						mot=mot+s[i+1]+" ";
					}
				}
				if (mot.matches(".+\\s.+")) {
					mot = apostrFj(mot);
					quantifNom.add(mot.trim().toLowerCase());
				}
				
		
			}
		System.out.println("TEST quantif_nom: "+quantifNom);
		for (String mot : quantifNom) {		
			if (!lookUp(mot.replace("_", " ").trim().toLowerCase())) {
				nonExistingWords.add(mot.trim().toLowerCase());
				this.wordListMap.put(mot.trim().toLowerCase().replace("_", " ")," Nom:Ajouté ");
			}
		}
		
		this.newText =  new String(replace_article(quantifNom, this.newText));
		return quantifNom;
	}

	public HashSet<String> nomAdj() throws Exception {
		String[] PPas = new String[] {"élevé","sacré"};
		HashSet<String> noms_adj_composes = new HashSet<String>();
		Lemmatisation abu = new Lemmatisation(this,this.ressourcePath);
		boolean relationFlag;
		String terme1 ="";
		String[] s = this.newText.split("\\s|[,.?!:;\\(\\)]+");
		for (int i = 0; i < s.length; i++) {
			relationFlag=false;
			String mot="";
			if (abu.isPosComp(s[i].trim().toLowerCase(),"Nom")||/*!s[i].equals("est")||*/(abu.isPos(s[i].toLowerCase(),"Nom") && !s[i].toLowerCase().equals("est") && !abu.isPos(s[i].toLowerCase(),"Det")/*abu.howManyLemmes(s[i].toLowerCase()) == 1)*/)) {		
				mot +=s[i]+" ";
//				if (s[i].equals("radiographies")) {
//					//System.out.println("XXXXXXXXX"+s[i+1]);
//					//System.out.println("XXXXXXXXX"+s[i+2]+(abu.isPos((s[i+2].trim().toLowerCase()),"Ver")));
//				}
				if (mot!="" && (wordList.contains(mot.trim().replace("_", " ")) || Lemmatisation.map.containsKey(mot))) {
					relationFlag=true;
					terme1=new String(mot);
				}
				for (int j = 1; i+j < s.length-1 
						&&(!s[i+j].toLowerCase().equals(new String("telles"))) 
						&&(!s[i+j].toLowerCase().equals(new String("telle")))
						&&(!s[i+j].toLowerCase().equals(new String("tels")))
						&&(!s[i+j].toLowerCase().equals(new String("tel")))
						&&	(!abu.isPos(s[i+j].toLowerCase(),"PP") || (Arrays.asList(PPas).contains(s[i+j])) || (abu.isPos((s[i+j].trim().toLowerCase()),"Adj") && s[i+j].trim().toLowerCase().endsWith("ie")) || (abu.isPos((s[i+j].trim().toLowerCase()),"Adj") && (abu.isPos((s[i+j+1].trim().toLowerCase()),"Ver")))) && (abu.isPos((s[i+j].trim().toLowerCase()),"Adj") && s[i+j].trim().toLowerCase().endsWith("ique") || abu.isPos((s[i+j].trim().toLowerCase()),"Adj") && s[i+j].trim().toLowerCase().endsWith("iques") || abu.isPosComp((s[i+j].trim().toLowerCase()),"Adj") || abu.isAdj(s[i+j].trim().toLowerCase())) ; j++) {
						mot=mot+s[i+j]+" ";
					}
				
				
				if (mot.matches(".+\\s.+")) {
					mot = apostrFj(mot);
					if (relationFlag) {
						if (!analyseur.foundRelationcomposee(new Relation("Caractérisation",terme1,mot.replace(terme1, ""),mot,"None"))) {
							analyseur.getRelations_composes_trouvees().add(new Relation("Caractérisation",terme1,mot.replace(terme1, ""),mot,"None"));
						}
						
					}
					noms_adj_composes.add(mot.trim());
				}
			}
		}
		System.out.println("TEST noms_adj: "+noms_adj_composes);
		for (String mot : noms_adj_composes) {		
			if (!lookUp(mot.replace("_", " ").trim().toLowerCase())) {
				nonExistingWords.add(mot.trim().toLowerCase());
				this.wordListMap.put(mot.trim().toLowerCase().replace("_", " ")," Nom:Ajouté ");
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
			
				if (Arrays.asList(new String[] {"traitement","proche","somme","mesure","abus","risque","présence","père","mère","sœur","soeur"
						,"frére","fils","fille","complément","capable","capables","degré","insuffisance","gain","carence","manque"
						,"excés","baisse","transfert","taux","diminution","perte","niveaux","niveau","augmentation","absence","montée","déficience"}).contains(s[i].trim().toLowerCase())) {
					String mot="";
					if (Arrays.asList(new String[] {"les","des","leur","d'","l'","du","en","de","dans","le","la","une","un","leurs","cette"}).contains(s[i+1].trim().toLowerCase())) {
						int k = 2;
						int p = 0;
						for (int j = 0; j <= k; j++) {
							mot = mot+s[i+j]+" ";
							if (Arrays.asList(new String[] {"les","d'","l'","du","en","de","des","dans","le","la","une","un","leurs","cette"}).contains(s[i+k+1].trim().toLowerCase())) {
								k=k+2;
							}
							if (j==k && Arrays.asList(new String[] {"plusieurs","ce","les","leur","presque","d'","l'","du","en","de","des","dans","le","la","une","un","leurs","cette","ces"}).contains(s[i+j].trim().toLowerCase()))  {
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
			if (!lookUp(mot.replace("_", " ").trim().toLowerCase())) {
				if (mot.toLowerCase().startsWith("l'")) {
					this.wordListMap.put(mot.trim().toLowerCase().replaceAll("^l'", "")," Nom:Ajouté ");
					nonExistingWords.add(mot.trim().toLowerCase().replaceAll("^l'", ""));
				}
				else{
				this.wordListMap.put(mot.trim().toLowerCase().replace("_", " ")," Nom:Ajouté ");
				nonExistingWords.add(mot.trim().toLowerCase());
				}
			}
		}
		
		this.newText =  new String(replace_article(mots_composes, this.newText));
		return mots_composes;	
	}
	
	public HashSet<String> noms_particuliers() throws Exception {
		HashSet<String> noms_composes = new HashSet<String>();
		String str = new String(this.newText);
		List<String> list =  Arrays.asList(new String[] {
				"chez","bien_que","est","la","les","le","un","une","en","pour","si","plus","avoir","fois","celui","celui_",
				"celle","celui-ci","celle-ci","terme","travers","sein","celui","celle","leur","moins","a","le_fait","suite",
				"pas","être","but","quelque","quelques","par","dans","soit","autres","sur","a","autres","particulier","cause",
				"d'","l'","du","en","de","dans","le","la","une","un","leurs","cette","ce","sa","au","chez_","bien_que_","est_",
				"la_","les_","le_","un_","une_","en_","pour_","si_","plus_","avoir_","fois_","celui_","type","même","cas","autre",
				"celle_","celui-ci_","celle-ci_","terme_","travers_","sein_","celui_","celle_","leur_","moins_","lors","synonyme",
				"pas_","être_","but_","quelque_","quelques_","par_","dans_","soit_","autres_","sur_","a_","autres_","particulier_",
				"d'_","l'_","du_","en_","de_","dans_","le_","la_","une_","un_","leurs_","cette_","ce_","sa_","au_","à","lors"});
		str = str.trim();
		String[] s = str.split("\\s|[,.?!:;\\(\\)]+");
		Lemmatisation abu = new Lemmatisation(this,this.ressourcePath);
		////System.out.println("+++++++++++contains++++++++++++++++"+nonExistingWords.toString());

		for (int i = 0; i < s.length; i++) {
			//System.out.println("++++++++++++++++++++++++++++here "+s[i]);
			////System.out.println("++++++++++++++++++++++++++++here trim"+s[i].trim().replace("_", " "));

			////System.out.println("contains++++++++++++++++"+nonExistingWords.contains(new String(s[i].trim().replace("_", " "))));
			
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
						////System.out.println("yabenaamiiiiiiiiiiiiiii "+i);
						i++;
					}
					//System.out.println("-------------------------here 2"+s[i]);
					if(s[i].toLowerCase().equals(new String("risque"))){
						//System.out.println("le risque "+abu.howManyLemmes(s[i].toLowerCase()));
					}
					
					Boolean v = true;

					while(!s[i].equals(new String("accompagnée")) && i+1<s.length && 
							(
							Arrays.asList(new String[] {"des","d'","l'","du","en","de","le","la","une","un","des","d'une","d'un"})
							.contains(s[i+1].toLowerCase())
							||
							s[i+1].startsWith(new String("d'"))
							||
							s[i+1].startsWith(new String("l'"))
							)
							&&!(abu.iscON(s[i].toLowerCase()))
							){
								//System.out.println(" le mot dans la boucle "+mot);
								mot = mot + s[i] + " " +s[i+1]+ " ";
								i =i +2;
								k = k+2;
						}
					
					mot = mot.trim();
					//System.out.println("the word ="+mot);

					String[] l = mot.split(" ");
					if(l.length>1 ){
						if(Arrays.asList(new String[] {"le","leurs","cette","ce","sa","les","eux","tels","ces","cette"})
								.contains(l[k-1].toLowerCase())){
							mot = null;
						}
						else if(Arrays.asList(new String[] {"des","un","une","du","de","dans","la","d'une","d'un","en"}).contains(l[k-1].toLowerCase())
								 ) {
							//System.out.println("inside d' ="+mot);

							if(isNumeric(s[i])){
								mot = null;
							}
							else if(Arrays.asList(new String[] {"tels"}).contains(s[i].toLowerCase())){
								mot = null;
							}
							else if(Arrays.asList(new String[] {"des","d'une","d'un","les","d'","du","de","dans","l'","la","le","en","une","un"}).
									contains(s[i].toLowerCase()) 
									|| s[i].endsWith(new String("_l'"))
									|| s[i].endsWith(new String("_d'"))
									|| s[i].endsWith(new String("_de"))
									|| s[i].endsWith(new String("_du"))
									|| s[i].endsWith(new String("_dans"))){	

							 if(l[k-1].toLowerCase().equals(new String("de")) || l[k-1].toLowerCase().equals(new String("des"))){	
									if(abu.isVerb(s[i+1].toLowerCase())){
										if(!abu.isNom(s[i+1].toLowerCase())){
												mot = null;
										}
										else{
											mot = mot +" "+ s[i]+ " "+ s[i+1];
											last =i+2;
										}
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
							else if(s[i].toLowerCase().equals(new String("certains"))||s[i].toLowerCase().equals(new String("certaines"))
									||s[i].toLowerCase().equals(new String("seul"))){
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
								System.out.println("inside en "+ mot);
								if(abu.isPosComp(s[i].toLowerCase(), "Nom")
									|| abu.isNomBis(s[i].toLowerCase())!=0){									
									if(Arrays.asList(new String[] {"_qui","eux","_que","quelque","lui-meme","quelques","eux"
											,"janvier","fevrier","mars","avril","mai","juin","juillet","aout","septembre","octobre","novembre","decembre"}).contains(s[i].toLowerCase())
											){
										//System.out.println("inside en condition after the word  delete="+mot);

										mot =null;
									}							
									else {
										mot = mot + " "+s[i];
										last = i+1;
									}
								}
								else{
									//System.out.println("inside en after the word  delete="+mot);

									mot = null;
								}
							}
							else { 
								//System.out.println("inside null ="+mot);
								if(l[k-1].startsWith("d'") || l[k-1].startsWith("l'")){
									//System.out.println("inside null ici="+mot);

								}
								else {
									if(mot.endsWith("le") || mot.endsWith("la")){
										//System.out.println("not ending correctly  ="+mot);
									}
									//System.out.println("after the word  delete="+mot);
									mot =null;
								}
							}
						
						//System.out.println("after the word ="+mot);

							if(mot!=null){
								if(mot.endsWith(new String("d' au"))){
								mot.replace("d' au", "");
							}
							
							}
							
							////System.out.println("mot2 ="+mot);
							if(	l[1].toLowerCase().equals(new String("dans") )
								||l[1].toLowerCase().equals(new String("le") )
								||l[1].toLowerCase().equals(new String("la") )
								||l[1].toLowerCase().equals(new String("les"))
								||l[1].toLowerCase().equals(new String("l'") )
								||l[1].toLowerCase().equals(new String("un") )
								||l[1].toLowerCase().equals(new String("une"))
								||l[1].toLowerCase().equals(new String("sur"))
								||l[1].toLowerCase().equals(new String("d'être"))
								)
							{mot = null;}
							
							if(mot!=null){

							if(	mot.toLowerCase().endsWith(new String(" d'") )
									||mot.toLowerCase().endsWith(new String(" le") )
									||mot.toLowerCase().endsWith(new String(" la") )
									||mot.toLowerCase().endsWith(new String(" les"))
									||mot.toLowerCase().endsWith(new String(" l'") )
									||mot.toLowerCase().endsWith(new String(" un") )
									||mot.toLowerCase().endsWith(new String(" une"))
									||mot.toLowerCase().endsWith(new String(" sur"))
									)
								{
								String[] tableau = mot.split(" ");
								String newMot = new String("");
								for (int h=0; h< tableau.length -2 ;h++){
									newMot = newMot + " "+ tableau[h];
								}
								newMot = newMot.trim();
								//System.out.println(" newmot "+newMot);
								mot = null;
								}
							}
							
							//System.out.println("after the word delete ="+mot);

							////System.out.println("mot3 ="+mot);

							
							/*if(l.length>2){
								if(l[2].toLowerCase().equals(new String("un"))
								|| l[2].toLowerCase().equals(new String("une"))){
									mot = null;

								}
							}*/
					}	
					
					if (mot != null && !mot.equals("")) {
						////System.out.println("mot4 ="+mot);
						if( mot.endsWith(new String("_l'"))
								|| mot.endsWith(new String("_d'"))
								|| mot.endsWith(new String("_de"))
								|| mot.endsWith(new String("_du"))
								|| mot.endsWith(new String("_dans"))
								|| mot.endsWith(new String(" certain"))
								|| mot.endsWith(new String(" certains"))
								|| mot.endsWith(new String(" certaines"))
								|| mot.endsWith(new String(" les"))){
									if(last!=0){
										mot = mot + " "+ s[last];
									}
								}
						////System.out.println("mot5 ="+mot);
						//apostrFj(mot);
					//	mot = mot.replace("_", " ");
						noms_composes.add(mot.trim());
					}
				}
			}
			
		}
		
		//System.out.println("TEST noms particuliers : "+noms_composes);
		for (String mot : noms_composes) {		
			if (!lookUp(mot.replace("_", " ").trim().toLowerCase())) {
				if (mot.toLowerCase().startsWith("l'")) {
					this.wordListMap.put(mot.trim().toLowerCase().replaceAll("^l'", "").replace("_", " ")," Nom:Ajouté ");
					nonExistingWords.add(mot.trim().toLowerCase().replaceAll("^l'", "").replace("_", " "));
				}
				else{
					//System.out.println("affich "+mot.trim().toLowerCase().replace("_", " "));
					String neww = mot.trim().replace("_", " "); 
					neww = neww.replaceAll("_", " ");

				this.wordListMap.put(mot.trim().replace("_", " ")," Nom:Ajouté ");
				nonExistingWords.add(mot.trim());
				}
			}
		}
		
		this.newText =  new String(replace_article(noms_composes, this.newText));
		
		return noms_composes;	
		
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
	public String replace_article(Set<String> mots_composes, String str){
		for (String compound_word: mots_composes) {
			//System.out.println("WWWAAAAAAAAAA "+compound_word);
			if (compound_word.replace("_", " ").endsWith(" à") || 
					compound_word.replace("_", " ").endsWith(" toute") || 
					compound_word.replace("_", " ").endsWith(" du") ||
					compound_word.replace("_", " ").endsWith(" de") || 
					compound_word.replace("_", " ").endsWith(" l'") || 
					compound_word.replace("_", " ").endsWith(" d'") || 
					compound_word.replace("_", " ").endsWith(" un") || 
					compound_word.replace("_", " ").endsWith(" une") || 
					compound_word.replace("_", " ").endsWith(" sur") || 
					compound_word.replace("_", " ").endsWith(" aucun") || 
					compound_word.replace("_", " ").endsWith(" aucune")) {
				System.out.println("compound word "+compound_word);
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
			
			//System.out.println("replacement "+ compound_word.trim()+" , "+compound_word.trim().toLowerCase().replace(" ", "_"));
			str = str.replace(compound_word.trim(), compound_word.trim().toLowerCase().replace(" ", "_"));
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
	
	public String incomplete_words(String str){
		String[] s = str.split(" ");
       
		for (int i=0;i+1<s.length;i++) {
			String compound_word = new String(s[i].toString());
				if ((compound_word.endsWith("_du") || compound_word.endsWith("_de") || compound_word.endsWith("_l")) 
						&& (!compound_word.startsWith("cause")) 
						&& (!compound_word.startsWith("synonyme")) ) {
					if(!s[i+1].toString().equals(new String("la")) && !s[i+1].toString().equals(new String("le"))
							&& !s[i+1].toString().equals(new String("un")) && !s[i+1].toString().equals(new String("une"))
							&& !s[i+1].toString().equals(new String("leurs")) && !s[i+1].toString().equals(new String("leur"))
							&& !s[i+1].toString().equals(new String("ces"))){
					  compound_word = compound_word + " "+ s[i+1].toString();
					  this.wordListMap.put(compound_word.trim().toLowerCase().replace("_", " ")," Nom:Ajouté ");
					  nonExistingWords.add(compound_word.trim().toLowerCase());
					  String newcompound_word = new String(compound_word.replace(" ", "_").trim());
					  str = str.replace(compound_word, newcompound_word);
					}
					 
				}
		}
		return str;	
	}
	
	public String complete_sur(String str) throws Exception{
		String[] s = str.split(" ");
		Lemmatisation abu = new Lemmatisation(this,this.ressourcePath);    
		for (int i=0;i+3<s.length;i++) {
			String word = new String(s[i].toString());
			if(!s[i].toLowerCase().equals(new String("aux")) && !s[i].toLowerCase().equals(new String("base"))
					&& !s[i].toLowerCase().startsWith(new String("ainsi")) && !s[i].toLowerCase().startsWith(new String("aux_"))){
				if (abu.isNom(word.toLowerCase()) || abu.isPosComp(word, "Nom") 
						|| nonExistingWords.contains(new String(s[i].trim().replace("_", " ")))) {
					if(s[i+1].toString().equals(new String("sur"))){
						if( s[i+2].toString().equals(new String("la")) ||s[i+2].toString().equals(new String("les"))
						|| s[i+2].toString().equals(new String("le"))|| s[i+2].toString().equals(new String("des")) ){
							 String compound_word = new String("");
							 compound_word = s[i].toString() + " "+ s[i+1].toString()+ " "+s[i+2].toString()+" "+s[i+3].toString();
							 this.wordListMap.put(compound_word.trim().toLowerCase().replace("_", " ")," Nom:Ajouté ");
							 nonExistingWords.add(compound_word.trim().toLowerCase());
							 String newcompound_word = new String(compound_word.replace(" ", "_").trim());
							 str = str.replace(compound_word, newcompound_word);
						}
					}
					 
				}
			}
				
		}
		return str;	
	}
	
	public String complete_et(String str) throws Exception{
		String[] s = str.split(" ");
		Lemmatisation abu = new Lemmatisation(this,this.ressourcePath);    
		for (int i=0;i+2<s.length;i++) {
			String word = new String(s[i].trim().replace("_", " ").toString());
			String cat = new String("");
			System.out.println(" word + "+word);
			if(wordListMap.containsKey(word)){
				cat = wordListMap.get(word);
				System.out.println(" word2 + "+word);
				System.out.println(" cat + "+cat);

				if(cat.contains(new String("Nom"))){
					if(s[i+1].equals("et")){
						System.out.println(" word + et "+word);
						if(
								(abu.isNom(s[i+2]) 
								&& !abu.isDet(s[i+2]) 
								&& !(abu.isVerb(s[i+2])&&!abu.isAdj(s[i+2])) 
								)
							||
								(!abu.isVerb(s[i+2])
								&& abu.isAdj(s[i+2])  
								&& !abu.isDet(s[i+2])
								)
							){
							System.out.println(" word + et +"+word+" et "+s[i+2]);
							String compound_word = new String("");
							 compound_word = s[i].toString() + " "+ s[i+1].toString()+ " "+s[i+2].toString();
							 this.wordListMap.put(compound_word.trim().toLowerCase().replace("_", " ")," Nom:Ajouté ");
							 nonExistingWords.add(compound_word.trim().toLowerCase());
							 String newcompound_word = new String(compound_word.replace(" ", "_").trim());
							 str = str.replace(compound_word, newcompound_word);
						}
					}
				}
			}
		}
				/*if (abu.isNom(word.toLowerCase()) || abu.isPosComp(word, "Nom") 
						|| nonExistingWords.contains(new String(s[i].trim().replace("_", " ")))) {
					if(s[i+1].toString().equals(new String("sur"))){
						if( s[i+2].toString().equals(new String("la")) ||s[i+2].toString().equals(new String("les"))
						|| s[i+2].toString().equals(new String("le"))|| s[i+2].toString().equals(new String("des")) ){
							 String compound_word = new String("");
							 compound_word = s[i].toString() + " "+ s[i+1].toString()+ " "+s[i+2].toString()+" "+s[i+3].toString();
							 this.wordListMap.put(compound_word.trim().toLowerCase().replace("_", " ")," Nom:Ajouté ");
							 nonExistingWords.add(compound_word.trim().toLowerCase().replace("_", " "));
							 String newcompound_word = new String(compound_word.replace(" ", "_").trim());
							 str = str.replace(compound_word, newcompound_word);
						}
					}
					 
				}
			}
				
		}*/
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
		// ////System.out.println(phrase[j]);
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
			found = lookUp(apostrFj(chaine_mots_compose));
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
		 * ////System.out.println("***************"+s5);
		 */
		return str;
	}
	public boolean isRemovable(String categorie){
		
		if (!categorie.contains("Nom") && !categorie.contains("GN") && !categorie.contains("Adj") && (categorie.contains("Con") || categorie.contains("Ver") || categorie.contains("Pro") || categorie.contains("Adv") || categorie.contains("Modifier"))) {
			return true;
		}
		
		return false;
	}

	public static void main(String[] args) throws Exception {
		////System.out.println((new MotsComposes("")).findMcLine("$x se trouvent souvent localis�es au niveau de la $y"));
		////System.out.println();
		
	}
}