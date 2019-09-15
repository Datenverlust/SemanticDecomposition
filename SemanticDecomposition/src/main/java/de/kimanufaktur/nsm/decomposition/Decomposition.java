/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.kimanufaktur.nsm.decomposition;


import de.kimanufaktur.nsm.decomposition.Dictionaries.BaseDictionary;
import de.kimanufaktur.nsm.decomposition.Dictionaries.RDFXMLDictionary;
import de.kimanufaktur.nsm.decomposition.Dictionaries.WiktionaryDictionary;
import de.kimanufaktur.nsm.decomposition.Dictionaries.WordNetDictionary;
import de.kimanufaktur.nsm.decomposition.exceptions.DictionaryDoesNotContainConceptException;
//import de.kimanufaktur.nsm.decomposition.manualDefinition.ManualDecompositionGUI;
//import de.kimanufaktur.nsm.decomposition.manualDefinition.model.Delegate;
import de.kimanufaktur.nsm.decomposition.persistence.ConceptCache;
import de.kimanufaktur.nsm.decomposition.settings.Config;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.*;

/**
 * Class containing the main decomposition algorithm.
 */
public class Decomposition {

    private static final Logger logger = Logger.getLogger(Decomposition.class);
    public static List<BaseDictionary> dictionaries = new ArrayList<>();
    //    static Map<Integer, Concept> lockMap = Collections.synchronizedMap(new Hashtable<Integer, Concept>());
    static Map<Integer, Concept> lockMap = new Hashtable<Integer, Concept>();
    private static HashSet<Concept> nSMPrimes = new HashSet<>();
    private static HashSet<Concept> concepts2Ignore = new HashSet<>();

    public static void setConceptCache(ConceptCache conceptCache) {
        Decomposition.conceptCache = conceptCache;
    }

    //private static Map<Integer, Concept> knownConcepts = Collections.synchronizedMap(new HashMap<Integer, Concept>());
    private static ConceptCache conceptCache = null;
    private static Concept concept = null;
    ExecutorService cachedThreadPool = Executors.newFixedThreadPool(DecompositionConfig.getThreadCount());//Executors.newCachedThreadPool();
    ConcurrentHashMap.KeySetView<Future<Concept>,Boolean> futures = ConcurrentHashMap.newKeySet();
    private int lockcount = 0;

    public void cleanUp(){
        ConceptCache.cleanUp();
    }

    public Decomposition() {
        init();
        concepts2Ignore = createConcepts2Ignor(); //Create A list of concepts which should be ignored during decomposition
        nSMPrimes = createPrimes(); //Create list of semantic primes form NSM a leaf nodes for the decomposition
        // knownConcepts.putAll(concepts2Ignore);
        addAllKnownConcepts(concepts2Ignore);
        //knownConcepts.putAll(nSMPrimes);
        addAllKnownConcepts(nSMPrimes);
        //addAllKnownConcepts(nSMPrimes);
    }

//    public static Map<Integer, Concept> getKnownConcept() {
//        return knownConcepts;
//    }
//
//    public static void setKnownConcepts(Map<Integer, Concept> knownConcepts) {
//        Decomposition.knownConcepts = knownConcepts;
//    }

    public static Concept getConcept() {
        return concept;
    }

    public static void setConcept(Concept concept) {
        Decomposition.concept = concept;
    }


    public static void main(String[] args) {
        String word2Decompose;
        if (args.length > 0) {
            word2Decompose = args[0];
        } else {
            word2Decompose = "girl";
        }
        logger.info("Semantic decomposition of " + word2Decompose + ".");
        Decomposition decomposition = new Decomposition();
        init();
        concept = decomposition.multiThreadedDecompose(word2Decompose, WordType.VB, 2);
        System.out.println(concept.getSynonyms());
        System.out.println(concept.getDecomposition());
        System.out.println(concept.getAntonyms());
        logger.info("We are done: " + concept.toString());
    }


