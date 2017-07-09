package Analyseur;

public abstract class TextClass {

	String oldText;
	String newText;

	public void apostrFs(){
		newText = newText.replace(" d'", " d' ");
		newText = newText.replace(" l'", " l' ");
		newText = newText.replace(" s'", " s' ");
		newText = newText.replace(" D'", " D' ");
		newText = newText.replace(" L'", " L' ");
		newText = newText.replace(" S'", " S' ");
	}
	public String apostrFj(String mot){
		mot = mot.replace("L' ", "L'");
		mot = mot.replace("S' ", "S'");
		mot = mot.replace("D' ", "D'");
		mot = mot.replace("l' ", "l'");
		mot = mot.replace("s' ", "s'");
		mot = mot.replace("d' ", "d'");
		return mot;
	}
	public void apostrFj(){
		newText = newText.replace("L' ", "L'");
		newText = newText.replace("S' ", "S'");
		newText = newText.replace("D' ", "D'");
		newText = newText.replace("l' ", "l'");
		newText = newText.replace("s' ", "s'");
		newText = newText.replace("d' ", "d'");
	}

}
