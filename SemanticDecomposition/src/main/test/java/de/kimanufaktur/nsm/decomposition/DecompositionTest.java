/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package test.de.kimanufaktur.nsm.decomposition;

import de.kimanufaktur.nsm.decomposition.Concept;
import de.kimanufaktur.nsm.decomposition.Decomposition;
import de.kimanufaktur.nsm.decomposition.Definition;
import de.kimanufaktur.nsm.decomposition.WordType;
import de.kimanufaktur.nsm.decomposition.settings.Config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;



/**
 * Unit test for simple Decomposition.
 */
public class DecompositionTest {


    public static void main(String[] args) {
        DecompositionTest comparisonTest = new DecompositionTest();
        comparisonTest.SimpleTest();
        //comparisonTest.comparisonTest();
       // comparisonTest.ManualTest();
       //Concept boy = comparisonTest.singelWordDecomposition("boy", WordType.NN,2);
      // Concept lad = comparisonTest.singelWordDecomposition("lad", WordType.NN,2);
       Concept midday = comparisonTest.singelWordDecomposition("midday", WordType.NN,3);
       //Concept noon = comparisonTest.singelWordDecomposition("noon", WordType.NN,6);

        System.out.println("Finisched: " +midday);
        //System.out.println(noon);
    }




    private void SimpleTest(){
        //german
//        Config.getInstance().getUserProps().setProperty(Config.LANGUAGE_KEY, Config.LANGUAGE.GER.toString());
        Decomposition dec  = new Decomposition();
//        String word = "Akkuschrauber";
//        String lang = "German";
//        Concept res = dec.decompose(word, WordType.NN, 2);
//        System.out.println(lang + " decomposition of \""+word+"\"");
//        System.out.println(res.toString());

        //english
        Config.getInstance().getUserProps().setProperty(Config.LANGUAGE_KEY, Config.LANGUAGE.EN.toString());

        //NOT WORKING AS INTENDED because of singleton patterns
        //dec  = new Decomposition();

        String word = "screwdriver";
        String lang = "English";
        Concept res = dec.decompose(word, WordType.NN, 2);
        System.out.println(lang + " decomposition of \""+word+"\"");
        System.out.println(res.toString());
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