    /**
     * Initialize the dictionaries and create list of primes. This includes loading WordNet into memmory, creating the
     * NSM semantic primes and concepts to ignore like "the","an" and "a".
     */
    public static void init() {
        conceptCache = ConceptCache.getInstance();
        if (dictionaries.size() > 0) {
            return;
        }
//        BaseDictionary wordNetDict = WordNetDictionary.getInstance(); //Create WordNet Dictionary in memory
//        dictionaries.add(wordNetDict);

        BaseDictionary measureMentOntology = new RDFXMLDictionary();//RDFXMLDictionary.getInstance();
        dictionaries.add(measureMentOntology);

        BaseDictionary wiktionaryDict = WiktionaryDictionary.getInstance();
        dictionaries.add(wiktionaryDict);

//        IDictionary wikidataDict = WikidataDictionary.getInstance();
//        dictionaries.add(wikidataDict);
    }

    public void addDictionary(BaseDictionary aDict){
        dictionaries.add(aDict);
    }


    /**
     * Create a list of concepts to ignore. Generally this is called stop words.
     * TODO: find a list of stopwords, and load it. Have a reference for it...
     *
     * @return the set of concepts to ignore.
     */
    private static HashSet<Concept> createConcepts2Ignor() {
        //String stopwordFile = System.getProperty("user.home") + File.separator + ".decomposition" + File.separator + "stopwords.txt";
        HashSet<Concept> ignoredConcepts = new HashSet<>(10);

        //List<String> ignores = Arrays.asList("a", "an", "the", "in", "or", "of", "on", "we", "us", "to", "that", "so", "our", "no", "me", "it", "etc", "for", "by", "as", "at", "am");

        Set<String> ignores = Config.getInstance().stopWords();

        for (String p : ignores){//loadStopwordList(stopwordFile)) {
            Concept i = new Concept(p);
            i.setDecompositionlevel(Integer.MAX_VALUE);
            for (BaseDictionary dic : dictionaries) {
                if (i.getWordType() != null) {
                    dic.setPOS(i);
                }
            }
            ignoredConcepts.add(i);
        }

        return ignoredConcepts;
    }

    private static List<String> loadStopwordList(String stopwordFile) {
        List<String> stopwords = new ArrayList<>();
        File f = new File(stopwordFile);
        try {
            //logger.log(f.getAbsolutePath());
            //logger.log(f.getCanonicalPath());
            FileReader fr = new FileReader(f);
            BufferedReader br = new BufferedReader(fr);
            String currentLine;
            while ((currentLine = br.readLine()) != null) {
                currentLine = currentLine.trim().toLowerCase();
                if (!"".equals(currentLine)) {
                    stopwords.add(currentLine);
                    //logger.log("adding "+currentLine+" to stopwords");
                }
            }
        } catch (IOException e) {
            logger.error("Could not load stopword list from " + f.getAbsolutePath());
            e.printStackTrace();
        }
        return stopwords;
    }

    /**
     * Fill the NSM Primes with all concepts of the 65 semantic primes and add all synonyms of those
     * primes to the set of nSMPrimes
     *
     * @return A set containing als NSM Primes and their synonyms as concepts.
     */
    private static HashSet<Concept> createPrimes() {

        HashSet<Concept> nsmPrimes = new HashSet<>(65);
        //TODO: use text resource and make configurable
        //List<String> primes = Arrays.asList("i", "you", "someone", "something", "thing", "people", "body", "kind", "part", "this", "same", "other", "else ", "one", "two", "much", "many", "some", "all", "good", "bad", "big", "small", "think", "know", "want", "feel", "see", "hear", "say ", "words", "true", "do", "happen", "move", "touch", "there is", "have", "be", "live", "die", "when", "now", "before", "after", "a long time", "a short time", "for some time", "moment", "where", "here", "above", "below", "far", "near", "side", "inside", "not", "maybe", "can", "because", "if", "very", "more", "like", "same");
        //german
        //List<String> primes = Arrays.asList("ich", "du", "jemand", "etwas", "ding", "leute", "körper", "art", "teil", "dieses", "gleiche", "andere", "eins", "zwei", "viele", "einige", "alle", "gut", "schlecht", "groß", "klein", "denken", "wissen", "wollen", "fühlen", "sehen", "hören", "sagen", "wort", "wahr", "tun", "geschehen", "bewegen", "berühren", "es gibt", "haben", "befinden", "sein", "leben", "sterben", "wann", "jetzt", "vorher", "nachher", "lange Zeit", "kurze Zeit", "eine Zeit lang", "moment", "wo", "ort", "hier", "über", "unter", "fern", "nah", "seite", "innerhalb", "nicht", "vielleicht", "können", "weil", "falls", "sehr", "mehr", "wie");

        Set<String> primes = Config.getInstance().primesWords();
        for (String p : primes) {
            Concept prime = new Concept(p);
            prime.setDecompositionlevel(Integer.MAX_VALUE);
            nsmPrimes.add(prime);
        }

        return nsmPrimes;
    }





