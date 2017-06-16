package Analyseur;

import java.util.ArrayList;
import java.util.HashMap;

public class Relation {
	public static ArrayList<String> types_de_relations = new ArrayList<String>();
	public static HashMap<String, ArrayList<String>> typePatrons = new HashMap<String, ArrayList<String>>();
	public static HashMap<String, Integer> patronNbrTerms = new HashMap<String, Integer>();
	public static HashMap<String, String> patronConstraint = new HashMap<String, String>();
	private String type;// Partitative,mÃ©ronymie...
	private String term1;// Premier terme liÃ© par la relation.
	private String term2;// DeuxiÃ¨me terme liÃ© par la relation.
	private String contexte;// Contexte (Ligne/texte) dans lequel la relation a
							// Ã©tÃ© trouvÃ©e.
	// Potentiellement : Ajouter contexte ou se trouve la relation.

	public static ArrayList<String> getTypes_de_relations() {
		return types_de_relations;
	}

	public Relation(String type, String term1, String term2, String contexte) {
		this.type = type;
		this.term1 = term1;
		this.term2 = term2;
		this.contexte = contexte;
		if (!types_de_relations.contains(type)) {
			types_de_relations.add(type);
		}
	}

	// Getters
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

	// Setters
	public void setTerm1(String term1) {
		this.term1 = term1;
	}

	public void setTerm2(String term2) {
		this.term2 = term2;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean equals(Relation relation) {

		if (this.term1.equals(relation.term1) && this.term2.equals(relation.term2) && this.type.equals(relation.type)) {
			return true;
		}

		else
			return false;

	}
}
