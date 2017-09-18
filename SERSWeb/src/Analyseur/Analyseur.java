package Analyseur;
import java.io.BufferedReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap; 
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.jsp.JspWriter;

import RequeterRezo.RequeterRezo;

public class Analyseur {
	String carAccentues = "œ,àâäçèéêëîïôöùûüœÀÂÄÇÈÉÊËÎÏÔÖÙÛÜ\\-";
	String motFr = "[A-Za-z0-9_" + carAccentues + "']";
	String filePath;
	private String text;
	private ArrayList<Relation> Relations_trouvees = new ArrayList<Relation>();
	private ArrayList<Relation> Relations_composes_trouvees = new ArrayList<Relation>();
	String ressourcePath;
	Parser p;
	Lemmatisation lm;
	Lemmatisation abu;
	MotsComposes mc;
	private String title;
	Path titlePath;
	HashMap<String,Integer> nbOfTermsUnderConstraintByPattern;


	public Analyseur(String ressourcePath) throws IOException {
		this.ressourcePath = ressourcePath;
		//abu = new Lemmatisation(ressourcePath);
		
	}
	public void setRessourcePath(String ressourcePath) {
		this.ressourcePath = ressourcePath;
	}
	public String getText() {
		return text;
	}
//	public Analyseur(String filePath) throws IOException {
//		this.filePath = filePath;
//		abu = new Lemmatisation();
//		BufferedReader buffer = Files.newBufferedReader(Paths.get(filePath), StandardCharsets.UTF_8);
//		String tmp;
//		text = new String();
//		while ((tmp = buffer.readLine()) != null) {
//			text = text + "\n" + tmp;
//		}
//
//	}

	public void analyserParMc() throws Exception {
		long startTime = System.currentTimeMillis();
		this.pretraitementParMc();
		long stopTime = System.currentTimeMillis();
		//System.out.println(stopTime - startTime);
		startTime = System.currentTimeMillis();
		// //System.out.println(this.text);
		for (String type : Relation.types_de_relations) {
			long startTimeP = System.currentTimeMillis();
			for (String patron : Relation.typePatrons.get(type)) {
				// Construction de la Regex pour l'extraction des termes
				String strExpReg = "";
				boolean postPatron = false;
				if (patron.endsWith("$Post")) {
					postPatron = true;
					patron = patron.replace("$Post", "");
				}
				for (int i = 0; i < patron.split("\\$").length; i++) {
					strExpReg += "(" + motFr + "+)\\s" + patron.split("\\$")[i].replace(" ", "\\s") + "\\s";
				}
				if (!postPatron) {
					strExpReg += "(" + motFr + "+)";
				} else {
					strExpReg = strExpReg.substring(0, strExpReg.length() - 2);
					patron = patron + "$Post";
				}

				Pattern ExpReg = Pattern.compile(strExpReg);
				Matcher matcher = ExpReg.matcher(this.text);
				while (matcher.find()) {
					for (int i = 2; i <= Relation.patronNbrTerms.get(patron); i++) {
						// Test si il n'y a pas confusion entre patrons
						if (unique(type, matcher.group(1), patron, matcher.group(i), matcher.group())) {
							// Test d'ambiguitï¿½ et dï¿½sambiguation (par
							// contraintes sï¿½mantiques)
							if (!isAmbigu(type,patron) || type
									.equals(this.desambiguation(type, patron, matcher.group(1), matcher.group(i)))) {
								if (!underGrammaticalConstraint(type, patron)
										|| grammaticalConstraint(type, matcher.group(1), patron, matcher.group(i))) {
									Relation R = new Relation(type, matcher.group(1), matcher.group(i), matcher.group(),patron);
									if (evaluate(R)) {
										Relations_trouvees.add(R);
									}
//									Relations_trouvees.add(
//											new Relation(type, matcher.group(1), matcher.group(i), matcher.group(),patron));
								}

							}
						}
					}
				}
			}
			long stopTimeP = System.currentTimeMillis();
			//System.out.println("Temps d'éxécution pour " + type + " : " + (stopTimeP - startTimeP));
		}
		stopTime = System.currentTimeMillis();
		//System.out.println(stopTime - startTime);
	}

