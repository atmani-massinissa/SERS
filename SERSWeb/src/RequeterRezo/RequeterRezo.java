package RequeterRezo;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Syst�me de requ�tes sur le r�seau lexical Rezo avec cache gérant les fichiers
 * ainsi que deux index : les fichiers contenus dans le cache ainsi que les mots
 * rencontrés mais dont l'importance ne justifie pas encore l'entrée dans le
 * cache.
 *
 * @author Jimmy Benoits
 */
public class RequeterRezo {

    /**
     * Mots en attentes.
     */
    private Index index;

    /**
     * Mots stockés dans le cache.
     */
    private Cache cache;

    /**
     * Temps par défaut à partir duquel un fichier est considéré comme obsolète.
     */
    private final static String PEREMPTION_DEFAUT = "7j";

    /**
     * Chemin par défaut du dossier contenant le cache.
     */
    private final static String CHEMIN_CACHE = "cache";

    /**
     * Nom par défaut du fichier contenant l'index des mots en attentes.
     */
    private final static String FICHIER_INDEX = CHEMIN_CACHE + File.separator + "indexAttente";

    /**
     * Nom par défaut du fichier contenant l'index des mots contenus dans le
     * cache.
     */
    private final static String FICHIER_CACHE = CHEMIN_CACHE + File.separator + "indexCache";

    /**
     * Taille maximale du cache par défaut (en nombre d'entrées).
     */
    private final static int TAILLE_MAX_DEFAUT = 1000;

    /**
     * Taille maximale du cache (en nombre d'entrées).
     */
    private final int taille_max;

    /**
     * Nombre d'heures à partir duquel un fichier est considéré comme obsolète.
     */
    private final int peremption;

    /**
     * Constructeur par défaut (utilise les valeurs par défaut).
     */
    public RequeterRezo() {
        this(PEREMPTION_DEFAUT, TAILLE_MAX_DEFAUT);
    }

    /**
     * Constructeur utilisant la péremption par défaut mais permettant de fixer
     * la taille maximale du cache (en nombre d'entrées). La taille du cache par
     * défaut peut ne pas convenir à des utilisations spécifiques. Dans le cas
     * general, la taille par défaut du cache peut être agrandi afin de gagner
     * en performance.
     *
     *
     * @param taille_max Taille maximale du cache (en nombre d'entrées).
     */
    public RequeterRezo(int taille_max) {
        this(PEREMPTION_DEFAUT, taille_max);
    }

    /**
     * Constructeur utilisant la taille max par défaut mais permettant de fixer
     * le temps de péremption (temps à partir duquel un fichier présent dans le
     * cache est considéré comme obsolète). Le format utilisé est le suivant :
     * une valeur numérique suivi de la lettre 'j' pour indiquer un temps en
     * jours ou 'h' pour un temps en heures. Exemple : "7j" pour 7 jours ou
     * "23h" pour 23 heures. Par défaut (absence d'unité ou unité non reconnue),
     * le système utilise l'heure.
     *
     *
     * @param peremption Chaîne de caractères décrivant le délais de péremption
     * en cache.
     */
    public RequeterRezo(String peremption) {
        this(peremption, TAILLE_MAX_DEFAUT);
    }

    /**
     * Constructeur paramétré. Il est nécessaire de spécifier tous les champs
     * (mais on peut utiliser les champs par défauts). La péremption est le
     * temps à partir duquel un fichier présent dans le cache est considéré
     * comme obsolète. La taille maximale est le nombre d'entrées max dans le
     * cache. Le format utilisé pour la péremption est le suivant : une valeur
     * numérique suivi de la lettre 'j' pour indiquer un temps en jours ou 'h'
     * pour un temps en heures. Exemple : "7j" pour 7 jours ou "23h" pour 23
     * heures. Par défaut (absence d'unité ou unité non reconnue), le système
     * utilise l'heure.
     *
     * @param peremption Temps avant péremption des données
     * @param tailleMax Nombre maximale d'entrée possible dans le cache.
     */
    public RequeterRezo(String peremption, int tailleMax) {
        String acc = "";
        int i = 0;
        int tmp = 0;
        while (i < peremption.length() && Character.isDigit(peremption.charAt(i))) {
            acc += peremption.charAt(i++);
        }
        try {
            tmp = Integer.parseInt(acc);
        } catch (NumberFormatException e) {
            System.err.println("Erreur dans la saisie du temps de péremption."
                    + " Si entrée manuellement, la valeur doit commencer par une série de chiffres suivie de l'unité : "
                    + "'h' pour heure et "
                    + "'j' pour jour. ");
            System.exit(1);
        }
        if (i == peremption.length() - 1 && peremption.charAt(i) == 'j') {
            tmp *= 24;
        }
        this.peremption = tmp;
        this.taille_max = tailleMax;
        initialisation();
    }

