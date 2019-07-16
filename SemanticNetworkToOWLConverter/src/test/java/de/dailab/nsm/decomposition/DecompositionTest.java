package de.dailab.nsm.decomposition;

import de.dailab.nsm.decomposition.Dictionaries.customDictionary.CustomGraph;
import de.dailab.nsm.decomposition.dictionaries.wiktionary.WiktionaryCrawler;
import de.dailab.nsm.decomposition.exceptions.DictionaryDoesNotContainConceptException;
import de.tudarmstadt.ukp.jwktl.api.IWiktionaryEdition;
import de.tudarmstadt.ukp.jwktl.api.IWiktionaryEntry;
import de.tudarmstadt.ukp.jwktl.api.filter.WiktionaryEntryFilter;
import de.tudarmstadt.ukp.jwktl.api.util.IWiktionaryIterator;
import de.tudarmstadt.ukp.jwktl.api.util.Language;
import org.junit.Test;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;


public class DecompositionTest extends Decomposition {

    String[] words = new String[]{"dog", "cat", "rain"};
    String connectedWord = "pet";

    private Instant lastInstant = null;

    @Test
    public void decomposeStatic() {
        logTime("start decomposeStatic");
        //Decompose "words" with the empty cache
        CustomGraph customGraph = new CustomGraph(true);
        Decomposition.customGraph = customGraph;

        Decomposition.disableCache(true);
        Decomposition decomposition = new Decomposition();


        Set<Concept> concepts = new HashSet<>();

        for (String word : words) {
            Concept concept = decomposition.decompose(word, WordType.UNKNOWN, 1);
            concepts.add(concept);
        }

        //did it work?
        //is it stored in the cache?

        for (Concept concept : concepts) {
            try {
                Concept loaded = Decomposition.customGraph.getConceptForWordAndType(concept.getLitheral(), concept.getWordType());
                //it has to be literally the same object, not just the same litheral
                assert loaded == concept;
            }
            catch (DictionaryDoesNotContainConceptException ex) {
                assert false;
            }

        }
        logTime("end decomposeStatic");
    }

    @Test
    public void compareDecompWithCache() {
        logTime("start compareDecompWithCache");
        //Decompose "words" with depth 1-3 (add them to the cache)
        // and then decompose the same words, while using the cache

        //Decompose "words" with the empty cache
        CustomGraph customGraph = new CustomGraph(true);
        Decomposition.customGraph = customGraph;

        Decomposition.disableCache(false);
        Decomposition decomposition = new Decomposition();
        logTime("start decomping");
        for (int i = 1; i < 3; i++) {

            for (String word : words) {
                logTime("start decomp " + i);
                Concept concept1 = decomposition.decompose(word, WordType.UNKNOWN, i);
                logTime("start 2nd decomp " + i);
                Concept concept2 = decomposition.decompose(word, WordType.UNKNOWN, i);
                logTime("finished decomp " + i);
                assert concept1 == concept2;
            }
        }
        logTime("end decomp");
        //compare the two results, are they the same?
        //compare the speed, and possibly memory usage
    }

    @Test
    public void reuseConcept() {
        logTime("start reuseConcept");
        //Decompose the connectedWord without using the cache
        //decompose words with the cache
        //decompose the connectedWord with the cache

        CustomGraph customGraph = new CustomGraph(true);
        Decomposition.customGraph = customGraph;

        Decomposition.disableCache(true);
        Decomposition decomposition = new Decomposition();

        logTime("start decomp connectedWord 1");
        Concept connectedWord1 = decomposition.decompose(connectedWord, WordType.UNKNOWN, 1);
        logTime("finished decomp connectedWord 1");

        Decomposition.disableCache(false);

        for (String word : words) {
            Concept concept1 = decomposition.decompose(word, WordType.UNKNOWN, 1);
        }

        logTime("start decomp connectedWord 2");
        Concept connectedWord2 = decomposition.decompose(connectedWord, WordType.UNKNOWN, 1);
        logTime("finished decomp connectedWord 2");

        assert connectedWord1 == connectedWord2;

        logTime("end reuseConcept");

        //compare the two results for the connectedWord, are they the same?
        //compare the speed, and possibly memory usage
    }

    @Test
    public void decomposeALot() {
        logTime("start decomposeALot");
        //generate? a list of 100 words
        //Decompose them with the cache enabled
        //Decompose them again

        IWiktionaryEdition wkt = WiktionaryCrawler.wkt;
        WiktionaryEntryFilter filter = new WiktionaryEntryFilter();
        filter.setAllowedWordLanguages(Language.ENGLISH);

        IWiktionaryIterator it = wkt.getAllEntries(filter);

        int i = 0;
        Set<String> words100 = new HashSet<>();

        //it.forEach(o -> {
        while (it.hasNext() && i++ < 100) {
            IWiktionaryEntry entry = (IWiktionaryEntry) it.next();
            words100.add(entry.getWord());
        }


        CustomGraph customGraph = new CustomGraph(true);
        Decomposition.customGraph = customGraph;

        Decomposition.disableCache(true);
        Decomposition decomposition = new Decomposition();

        logTime("Decomposing 100 start 1");
        for (String word : words100) {
            Concept concept1 = decomposition.decompose(word, WordType.UNKNOWN, 1);
        }
        logTime("Decomposing 100 end 1");

        Decomposition.disableCache(false);

        logTime("Decomposing 100 start 2");
        for (String word : words100) {
            Concept concept1 = decomposition.decompose(word, WordType.UNKNOWN, 1);
        }
        logTime("Decomposing 100 end 2");

        logTime("end decomposeALot");
        //compare the two results, the number of nodes/edges, and the speed and memory usage
    }
    