    /**
     * Create a concept and check if we know it already.
     *
     * @param word the string to get a concept for.
     * @return a concept with the literal set to the given string.
     */
    public static Concept createConcept(String word) {
        Concept result = new Concept(word);
        if (checkIsPrime(result)) {
            result = getPrimeofConcept(result);
        } else {
            Concept knownConcept = getKnownConcept(result);
            if (knownConcept.getDecompositionlevel() >= 0) {
                result = knownConcept;
            }
        }
        return result;
    }

    /**
     * Create a concept for the given word with the given wordtype. If the wordtype is null, the word type of the created
     * concept will be WordType.UNKNOWN
     *
     * @param word     the word we want to turn in to a concept.
     * @param wordType the part of speech (POS) of the given word. This is important since the selection of the definition
     *                 used for the decomposition will depend on the word type. Privide it if you can, so the decomposition
     *                 will be more precise.
     * @return a concept presenting the given word with the given word type.
     */
    public static Concept createConcept(String word, WordType wordType) {
        Concept result = new Concept(word);
        if (wordType != null) {
            result.setWordType(wordType);
        } else {
            result.setWordType(WordType.UNKNOWN);
        }
        if (checkIsPrime(result)) {
            return getPrimeofConcept(result);
        } else {
            Concept knownConcept = getKnownConcept(result);
            if (knownConcept.getDecompositionlevel() >= 0) {
                return knownConcept;
            }
            for (BaseDictionary dic : dictionaries) {
                try {
                    dic.fillConcept(result, wordType);
                } catch (DictionaryDoesNotContainConceptException e) {
                    //e.printStackTrace();
                }
            }
            if (result.getDefinitions() == null || result.getDefinitions().size() < 1) {
                //    getManualDefinition(result,);
            }
            addKnownConcept(result);
            return result;
        }
    }

    /**
     * Get the corresponding Prim for the given concept. Here all synonyms are checked as well.
     *
     * @param concept concept to get the semantic prime for.
     * @return the prime fitting this concept
     */
    public static Concept getPrimeofConcept(Concept concept) {
        for (Concept prime : nSMPrimes) {
            if (concept.getLemma() != null) {
                if (prime.hashCode() == concept.getLemma().hashCode()) {
                    return prime;
                }
            }
            if (prime.getLitheral().equals(concept.getLitheral())) {
                return prime;
            }
            if (prime.getLitheral().equals(concept.getLitheral().toUpperCase())) {
                return prime;
            }
            if (prime.equals(concept)) {
                return prime;
            }
            for (Concept syn : concept.getSynonyms()) {
                if (prime.equals(syn)) {
                    return prime;
                }
            }
            for (Concept psyn : prime.getSynonyms()) {
                if (psyn.equals(concept)) {
                    return prime;
                }
            }
        }
        return null;
    }

    /**
     * Check rather this concept is a prime.
     *
     * @param concept the concept which could be a prime
     * @return true if the the given concept is a prime.
     */
    public static boolean checkIsPrime(Concept concept) {
        if (nSMPrimes.contains(concept)) {
            return true;
        }
        if (concept.getLemma() != null) {
            Concept lemma = new Concept(concept.getLemma());
            if (nSMPrimes.contains(lemma)) {
                return true;
            }
        }
        if (concept.getLitheral() != null) {
            Concept c = new Concept(concept.getLitheral().toUpperCase());
            if (nSMPrimes.contains(c)) {
                return true;
            }
        }
        for (Concept s : concept.getSynonyms()) {
            if (nSMPrimes.contains(s)) {
                return true;
            }
        }
        for (Concept prime : nSMPrimes) {
            if (prime.getSynonyms().contains(concept)) {
                return true;
            }
        }
        return false;
    }

    /**
     * get  the concept of  known concept.
     *
     * @param concept concept to check if it is known.
     * @return boolean indication if the concept is known.
     */
    public static Concept getKnownConcept(Concept concept) {
        assert concept != null;
        return conceptCache.get(concept.getId());
    }