	public void analyserParLem() throws Exception {
		long startTime = System.currentTimeMillis();
		this.pretraitementParLem();
		long stopTime = System.currentTimeMillis();
		//System.out.println(stopTime - startTime);
		startTime = System.currentTimeMillis();
		// //System.out.println(this.text);
		for (String type : Relation.types_de_relations) {
			long startTimeP = System.currentTimeMillis();
			for (String patron : Relation.typePatrons.get(type)) {
				// Construction de la Regex pour l'extraction des termes
				String strExpReg = "";
				boolean postPatron = false;
				if (patron.endsWith("$Post")) {
					postPatron = true;
					patron = patron.replace("$Post", "");
				}
				for (int i = 0; i < patron.split("\\$").length; i++) {
					strExpReg += "(" + motFr + "+)\\s" + patron.split("\\$")[i].replace(" ", "\\s") + "\\s";
				}
				if (!postPatron) {
					strExpReg += "(" + motFr + "+)";
				} else {
					strExpReg = strExpReg.substring(0, strExpReg.length() - 2);
					patron = patron + "$Post";
				}

				Pattern ExpReg = Pattern.compile(strExpReg);
				Matcher matcher = ExpReg.matcher(this.text);
				while (matcher.find()) {
					for (int i = 2; i <= Relation.patronNbrTerms.get(patron); i++) {
						// Test si il n'y a pas confusion entre patrons
						if (unique(type, matcher.group(1), patron, matcher.group(i), matcher.group())) {
							// Test d'ambiguitï¿½ et dï¿½sambiguation (par
							// contraintes sï¿½mantiques)
							if (!isAmbigu(type,patron) || type
									.equals(this.desambiguation(type, patron, matcher.group(1), matcher.group(i)))) {
								if (!underGrammaticalConstraint(type, patron)
										|| grammaticalConstraint(type, matcher.group(1), patron, matcher.group(i))) {
									Relation R = new Relation(type, matcher.group(1), matcher.group(i), matcher.group(),patron);
									if (evaluate(R)) {
										Relations_trouvees.add(R);
									}
//									Relations_trouvees.add(
//											new Relation(type, matcher.group(1), matcher.group(i), matcher.group(),patron));
								}

							}
						}
					}
				}
			}
			long stopTimeP = System.currentTimeMillis();
			//System.out.println("Temps d'éxécution pour " + type + " : " + (stopTimeP - startTimeP));
		}
		stopTime = System.currentTimeMillis();
		//System.out.println(stopTime - startTime);
	}