    /**
     * Point d'entré principal du programme : permet de retourner un objet
     * formaté correspondant à une page JeuxDeMots à partir d'une chaîne de
     * caractères ou null si le mot n'existe pas dans le réseau ou si ce dernier
     * ne répond pas.
     *
     * @param mot Terme dont on souhaite retrouver la page JeuxDeMots.
     * @return Un objet formaté "Mot" donnant accès aux champs extraits depuis
     * JeuxDeMots ou null si le mot n'existe pas dans JDM ou si ce dernier ne
     * répond pas.
     * @throws java.io.IOException
     * @throws java.net.MalformedURLException
     * @throws java.lang.InterruptedException
     */
    public Mot requete(String mot) throws IOException, MalformedURLException, InterruptedException {
        String avisCache = rencontrerMot(mot);
        boolean demande;
        switch (avisCache) {
            case "$DEMANDE$": {
                demande = true;
                break;
            }
            case "$OSEF$": {
                demande = false;
                break;
            }
            default: {
                return Mot.lire(avisCache);
            }
        }
        Mot res = construireMot(mot);
        if (demande) {
            reponseDemande(res);
        }
        return res;
    }

    /**
     * {@link RequeterRezo#requete(java.lang.String)} Comportement identique
     * sauf que cette méthode permet de forcer un comportement vis-à-vis du
     * cache.
     *
     * @param mot Terme dont on souhaite retrouver la page JeuxDeMots.
     * @param majCache True si l'on souhaite forcer la mise à jour, False si
     * l'on souhaite empêcher la requête d'avoir un effet sur le cache.
     * @return Un objet formaté "Mot" donnant accès aux champs extraits depuis
     * JeuxDeMots ou null si le mot n'existe pas sur le réseau ou si ce dernier
     * ne répond pas
     * @throws java.io.IOException
     * @throws java.net.MalformedURLException
     * @throws java.lang.InterruptedException
     */
    public Mot requete(String mot, boolean majCache) throws IOException, MalformedURLException, InterruptedException {
        Mot res = construireMot(mot);
        if (majCache) {
            //Fait une place si le cache est plein
            this.fairePlace();
            reponseDemande(res);
        }
        return res;
    }

    /**
     * Vide le cache (supprime le dossier ainsi que tous ses sous-éléments).
     */
    public void viderCache() {
        File racine = new File(CHEMIN_CACHE);
        supprimerRepertoire(racine);
        initialisation();
    }

    /**
     * Supprime un mot du cache. Supprime son apparition dans l'index du cache
     * ainsi que le fichier correspondant.
     *
     * @param mot Mot à supprimer.
     */
    public void supprimer(String mot) {
        File fichier = this.construireChemin(mot);
        if (fichier.exists()) {
            fichier.delete();
            //Suppression du dossier s'il est vide
            fichier = fichier.getParentFile();
            if (fichier.list().length == 0) {
                fichier.delete();
            }
            this.cache.supprimer(mot);
        }
    }