    /**
     * Add a concept to the known concept colleciton. This adds the concept to the conceptCache.
     *
     * @param decompsotedConcept the concept added to the known concepts.
     */
    public static void addKnownConcept(Concept decompsotedConcept) {
        assert decompsotedConcept != null;
        assert decompsotedConcept.getDecompositionlevel() >= 0;
        conceptCache.put(decompsotedConcept);
    }

    /**
     * Adds all given concepts to the list of known concepts.
     *
     * @param concepts2add the list of concepts to add.
     */
    public static void addAllKnownConcepts(HashSet<Concept> concepts2add) {
        for (Concept c : concepts2add) {
            addKnownConcept(c);
        }
    }

    public static List<BaseDictionary> getDictionaries() {
        return dictionaries;
    }

    public static HashSet<Concept> getConcepts2Ignore() {
        if(concepts2Ignore.isEmpty()){
            concepts2Ignore = createConcepts2Ignor();
        }
        return concepts2Ignore;
    }

    /**
     * Decompose the given word of the wordtype.
     *
     * @param word     the word to multiThreadedDecompose
     * @param wordType the wordtype to be used for the look up of this word
     * @return a concept representing the given word and its decomposition.
     */
    public Concept multiThreadedDecompose(String word, WordType wordType, int decompositionDepth) {
        //resetDecomposition();
        logger.info("Decomposing: " + word + " of type: " + wordType.type());
        Concept concept = null;
        if (word != null && !word.equals("")) {
            concept = new Concept(word);
        }else {
            return null;
        }
        assert concept != null;
        concept.setWordType(wordType);
        if (concepts2Ignore.contains(concept)) {
            return concept;
        }
        for (BaseDictionary dictionary : dictionaries) {
            dictionary.flushCache();
        }
        //Create the decomposition recursively until the maximal decomposition depth is found.
        concept = multiThreadedDecompose(concept, decompositionDepth);
        return concept;
    }

    /**
     * Decompose the concept using its definitions and the synonyms. if the decomposition depth is reached, the decomposition is set to null.
     * TODO: Here all other decomposition possibilities should be implemented. (Meronyms are still missing)
     *
     * @param concept            the concept which is subject to decomposition. The result is found in concept.decomposition.
     * @param decompositionDepth integer to specify the depth of the decomposition. If set to -1 infinity is used.
     */
    private Concept multiThreadedDecompose(Concept concept, int decompositionDepth) {
        if (0 < decompositionDepth) {
            if (concepts2Ignore.contains(concept)) {
                return concept;
            }
            if (checkIsPrime(concept)) {
                concept = getPrimeofConcept(concept);
                return concept;
            }
            Concept knownConcept = conceptCache.get(Long.valueOf(concept.hashCode()));
            if (knownConcept.getDecompositionlevel() < 0 || knownConcept.getDecompositionlevel() < decompositionDepth) {
                Concept lock = lockMap.get(concept.hashCode());
                if (lock == null) {
                    //System.out.println("locking: " + concept.getLitheral() + " with " + lockcount + " locks in use.");
                    lock = concept;
                    lockMap.put(concept.hashCode(), concept);
                    //Fill concept: this fills e.g. the synonyms and the definitions
                    for (BaseDictionary dic : dictionaries) {
                        try {
                            concept.setDecompositionlevel(decompositionDepth);
                            dic.fillConcept(concept, concept.getWordType());
                        } catch (DictionaryDoesNotContainConceptException ignored) {
                            //Adding a manual definition because the dictionaries did not contain one.
//                            getManualDefinition(concept);
                        }
                    }
                    DecomposeChildren(concept, decompositionDepth);
                    //Wait for children to be done.
                    for (int i = 0; i < futures.size(); i++) {
                        try {
                            futures.iterator().next().get();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }
                    }

                    concept.setDecompositionlevel(decompositionDepth);
                    addKnownConcept(concept);
                    //synchronized (lock) {
                    lockMap.remove(concept.hashCode());
                    //lock.notifyAll();
                    //System.out.println("Notify: " + concept.getLitheral() + " with " + lockcount + " locks in use.");
                    //}
                    //logger.info("Decomposed: " + concept.getLitheral() + " @ lvl " + decompositionDepth);
                    return concept;
                } else { //concept unknown and we have a lock
                    //waitOnLock(concept, lock);
                    return concept;
                }
            } else { // concept known and we have a no lock
                Concept lock = lockMap.get(knownConcept.hashCode());
                if (lock == null) {
                    return knownConcept;
                } else {
                    //synchronized (lock) {
                    lockMap.remove(knownConcept.hashCode());
                    //lock.notifyAll();
                    // }
                    return knownConcept;
                }
            }

        }
        return concept;
    }