	public void analyserParMcLem(Double threshold) throws Exception {
		
		//System.out.println("************************AnalyserParMcLem*****************************");
		this.nbOfTermsUnderConstraintByPattern = new HashMap<String,Integer>();

		//long startTime = System.currentTimeMillis();
		this.pretraitementParMcLem();
		//long stopTime = System.currentTimeMillis();
		////System.out.println(stopTime - startTime);
		//startTime = System.currentTimeMillis();
		// //System.out.println(this.text);
		for (String type : Relation.types_de_relations) {
			long startTimeP = System.currentTimeMillis();
			for (String patron : Relation.typePatrons.get(type)) {
				// Construction de la Regex pour l'extraction des termes
				String strExpReg = "";
				boolean postPatron = false;
				if (patron.endsWith("$Post")) {
					postPatron = true;
					patron = patron.replace("$Post", "");
				}
				for (int i = 0; i < patron.split("\\$").length; i++) {
					strExpReg += "(" + motFr + "+)\\s" + patron.split("\\$")[i].replace(" ", "\\s") + "\\s";
				}
				if (!postPatron) {
					strExpReg += "(" + motFr + "+)";
				} else {
					strExpReg = strExpReg.substring(0, strExpReg.length() - 2);
					patron = patron + "$Post";
				}

				Pattern ExpReg = Pattern.compile(strExpReg);
				Matcher matcher = ExpReg.matcher(this.text);
				while (matcher.find()) {
					for (int i = 2; i <= Relation.patronNbrTerms.get(patron); i++) {
						// Test si il n'y a pas confusion entre patrons
						if (unique(type, matcher.group(1), patron, matcher.group(i), matcher.group())) {
							// Test d'ambiguitïé et désambiguation (par
							// contraintes sémantiques)
//							if (!isAmbigu(type,patron) || type
//									.equals(this.desambiguation(type, patron, matcher.group(1), matcher.group(i)))) {
								if (!underGrammaticalConstraint(type, patron)
										|| grammaticalConstraint(type, matcher.group(1), patron, matcher.group(i))) {
									if (type.equals("Possession") && patron.equals("a un")) {
										//System.out.println("ENTREE BOUCLE "+semanticConstraint(type, matcher.group(1), patron, matcher.group(i)));
									}
								
									if (!underSemanticConstraint(type, patron)
											|| semanticConstraint(type, matcher.group(1), patron, matcher.group(i))) {
										
										 Integer term1Exists	=	0;
										 Integer term2Exists	=	0;
										 Integer nbOfTermsUnderGrammaticalConstraint	=	0;
										 Integer underSemanticConstraint	=	0;
										 
										if(mc.wordList.contains(matcher.group(1)))
											term1Exists		=	1;
										else
											term1Exists		=	0;
										if(mc.wordList.contains(matcher.group(i)))
											term2Exists		=	1;	
										else
											term2Exists		=	0;
										if(this.nbOfTermsUnderConstraintByPattern.containsKey(patron))
											nbOfTermsUnderGrammaticalConstraint		=	this.nbOfTermsUnderConstraintByPattern.get(patron);
										else
											nbOfTermsUnderGrammaticalConstraint		=	0;
										if(underSemanticConstraint(type, patron))
											underSemanticConstraint	=	1;
										else
											underSemanticConstraint	=	0;
										
										Integer nbLemmaTerm1,nbLemmaTerm2;
										nbLemmaTerm1 = Math.max(this.abu.howManyLemmes(matcher.group(1)), 1);
										nbLemmaTerm2 = Math.max(this.abu.howManyLemmes(matcher.group(i)), 1);

										Relation R = new Relation(type, matcher.group(1), matcher.group(i), matcher.group(),patron,nbOfTermsUnderGrammaticalConstraint,underSemanticConstraint,
																	term1Exists,term2Exists,nbLemmaTerm1,nbLemmaTerm2);
										

									if (R.getScore() >= threshold ) {
										Relations_trouvees.add(R);
									}
//									Relations_trouvees.add(
//											new Relation(type, matcher.group(1), matcher.group(i), matcher.group(),patron));
									}
									
								}

//							}
						}
					}
				}
			}
			long stopTimeP = System.currentTimeMillis();
			//System.out.println("Temps d'éxécution pour " + type + " : " + (stopTimeP - startTimeP));
		}
		//stopTime = System.currentTimeMillis();
		////System.out.println(stopTime - startTime);
	}

	public void analyserParLemMc() throws Exception {
		long startTime = System.currentTimeMillis();
		this.pretraitementParLemMc();
		long stopTime = System.currentTimeMillis();
		//System.out.println(stopTime - startTime);
		startTime = System.currentTimeMillis();
		// //System.out.println(this.text);
		for (String type : Relation.types_de_relations) {
			long startTimeP = System.currentTimeMillis();
			for (String patron : Relation.typePatrons.get(type)) {
				// Construction de la Regex pour l'extraction des termes
				String strExpReg = "";
				boolean postPatron = false;
				if (patron.endsWith("$Post")) {
					postPatron = true;
					patron = patron.replace("$Post", "");
				}
				for (int i = 0; i < patron.split("\\$").length; i++) {
					strExpReg += "(" + motFr + "+)\\s" + patron.split("\\$")[i].replace(" ", "\\s") + "\\s";
				}
				if (!postPatron) {
					strExpReg += "(" + motFr + "+)";
				} else {
					strExpReg = strExpReg.substring(0, strExpReg.length() - 2);
					patron = patron + "$Post";
				}

				Pattern ExpReg = Pattern.compile(strExpReg);
				Matcher matcher = ExpReg.matcher(this.text);
				while (matcher.find()) {
					for (int i = 2; i <= Relation.patronNbrTerms.get(patron); i++) {
						// Test si il n'y a pas confusion entre patrons
						if (unique(type, matcher.group(1), patron, matcher.group(i), matcher.group())) {
							// Test d'ambiguitï¿½ et dï¿½sambiguation (par
							// contraintes sï¿½mantiques)
							if (!isAmbigu(type,patron) || type
									.equals(this.desambiguation(type, patron, matcher.group(1), matcher.group(i)))) {
								if (!underGrammaticalConstraint(type, patron)
										|| grammaticalConstraint(type, matcher.group(1), patron, matcher.group(i))) {
									Relation R = new Relation(type, matcher.group(1), matcher.group(i), matcher.group(),patron);
									if (evaluate(R)) {
										Relations_trouvees.add(R);
									}
//									Relations_trouvees.add(
//											new Relation(type, matcher.group(1), matcher.group(i), matcher.group(),patron));
								}
							}
						}
					}
				}
			}
			long stopTimeP = System.currentTimeMillis();
			//System.out.println("Temps d'éxécution pour " + type + " : " + (stopTimeP - startTimeP));
		}
		stopTime = System.currentTimeMillis();
		//System.out.println(stopTime - startTime);
	}