    @Test
    public void repeadetlyDecompose() {
        logTime("start repeadetlyDecompose");
        //decompose the connectedWord with cache disabled 100 times
        //decompose the connectedWord with cache enabled 100 times

        CustomGraph customGraph = new CustomGraph(true);
        Decomposition.customGraph = customGraph;

        Decomposition.disableCache(true);
        Decomposition decomposition = new Decomposition();

        logTime("start 100 repetition 1");
        for (int i = 0; i < 100; i++) {
            Concept concept1 = decomposition.decompose(connectedWord, WordType.UNKNOWN, 1);
        }
        logTime("end 100 repetition 1");

        Decomposition.disableCache(false);

        logTime("start 100 repetition 2");
        for (int i = 0; i < 100; i++) {
            Concept concept1 = decomposition.decompose(connectedWord, WordType.UNKNOWN, 1);
        }
        logTime("end 100 repetition 2");

        logTime("end repeadetlyDecompose");
        //compare the speed, and possibly memory usage
    }

    @Test
    public void saveAndLoad() {
        logTime("start saveAndLoad");
        //generate? a list of 100 words
        //decompose 100 words with the cache enabled
        //store the graph
        //load the graph
        //decompose the same 100 words with the cache enabled


        IWiktionaryEdition wkt = WiktionaryCrawler.wkt;
        WiktionaryEntryFilter filter = new WiktionaryEntryFilter();
        filter.setAllowedWordLanguages(Language.ENGLISH);

        IWiktionaryIterator it = wkt.getAllEntries(filter);

        int i = 0;
        Set<String> words100 = new HashSet<>();

        //it.forEach(o -> {
        while (it.hasNext() && i++ < 100) {
            IWiktionaryEntry entry = (IWiktionaryEntry) it.next();
            words100.add(entry.getWord());
        }

        //Delete the file first? cause this aint repeatable...
        logTime("Creating graph");
        CustomGraph customGraph1 = new CustomGraph(false);
        logTime("Created graph");

        Decomposition.customGraph = customGraph1;

        Decomposition.disableCache(false);
        Decomposition decomposition1 = new Decomposition();


        logTime("decomposing 100 to save");
        for (String word : words100) {
            Concept concept1 = decomposition1.decompose(word, WordType.UNKNOWN, 1);
        }
        logTime("decomposed 100 to save");

        customGraph1.save();
        logTime("saved");

        logTime("Creating graph");
        CustomGraph customGraph2 = new CustomGraph(false);
        logTime("Created graph");

        Decomposition.customGraph = customGraph1;

        Decomposition.disableCache(false);
        Decomposition decomposition2 = new Decomposition();


        logTime("decomposing 100 loaded");
        for (String word : words100) {
            Concept concept1 = decomposition2.decompose(word, WordType.UNKNOWN, 1);
        }
        logTime("decomposed 100 loaded");

        logTime("end saveAndLoad");
        //compare the speed, and possibly memory usage
        // note the time it took to save and load
    }

    @Test
    public void generateConnectionCount() {
        logTime("start generateConnectionCount");
        //generate? a list of 100 words
        //decompose 100 words with the cache enabled

        IWiktionaryEdition wkt = WiktionaryCrawler.wkt;
        WiktionaryEntryFilter filter = new WiktionaryEntryFilter();
        filter.setAllowedWordLanguages(Language.ENGLISH);

        IWiktionaryIterator it = wkt.getAllEntries(filter);

        int i = 0;
        Set<String> words100 = new HashSet<>();

        //it.forEach(o -> {
        while (it.hasNext() && i++ < 100) {
            IWiktionaryEntry entry = (IWiktionaryEntry) it.next();
            words100.add(entry.getWord());
        }

        CustomGraph customGraph1 = new CustomGraph(false);
        Decomposition.customGraph = customGraph1;

        Decomposition.disableCache(false);
        Decomposition decomposition = new Decomposition();

        int total = 0;

        for (String word : words100) {
            Concept concept = decomposition.decompose(word, WordType.UNKNOWN, 1);
            total += concept.getDecompositionElementCount();
        }

        logTime("end generateConnectionCount");
        //total contains the total, counting duplicates, total / 100 is the average

        //count the number of connections on each word
    }

    private void logTime(String comment) {
        System.out.println(comment);
        logTime();
    }
    private void logTime() {
        Instant instant = Instant.now();
        System.out.println("This instant: " + instant);
        if(lastInstant != null) {
            System.out.println("Diff to last: " + instant.compareTo(lastInstant));
        }
        lastInstant = instant;

    }

}