    private void DecomposeChildren(Concept concept, int decompositionDepth) {
        //multiThreadedDecompose the definitions of the concept
        decomposeDefinitions(concept, decompositionDepth);
        //now lets multiThreadedDecompose all synonyms so that the graph gets broader and we can faster find common concepts.
        //TODO maybe we have to ignore the given concept from the synonyms list?
        decomposeChildren(concept.getSynonyms(), decompositionDepth);
        //now lets multiThreadedDecompose all hyponyms so that the graph gets broader and we can faster find common concepts.
        decomposeChildren(concept.getHyponyms(), decompositionDepth);
        //now lets multiThreadedDecompose all hypernyms so that the graph gets broader and we can faster find common concepts.
        decomposeChildren(concept.getHypernyms(), decompositionDepth);
        //now lets multiThreadedDecompose all antonyms so that the graph gets broader and we can faster find common concepts.
        decomposeChildren(concept.getAntonyms(), decompositionDepth);
        //now lets multiThreadedDecompose all meronyms so that the graph gets broader and we can faster find common concepts.
        decomposeChildren(concept.getMeronyms(), decompositionDepth);
        //now lets multiThreadedDecompose all arbitrary relations
        decomposeChildren(concept.getArbitraryRelations(), decompositionDepth);
    }

    private void decomposeDefinitions(Concept concept, int decompositionDepth) {
        for (Definition def2decompose : concept.getDefinitions()) {
            for (Concept def : def2decompose.getDefinition()) {
                if (def.equals(concept) || lockMap.get(def) != null) {
                    continue;
                } else {
                    //System.out.println("Starting thread for concept: " + def.getLitheral());
                    futures.add(startDecompositionThread(def, decompositionDepth - 1));
                    //System.out.println("Got the concept: " + def.getLitheral());
                }
            }
        }
    }


    private void decomposeChildren(Set<Concept> concepts2Decompost, int decompositionDepth) {
        for (Concept child : concepts2Decompost) {
            if (child.equals(concept) || lockMap.get(child) != null) {
                continue;
            } else {
                futures.add(startDecompositionThread(child, decompositionDepth - 1));
            }
        }
    }

    private void decomposeChildrenSingleThreaded(Set<Concept> concepts2Decompost, int decompositionDepth) {
        for (Concept child : concepts2Decompost) {
            if (child.equals(concept) || lockMap.get(child) != null) {
                continue;
            } else {
                decompose(child, decompositionDepth - 1);
            }
        }
    }

    /**
     * Decompose the concept with the given wordtype
     * @param concept the concept to decompose
     * @param wordType the POS of the given concept to use during the decomposition
     * @param decompositionDepth the decomposition depth to which to decompose the concept
     * @return a concept filled with its definition
     */
    public Concept decompose(Concept concept, WordType wordType, int decompositionDepth){
        concept.setWordType(wordType);
        return decompose(concept,decompositionDepth);
    }

    /**
     * Decompose the concept with the given wordtype
     * @param word the word to decompose
     * @param wordType the POS of the given concept to use during the decomposition
     * @param decompositionDepth the decomposition depth to which to decompose the concept
     * @return a concept filled with its definition
     */
    public Concept decompose(String word, WordType wordType, int decompositionDepth){
        Concept concept = new Concept(word);
        concept.setWordType(wordType);
        if(dictionaries.size()<1){
            init();
        }
        return decompose(concept,decompositionDepth);
    }