	private boolean underGrammaticalConstraint(String type, String patron) {
		// Méthode vérifiant si le patron définit une contrainte grammaticale

		if (Relation.patronGrammaticalConstraint.containsKey(type + " : " + patron)) {
			return true;
		} else
			return false;

	}
	private boolean underSemanticConstraint(String type, String patron) {
		// Méthode vérifiant si le patron définit une contrainte grammaticale

		if (Relation.patronSemanticConstraint.containsKey(type + " : " + patron)) {
			{
				////System.out.println("******* "+type + " : " + patron);
				return true;
			}
		} else
			return false;

	}

	private boolean semanticConstraint(String type, String term1, String patron, String term2) throws IOException, InterruptedException {
		/*
		 * Méthode vérifiant la satisfiabilité des contraintes sémantiques.
		 */

		boolean satisfaction = true;
		RequeterRezo jeuxDeMots = new RequeterRezo();
		String strExpReg = "\\$([xy]):\\[(.+)\\]";
		Pattern ExpReg = Pattern.compile(strExpReg);
		if (Relation.patronSemanticConstraint.get(type + " : " + patron).contains(",")) {
			for (String constraint : Relation.patronGrammaticalConstraint.get(type + " : " + patron).split(",")) {
				if (constraint.contains("$")) {
					Matcher matcher = ExpReg.matcher(constraint);
					if (matcher.find()) {
						if (matcher.group(1).equals("x")) {
							if (jeuxDeMots.requete(term1.replace("_", " ")) != null  ) {
//								//System.out.println(term1);
//								//System.out.println(matcher.group(2));
//								//System.out.println(!jeuxDeMots.containsClasse("r_isa",term1.replace("_", " "),matcher.group(2)));
								if (!jeuxDeMots.containsClasse("r_isa",term1.replace("_", " "),matcher.group(2))) {
									return false;
								}
							}
							else{
								if (term1.contains("_")){
									if (!abu.isPosComp(term1,"Source")) {
										if (!abu.isPosComp(term1,matcher.group(2))) {
											return false;
										}
									}
								}
								else {
									return false;
								}
							}
						} else if (matcher.group(1).equals("y")) {
							if (jeuxDeMots.requete(term2) != null  ) {
								if (!jeuxDeMots.containsClasse("r_isa",term2,matcher.group(2))) {									
									return false;
								}
							}
							else{
								if (term2.contains("_")){
									if (!abu.isPosComp(term1,"Source")) {
										if (!abu.isPosComp(term2,matcher.group(2))) {
											return false;
										}
									}
								}
								else {
									return false;
								}
							}
							}

					}
					/*
					 * else { //System.out.println(
					 * "!!!!!! EXPRESSION REGULIERE N'A PAS FONCTIONNE !!!!!! AVEC VIRGULE"
					 * ); }
					 */
				}
			}
		} else {
//			//System.out.println(type + " : " + patron);
//			//System.out.println(Relation.patronGrammaticalConstraint);
			if (Relation.patronSemanticConstraint.get(type + " : " + patron).contains("$")) {
				Matcher matcher = ExpReg.matcher(Relation.patronSemanticConstraint.get(type + " : " + patron));
				if (matcher.find()) {
					if (matcher.group(1).equals("x")) {
						////System.out.println("MOT "+term1.replace("_", " "));
						if (jeuxDeMots.requete(term1.replace("_", " ")) == null)
							return false;
						else {
//							//System.out.println(term1.replace("_", " "));
//							//System.out.println(matcher.group(2));
//							//System.out.println(jeuxDeMots.containsClasse("r_isa",term1.replace("_", " "),matcher.group(2).toLowerCase()));
							if (!jeuxDeMots.containsClasse("r_isa",term1,matcher.group(2).toLowerCase())) {
								return false;
							}
						}
					} else if (matcher.group(1).equals("y")) {
						if (!term2.contains("_") && jeuxDeMots.requete(term2) == null)
							return false;
						if (jeuxDeMots.requete(term2) != null  ) {
							if (!jeuxDeMots.containsClasse("r_isa",term2,matcher.group(2))) {
								return false;
							}
						}
					}

				}
				/*
				 * else { //System.out.println(
				 * "!!!!!! EXPRESSION REGULIERE N'A RIEN TROUVE !!!!!!"); }
				 */
			}
		}
		return satisfaction;

	}


