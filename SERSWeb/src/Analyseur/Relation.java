package Analyseur;
import java.util.ArrayList;
import java.util.HashMap;

public class Relation {
	public static ArrayList<String> types_de_relations = new ArrayList<String>();
	public static HashMap<String, ArrayList<String>> typePatrons = new HashMap<String, ArrayList<String>>();
	public static HashMap<String, Integer> patronNbrTerms = new HashMap<String, Integer>(); 
	public static HashMap<String, String> patronGrammaticalConstraint = new HashMap<String, String>();
	public static HashMap<String, String> patronSemanticConstraint = new HashMap<String, String>();
	public static String PatternsTexte; 
	private String type;//Partitative,mÃ©ronymie...
	private String term1;//Premier terme liÃ© par la relation.
	private String term2;//DeuxiÃ¨me terme liÃ© par la relation.
	private String contexte;//Contexte (Ligne/texte) dans lequel la relation a Ã©tÃ© trouvÃ©e.
	private String patron;//Patron linguistique
	private Integer nbOfTermsUnderGrammaticalConstraint;
	private Integer underSemanticConstraint;
	private Integer valueOfPattern;
	private double term1IsCompound;
	private double term2IsCompound;
	private Integer term1Exists;
	private Integer term2Exists;
	private Double score;
	private Integer nbLemmaTerm1;
	private Integer nbLemmaTerm2;

	//Potentiellement : Ajouter contexte ou se trouve la relation.
	 
	public static ArrayList<String> getTypes_de_relations() {
		return types_de_relations;
	}

	
	
	public Relation(String type, String term1, String term2, String contexte, String patron) {
		this.type = type;
		this.term1 = term1;
		this.term2 = term2;
		this.contexte = contexte;
		this.patron = patron;
		if (!types_de_relations.contains(type)) {
			types_de_relations.add(type);
		}
	}
	
	public Relation(String type, String term1, String term2, String contexte, String patron, Integer nbOfTermsUnderGrammaticalConstraint, Integer underSemanticConstraint, 
			Integer term1Exists, Integer term2Exists,Integer nbLemmaTerm1, Integer nbLemmaTerm2) {
		setType(type);
		setTerm1(term1);
		setTerm2(term2);
		this.score = 0.0;
		this.contexte = contexte;
		setNbOfTermsUnderGrammaticalConstraint(nbOfTermsUnderGrammaticalConstraint);
		setUnderSemanticConstraint(underSemanticConstraint);
		setTerm1Exists(term1Exists);
		setTerm2Exists(term2Exists);
		setTerm1IsCompound();
		setTerm2IsCompound();
		setDefaultValueOfPattern(1);
		setNbLemmaTerm1(nbLemmaTerm1);
		setNbLemmaTerm2(nbLemmaTerm2);
		this.patron = patron;
		if (!types_de_relations.contains(type)) {
			types_de_relations.add(type);
		}

		setValueOfPattern();
		setScore(this.evaluate());

//		System.out.println("*************************************************************");
//
//		System.out.println("Relation [Pattern] "+this.getType()+"["+this.getPatron()+"]( "+this.getTerm1()+", "+this.getTerm2()+") ("+this.getScore()+")");
//
//		System.out.println("evaluatePattern equals "+this.evaluatePattern());
//		System.out.println("getValueOfPattern equals "+this.getValueOfPattern());
//		System.out.println("getNbOfTermsUnderGrammaticalConstraint equals "+this.getNbOfTermsUnderGrammaticalConstraint());
//		System.out.println("getUnderSemanticConstraint equals "+this.getUnderSemanticConstraint());
//
//		System.out.println("+++++++++++++++++++++++++++++++++++++++++++ ");
//
//		System.out.println("evaluateTerm1 equals "+this.evaluateTerm1());
//		System.out.println("getTerm1IsCompound equals "+this.getTerm1IsCompound());
//		System.out.println("getTerm1Exists equals "+this.getTerm1Exists());
//		System.out.println("getNbLemmaTerm1 equals "+this.getNbLemmaTerm1());
//		
//		System.out.println("------------------------------------------ ");
//
//		System.out.println("evaluateTerm2 equals "+this.evaluateTerm2());
//		System.out.println("getTerm2IsCompound equals "+this.getTerm2IsCompound());
//		System.out.println("getTerm2Exists equals "+this.getTerm2Exists());
//		System.out.println("getNbLemmaTerm2 equals "+this.getNbLemmaTerm2());
//		
//		System.out.println("*************************************************************");




		//System.out.println("evaluateTerm2 equals "+this.evaluateTerm2());
		//System.out.println("evaluateHowManyTerm1 equals "+this.getNbLemmaTerm1());
		//System.out.println("evaluateHowManyTerm2 equals "+this.getNbLemmaTerm2());
		//System.out.println("divt1 equals "+ (1/this.getNbLemmaTerm1()));
		//System.out.println("divt2 equals "+ (1/this.getNbLemmaTerm2()));

		//System.out.println("getValueOfPattern equals "+this.getValueOfPattern());
		//System.out.println("getUnderSemanticConstraint equals "+this.getUnderSemanticConstraint());
		//System.out.println("getNbOfTermsUnderGrammaticalConstraint equals "+this.getNbOfTermsUnderGrammaticalConstraint());
		double p = evaluateTerm1()*evaluateTerm2();
		double q = 1+ evaluateTerm1() + evaluateTerm2();
		//System.out.println("evaluateTerm1()*evaluateTerm2() equals "+(evaluateTerm1()*evaluateTerm2()));
		//System.out.println("1+ evaluateTerm1() + evaluateTerm2() equals "+(1+ evaluateTerm1() + evaluateTerm2()));
		////System.out.println("(p/q)+1 equals "+((p/q)+1));
		////System.out.println("((p/q)+1)/100 equals "+(((p/q)+1)/100));
		//System.out.println("p equals "+(p));
		////System.out.println("((p)/100 equals "+(((p/q)+1)/100));
		//System.out.println("final equals "+(this.evaluatePattern())*((p)/100));
		//System.out.println("score equals "+this.evaluate());
		setScore(this.evaluate());
	}
	
	
	//Getters
	public String getTerm1() {
		return term1;
	}
	public String getTerm2() {
		return term2;
	}
	public String getType() {
		return type;
	}
	public String getContexte() {
		return contexte;
	}
	public String getPatron() {
		return patron;
	}
	//Setters
	public void setTerm1(String term1) {
		this.term1 = term1;
	}
	public void setTerm2(String term2) {
		this.term2 = term2;
	}
	public void setType(String type) {
		this.type = type;
	}
	public boolean equals(Relation relation){
		
		if (this.term1.equals(relation.term1) && this.term2.equals(relation.term2) 
			&& this.type.equals(relation.type)){
			return true ; 
		}
		
		else return false;
		
	}
	
