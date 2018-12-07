/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package test.test.de.dailab.nsm.decomposition;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.StringUtils;

import java.util.Properties;

/**
 * NOTE: non JUnit test
 */
public class StanfordCoreNLPTest {

    public static void main(String[] args){
        // creates a StanfordCoreNLP object, with POS tagging, lemmatization, NER, parsing, and coreference resolution
        Properties props =StringUtils.argsToProperties(
                new String[]{"-props", "StanfordCoreNLP-german.properties"});
        //props.put("annotators", "tokenize, ssplit, pos, lemma, parse, depparse");
        long start = System.currentTimeMillis();
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        long initTime = System.currentTimeMillis() - start;

        String sentence = "Das hier ist ein deutscher Satz.";

        start = System.currentTimeMillis();
        Annotation anno = new Annotation(sentence);
        pipeline.annotate(anno);
        long annotateTime = System.currentTimeMillis() - start;

        System.out.println("StanfordCoreNLP German init time (s): " +initTime/1000L) ;
        System.out.println("StanfordCoreNLP German annotate time (s): " +annotateTime/1000L) ;
        System.out.println("StanfordCoreNLP German input sentence: " + sentence);
        long usedMemoryMB = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024L * 1024L);
        long totoalMemMB  = Runtime.getRuntime().maxMemory() / (1024L * 1024L);
        System.out.println(usedMemoryMB + " /" + totoalMemMB + " MB used.");

        for (CoreMap map : anno.get(CoreAnnotations.SentencesAnnotation.class)) {
            Tree sentenceTree = map.get(TreeCoreAnnotations.TreeAnnotation.class);
            System.out.println(sentenceTree.toString());
        }
    }
}