	private boolean grammaticalConstraint(String type, String term1, String patron, String term2) throws IOException {
		/*
		 * Méthode vérifiant la satisfiabilité des contraintes grammaticales.
		 */

		boolean satisfaction = true;
		String strExpReg = "\\$([xy]):\\[(.+)\\]";
		int count =0;
		Pattern ExpReg = Pattern.compile(strExpReg);
		if (Relation.patronGrammaticalConstraint.get(type + " : " + patron).contains(",")) {
			for (String constraint : Relation.patronGrammaticalConstraint.get(type + " : " + patron).split(",")) {
				if (constraint.contains("$")) {
					count++;
					Matcher matcher = ExpReg.matcher(constraint);
					if (matcher.find()) {
						if (matcher.group(1).equals("x")) {
							if (abu.getPos(term1) != null  ) {
								if (!abu.getPos(term1).equals(matcher.group(2))) {
									return false;
								}
							}
							else{
								if (term1.contains("_")){
									if (!abu.isPosComp(term1,"Source")) {
										if (!abu.isPosComp(term1,matcher.group(2))) {
											return false;
										}
									}
								}
								else {
									return false;
								}
							}
						} else if (matcher.group(1).equals("y")) {
							if (abu.getPos(term2) != null) {
								if (!abu.getPos(term2).equals(matcher.group(2))) {
									return false;
								}
							}
							else{
								if (term2.contains("_")){
									if (!abu.isPosComp(term1,"Source")) {
										if (!abu.isPosComp(term2,matcher.group(2))) {
											return false;
										}
									}
								}
								else {
									return false;
								}
							}
							}

					}
					/*
					 * else { //System.out.println(
					 * "!!!!!! EXPRESSION REGULIERE N'A PAS FONCTIONNE !!!!!! AVEC VIRGULE"
					 * ); }
					 */
				}
			}
		} else {
			if (Relation.patronGrammaticalConstraint.get(type + " : " + patron).contains("$")) {
				count=1;
				Matcher matcher = ExpReg.matcher(Relation.patronGrammaticalConstraint.get(type + " : " + patron));
				if (matcher.find()) {
					if (matcher.group(1).equals("x")) {
						if (!term1.contains("_") && abu.getPos(term1) == null)
							return false;
						if (abu.getPos(term1) != null) {
							if (!abu.getPos(term1).equals(matcher.group(2))) {
								return false;
							}
						}
					} else if (matcher.group(1).equals("y")) {
						if (!term2.contains("_") && abu.getPos(term2) == null)
							return false;
						if (abu.getPos(term2) != null) {
							if (!abu.getPos(term2).equals(matcher.group(2))) {
								return false;
							}
						}
					}

				}
				/*
				 * else { //System.out.println(
				 * "!!!!!! EXPRESSION REGULIERE N'A RIEN TROUVE !!!!!!"); }
				 */
			}
		}
		this.nbOfTermsUnderConstraintByPattern.put(patron, count);
		return satisfaction;

	}