	public Integer getNbOfTermsUnderGrammaticalConstraint() {
		return nbOfTermsUnderGrammaticalConstraint;
	}



	public void setNbOfTermsUnderGrammaticalConstraint(Integer nbOfTermsUnderGrammaticalConstraint) {
		this.nbOfTermsUnderGrammaticalConstraint = nbOfTermsUnderGrammaticalConstraint;
	}



	public Integer getUnderSemanticConstraint() {
		return underSemanticConstraint;
	}



	public void setUnderSemanticConstraint(Integer underSemanticConstraint) {
		this.underSemanticConstraint = underSemanticConstraint;
	}



	public Integer getValueOfPattern() {
		return valueOfPattern;
	}



	public void setDefaultValueOfPattern(Integer valueOfPattern) {
		this.valueOfPattern = valueOfPattern;
	}
	
	public void setValueOfPattern(Integer valueOfPattern) {
		this.valueOfPattern = valueOfPattern;
	}



	public double getTerm1IsCompound() {
		return term1IsCompound;
	}



	public void setTerm1IsCompound() {
		if(this.term1.contains("_"))
			this.term1IsCompound = 1;
		else
			this.term1IsCompound = 0;
	}



	public double getTerm2IsCompound() {
		return term2IsCompound;
	}



	public void setTerm2IsCompound() {
		if(this.term2.contains("_"))
			this.term2IsCompound = 1;
		else
			this.term2IsCompound = 0;
	}



	public Integer getTerm1Exists() {
		return term1Exists;
	}



	public void setTerm1Exists(Integer term1Exists) {
		this.term1Exists = term1Exists;
	}



	public Integer getTerm2Exists() {
		return term2Exists;
	}



	public void setTerm2Exists(Integer term2Exists) {
		this.term2Exists = term2Exists;
	}
	
	public Integer grammaticalConstraintVal(){
		return (1+ getNbOfTermsUnderGrammaticalConstraint());
	}
	public Integer semanticConstraintVal(){
		return (Math.max(4*getUnderSemanticConstraint(),1));
	}
	public Integer evaluatePattern(){
		return ((grammaticalConstraintVal()*semanticConstraintVal())*getValueOfPattern());
	}
	public double evaluateTerm1(){
		return (1 + ((getTerm1Exists()+getTerm1IsCompound())*(1/getNbLemmaTerm1())));
	}
	public double evaluateTerm2(){
		return (1+	((getTerm2Exists()+getTerm2IsCompound())*(1/getNbLemmaTerm2())));
	}
	public Double evaluate(){
		double a = (evaluatePattern());
		double p = evaluateTerm1()*evaluateTerm2();
		double q = 1+ evaluateTerm1() + evaluateTerm2();
		double b = (p/q)+1;
		return (double) (a*p/100);
	}



