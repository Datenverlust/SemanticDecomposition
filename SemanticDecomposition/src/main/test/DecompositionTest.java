

/*
 * * Copyright (C) Johannes Fähndrich - All Rights Reserved
 *  * Unauthorized copying of this file, via any medium is strictly prohibited
 *  * Proprietary and confidential
 *  * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 *  *
 */

import de.dailab.nsm.decomposition.Concept;
import de.dailab.nsm.decomposition.Decomposition;
import de.dailab.nsm.decomposition.Definition;
import de.dailab.nsm.decomposition.Dictionaries.BaseDictionary;
import de.dailab.nsm.decomposition.Dictionaries.WiktionaryDictionary;
import de.dailab.nsm.decomposition.Dictionaries.WordNetDictionary;
import de.dailab.nsm.decomposition.WordType;
import de.dailab.nsm.decomposition.persistence.ConceptCache;
import de.dailab.nsm.decomposition.persistence.Neo4jConceptCache;
import de.dailab.nsm.decomposition.settings.Config;
import org.junit.Test;
import org.parboiled.trees.GraphUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;



/**
 * Unit test for simple Decomposition.
 */
public class DecompositionTest {

    @Test
    public static void main(String[] args) {
        //only english
        //BaseDictionary wordNetDict = WordNetDictionary.getInstance(); //Create WordNet Dictionary in memory
        //dictionaries.add(wordNetDict);
        BaseDictionary wiktionaryDict = WiktionaryDictionary.getInstance();
        DecompositionTest comparisonTest = new DecompositionTest();
        comparisonTest.SimpleTest();
        //comparisonTest.comparisonTest();
       // comparisonTest.ManualTest();
       //Concept boy = comparisonTest.singelWordDecomposition("boy", WordType.NN,2);
      // Concept lad = comparisonTest.singelWordDecomposition("lad", WordType.NN,2);
       //Concept midday = comparisonTest.singelWordDecomposition("midday", WordType.NN,6);
       //Concept noon = comparisonTest.singelWordDecomposition("noon", WordType.NN,6);
      //  System.out.println("Finisched: " +midday);
        //System.out.println(noon);

    }

    private void SimpleTest(){
        //german
        //Neo4jConceptCache cache = new Neo4jConceptCache();
        Config.getInstance().getUserProps().setProperty(Config.LANGUAGE_KEY, Config.LANGUAGE.GER.toString());
        //Decomposition dec  = new Decomposition();
        String word = "Bohrschrauber";
        String lang = "German";
        Concept res = decomposition.decompose(word, WordType.NN, 1);

        System.out.println(lang + " decomposition of \""+word+"\"");
        System.out.println(res.toString());
//        cache.put(res);



        //english
//        Config.getInstance().getUserProps().setProperty(Config.LANGUAGE_KEY, Config.LANGUAGE.EN.toString());

        //NOT WORKING AS INTENDED because of singleton patterns
//        dec  = new Decomposition();
//
//        word = "screwdriver";
//        lang = "English";
//        res = dec.decompose(word, WordType.NN, 2);
//        System.out.println(lang + " decomposition of \""+word+"\"");
//        System.out.println(res.toString());
    }


    private final Decomposition decomposition = new Decomposition();

    public Concept singelWordDecomposition(String word, WordType pos, int decompositionDepth){

        return decomposition.multiThreadedDecompose(word,pos,decompositionDepth);
    }

    private DecompositionTest() {
        Decomposition.init();
    }

   /* //@org.junit.Test
    public void decompositionTest(){
        int decompositionDepth = 2;
        Concept c = decomposition.multiThreadedDecompose("face", WordType.NN, decompositionDepth);
//        decomposition.serializeConcept(c);
//        decomposition.setConcept(c);
//        decomposition.toString();
        assertNotNull(c);
        //Test Meronyms
        Map<Integer, Concept> meronyms = new HashMap<>();
        for (IDictionary dict : decomposition.dictionaries){

            meronyms.putAll(dict.getMeronyms(c));
        }
        assert(meronyms.size()>0);


    }*/

    private void SingleWOrdDecomposition(String word, WordType POS, int decompositiondepth){
        Concept c = decomposition.multiThreadedDecompose(word,POS,decompositiondepth);


    }

    private void ManualTest() {
        Concept c = decomposition.multiThreadedDecompose("adult", WordType.NN, 1);
        int defindex = 0;
        for(Definition d :c.getDefinitions()){
            System.out.println(defindex +". definition is: " + d.toString());
            defindex++;
        }
        getManualDefinition(c);

    }
    private static Concept getManualDefinition(Concept concept) {
        System.out.println("Please give a definitions for the concept: " + concept.getLitheral() + " which is not one of the given definitions.");
        BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
        try {
            String def = bufferRead.readLine();
            Definition definition = new Definition(def);
            definition.setTerm(concept);
            //Check definition
            concept.getDefinitions().add(definition);

        } catch (IOException e) {
            System.out.println("Please give a definitions for the concept: " + concept.getLitheral());

        }
        return concept;
    }
}