	private boolean unique(String type, String term1, String patron, String term2, String contexte) {
		/*
		 * Mï¿½thode ï¿½vitant la confusion entre patrons.
		 */
		for (ArrayList<String> Set : Relation.typePatrons.values()) {
			for (String pattern : Set) {
				// Cas 1 : Patron inclut dans un autre --> Interdire la
				// duplicaion.
				if (!patron.equals(pattern) && this.foundRelation(new Relation(type, term1, term2, contexte,patron))) {
					return false;
				}
				// Cas 2 : [(Terme1+Patron) ou (Patron+Terme2)] est un patron
				// --> Interdire la crï¿½ation d'une relation.
				if (pattern.contains(patron + " " + term2) || pattern.contains(term1 + " " + patron)) {
					return false;
				}
				if (Arrays
						.asList(new String[] { "ne", "celui", "ceux", "pour", "se", "ou", "qu'elles", "à", "moins",
								"qu'un", "qu'une", "que", "ce", "au", "aux", "sur", "le", "en", "qu'il", "qu'elle",
								"ceci", "plus", "sous", "très", "lui", "de", "un", "être", "est", "qui", "elle",
								"celle-ci", "il", "ils", "elles", "celui-ci", "la", "qu'il", "y", "et", "autre", ",",
								"s'", "l'", "ce_qui", "au_moins", "être","n'a","n'est_pas","on","tel","quelque",
								"quelques","par","dans","soit","autres","sur","a","autres" })
						.contains(term1.toLowerCase())
						|| Arrays
								.asList(new String[] { "ne", "celui", "ceux", "pour", "se", "ou", "qu'elles", "à",
										"moins", "qu'un", "qu'une", "que", "ce", "au", "aux", "sur", "le", "en",
										"qu'il", "qu'elle", "ceci", "plus", "sous", "très", "lui", "de", "un", "être",
										"est", "qui", "elle", "celle-ci", "il", "ils", "elles", "celui-ci", "la",
										"qu'il", "y", "et", "autre", ",", "s'", "l'", "ce_qui", "au_moins", "être","n'est_pas","on","tel","quelque",
										"quelques","par","dans","soit","autres","sur","a","autres" })
								.contains(term2.toLowerCase())) {
					return false;
				}
			}
		}
		return true;
	}

	private String desambiguation(String inputType, String patron, String term1, String term2) {
		/*
		 * C'est ici que seront les contraintes sï¿½mantiques. Doit retourner le
		 * type de relation. Utilise l'API de jeuxdemots pour vï¿½rifier les
		 * contraintes sur les termes.
		 * 
		 */
		String type = inputType;
		if (patron.equals("a des")) {
			// Simulation de contraintes sï¿½mantiques sur le patron "a des" (A
			// titre d'exemple).
			if (term1.equals("lapin")) {
				type = "Holonymie";
			}

			if (term1.equals("fille")) {
				type = "Possession";
			}
		}
		return type;

	}

	private boolean isAmbigu(String type, String patron) {
		/*
		 * Liste de patrons qui créent une ambiguité / prêtent à
		 * confusion.
		 */
		for (String Type : Relation.typePatrons.keySet()) {
			if (Relation.typePatrons.get(Type).contains(patron)) {
				if ((!Type.equals(type)) && (underSemanticConstraint(Type, patron) || underSemanticConstraint(type, patron))) {
					return true;
				}
			}
		}
		
		return false;
	}

	public void pretraitementParMc() throws Exception {
		this.parser();
		// this.mots_composes(p);
		// this.lemmatisation(p);
		this.mots_composes(p);
		// //System.out.println("Parser + Mots Composés : "+mc.newText);
		this.text = new String(mc.newText);
	}

	public void pretraitementParLem() throws Exception {
		this.parser();

		// this.mots_composes(p);
		// this.lemmatisation(p);
		this.lemmatisation(p);
		// //System.out.println("Parser + Lemmatisation "+lm.newText);
		this.text = new String(lm.newText);
		// this.text=this.text.replaceAll("[.|;|,|==|\\n|?|!|:|(|)|\\[|\\]|«|»|“|”]",
		// "");
	}

	public void pretraitementParMcLem() throws Exception {
		//System.out.println("*************************prétraitementsParMcLem*****************************");;
		this.parser();
		this.text=this.text.replaceAll("[.|;|,|==|\\n|?|!|:|(|)|\\[|\\]|«|»|“|”]",
				 "");
		// this.mots_composes(p);
		// this.lemmatisation(p);
		this.mots_composes(p);
		this.lemmatisation(mc);
		// //System.out.println("Parser + Mots Composés + Lemmatisation :
		// "+lm.newText);
		this.text = new String(abu.newText);
		 
		////System.out.println(this.text);
	}

	public void pretraitementParLemMc() throws Exception {
		this.parser();
		// this.mots_composes(p);
		// this.lemmatisation(p);
		this.lemmatisation(p);
		this.mots_composes(lm);
		// //System.out.println("Parser + Lemmatisation + Mots Composés :
		// "+mc.newText);
		this.text = new String(mc.newText);
		// this.text=this.text.replaceAll("[.|;|,|==|\\n|?|!|:|(|)|\\[|\\]|«|»|“|”]",
		// "");
	}

	public void parser() {
		/*
		 * Nettoyage du contenu tÃ©lÃ©chargÃ© (HTML ou autre).
		 */

		/*
		 * Probleme a rï¿½gler : le texte commence par null/n, essayer de
		 * l'enlver du texte
		 */
		//System.out.println("*************************DébutParserParMcLem*****************************");;
		p = new Parser(text);
		//System.out.println("*************************FinParserParMcLem*****************************");;
		// //System.out.println(" Analyser Parser "+p.newText);

	}