    public Concept decompose(Concept concept, int decompositionDepth) {
        if (0 < decompositionDepth) {
            if (concepts2Ignore.contains(concept)) {
                return concept;
            }
            if (checkIsPrime(concept)) {
                concept = getPrimeofConcept(concept);
                return concept;
            }
            Concept knownConcept = conceptCache.get(Long.valueOf(concept.hashCode()));
            if (knownConcept.getDecompositionlevel() < 0 || knownConcept.getDecompositionlevel() < decompositionDepth) {
                Concept lock = lockMap.get(concept.hashCode());
                if (lock == null) {
                    //System.out.println("locking: " + concept.getLitheral() + " with " + lockcount + " locks in use.");
                    lock = concept;
                    lockMap.put(concept.hashCode(), concept);
                    //Fill concept: this fills e.g. the synonyms and the definitions
                    for (BaseDictionary dic : dictionaries) {
                        try {
                            concept.setDecompositionlevel(decompositionDepth);
                            dic.fillConcept(concept, concept.getWordType());
                        } catch (DictionaryDoesNotContainConceptException ignored) {
                            //Adding a manual definition because the dictionaries did not contain one.
                            //concept = getManualDefinition(concept);
                        }
                    }
                    decomposeChildrenSingleThreaded(concept, decompositionDepth);
                    concept.setDecompositionlevel(decompositionDepth);
                    addKnownConcept(concept);
                    lockMap.remove(concept.hashCode());
                    return concept;
                } else { //concept unknown and we have a lock
                    return concept;
                }
            } else { // concept known and we have a no lock
                Concept lock = lockMap.get(knownConcept.hashCode());
                if (lock == null) {
                    return knownConcept;
                } else {
                    lockMap.remove(knownConcept.hashCode());
                    return knownConcept;
                }
            }

        }
        return concept;
    }

    /**
     * Fill a definitions of a concept manually. This is recursive, which is not perfect. TODO: change the recursion.
     *
     * @param concept the concept to get a definitions for.
     * @return the given concept with an definitions.
     */
   /* private Concept getManualDefinition(Concept concept) {
        ManualDecompositionGUI manualDecompositionGUI = new ManualDecompositionGUI(concept.getLitheral());

        Delegate delegate = new Delegate();
        delegate.setDecomposition(this);

        if (concept.getDefinitions() != null && concept.getDefinitions().size() == 1) {
            boolean cycle = delegate.checkCycle(concept, concept.getDefinitions().iterator().next(), 3);
            if (cycle) {
                concept = delegate.getManualDefinition(concept);
            }
        }
        if (concept.getDefinitions() != null && concept.getDefinitions().size() > 1) {
            concept = delegate.showSelectDefinition(concept);
        }

        if (concept.getDefinitions() == null || concept.getDefinitions().size() < 1) {
            concept = delegate.getManualDefinition(concept);
        }
        return concept;
    }
*/
    /**
     * Decompose the concept which are connceted to the given concept in an single threaded environment.
     * @param concept the concept to decompose the children for.
     * @param decompositionDepth the decomposition depth to decompose the children with
     */
    private void decomposeChildrenSingleThreaded(Concept concept, int decompositionDepth) {
        //SingleThreadedDecompose the definitions of the concept
        DecomposeDefinitionSingleThreaded(concept, decompositionDepth);
        //now lets multiThreadedDecompose all synonyms so that the graph gets broader and we can faster find common concepts.
        //TODO maybe we have to ignore the given concept from the synonyms list?
        decomposeChildrenSingleThreaded(concept.getSynonyms(), decompositionDepth);
        //now lets multiThreadedDecompose all hyponyms so that the graph gets broader and we can faster find common concepts.
        decomposeChildrenSingleThreaded(concept.getHyponyms(), decompositionDepth);
        //now lets multiThreadedDecompose all hypernyms so that the graph gets broader and we can faster find common concepts.
        decomposeChildrenSingleThreaded(concept.getHypernyms(), decompositionDepth);
        //now lets multiThreadedDecompose all antonyms so that the graph gets broader and we can faster find common concepts.
        decomposeChildrenSingleThreaded(concept.getAntonyms(), decompositionDepth);
        //now lets multiThreadedDecompose all meronyms so that the graph gets broader and we can faster find common concepts.
        decomposeChildrenSingleThreaded(concept.getMeronyms(), decompositionDepth);
        //now lets multiThreadedDecompose all arbitrary relations
        decomposeChildrenSingleThreaded(concept.getArbitraryRelations(), decompositionDepth);
    }
    /**
     * Decompose the concept which make up the definition of the given concept in an single threaded environment.
     * @param concept the concept to decompose the children for.
     * @param decompositionDepth the decomposition depth to decompose the children with
     */
    private void DecomposeDefinitionSingleThreaded(Concept concept, int decompositionDepth) {
        for (Definition def2decompose : concept.getDefinitions()) {
            for (Concept def : def2decompose.getDefinition()) {
                if (def.equals(concept) || lockMap.get(def) != null) {
                    continue;
                } else {
                    //System.out.println("Starting thread for concept: " + def.getLitheral());
                    decompose(def, decompositionDepth - 1);
                    //System.out.println("Got the concept: " + def.getLitheral());
                }
            }
        }
    }