    /**
     * Sauvegarde l'état du cache (les deux index). Il est nécessaire d'appeler
     * cette méthode lors de la fin d'une session sinon l'intégrité du cache ne
     * sera pas conservée.
     */
    public void sauvegarder() {
        try {
            Cache.sauvegarderCache(cache, FICHIER_CACHE);
            Index.sauvegarderIndex(index, FICHIER_INDEX);
        } catch (IOException ex) {
            Logger.getLogger(RequeterRezo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Fonction de construction d'un Mot à partir d'une chaîne de caractère.
     * Permet de récupérer la structure de Mot depuis le réseau JeuxdeMots.
     *
     * @param nom Nom du mot à construire.
     * @return La strucutre de Mot construite depuis le réseau JeuxdeMots.
     * @throws MalformedURLException
     * @throws IOException
     * @throws InterruptedException
     */
    private Mot construireMot(String nom) throws MalformedURLException, IOException, InterruptedException {
        Mot mot = new Mot(nom);
        String relation;
        URL jdm = new URL(Mot.recupURL(nom));
        //System.out.println(jdm);
        URLConnection jd = jdm.openConnection();
        jd.setConnectTimeout(10000);
        jd.setReadTimeout(10000);
        try (BufferedReader lecteur = new BufferedReader(new InputStreamReader(jd.getInputStream(), "ISO-8859-1"))) {
            String ligne;
            String m;
            String deuxiemePartie;
            String def = "";
            String[] divisions;
            String[] sousDivisions;
            String[] pdivisions;
            String annotation;
            //poids general :
            //definition
            while ((ligne = lecteur.readLine()) != null && !(ligne.startsWith("<def>"))) {
                if (ligne.contains("poids=")) {
                    pdivisions = ligne.split("\\\"");
                    System.out.println(ligne);
                    mot.setPoids_general(Double.parseDouble(pdivisions[1]));
                } else if (ligne.startsWith("<mot-formate>:")) {
                    mot.setMotFormate(ligne.substring(13, ligne.length() - 14));
                }
            }

            if (ligne == null) {
                return null;
            }
            if (ligne.endsWith("</def>")) {
                mot.setDefinition("Pas de définition disponible ou définition répartie dans les raffinements sémantiques (voir relation \"r_raff_sem\").");
            } else {
                do {
                    def += ligne;
                } while ((ligne = lecteur.readLine()) != null && !(ligne.endsWith("</def>")));
                def += ligne;
                def = def.substring(5, def.length() - 6);
                mot.setDefinition(def.replaceAll("<br />", ""));
            }
            // relations sortantes :
            while ((ligne = lecteur.readLine()) != null && !(ligne.equals("<sortant>"))) {
            }
            while (((ligne = lecteur.readLine()) != null) && !(ligne.equals("</sortant>"))) {
                // si la ligne contient une relation :
                if (ligne.startsWith("<rel type=")) {
                    divisions = ligne.split("\\\"");
                    //rel.add(line[1]);
                    relation = divisions[1];
                    if (ligne.contains("te=")) {
                        m = divisions[8];
                    } else {
                        m = divisions[6];
                    }
                    sousDivisions = m.split("<");
                    m = sousDivisions[0];
                    m = m.substring(1);
                    if (m.contains("[") && m.contains("]")) {
                        ligne = lecteur.readLine();
                        deuxiemePartie = ligne.substring(0, ligne.length() - 6);
                        deuxiemePartie = deuxiemePartie.replaceAll("\\s", "");
                        m += " " + deuxiemePartie;
                    }
                    //TEST SUR M pour :rXXX : les annotations
                    if (m.startsWith(":r")) {
                        String poids = divisions[3];
                        divisions = m.split(" \\[");
                        annotation = divisions[0];
                        String[] subdivisions = divisions[1].split("#");
                        relation = subdivisions[0].split(" --")[1];
                        String tmp = divisions[1].split("--> ")[1];
                        tmp = tmp.substring(0, tmp.length() - 1);
                        //mot.getAnnotations().put(new TupleRelationTerme(relation, new Terme(tmp, Double.parseDouble(divisions[3]))), annotation);
                        mot.getAnnotations().put(new TupleRelationTerme(relation, new Terme(tmp, Double.parseDouble(poids))), annotation);
                    } else {
                        if (!(mot.getRelations_sortantes().containsKey(relation)) && !m.equals("_COM") && !m.equals("_SW")) {
                            mot.getRelations_sortantes().put(relation, new ArrayList<Terme>());
                        }
                        if (divisions[3].contains(".") && !m.equals("_COM") && !m.equals("_SW")) {
                            mot.getRelations_sortantes().get(relation).add(new Terme(m, 0));
                        } else if (!m.equals("_COM") && !m.equals("_SW")) {
                            mot.getRelations_sortantes().get(relation).add(new Terme(m, Integer.parseInt(divisions[3])));
                        }
                    }
                }
            }
            while ((ligne = lecteur.readLine()) != null && !(ligne.equals("<entrant>"))) {
            }
            while (((ligne = lecteur.readLine()) != null) && !(ligne.equals("</entrant>"))) {
                if (ligne.startsWith("<rel type=")) {
                    divisions = ligne.split("\\\"");
                    relation = divisions[1];
                    if (ligne.contains("te=")) {
                        m = divisions[8];
                    } else {
                        m = divisions[6];
                    }
                    sousDivisions = m.split("<");
                    m = sousDivisions[0];
                    m = m.substring(1);
                    if (!(mot.getRelations_entrantes().containsKey(relation)) && !m.equals("_COM") && !m.equals("_SW")) {
                        mot.getRelations_entrantes().put(relation, new ArrayList<Terme>());
                    }
                    if (divisions[3].contains(".") && !m.equals("_COM") && !m.equals("_SW")) {
                        mot.getRelations_sortantes().get(relation).add(new Terme(m, 0));
                    } else if (!m.equals("_COM") && !m.equals("_SW")) {
                        mot.getRelations_entrantes().get(relation).add(new Terme(m, Integer.parseInt(divisions[3])));
                    }
                }
            }
        } catch (SocketTimeoutException ex) {
            return null;
        }
        //Important : demandé par le créateur de JeuxDeMots 
        //afin d'éviter une surcharge du serveur. 
        //La mise en cache permet de limiter néanmoins ces appels.
        Thread.sleep(100);
        return mot;
    }

    /**
     * Méthode appelée lors de l'exécution d'une requête. Permet de retourner le
     * chemin d'un fichier si le mot demandé est dans le cache (et à jour).
     * Permet aussi de demander un éventuel ajout ou une mise à jour du fichier
     * du cache.
     *
     * @param mot Mot de la requête.
     * @return Une chaîne de caractère : $DEMANDE$ : le cache ne contient pas le
     * fichier mais le souhaiterait. $OSEF$ : le cache ne contient pas le
     * fichier et ne souhaite pas le récupérer. Sinon, chemin vers le fichier
     * dans le cache. Il faut alors simplement appeler la méthode lire de mot
     * (constructeur static depuis un chemin vers un fichier)
     *
     */
    private String rencontrerMot(String mot) {
        //Si le cache contient le mot
        if (cache.containsKey(mot)) {
            //On incrémente l'occurence du cache
            cache.get(mot).incrementeOccurrences();
            //Et que la version du mot dans le cache est à jour
            if (!cache.estPerime(mot)) {
                //On retourne la valeur du cache                
                return construireChemin(mot).getAbsolutePath();
                //Mais si la valeur du mot dans le cache n'est pas à jour 
            } else if (demande(mot)) {
                //Si le mot est intéressant (il y a de la place 
                //ou il est récurrent), on le demande pour le stocker en cache
                return "$DEMANDE$";
            } else {
                //Sinon on laisse faire la partie requête
                return "$OSEF$";
            }
            //Si le cache ne contient pas le mot         
        } else {
            //Si l'index non plus (première apparition), on ajoute dans index
            if (!index.containsKey(mot)) {
                index.put(mot, new IndexInfo());
            } //S'il était présent dans l'index, on incrément (sauf si on 
            //s'apperçoit qu'on ne l'a pas vu depuis plus de 7 jours : 
            //auquel cas on remet à 0 son compteur            
            else {
                index.get(mot).incrementeOccurrences(peremption);
            }
            //=> Dans tous les cas, le mot est présent maintenant dans index
            //Même principe que plus haut : on regarde si l'on doit stocker
            if (demande(mot)) {
                return "$DEMANDE$";
            } else {
                return "$OSEF$";
            }
        }
    }

    /**
     * Réponse à l'appel de "DEMANDE" : enregistre le résultat dans le système
     * cache et met à jour l'indexation
     *
     * @param mot Mot construit grâce au résultat de la requête sur le serveur
     * Rezo.
     */
    private void reponseDemande(Mot mot) {
        if (mot != null) {
            //On ajoute au cache en gardant le nombre d'occurrence (les dates sont
            //remises à zéro)
            String nom = mot.getNom();
            int occ = 1;
            if (index.containsKey(nom)) {
                occ = index.get(nom).getOccurrences();
                //On retire de l'index
                index.supprimer(nom);
            }
            if (cache.containsKey(nom)) {
                this.supprimer(nom);
            }
            cache.ajouter(nom, occ);
            //PARTIE SYSTEME DE FICHIER
            File fichier = this.construireChemin(nom);
            fichier.getParentFile().mkdirs();
            try {
                fichier.createNewFile();
                Mot.ecrire(mot, fichier);
                //fichier.setReadOnly();
            } catch (IOException ex) {
                Logger.getLogger(RequeterRezo.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Initialise le système à partir d'un dossier cache s'il existe, sinon le
     * créé.
     */
    private void initialisation() {
        File dossier = new File(CHEMIN_CACHE);
        if (dossier.exists() && dossier.isDirectory()) {
            try {
                this.index = Index.chargerIndex(FICHIER_INDEX, this.taille_max);
                this.cache = Cache.chargerCache(FICHIER_CACHE, peremption, this.taille_max, CHEMIN_CACHE);
                integrite();
            } catch (IOException | ParseException ex) {
                Logger.getLogger(RequeterRezo.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            this.cache = new Cache(peremption, this.taille_max);
            this.index = new Index(this.taille_max);
            File fichier = new File(dossier + File.separator + "indexAttente");
            Path chemin = FileSystems.getDefault().getPath(dossier.getAbsolutePath());
            try {
                dossier.mkdir();
                //Si c'est sur Windows, on cache, sinon on laisse apparent
                String SE = System.getProperty("os.name").toLowerCase();
                if (SE.contains("win")) {
                    Files.setAttribute(chemin, "dos:hidden", true);
                }
                fichier.createNewFile();
                fichier.setReadOnly();
                fichier = new File(dossier + File.separator + "indexCache");
                fichier.createNewFile();
                fichier.setReadOnly();
            } catch (IOException ex) {
                Logger.getLogger(RequeterRezo.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Vérification de l'intégrité de l'index du cache : tous les fichiers
     * présents dans l'index existent et sa taille de dépasse pas la taille
     * maximale autorisée. De plus les fichiers obsolètes sont supprimés.
     */
    private void integrite() {
        //Verification que tous les éléments présents dans l'index du cache 
        //sont bels et biens dans le cache. Sinon on supprime le fichier
        //On supprime aussi les fichiers obsolètes
        //On vérifie que la taille du cache ne soit pas dépassée. 
        //Si c'est le cas, on supprime autant de fichiers que nécessaire
        Iterator<Entry<String, CacheInfo>> iter = this.cache.entrySet().iterator();
        Entry<String, CacheInfo> element;
        ArrayList<String> a_supprimer_complet = new ArrayList<>();
        ArrayList<String> a_supprimer_index = new ArrayList<>();
        File fichier;
        while (iter.hasNext()) {
            element = iter.next();
            fichier = this.construireChemin(element.getKey());
            //Vérification existence fichier
            if (fichier.exists()) {
                //Vérification obsolescence
                if (RequeterRezo.perime(element.getValue().getDateCache(), peremption)) {
                    a_supprimer_complet.add(element.getKey());
                }
            } else {
                //Le fichier n'existe pas mais apparait dans l'index
                a_supprimer_index.add(element.getKey());
                //this.supprimer(element.getKey());
            }
        }
        
        for( String a_supprimer :  a_supprimer_complet){
            this.supprimer(a_supprimer);
        }
        
        for( String a_supprimer :  a_supprimer_index){
        	this.cache.supprimer(a_supprimer);
        }
        /**cause des erreurs, remplac� par des boucles for**/
       /** 
        a_supprimer_complet.stream().forEach((a_supprimer) -> {
            this.supprimer(a_supprimer);
        });
        a_supprimer_index.stream().forEach((a_supprimer) -> {
            this.cache.supprimer(a_supprimer);
        });
        
        **/
        //Tous les éléments dans le cache (côté index) sont présents (côté fichier)
        if (!this.nonPlein()) {
            //Malgré tout, le cache est plein (notamment lorsqu'une nouvelle 
            //taille (plus petite) a été allouée
            //On supprime X fichiers (au hasard)
            int x = this.cache.size() - this.taille_max;
            iter = this.cache.entrySet().iterator();
            while (x > 0 && iter.hasNext()) {
                element = iter.next();
                this.supprimer(element.getKey());
                --x;
            }
        }
    }

    /**
     * Retourne True si le Cache a besoin du mot, false sinon. Les conditions
     * pour demander un mot sont les suivantes : - Le cache n'est pas plein - Il
     * existe un mot périmé dans le cache - Le terme le plus proche de la
     * péremption est moins courant que le terme à ajouter
     *
     * @param mot Mot dont on cherche à savoir si son entrée dans le cache est
     * souhaitable
     * @return Retourne True si le Cache a besoin du mot, false sinon
     */
    private boolean demande(String mot) {
        //False SAUF SI : 
        boolean res = false;
        //->Il y a de la place
        //->Il existe un terme périmé dans le cache
        //->Le terme le plus proche de la péremption est moins courant que 
        //le terme à ajouter        
        if (this.nonPlein()) {
            res = true;
        } else {
            //Parcours du cache afin de trouver un terme périmé
            //On en profite pour garder en mémoire le terme le moins courant
            //(au cas où il n'y ait aucun périmé)
            boolean existePerime = false;
            Iterator<Entry<String, CacheInfo>> iter = this.cache.entrySet().iterator();
            Entry<String, CacheInfo> element;
            Entry<String, CacheInfo> moinsCourant;
            int min;
            Date dateMin;
            //initialisation
            if (iter.hasNext()) {
                element = iter.next();
                min = element.getValue().getOccurrences();
                moinsCourant = element;
                dateMin = element.getValue().getDateCache();
                if (RequeterRezo.perime(element.getValue().getDateCache(), peremption)) {
                    existePerime = true;
                }
                while (!existePerime && iter.hasNext()) {
                    element = iter.next();
                    if (RequeterRezo.perime(element.getValue().getDateCache(), peremption)) {
                        existePerime = true;
                    } else if (element.getValue().getOccurrences() < min) {
                        moinsCourant = element;
                        min = element.getValue().getOccurrences();
                        dateMin = element.getValue().getDateCache();
                    } else if (element.getValue().getOccurrences() == min) {
                        //En cas d'égalité, on regarde l'ancienneté (-> sélection du plus vieux)
                        if (element.getValue().getDateCache().before(dateMin)) {
                            moinsCourant = element;
                            min = element.getValue().getOccurrences();
                            dateMin = element.getValue().getDateCache();
                        }
                    }
                }
                if (existePerime) {
                    supprimer(element.getKey());
                    res = true;
                } //On compare avec les occurrences du moins courant                                 
                else if ((this.index.get(mot).getOccurrences() > min)) {
                    //On remet dans l'index d'attente le mot le moins courant
                    this.index.put(moinsCourant.getKey(), new IndexInfo(moinsCourant.getValue().getOccurrences(), moinsCourant.getValue().getDateOccurrences()));
                    //Mais on le supprime du cache
                    supprimer(moinsCourant.getKey());
                    res = true;
                }
            }
        }
        return res;
    }

    /**
     * Retourne le fichier associé à un mot dans le cache.
     *
     * @param mot Mot recherché.
     * @return Fichier représentant le mot dans le cache.
     */
    private File construireChemin(String mot) {
        return new File(CHEMIN_CACHE + File.separator + CacheInfo.construireChemin(this.cache.get(mot).getID()));
    }

    /**
     * Détermine si le cache n'est pas plein.
     *
     * @return True si le cache a encore de la place, false sinon.
     */
    private boolean nonPlein() {
        return this.cache.size() < this.taille_max;
    }

    /**
     * Assure la place pour au moins un élément (suppresion du 1er terme périmé
     * rencontré ou bien du terme le plus vieux parmis les moins consultés) si
     * cela est nécessaire.
     *
     */
    private void fairePlace() {
        //Si le cache n'est pas plein, pas besoin de supprimer un élément
        if (!this.nonPlein()) {
            //Parcours du cache afin de trouver un terme périmé
            //On en profite pour garder en mémoire le terme le moins courant
            //(au cas où il n'y ait aucun périmé)
            boolean existePerime = false;
            Iterator<Entry<String, CacheInfo>> iter = this.cache.entrySet().iterator();
            Entry<String, CacheInfo> element;
            Entry<String, CacheInfo> moinsCourant;
            int min;
            Date dateMin;
            //initialisation
            if (iter.hasNext()) {
                element = iter.next();
                min = element.getValue().getOccurrences();
                moinsCourant = element;
                dateMin = element.getValue().getDateCache();
                if (RequeterRezo.perime(element.getValue().getDateCache(), peremption)) {
                    existePerime = true;
                }
                while (!existePerime && iter.hasNext()) {
                    element = iter.next();
                    if (RequeterRezo.perime(element.getValue().getDateCache(), peremption)) {
                        existePerime = true;
                    } else if (element.getValue().getOccurrences() < min) {
                        moinsCourant = element;
                        min = element.getValue().getOccurrences();
                        dateMin = element.getValue().getDateCache();
                    } else if (element.getValue().getOccurrences() == min) {
                        //En cas d'égalité, on regarde l'ancienneté (-> sélection du plus vieux)
                        if (element.getValue().getDateCache().before(dateMin)) {
                            moinsCourant = element;
                            min = element.getValue().getOccurrences();
                            dateMin = element.getValue().getDateCache();
                        }
                    }
                }
                //S'il existe un périmé : on le supprime
                if (existePerime) {
                    supprimer(element.getKey());
                } //Sinon on supprime le moins courant
                else {
                    supprimer(moinsCourant.getKey());
                }
            }
        }
    }

    /**
     * Détermine si une date est périmée.
     *
     * @param date Date à tester.
     * @param peremption Nombre d'heures déterminant le délais de péremption.
     * @return True si le nombre d'heures séparant la date à tester de la date
     * actuelle est inférieur au délais de péremption.
     */
    public static boolean perime(Date date, int peremption) {
        return RequeterRezo.heuresEcarts(new Date(), date) > peremption;
    }

    /**
     * Retourne le nombre d'heures séparant deux dates.
     *
     * @param maintenant Date récente.
     * @param enregistrement Date ancienne.
     * @return Le nombre d'heures séparant les deux dates.
     */
    private static int heuresEcarts(Date maintenant, Date enregistrement) {
        return (int) ((maintenant.getTime() - enregistrement.getTime()) / 3600000);
    }

    /**
     * Supprime un fichier / dossier ainsi que tous ses sous-fichiers.
     *
     * @param unFichier Fichier à supprimer.
     */
    private static void supprimerRepertoire(File unFichier) {
        if (unFichier.isDirectory()) {
            File[] files = unFichier.listFiles();
            if (files != null && files.length > 0) {
                for (File aFile : files) {
                    supprimerRepertoire(aFile);
                }
            }
            unFichier.delete();
        } else {
            unFichier.delete();
        }
    }

    public class TupleRelationTerme {

        private final String relation;
        private final Terme terme;

        TupleRelationTerme(String relation, Terme terme) {
            this.relation = relation;
            this.terme = terme;
        }

        public String getRelation() {
            return this.relation;
        }

        public Terme getTerme() {
            return this.terme;
        }
    }
    public boolean containsClasse(String relation, String mot, String classe) {
    	try {
			for (Terme rel : requete(mot).getRelations_sortantes().get(relation)) {
				if (rel.getTerme().startsWith(classe)) {
					return true;
				}
			}
		} catch (NullPointerException e) {
			System.out.println("Une erreur s'est produite lors de l'acc�s � jeuxdemots");
			return true;
		}catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return false;
		
	}
}
