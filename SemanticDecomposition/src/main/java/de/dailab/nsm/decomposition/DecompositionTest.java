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
import org.junit.After;
import org.junit.Before;


import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;


public class DecompositionTest extends Decomposition {

    String[] words = new String[]{"dog", "cat", "rain"};
    String connectedWord = "pet";

    private Instant lastInstant = null;
    private Set<String> _words100 = null;

    private final int maxDecompDepth = 1;

    enum Stats {
        INIT,
        GET_100_WORDS,
        DECOMPOSE_STATIC,
        DECOMPOSE_STATIC_FROM_DICT,
        DECOMPOSE_STATIC_FROM_CACHE,
        DECOMPOSE_WITH_CACHE,
        DECOMPOSE_WITH_CACHE_FIRST,
        DECOMPOSE_WITH_CACHE_SECOND,
        DECOMPOSE_CONNECTED,
        DECOMPOSE_CONNECTED_FROM_DICT,
        DECOMPOSE_CONNECTED_WITH_CACHE,
        DECOMPOSE_A_LOT,
        DECOMPOSE_A_LOT_FROM_DICT,
        DECOMPOSE_A_LOT_WITH_CACHE,
        DECOMPOSE_REPEATEDLY,
        DECOMPOSE_REPEATEDLY_FROM_DICT,
        DECOMPOSE_REPEATEDLY_WITH_CACHE,
        SAVE,
        SAVE_DECOMPOSE_FIRST,
        SAVE_DECOMPOSE_SECOND,
        SAVE_WRITE_FILE,
        SAVE_CREATE_GRAPH_EMPTY,
        SAVE_CREATE_GRAPH_FILLED,
    }

    @Test
    public void initDicts() {
        logStat(Stats.INIT, 0, "start dict init");
        this.init();
        logStat(Stats.INIT, 0, "end dict init");

        logStat(Stats.GET_100_WORDS, 0, "getting list");
        get100Words();
        logStat(Stats.GET_100_WORDS, 0, "got list");
    }

    @Test
    public void decomposeStatic() {
        //Decompose "words" with the empty cache

        //did it work?
        //is it stored in the cache?
        System.out.println("start decomposeStatic");

        CustomGraph.disableTests = true;
        CustomGraph customGraph = new CustomGraph(true);
        Decomposition.customGraph = customGraph;

        Decomposition.disableCache(false);
        Decomposition decomposition = new Decomposition();


        Set<Concept> concepts = new HashSet<>();

        System.out.println("Starting first decomposition");
        int i = 0;
        for (String word : words) {
            logStat(Stats.DECOMPOSE_STATIC_FROM_DICT, i, "start decomp");
            Concept concept = decomposition.decompose(word, WordType.UNKNOWN, 1);
            logStat(Stats.DECOMPOSE_STATIC_FROM_DICT, i, "finished decomp");
            i++;
            concepts.add(concept);
        }
        System.out.println("Finished first decomposition");

        i=0;
        System.out.println("Started second decomposition");
        for (Concept concept : concepts) {
            try {
                logStat(Stats.DECOMPOSE_STATIC_FROM_CACHE, i, "start lookup");
                Concept loaded = Decomposition.customGraph.getConceptForWordAndType(concept.getLitheral(), concept.getWordType());
                logStat(Stats.DECOMPOSE_STATIC_FROM_CACHE, i, "finished lookup");
                //it has to be literally the same object, not just the same litheral
                assert loaded.equals(concept);
            }
            catch (DictionaryDoesNotContainConceptException ex) {
                assert false;
            }
        }
        System.out.println("Finished second decomposition");

        System.out.println("end decomposeStatic");
    }

    @Test
    public void compareDecompWithCache() {
        System.out.println("start compareDecompWithCache");
        //Decompose "words" with depth 1-3 (add them to the cache)
        // and then decompose the same words, while using the cache

        //compare the two results, are they the same?
        //compare the speed, and possibly memory usage

        CustomGraph.disableTests = true;
        CustomGraph customGraph = new CustomGraph(true);
        Decomposition.customGraph = customGraph;

        Decomposition.disableCache(false);
        Decomposition decomposition = new Decomposition();
        System.out.println("start decomping");
        for (int i = 1; i <= maxDecompDepth; i++) {
            int j = 0;
            logStat(Stats.DECOMPOSE_WITH_CACHE, i, "start loop");
            for (String word : words) {
                logStat(Stats.DECOMPOSE_WITH_CACHE_FIRST, j, "start first decomposing");
                Concept concept1 = decomposition.decompose(word, WordType.UNKNOWN, i);
                logStat(Stats.DECOMPOSE_WITH_CACHE_FIRST, j, "finished first decomposing");
                logStat(Stats.DECOMPOSE_WITH_CACHE_SECOND, j, "start first decomposing");
                Concept concept2 = decomposition.decompose(word, WordType.UNKNOWN, i);
                logStat(Stats.DECOMPOSE_WITH_CACHE_SECOND, j, "finished first decomposing");
                j++;
                assert concept1.equals(concept2);
            }
            logStat(Stats.DECOMPOSE_WITH_CACHE, i, "finished loop");
        }
        System.out.println("end decomp");
    }

