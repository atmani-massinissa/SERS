package Analyseur;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MesTests {
	public static void main(String[] args) throws IOException {
		//Lemmatisation lm=new Lemmatisation("");
		ArrayList<String> motsSupprimes = new ArrayList<String>();
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("C:\\Users\\user\\workspace\\SERSWeb\\WebContent\\WEB-INF\\"+"jdm-mc.txt"),"utf-8"));
		String line;
		while ((line = reader.readLine()) != null) {
			if (line.contains(";")) {
				String[] tab = line.split(";");
				String categorie="";
				for (int i = 1; i < tab.length; i++) {
					if (tab[i].contains(":")) {
						categorie=categorie + tab[i]+" / ";
					}
				}
				if (!categorie.contains("Nom") && !categorie.contains("GN") && categorie.contains("Adj")) {
					motsSupprimes.add(tab[0]+" --> "+categorie);
				}
				
			}
		}
		for (String string : motsSupprimes) {
			System.out.println(string);
		}
		System.out.println("COUNT : "+motsSupprimes.size());

	}
	
}