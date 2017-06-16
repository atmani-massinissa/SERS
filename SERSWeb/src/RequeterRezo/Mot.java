package RequeterRezo;

import RequeterRezo.RequeterRezo.TupleRelationTerme;
import java.io.*;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Objects;

/**
 * Structure regroupant les informations obtenus sur un mot par une requête sur
 * jeuxdemots.
 */
public class Mot {

    /**
     * Chaîne de caractère décrivant le mot.
     */
    protected final String nom;

    /**
     * Bien souvent identique à "nom", mais propose parfois d'autres
     * informations (notamment pour les annotations).
     */
    protected String mot_formate;

    /**
     * Définition retourné par jeuxdemots (partie entre les balises "def").
     */
    protected String definition = "";

    /**
     * Poids du mot dans la langue française.
     */
    protected double poids_general;

    /**
     * Ensemble des relations entrantes associées au mot dans le réseau. Les
     * clés sont les noms des relations dans jeuxdemots, les valeurs sont des
     * couples "terme,poids". voir ({@link Terme})
     */
    protected HashMap<String, ArrayList<Terme>> relations_entrantes;

    /**
     * Ensemble des relations sortantes associées au mot dans le réseau. Les
     * clés sont les noms des relations dans jeuxdemots.
     */
    protected HashMap<String, ArrayList<Terme>> relations_sortantes;

    /**
     * Ensemble des id des annotations liés aux relations du terme. Une
     * annotation est formée de la même façon qu'un mot (un descriptif lisible
     * est consultable dans le champ mot_formate). Une annotation s'applique à
     * un tuple Relation - Terme. voir
     * {@link RequeterRezo.TupleRelationTerme}
     */
    protected HashMap<TupleRelationTerme, String> annotations;

    /**
     * Constructeur simple. Appelé seulement par le système lorsqu'il doit
     * construire un mot à partir d'une requête sur JDM.
     *
     * @param nom Mot à construire
     */
    public Mot(String nom) {
        this.nom = nom;
        this.mot_formate = nom;
        this.relations_entrantes = new HashMap<>();
        this.relations_sortantes = new HashMap<>();
        this.annotations = new HashMap<>();
    }

    /**
     * Constructeur paramétré complet. Appelé seulement par le système lorsqu'il
     * doit construire un mot à partir d'un fichier présent dans le cache.
     *
     *
     * @param nom Mot à construire
     * @param description Définition extraite de JeuxDeMots
     * @param mot_formate Mot_formate extrait de JeuxDeMots
     * @param pg Poids dans la langue française extrait de JeuxDeMots
     * @param relations_entrantes Relations entrantes extraites de JeuxDeMots
     * @param relations_sortantes Relations sortantes extraites de JeuxDeMots
     */
    protected Mot(String nom, String mot_formate, double pg, String description, HashMap<String, ArrayList<Terme>> relations_entrantes, HashMap<String, ArrayList<Terme>> relations_sortantes) {
        this.nom = nom;
        this.mot_formate = mot_formate;
        this.poids_general = pg;
        this.relations_entrantes = relations_entrantes;
        this.relations_sortantes = relations_sortantes;
        this.definition = description;
    }

    /**
     * Retourne le nom du mot ("lui-même")
     *
     * @return Le nom du mot
     */
    public String getNom() {
        return nom;
    }

    public String getMotFormate() {
        return this.mot_formate;
    }

    /**
     * Retourne les relations entrantes du mot dans le réseau.
     *
     * @return Retourne les relations entrantes du mot dans le réseau.
     */
    public HashMap<String, ArrayList<Terme>> getRelations_entrantes() {
        return relations_entrantes;
    }

    /**
     * Retourne les relations sortantes du mot dans le réseau.
     *
     * @return Retourne les relations sortantes du mot dans le réseau.
     */
    public HashMap<String, ArrayList<Terme>> getRelations_sortantes() {
        return relations_sortantes;
    }

    /**
     * Retourne les annotations associées aux relations sortantes du mot.
     *
     * @return Retourne les annotations associées aux relations sortantes du
     * mot.
     */
    public HashMap<TupleRelationTerme, String> getAnnotations() {
        return this.annotations;
    }

