package Analyseur;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.*;

public class Parser extends TextClass {

	ArrayList<String> linksWiki;
	ArrayList<String> bold;
	ArrayList<String> italic;
	ArrayList<String> boldItalic;
	ArrayList<String> deleted;
	ArrayList<String> sectionTitles;
	ArrayList<String> sectionTexts;

	public Parser() {
		oldText = new String();
		newText = new String();
		linksWiki = new ArrayList<String>();
		bold = new ArrayList<String>();
		italic = new ArrayList<String>();
		boldItalic = new ArrayList<String>();
		deleted = new ArrayList<String>();
		sectionTitles = new ArrayList<String>();
		sectionTexts = new ArrayList<String>();
	}

	public Parser(String oldText) {
		this.oldText = oldText;
		linksWiki = new ArrayList<String>();
		bold = new ArrayList<String>();
		italic = new ArrayList<String>();
		boldItalic = new ArrayList<String>();
		deleted = new ArrayList<String>();
		sectionTitles = new ArrayList<String>();
		sectionTexts = new ArrayList<String>();
		this.newText = cleanText();
	}

	public String cleanText() {
		String str = new String(oldText);
		Pattern pattern = Pattern.compile("\\{\\{Lang\\|\\w\\w\\|((.)+?)\\}\\}");
		Matcher matcher = pattern.matcher(str);
		while (matcher.find()) {
			String group = matcher.group(0);
			String group1 = matcher.group(1);
			str = str.replace(group, group1);
			group = group.replace(group1, "");
			deleted.add(group);
		}

		pattern = Pattern.compile("\\{\\{\\w\\w\\}\\}");
		matcher = pattern.matcher(str);
		while (matcher.find()) {
			String group = matcher.group(0);
			deleted.add(group);
		}
		str = matcher.replaceAll("");

		pattern = Pattern.compile("\\{\\{([0-9]+e)\\|((.)+?)}\\}");
		matcher = pattern.matcher(str);
		while (matcher.find()) {
			String group = matcher.group(0);
			String group1 = matcher.group(1);
			String group2 = matcher.group(2);
			str = str.replace(group, group1 + " " + group2);
		}
		// str=matcher.replaceAll("");

		pattern = Pattern.compile("\\{\\{Terme défini \\|(.+?)\\}\\}");
		matcher = pattern.matcher(str);
		while (matcher.find()) {
			String group = matcher.group();
			String group1 = matcher.group(1);
			str = str.replace(group, group1);
		}

		pattern = Pattern.compile("\\{\\{citation\\|(.+?)\\}\\}");
		matcher = pattern.matcher(str);
		while (matcher.find()) {
			String group = matcher.group();
			String group1 = matcher.group(1);
			//System.out.println("Ma citation : "+group1);
			str = str.replace(group, group1);
		}
		pattern = Pattern.compile("\\{\\{Citation \\|(.+?)\\}\\}");
		matcher = pattern.matcher(str);
		while (matcher.find()) {
			String group = matcher.group();
			String group1 = matcher.group(1);
			System.out.println("Ma citation : "+group1);
			str = str.replace(group, group1);
		}

		pattern = Pattern.compile("\\{\\{(.)+?\\}\\}");
		matcher = pattern.matcher(str);
		while (matcher.find()) {
			String group = matcher.group(0);
			deleted.add(group);
		}
		str = matcher.replaceAll("");

		pattern = Pattern.compile("<ref([^>])+?\\/>");
		matcher = pattern.matcher(str);
		while (matcher.find()) {
			String group = matcher.group(0);
			deleted.add(group);
		}
		str = matcher.replaceAll("");

		pattern = Pattern.compile(
				"(<ref(.|\n)*?\\/ref>)+?|\\[\\[Catégorie:((.)+?)\\]\\]|\\[\\[Fichier:((.)+?)\\]\\]\n|\\[\\[File:((.)+?)\\]\\]\n|\\[http((.)+?)\\]|<!--(.)?-->");
		matcher = pattern.matcher(str);
		while (matcher.find()) {
			String group = matcher.group(0);
			deleted.add(group);
		}
		str = matcher.replaceAll("");

		pattern = Pattern.compile("\\[\\[(Image:(.)+?)\\]\\]\n");
		matcher = pattern.matcher(str);
		while (matcher.find()) {
			String group = matcher.group(0);
			deleted.add(group);
		}
		str = matcher.replaceAll("");

		pattern = Pattern.compile("\\[\\[([^|]+?)\\]\\]");
		matcher = pattern.matcher(str);
		while (matcher.find()) {
			String group = matcher.group(0);
			String group1 = matcher.group(1);
			str = str.replace(group, group1);
			if (!linksWiki.contains(group1.trim()))
				linksWiki.add(group1.trim());
		}

		pattern = Pattern.compile("\\[\\[((.)+?\\|((.)+?))\\]\\]");
		matcher = pattern.matcher(str);
		while (matcher.find()) {
			String group = matcher.group(0);
			String group1 = matcher.group(3);
			str = str.replace(group, group1);
			group = group.replace(group1, "");
			deleted.add(group);
			if (!linksWiki.contains(group1.trim()))
				linksWiki.add(group1.trim());
		}

		str = str.replaceAll("<br(\\s)*?/>", "\n");

		pattern = Pattern.compile("''((.)+?)''");
		matcher = pattern.matcher(str);
		while (matcher.find()) {
			String group = matcher.group(1);
			if (group.charAt(0) != '\'') {
				if (group.length() > 1)
					italic.add(group);
			} else if (group.charAt(1) != '\'') {
				group = group.substring(1, group.length());
				if (group.length() > 1)
					bold.add(group.trim());
			} else if (group.charAt(2) == '\'') {
				group = group.substring(3, group.length() - 3);
				if (group.length() > 1)
					boldItalic.add(group);
			}
		}
		str = str.replaceAll("('){2,}", "");

		pattern = Pattern.compile("==((.)+)==");
		matcher = pattern.matcher(str);
		while (matcher.find()) {
			String group = matcher.group(1).trim();
			sectionTitles.add(group);
		}

		pattern = Pattern.compile("((.|\n)+?)\n==");
		matcher = pattern.matcher(str);
		while (matcher.find()) {
			String group = matcher.group(1).trim();
			sectionTexts.add(group);
		}

		pattern = Pattern.compile("<!\\-\\-\n(.)+?\n\\-\\->");
		matcher = pattern.matcher(str);
		while (matcher.find()) {
			String group = matcher.group(1);
			deleted.add(group);
		}
		str = matcher.replaceAll("");

		str = str.replaceAll("\\(|\\)", "");
		// str=str.replaceAll("\\s\\((.)+?\\)","");

		str = str.replaceAll("(\\h){2,}", " ");
		str = str.replaceAll("(\n){3,}", "\n\n");
		str = str.replace(".", " .");
		str = str.replace(",", " ,");
//		str = str.replace("l'", "l' ");
//		str = str.replace("L'", "L' ");
//		str = str.replace("s'", "s' ");
//		str = str.replace("S'", "S' ");
		str = str.replace(",", " ,");
		str = str.replace("’", "'");
		str = str.replace("(,)+", ",");
		str = str.replace(" ", " ");
		str = str.replace(" ", " ");
		str = str.trim();
		return str;

	}

