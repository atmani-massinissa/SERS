package Analyseur;

import java.io.BufferedReader;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import RequeterRezo.RequeterRezo;
import RequeterRezo.Terme;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.io.*;
import java.util.*;

public class MesTests {
	public static void main(String[] args){
		System.out.println("avoir un".split(" ").length);
		expReg();
		//Lemmatisation abu = new Lemmatisation("");
		//System.out.println(" howma "+abu.howManyLemmes("risque"));
		//essaiSupp();
		//essaiCntrntSemantic();
//		String s = new String("Dépression_(psychiatrie)_Results");
//		URI t = URLEncoder.encode(s, "UTF-8").
//		System.out.println(" encode "+t);
//		System.out.println(t);
		
	}
	public static void essaiCntrntSemantic() throws MalformedURLException, IOException, InterruptedException{
		RequeterRezo test = new RequeterRezo();
		//System.out.println(test.requete("industrie pharmaceutique").getRelations_sortantes().get("r_isa"));
		//System.out.println(test.requete("quelqu'un").getRelations_sortantes().get("r_isa").contains(new String().matches(".+")))	;
		System.out.println(test.requete("pharmaceutique").getRelations_sortantes().get("r_isa"));
		//[r_patient-1, r_carac-1, r_aki, r_holo, r_meaning, r_lieu-1, r_domain_subst, r_is_smaller_than, r_syn, r_hypo, r_raff_sem, r_predecesseur-time, r_antimagn, r_link, r_descend_de, r_agent-1, r_associated, r_domain, r_has_part, r_isa, r_quantificateur, r_magn, r_color, r_family, r_lemma, r_isa-incompatible, r_data, r_sentiment, r_infopot, r_wiki, r_carac, r_causatif, r_make, r_lieu, r_flpot, r_instance, r_inhib, r_pos, r_locution, r_is_bigger_than, r_domain-1, r_conseq]
//		for (Terme rel : test.requete("quelqu'un").getRelations_sortantes().get("r_isa")) {
//			if (rel.getTerme().startsWith("personne")) {
//				System.out.println(rel);
//			}
//		}
	}
	

	public static void essaiSupp() throws IOException{
		ArrayList<String> motsSupprimes = new ArrayList<String>();
		@SuppressWarnings("resource")
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
				if (!categorie.contains("Nom") && !categorie.contains("GN") && !categorie.contains("Adj") && (categorie.contains("Con") || categorie.contains("Ver") || categorie.contains("Pro") || categorie.contains("Adv") || categorie.contains("Modifier"))) {
					motsSupprimes.add(tab[0]+" --> "+categorie);
				}
				
			}
		}
		for (String string : motsSupprimes) {
			System.out.println(string);
		}
		System.out.println("COUNT : "+motsSupprimes.size());

	}

	public static void expReg(){
		String carAccentues = "�,�������������������������������\\-";
		String motFr = "[A-Za-z0-9_" + carAccentues + "']";
		String strExpReg ="(([A-Za-z0-9_�,�������������������������������\\-'])+[\\s\\.,]+){1,4}petite\\s([A-Za-z0-9_�,�������������������������������\\-'�]+\\s?){1,5}pi�ce[\\s\\.,]+(([A-Za-z0-9_�,�������������������������������\\-'])+[\\s\\.,]){1,4}";
		Pattern ExpReg = Pattern.compile(strExpReg);
		Matcher matcher = ExpReg.matcher("Le vieux mineur et le jeune avaient l�air d�attendre encore ; puis, ils partirent, les �paules cass�es.");
		if (matcher.find()) {
			System.out.println("TEST expression reg");
			System.out.println(matcher.group());
		}
		
		else {
			System.out.println("ERREUR !!!!!!");
		}
	}
}