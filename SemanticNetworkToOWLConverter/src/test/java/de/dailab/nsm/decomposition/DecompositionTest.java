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

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;


/*
Edelstein -/> (1675) Diamant
Edelstein -> Stein -> Diamant -> Edelstein
Edelstein -> Stein -> Dichte -> Diamant -> Edelstein

Fixe Worte decomp, nochmal schneller?
Verzweigt laden,
wieviel knoten, kanten hat es

mit vs ohne
Zeit/ speicher

jprofiler

wortbedeutungen "zählen"


 */


public class DecompositionTest extends Decomposition {

    String[] words = new String[]{"dog", "cat", "rain"};
    String connectedWord = "pet";



    @Test
    public void decomposeStatic() {
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
    }

    @Test
    public void compareDecompWithCache() {
        //Decompose "words" with depth 1-3 (add them to the cache)
        // and then decompose the same words, while using the cache

        //Decompose "words" with the empty cache
        CustomGraph customGraph = new CustomGraph(true);
        Decomposition.customGraph = customGraph;

        Decomposition.disableCache(false);
        Decomposition decomposition = new Decomposition();

        for (int i = 1; i < 3; i++) {

            for (String word : words) {
                Concept concept1 = decomposition.decompose(word, WordType.UNKNOWN, i);
                Concept concept2 = decomposition.decompose(word, WordType.UNKNOWN, i);
                assert concept1 == concept2;
            }
        }
        //compare the two results, are they the same?
        //compare the speed, and possibly memory usage
    }

    @Test
    public void reuseConcept() {
        //Decompose the connectedWord without using the cache
        //decompose words with the cache
        //decompose the connectedWord with the cache

        CustomGraph customGraph = new CustomGraph(true);
        Decomposition.customGraph = customGraph;

        Decomposition.disableCache(true);
        Decomposition decomposition = new Decomposition();

        Concept connectedWord1 = decomposition.decompose(connectedWord, WordType.UNKNOWN, 1);

        Decomposition.disableCache(false);

        for (String word : words) {
            Concept concept1 = decomposition.decompose(word, WordType.UNKNOWN, 1);
        }

        Concept connectedWord2 = decomposition.decompose(connectedWord, WordType.UNKNOWN, 1);

        assert connectedWord1 == connectedWord2;

        //compare the two results for the connectedWord, are they the same?
        //compare the speed, and possibly memory usage
    }

    @Test
    public void decomposeALot() {
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


        for (String word : words100) {
            Concept concept1 = decomposition.decompose(word, WordType.UNKNOWN, 1);
        }

        Decomposition.disableCache(false);

        for (String word : words100) {
            Concept concept1 = decomposition.decompose(word, WordType.UNKNOWN, 1);
        }

        //compare the two results, the number of nodes/edges, and the speed and memory usage
    }
    
    @Test
    public void repeadetlyDecompose() {
        //decompose the connectedWord with cache disabled 100 times
        //decompose the connectedWord with cache enabled 100 times

        CustomGraph customGraph = new CustomGraph(true);
        Decomposition.customGraph = customGraph;

        Decomposition.disableCache(true);
        Decomposition decomposition = new Decomposition();


        for (int i = 0; i < 100; i++) {
            Concept concept1 = decomposition.decompose(connectedWord, WordType.UNKNOWN, 1);
        }

        Decomposition.disableCache(false);

        for (int i = 0; i < 100; i++) {
            Concept concept1 = decomposition.decompose(connectedWord, WordType.UNKNOWN, 1);
        }


        //compare the speed, and possibly memory usage
    }

    @Test
    public void saveAndLoad() {
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
        CustomGraph customGraph1 = new CustomGraph(false);
        Decomposition.customGraph = customGraph1;

        Decomposition.disableCache(false);
        Decomposition decomposition1 = new Decomposition();


        for (String word : words100) {
            Concept concept1 = decomposition1.decompose(word, WordType.UNKNOWN, 1);
        }

        customGraph1.save();

        CustomGraph customGraph2 = new CustomGraph(false);
        Decomposition.customGraph = customGraph1;

        Decomposition.disableCache(false);
        Decomposition decomposition2 = new Decomposition();


        for (String word : words100) {
            Concept concept1 = decomposition2.decompose(word, WordType.UNKNOWN, 1);
        }

        //compare the speed, and possibly memory usage
        // note the time it took to save and load
    }

    @Test
    public void generateConnectionCount() {
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

        //total contains the total, counting duplicates, total / 100 is the average

        //count the number of connections on each word

    }

}