    /**
     * Retourne la définition du mot.
     *
     * @return Retourne la définition du mot.
     */
    public String getDefinition() {
        return this.definition;
    }

    /**
     * Retourne le poids du mot dans la langue française.
     *
     * @return Le poids du mot dans la langue française.
     */
    public double getPoids_general() {
        return poids_general;
    }

    /**
     * Ecrit toute la structure du Mot dans un fichier, pour une réutilisation
     * future (notamment dans le cache).
     *
     * @param mot Mot à conserver.
     * @param fichier Fichier où le Mot doit être stocké.
     * @throws IOException
     */
    public static void ecrire(Mot mot, File fichier) throws IOException {
        try (BufferedWriter ecrivain = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fichier), "UTF-8"))) {
            //System.out.println(mot.getNom());
            ecrivain.write(mot.getNom());
            ecrivain.newLine();
            ecrivain.write(mot.getMotFormate());
            ecrivain.newLine();
            //System.out.println(mot.getPoids_general());
            ecrivain.write("" + mot.getPoids_general());
            ecrivain.newLine();
            ecrivain.write("<def>");
            ecrivain.newLine();
            ecrivain.write(mot.getDefinition());
            ecrivain.newLine();
            ecrivain.write("</def>");
            ecrivain.newLine();
            ecrivain.write("sortant");
            ecrivain.newLine();
            // pour toutes les relations dans les relations_sortantes
            for (Entry<String, ArrayList<Terme>> entree : mot.relations_sortantes.entrySet()) {
                ecrivain.write(entree.getKey());
                for (Terme motCible : entree.getValue()) {
                    ecrivain.write(";" + motCible.getTerme() + ",," + motCible.getPoids());
                }
                ecrivain.newLine();
            }
            ecrivain.write("entrant");
            ecrivain.newLine();
            for (Entry<String, ArrayList<Terme>> entree : mot.relations_entrantes.entrySet()) {
                ecrivain.write(entree.getKey());
                for (Terme motCible : entree.getValue()) {
                    ecrivain.write(";" + motCible.getTerme() + ",," + motCible.getPoids());
                }
                ecrivain.newLine();
            }
        }
    }

    /**
     * Retourne la structure de Mot depuis un fichier écrit par la fonction
     * "ecrire".
     *
     * @param chemin Chemin du fichier qui doit être lu.
     * @return Le Mot sauvegardé dans le fichier.
     * @throws IOException
     */
    public static Mot lire(String chemin) throws IOException {
        String nom;
        String description;
        String mot_formate;
        String poids_general = "";
        HashMap<String, ArrayList<Terme>> relations_sortantes;
        HashMap<String, ArrayList<Terme>> relations_entrantes;
        try (BufferedReader lecteur = new BufferedReader(new FileReader(chemin))) {
            String ligne;
            nom = "";
            mot_formate = "";
            description = "";
            String[] divisions;
            String[] sous_divisions;
            relations_sortantes = new HashMap<>();
            relations_entrantes = new HashMap<>();
            // lecture du nom :
            if ((ligne = lecteur.readLine()) != null) {
                nom = ligne;
            }
            // lecture du mot_formate
            if ((ligne = lecteur.readLine()) != null) {
                mot_formate = ligne;
            }
            // lecture du poids
            if ((ligne = lecteur.readLine()) != null) {
                poids_general = ligne;
            }
            //Saut de la ligne "<def>"
            lecteur.readLine();
            //lecture de la description
            while (((ligne = lecteur.readLine()) != null) && !(ligne.equals("</def>"))) {
                description += ligne;
            }
            //Saut de la ligne "sortant"
            lecteur.readLine();
            // lecture des relations sortantes
            while (((ligne = lecteur.readLine()) != null) && !(ligne.equals("entrant"))) {
                divisions = ligne.split(";");
                relations_sortantes.put(divisions[0], new ArrayList<Terme>());
                for (int i = 1; i < divisions.length; ++i) {
                    sous_divisions = divisions[i].split(",,");
                    if (sous_divisions.length == 2) {
                        relations_sortantes.get(divisions[0]).add(new Terme(sous_divisions[0], Double.parseDouble(sous_divisions[1])));
                    }
                }
            }
            while ((ligne = lecteur.readLine()) != null) {
                divisions = ligne.split(";");
                relations_entrantes.put(divisions[0], new ArrayList<Terme>());
                for (int i = 1; i < divisions.length; ++i) {
                    sous_divisions = divisions[i].split(",,");
                    if (sous_divisions.length == 2) {
                        relations_entrantes.get(divisions[0]).add(new Terme(sous_divisions[0], Double.parseDouble(sous_divisions[1])));
                    }
                }
            }
        }
        return new Mot(nom, mot_formate, Double.parseDouble(poids_general), description, relations_entrantes, relations_sortantes);
    }

    /**
     * Construit l'URL d'un mot dans JeuxdeMot.
     *
     * @param mot Nom du mot dont il faut construire l'URL.
     * @return L'URL complète permettant de retrouver le mot sur le réseau.
     * @throws java.io.UnsupportedEncodingException
     */
    protected static String recupURL(String mot) throws UnsupportedEncodingException {
        String encode = URLEncoder.encode(mot, "LATIN1");
        return "http://www.jeuxdemots.org/rezo-xml.php?gotermsubmit=Chercher&gotermrel=" + encode + "&output=onlyxml";
    }

    /**
     * Retourne une chaîne de caractères décrivant l'ensemble de la structure.
     * Pour une lecture / écriture dans un fichier, préférez     
     * {@link Mot#ecrire(RequeterRezo.Mot, java.io.File)}  et 
     * {@link Mot#lire(java.lang.String)}
     *
     * @return Une chaîne de caractères décrivant l'ensemble de la structure
     * extraite depuis JeuxDeMots.
     */
    @Override
    public String toString() {
        return "Mot{"
                + "nom=" + nom + ", "
                + "mot_formate=" + mot_formate + ", "
                + "definition=" + definition + ", "
                + "relations_entrantes=" + relations_entrantes + ", "
                + "relations_sortantes=" + relations_sortantes + '}';
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + Objects.hashCode(this.nom);
        return hash;
    }

    /**
     * Deux mots sont identiques s'il partage le même champ "nom"
     *
     * @param obj Mot à comparer
     * @return True si les deux mots partagent le champ "nom"
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Mot other = (Mot) obj;
        return Objects.equals(this.nom, other.nom);
    }

    /**
     * Permet de changer le poids d'un mot (en local, n'a aucun effet sur le
     * réseau).
     *
     * @param poids_general Nouveau poids
     */
    public void setPoids_general(double poids_general) {
        this.poids_general = poids_general;
    }

    /**
     * Permet de changer les relations entrantes d'un mot (en local, n'a aucun
     * effet sur le réseau).
     *
     * @param relations_entrantes Nouvelles relations entrantes
     */
    public void setRelations_entrantes(HashMap<String, ArrayList<Terme>> relations_entrantes) {
        this.relations_entrantes = relations_entrantes;
    }

    /**
     * Permet de changer les relations sortantes d'un mot (en local, n'a aucun
     * effet sur le réseau).
     *
     * @param relations_sortantes Nouvelles relations sortantes
     */
    public void setRelations_sortantes(HashMap<String, ArrayList<Terme>> relations_sortantes) {
        this.relations_sortantes = relations_sortantes;
    }

    /**
     * Permet de changer la définition d'un mot (en local, n'a aucun effet sur
     * le réseau).
     *
     * @param definition Nouvelle définition du mot.
     */
    public void setDefinition(String definition) {
        this.definition = definition;
    }

    /**
     * Permet de changer la forme formaté d'un mot (en local, n'a aucun effet
     * sur le réseau).
     *
     * @param mot_formate Nouveau mot formate.
     */
    public void setMotFormate(String mot_formate) {
        this.mot_formate = mot_formate;
    }
}
