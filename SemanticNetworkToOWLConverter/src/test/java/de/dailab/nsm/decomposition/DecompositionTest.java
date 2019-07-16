package de.dailab.nsm.decomposition;

import org.junit.Test;

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

        //did it work?
        //is it stored in the cache?
    }

    @Test
    public void compareDecompWithCache() {
        //Decompose "words" with depth 1-3 (add them to the cache)
        // and then decompose the same words, while using the cache

        //compare the two results, are they the same?
        //compare the speed, and possibly memory usage
    }

    @Test
    public void reuseConcept() {
        //Decompose the connectedWord without using the cache
        //decompose words with the cache
        //decompose the connectedWord with the cache

        //compare the two results for the connectedWord, are they the same?
        //compare the speed, and possibly memory usage
    }

    @Test
    public void decomposeALot() {
        //generate? a list of 100 words
        //Decompose them with the cache enabled
        //Decompose them again

        //compare the two results, the number of nodes/edges, and the speed and memory usage
    }
    
    @Test
    public void repeadetlyDecompose() {
        //decompose the connectedWord with cache disabled 100 times
        //decompose the connectedWord with cache enabled 100 times

        //compare the speed, and possibly memory usage
    }

    @Test
    public void saveAndLoad() {
        //generate? a list of 100 words
        //decompose 100 words with the cache enabled
        //store the graph
        //load the graph
        //decompose the same 100 words with the cache enabled

        //compare the speed, and possibly memory usage
        // note the time it took to save and load
    }

    @Test
    public void generateConnectionCount() {
        //generate? a list of 100 words
        //decompose 100 words with the cache enabled

        //count the number of connections on each word

    }

}