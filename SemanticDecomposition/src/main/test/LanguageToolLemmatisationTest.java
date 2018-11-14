/*
 * * Copyright (C) Johannes Fähndrich - All Rights Reserved
 *  * Unauthorized copying of this file, via any medium is strictly prohibited
 *  * Proprietary and confidential
 *  * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 *  *
 */

package test.test.de.dailab.nsm.decomposition;

import de.dailab.nsm.decomposition.nlp.GermanLemma;
import org.languagetool.AnalyzedToken;
import org.languagetool.tagging.de.GermanTagger;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class LanguageToolLemmatisationTest {

    public static void main(String[] args){
        try {
            testGermanLemmatisation();
            testTaggerBaseforms();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public static void testGermanLemmatisation(){
        GermanLemma gl = new GermanLemma();
        List<String> words = Arrays.asList(new String[]{"batterie","Batterie","betrieben","betreiben","betriebenes","batteriebetrieben","batteriebetriebenes","übrigbleibst","Haus","gewässert","Häuserkämpfe"});
        for(String word: words){
            System.out.println(word + " Lemma: " + gl.lemma(word));
        }

    }

    public static void testTaggerBaseforms() throws IOException {
        GermanTagger tagger = new GermanTagger();

        List<AnalyzedToken> readings1 = tagger.lookup("übrigbleibst").getReadings();
        System.out.println("Readings for \"" + "übrigbleibst\"");
        print(readings1);
        assertEquals(1, readings1.size());
        assertEquals("übrigbleiben", readings1.get(0).getLemma());

        List<AnalyzedToken> readings2 = tagger.lookup("Haus").getReadings();
        System.out.println("Readings for \"" + "Haus\"");
        print(readings2);
        assertEquals(3, readings2.size());
        assertEquals("Haus", readings2.get(0).getLemma());
        assertEquals("Haus", readings2.get(1).getLemma());
        assertEquals("Haus", readings2.get(2).getLemma());

        List<AnalyzedToken> readings3 = tagger.lookup("Häuser").getReadings();
        System.out.println("Readings for \"" + "Häuser\"");
        print(readings3);
        assertEquals(3, readings3.size());
        assertEquals("Haus", readings3.get(0).getLemma());
        assertEquals("Haus", readings3.get(1).getLemma());
        assertEquals("Haus", readings3.get(2).getLemma());

        List<AnalyzedToken> readings4 = tagger.lookup("gewässert").getReadings();
        System.out.println("Readings for \"" + "gewässert\"");
        print(readings4);
//        assertEquals(3, readings3.size());
//        assertEquals("Haus", readings3.get(0).getLemma());
//        assertEquals("Haus", readings3.get(1).getLemma());
//        assertEquals("Haus", readings3.get(2).getLemma());

    }

    private static void print(List<AnalyzedToken> readings){
        for(AnalyzedToken t : readings){
            System.out.println(t.toString());
        }
    }
}