	public String cleanText(String str) {

		Pattern pattern = Pattern.compile("\\{\\{Lang\\|\\w\\w\\|((.)+?)\\}\\}");
		Matcher matcher = pattern.matcher(str);
		while (matcher.find()) {
			String group = matcher.group(0);
			String group1 = matcher.group(1);
			str = str.replace(group, group1);
			group = group.replace(group1, "");
			deleted.add(group);
		}

		pattern = Pattern.compile("\\{\\{\\w\\w\\}\\}");
		matcher = pattern.matcher(str);
		while (matcher.find()) {
			String group = matcher.group(0);
			deleted.add(group);
		}
		str = matcher.replaceAll("");

		pattern = Pattern.compile("\\{\\{([0-9]+e)\\|((.)+?)}\\}");
		matcher = pattern.matcher(str);
		while (matcher.find()) {
			String group = matcher.group(0);
			String group1 = matcher.group(1);
			String group2 = matcher.group(2);
			str = str.replace(group, group1 + " " + group2);
		}
		// str=matcher.replaceAll("");

		pattern = Pattern.compile("\\{\\{Terme défini \\|(.+?)\\}\\}");
		matcher = pattern.matcher(str);
		while (matcher.find()) {
			String group = matcher.group();
			String group1 = matcher.group(1);
			str = str.replace(group, group1);
		}

		pattern = Pattern.compile("\\{\\{(.)+?\\}\\}");
		matcher = pattern.matcher(str);
		while (matcher.find()) {
			String group = matcher.group(0);
			deleted.add(group);
		}
		str = matcher.replaceAll("");

		pattern = Pattern.compile("<ref([^>])+?\\/>");
		matcher = pattern.matcher(str);
		while (matcher.find()) {
			String group = matcher.group(0);
			deleted.add(group);
		}
		str = matcher.replaceAll("");

		pattern = Pattern.compile(
				"(<ref(.|\n)*?\\/ref>)+?|\\[\\[Catégorie:((.)+?)\\]\\]|\\[\\[Fichier:((.)+?)\\]\\]\n|\\[\\[File:((.)+?)\\]\\]\n|\\[http((.)+?)\\]|<!--(.)?-->");
		matcher = pattern.matcher(str);
		while (matcher.find()) {
			String group = matcher.group(0);
			deleted.add(group);
		}
		str = matcher.replaceAll("");

		pattern = Pattern.compile("\\[\\[(Image:(.)+?)\\]\\]\n");
		matcher = pattern.matcher(str);
		while (matcher.find()) {
			String group = matcher.group(0);
			deleted.add(group);
		}
		str = matcher.replaceAll("");

		pattern = Pattern.compile("\\[\\[([^|]+?)\\]\\]");
		matcher = pattern.matcher(str);
		while (matcher.find()) {
			String group = matcher.group(0);
			String group1 = matcher.group(1);
			str = str.replace(group, group1);
			if (!linksWiki.contains(group1.trim()))
				linksWiki.add(group1.trim());
		}

		pattern = Pattern.compile("\\[\\[((.)+?\\|((.)+?))\\]\\]");
		matcher = pattern.matcher(str);
		while (matcher.find()) {
			String group = matcher.group(0);
			String group1 = matcher.group(3);
			str = str.replace(group, group1);
			group = group.replace(group1, "");
			deleted.add(group);
			if (!linksWiki.contains(group1.trim()))
				linksWiki.add(group1.trim());
		}

		str = str.replaceAll("<br(\\s)*?/>", "\n");

		pattern = Pattern.compile("''((.)+?)''");
		matcher = pattern.matcher(str);
		while (matcher.find()) {
			String group = matcher.group(1);
			if (group.charAt(0) != '\'') {
				if (group.length() > 1)
					italic.add(group);
			} else if (group.charAt(1) != '\'') {
				group = group.substring(1, group.length());
				if (group.length() > 1)
					bold.add(group);
			} else if (group.charAt(2) == '\'') {
				group = group.substring(3, group.length() - 3);
				if (group.length() > 1)
					boldItalic.add(group);
			}
		}
		str = str.replaceAll("('){2,}", "");

		pattern = Pattern.compile("==((.)+)==");
		matcher = pattern.matcher(str);
		while (matcher.find()) {
			String group = matcher.group(1).trim();
			sectionTitles.add(group);
		}

		pattern = Pattern.compile("((.|\n)+?)\n==");
		matcher = pattern.matcher(str);
		while (matcher.find()) {
			String group = matcher.group(1).trim();
			sectionTexts.add(group);
		}

		pattern = Pattern.compile("<!\\-\\-\n(.)+?\n\\-\\->");
		matcher = pattern.matcher(str);
		while (matcher.find()) {
			String group = matcher.group(1);
			deleted.add(group);
		}
		str = matcher.replaceAll("");

		str = str.replaceAll("\\(|\\)", "");
		// str=str.replaceAll("\\s\\((.)+?\\)","");

		str = str.replaceAll("(\\h){2,}", " ");
		str = str.replaceAll("(\n){3,}", "\n\n");
		str = str.replace(".", " .");
		str = str.replace(",", " ,");
		str = str.replace("l'", "l' ");
		str = str.replace("L'", "L' ");
		str = str.replace("s'", "s' ");
		str = str.replace("S'", "S' ");
		str = str.replace("’", "'");
		str = str.replace("(,)+", ",");
		str = str.replace(" ", " ");
		str = str.trim();
		return str;

	}

	public static void main(String[] args) throws IOException {

		Parser c = new Parser();

		File file = new File("/auto_home/msebih/Articles/D/Dépression (psychiatrie).txt");
		FileInputStream fis = new FileInputStream(file);
		byte[] data = new byte[(int) file.length()];
		fis.read(data);
		fis.close();
		String str = new String(data, "UTF-8");
		str = c.cleanText(str);
		// System.out.println(str);
		// System.out.println(c.cleanText(str));

		// System.out.println(c.linksWiki.size());
		/*
		 * HashSet<String> wordList = new HashSet<String>(); String line;
		 * BufferedReader reader =
		 * Files.newBufferedReader(Paths.get("mwe-jdm.txt"),
		 * StandardCharsets.UTF_8); while ((line = reader.readLine()) != null) {
		 * wordList.add(line.substring(0, line.length()-1)); }
		 * 
		 * 
		 * int co=0; for (String s : c.bold){ if (s.contains(" ") &&
		 * !wordList.contains(s)) co++; }
		 */

		System.out.println(str);
		// System.out.println(c.linksWiki.size());

		/*
		 * for (String s : c.linksWiki) System.out.println(s);
		 */
	}

}