    @Test
    public void reuseConcept() {
        System.out.println("start reuseConcept");
        //Decompose the connectedWord without using the cache
        //decompose words with the cache
        //decompose the connectedWord with the cache

        //compare the two results for the connectedWord, are they the same?
        //compare the speed, and possibly memory usage

        CustomGraph.disableTests = true;
        CustomGraph customGraph = new CustomGraph(true);
        Decomposition.customGraph = customGraph;

        Decomposition.disableCache(true);
        Decomposition decomposition = new Decomposition();

        logStat(Stats.DECOMPOSE_CONNECTED_FROM_DICT,0 ,"start decomp connectedWord 1");
        Concept connectedWord1 = decomposition.decompose(connectedWord, WordType.UNKNOWN, 2);
        logStat(Stats.DECOMPOSE_CONNECTED_FROM_DICT,1,"end decomp connectedWord 1");

        Decomposition.disableCache(false);

        for (String word : words) {
            Concept concept1 = decomposition.decompose(word, WordType.UNKNOWN, 1);
        }

        logStat(Stats.DECOMPOSE_CONNECTED_WITH_CACHE,0 ,"start decomp connectedWord 2");
        Concept connectedWord2 = decomposition.decompose(connectedWord, WordType.UNKNOWN, 2);
        logStat(Stats.DECOMPOSE_CONNECTED_WITH_CACHE,1 ,"finished decomp connectedWord 2");

        assert connectedWord1.equals(connectedWord2);

        System.out.println("end reuseConcept");
    }

    @Test
    public void decomposeALot() {
        System.out.println("start decomposeALot");
        //generate? a list of 100 words
        //Decompose them with the cache enabled
        //Decompose them again

        //compare the two results, the number of nodes/edges, and the speed and memory usage

        Set<String> words100 = get100Words();

        CustomGraph.disableTests = true;
        CustomGraph customGraph = new CustomGraph(true);
        Decomposition.customGraph = customGraph;

        Decomposition.disableCache(true);
        Decomposition decomposition = new Decomposition();

        int i = 0;
        System.out.println("Decomposing 100 start 1");
        for (String word : words100) {
            logStat(Stats.DECOMPOSE_A_LOT_FROM_DICT, i, "start decomp");
            Concept concept1 = decomposition.decompose(word, WordType.UNKNOWN, 1);
            logStat(Stats.DECOMPOSE_A_LOT_FROM_DICT, i, "finished decomp");
            i++;
        }
        System.out.println("Decomposing 100 end 1");

        Decomposition.disableCache(false);

        i = 0;
        System.out.println("Decomposing 100 start 2");
        for (String word : words100) {
            logStat(Stats.DECOMPOSE_A_LOT_WITH_CACHE, i, "start decomp");
            Concept concept1 = decomposition.decompose(word, WordType.UNKNOWN, 1);
            logStat(Stats.DECOMPOSE_A_LOT_WITH_CACHE, i, "finished decomp");
            i++;
        }
        System.out.println("Decomposing 100 end 2");

        System.out.println("end decomposeALot");
    }

    @Test
    public void repeadetlyDecompose() {
        System.out.println("start repeadetlyDecompose");
        //decompose the connectedWord with cache disabled 100 times
        //decompose the connectedWord with cache enabled 100 times

        //compare the speed, and possibly memory usage

        CustomGraph.disableTests = true;
        CustomGraph customGraph = new CustomGraph(true);
        Decomposition.customGraph = customGraph;

        Decomposition.disableCache(true);
        Decomposition decomposition = new Decomposition();

        System.out.println("start 100 repetition 1");
        for (int i = 0; i < 100; i++) {
            logStat(Stats.DECOMPOSE_REPEATEDLY_FROM_DICT, i ,"start decomp");
            Concept concept1 = decomposition.decompose(connectedWord, WordType.UNKNOWN, 1);
            logStat(Stats.DECOMPOSE_REPEATEDLY_FROM_DICT, i ,"finished decomp");
        }
        System.out.println("end 100 repetition 1");

        Decomposition.disableCache(false);

        System.out.println("start 100 repetition 2");
        for (int i = 0; i < 100; i++) {
            logStat(Stats.DECOMPOSE_REPEATEDLY_WITH_CACHE, i ,"start decomp");
            Concept concept1 = decomposition.decompose(connectedWord, WordType.UNKNOWN, 1);
            logStat(Stats.DECOMPOSE_REPEATEDLY_WITH_CACHE, i ,"finished decomp");
        }
        System.out.println("end 100 repetition 2");

        System.out.println("end repeadetlyDecompose");
    }