	public Double getScore() {
		return score;
	}



	public void setScore(Double score) {
		this.score = score;
	}
	
/**
* E(R) = (ValueOfPattern(R.getPattern())*getNbOfTermUndeGrammaticalConstraint()*getSemanticConstraint())*Evaluate(R.t1)*Evalute(R.t2)
Evaluate(t) = 1 + termIsCompound(t)+ termIsExist(t)
**/
	
	public void setValueOfPattern(){
		String relation;
		relation = getType();
        String patron = getPatron();
		switch (relation) {
	    case "Hyperonymie":
	      	{	  
	      		if(patron.startsWith("est un") ||patron.startsWith("être un") ||patron.startsWith("c'est-à-dire") ||
	      				patron.startsWith("est appel") ||patron.startsWith("être appel") ){
	      			setValueOfPattern(5);
	      		}
	      		else if(patron.startsWith("qui ") ||patron.startsWith("appel")){
	      			setValueOfPattern(4);
	      		}
	      		break;
	    	}
	    case "Holonymie":
	    	{
	    		if(patron.startsWith("est constit") ||patron.startsWith("être constit") ||
	      				patron.startsWith("est compos") ||patron.startsWith("être compos")|| 
	      				patron.startsWith("a un")){
	      			setValueOfPattern(5);
	      		}
	      		else if(patron.startsWith("avoir un") ||patron.startsWith("qui a")||patron.startsWith("qui avoir")){
	      			setValueOfPattern(5);
	      		}
	      		break;
	    	}
	    case "Méronymie":
	    	{
      			setValueOfPattern(5);

	    	}		
	    case "Causalite/Cause":
    		{
    		if(patron.contains(new String("entraîner")) ||patron.contains(new String("mener"))){
      			setValueOfPattern(7);
      		}
      		else if(patron.contains(new String("cause")) ||patron.contains(new String("provoquer"))){
      			setValueOfPattern(6);
      		}
      		else /*if(patron.contains(new String("affecte")))*/{
      			setValueOfPattern(5);
      		}
      		break;
    		}
	    case "Effet":
		    {
		    	  if(patron.contains(new String("un$"))){
		      			setValueOfPattern(9);
		      		}
		      	
		      		else /*if(patron.contains(new String("affecte")))*/{
		      			setValueOfPattern(8);
		      		}
		      		break;
		    }
	    case "Causalite/Consequence":
	    	{
		    	  if(patron.contains(new String(" par "))){
		    		  setValueOfPattern(7);
		    	  }
		    	  else{
		    		  setValueOfPattern(5); 
		    	  }
	    			break;
	    	}
	    case "Possession":
	    	{
	    			setValueOfPattern(5);
	    			break;

	    	}
	    case "Caractérisation":
	    	{
		    	  if(patron.contains(new String("carac"))){
		    		  setValueOfPattern(4);
		    			break;
		    	  }
		    	  else{
		    		  setValueOfPattern(2);
		    		  break;
		    	  }
	    			
	    	}
	    case "Opposition":
			{
		    		 setValueOfPattern(5);
					break;
			}
	    case "Localisation":
			{
			    	  if(patron.contains(new String("localisatio"))){
			    		  setValueOfPattern(5);
			    	  }
			    	  else {
			    		  setValueOfPattern(4);  
			    	  }
					break;
			}
	    case "Accompagnement":
			{
			 if(patron.contains(new String("un$"))){
	      			setValueOfPattern(9);
	      		}
	      	
	      		else /*if(patron.contains(new String("affecte")))*/{
	      			setValueOfPattern(8);
	      		}
	      		break;
			}
	    case "Traitement":
			{
  			setValueOfPattern(8);
			break;
			}
		
	    case "Signe":
			{
		  	setValueOfPattern(8);
			break;
			}
	    case "Synonymie":
			{	
  			setValueOfPattern(8);
			break;
			}
		}

	}



	public Integer getNbLemmaTerm1() {
		return nbLemmaTerm1;
	}



	public void setNbLemmaTerm1(Integer nbLemmaTerm1) {
		this.nbLemmaTerm1 = nbLemmaTerm1;
	}



	public Integer getNbLemmaTerm2() {
		return nbLemmaTerm2;
	}



	public void setNbLemmaTerm2(Integer nbLemmaTerm2) {
		this.nbLemmaTerm2 = nbLemmaTerm2;
	}
}