	public void mots_composes(TextClass TIp) throws Exception {
		/*
		 * Remplacement des espaces par des underscores.
		 */
		//System.out.println("*************************débutMotCompParMcLem*****************************");;
		this.mc = new MotsComposes(TIp,ressourcePath,this);
		//System.out.println("*************************FinMotCompParMcLem*****************************");;
	}

	public void lemmatisation(TextClass TIp) throws Exception {
		/*
		 * Mise des verbes conjuguÃ©s Ã l'infinitif...
		 */

		abu = new Lemmatisation(this.mc, ressourcePath);
		// //System.out.println(" Analyser Lemmatisation "+lm.newText);

	}

	// Getters
	public ArrayList<Relation> getRelations_trouvees() {
		return Relations_trouvees;
	}
	
	public ArrayList<Relation> getRelations_composes_trouvees() {
		return Relations_composes_trouvees;
	}

	// Setters
	public void setText(String text) {
		this.text = text;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	public boolean foundRelationcomposee(Relation relation) {
		/*
		 * VÃ©rifie si une relation a dÃ©jÃ Ã©tÃ© trouvÃ©e.
		 */
		for (Relation relation_trouvee : Relations_composes_trouvees) {
			if (relation_trouvee.equals(relation)) {
				return true;
			}
		}
		return false;

	}

	
	public boolean foundRelation(Relation relation) {
		/*
		 * VÃ©rifie si une relation a dÃ©jÃ Ã©tÃ© trouvÃ©e.
		 */
		for (Relation relation_trouvee : Relations_trouvees) {
			if (relation_trouvee.equals(relation)) {
				return true;
			}
		}
		return false;

	}

	public void writeResults() throws FileNotFoundException
	{
		titlePath = null;
		try {
			titlePath = Paths.get(Analyseur.class.getResource("/").toURI());

		} catch (URISyntaxException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		PrintWriter out = new PrintWriter(titlePath.toString()+"/results/"+this.title+"_Results.txt");
		out.println("// Résultats de l'analyse de l'article : "+this.title+"\n");
		out.println("Relations extraites :");
		for (Relation relation : this.getRelations_trouvees()) {
			out.println("<br>" +relation.getPatron()+": "+"(" + relation.getTerm1() + ":" +relation.getType()+";"+ relation.getTerm2() +";"+";CONTEXTE"+ "("+relation.getScore()+")");//// Contexte 	
		}
		out.flush();
		out.close();
	}

	public boolean evaluate(Relation R) {
		if (R.getTerm1().contains("_") && R.getTerm2().contains("_")) {
			////System.out.println(R.getTerm1()+" ---- "+R.getTerm2());
			return true;
			
		}
		
	return false;
		
	}
	
	public void displayResultsComposes(JspWriter out) throws IOException {
		/*
		 * Affiche la liste des relations trouvées.
		 */
		
		out.println("<br> Relations tirées des mots composés trouvés:");
		for (Relation relation : this.getRelations_composes_trouvees()) {
			out.println("<br>" +relation.getPatron()+": "+"(" + relation.getTerm1() + ":" +relation.getType()+";"+ relation.getTerm2() +";"+";CONTEXTE"+ "("+relation.getScore()+")");//// Contexte
																														//// :
																														//// "+relation.getContexte()+"<br><br>");
			// //System.out.println(" contexte d e la phrase
			// "+relation.getContexte());
		}
	}
	
	public void displayResults(JspWriter out) throws IOException {
		/*
		 * Affiche la liste des relations trouvées.
		 */
		
		out.println("Relations extraites :");
		for (Relation relation : this.getRelations_trouvees()) {
			out.println("<br>" +relation.getPatron()+": "+"(" + relation.getTerm1() + ":" +relation.getType()+";"+ relation.getTerm2() +";"+";CONTEXTE"+ "("+relation.getScore()+")");//// Contexte
																														//// :
																														//// "+relation.getContexte()+"<br><br>");
			// //System.out.println(" contexte d e la phrase
			// "+relation.getContexte());
		}
	}
	public String getTitle() {
		return this.title;
	}
	
	 public String MakePath(String path){
    	return(new String((path.replace("\"", ""))));
    }
}