    @Test
    public void saveAndLoad() {
        System.out.println("start saveAndLoad");
        //generate? a list of 100 words
        //decompose 100 words with the cache enabled
        //store the graph
        //load the graph
        //decompose the same 100 words with the cache enabled

        //compare the speed, and possibly memory usage
        // note the time it took to save and load

        Set<String> words100 = get100Words();

        CustomGraph.disableTests = true;

        CustomGraph.deleteArchive();

        logStat(Stats.SAVE_CREATE_GRAPH_EMPTY, 0, "Creating graph");
        CustomGraph customGraph1 = new CustomGraph(false);
        logStat(Stats.SAVE_CREATE_GRAPH_EMPTY, 0, "Created graph");

        Decomposition.customGraph = customGraph1;

        Decomposition.disableCache(false);
        Decomposition decomposition1 = new Decomposition();

        int i = 0;
        System.out.println("decomposing 100 to save");
        for (String word : words100) {
            logStat(Stats.SAVE_DECOMPOSE_FIRST, i, "start decomposing");
            Concept concept1 = decomposition1.decompose(word, WordType.UNKNOWN, 1);
            logStat(Stats.SAVE_DECOMPOSE_FIRST, i, "finished decomposing");
            i++;
        }
        System.out.println("decomposed 100 to save");

        logStat(Stats.SAVE_WRITE_FILE, 0, "starting to write file");
        customGraph1.save();
        logStat(Stats.SAVE_WRITE_FILE, 0, "finished to write file");


        logStat(Stats.SAVE_CREATE_GRAPH_EMPTY, 0, "Creating graph");
        CustomGraph customGraph2 = new CustomGraph(false);
        logStat(Stats.SAVE_CREATE_GRAPH_EMPTY, 0, "Created graph");

        Decomposition.customGraph = customGraph2;

        Decomposition.disableCache(false);
        Decomposition decomposition2 = new Decomposition();


        i = 0;
        System.out.println("decomposing 100 loaded");
        for (String word : words100) {
            logStat(Stats.SAVE_DECOMPOSE_SECOND, i, "start decomposing");
            Concept concept1 = decomposition2.decompose(word, WordType.UNKNOWN, 1);
            logStat(Stats.SAVE_DECOMPOSE_SECOND, i, "finished decomposing");
            i++;
        }
        System.out.println("decomposed 100 loaded");

        System.out.println("end saveAndLoad");
    }

    @Test
    public void generateConnectionCount() {
        System.out.println("start generateConnectionCount");
        //generate? a list of 100 words
        //decompose 100 words with the cache enabled

        //count the number of connections on each word

        Set<String> words100 = get100Words();

        CustomGraph.disableTests = true;
        CustomGraph customGraph1 = new CustomGraph(false);
        Decomposition.customGraph = customGraph1;

        Decomposition.disableCache(false);
        Decomposition decomposition = new Decomposition();

        int total = 0;

        for (String word : words100) {
            Concept concept = decomposition.decompose(word, WordType.UNKNOWN, 1);
            total += concept.getDecompositionElementCount();
        }

        //total contains the total, counting duplicates, total / 100 is the average
        System.out.println("Total: " + total);

        System.out.println("end generateConnectionCount");

    }

    private void logStat(Stats stat, int number, String message) {
        synchronized (this) {
            Instant instant = Instant.now();
            System.out.println(stat + "(" + number + "): " + instant + "(" + message + ")");
            if(lastInstant != null) {
                long distance = Duration.between(lastInstant, instant).toNanos();
                System.out.println("Diff to last: " + distance+"ns");
            }
            lastInstant = instant;
        }
    }

    private Set<String> get100Words() {
        if(_words100 == null) {
            _words100 = new HashSet<>();
            IWiktionaryEdition wkt = WiktionaryCrawler.wkt;
            WiktionaryEntryFilter filter = new WiktionaryEntryFilter();
            filter.setAllowedWordLanguages(Language.ENGLISH);

            IWiktionaryIterator it = wkt.getAllEntries(filter);

            int i = 0;

            while (it.hasNext() && i++ < 100) {
                IWiktionaryEntry entry = (IWiktionaryEntry) it.next();
                _words100.add(entry.getWord());
            }

        }
        return _words100;
    }
}