    /**
     * Start a future task for the decomposition of the given concept with the given depth.
     * @param concept2decompose the concept to decompose
     * @param decompositionDepth the depth to decompose the concept in. This should be greater then 0.
     * @return the future which contains the decomposition task.
     */
    private Future<Concept> startDecompositionThread(Concept concept2decompose, int decompositionDepth) {

        Future<Concept> future = cachedThreadPool.submit(new Callable<Concept>() {
            @Override
            public Concept call() {
                //System.out.println("Execute Call for concept: " + concept2decompose.getLitheral());
                return decompose(concept2decompose, decompositionDepth - 1);
            }
        });
        return future;
    }

    /**
     * In an concurrent implementation of the decomposition it is necessarry to wait on decompositions which finish concepts we
     * want to use.
     *
     * @param concept the concept we want to wait on.
     * @param lock    the lock object which gets the notify, we are waiting for.
     */
    private void waitOnLock(Concept concept, Object lock) {
        synchronized (lock) {
            try {
                lockcount++;
                //System.out.println("Wait: " + concept.getLitheral() + " with " + lockcount + " locks in use.");
                lock.wait();
                lockcount--;
                //System.out.println("Continuing: " + concept.getLitheral() + " with " + lockcount + " locks in use.");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /*
    Printing method for manual analysis of the correctness of the decomposition.
     */
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("==============================").append(System.getProperty("line.separator"));
        stringBuilder.append(concept.getLitheral()).append(":").append(System.getProperty("line.separator"));

        if (concept.getDefinitions() != null) {
            stringBuilder.append(" " + "Definition: ").append(concept.getDefinitions().toString()).append(System.getProperty("line.separator"));
        } else {
            stringBuilder.append(" " + "Definition: No Definition").append(System.getProperty("line.separator"));
        }
        if (concept.getDecomposition() != null) {
            stringBuilder.append(" " + "Decomposition: ").append(concept.getDecomposition().toString()).append(System.getProperty("line.separator"));
            for (Concept decomp : concept.getDecomposition()) {
                if (decomp != null) {
                    stringBuilder.append(printDecompositionRecursivly(decomp, ""));
                }
            }
        } else {
            stringBuilder.append(" " + "Decomposition: No Decomposition ").append(System.getProperty("line.separator"));
        }
        stringBuilder.append("==============================").append(System.getProperty("line.separator"));
        return stringBuilder.toString();
    }

    /**
     * Print the decomposition in the terminal. This has been done to evaluate the results of the decomposition manually.
     *
     * @param concept The concept which decompostion we want to see.
     * @param spacer  the recursivly used spacer to indicat one level of decomposition to another.
     * @return the decomposition as a kind of tree view.
     */
    private String printDecompositionRecursivly(Concept concept, String spacer) {
        spacer = spacer + "----";
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(spacer).append(concept.getLitheral()).append(":").append(System.getProperty("line.separator"));
        if (concept.getDecomposition() != null) {
            stringBuilder.append("|").append(spacer).append(" Decomposition: ").append(concept.getDecomposition().toString()).append(System.getProperty("line.separator"));
            for (Concept decomp : concept.getDecomposition()) {
                if (decomp != null) {
                    stringBuilder.append("|").append(spacer).append("-").append("Decomposition: ").append(decomp.getLitheral()).append(System.getProperty("line.separator"));
                    stringBuilder.append(spacer).append(printDecompositionRecursivly(decomp, spacer));
                } else {
                    stringBuilder.append("|").append(spacer).append("null").append(System.getProperty("line.separator"));
                }
            }
        } else {
            stringBuilder.append("|").append(spacer).append(" ").append("Decomposition: No Decomposition ").append(System.getProperty("line.separator"));
        }
        return stringBuilder.toString();
    }

}
