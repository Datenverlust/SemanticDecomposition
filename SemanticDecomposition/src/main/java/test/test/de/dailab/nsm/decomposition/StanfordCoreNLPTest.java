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
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        String sentence = "Das hier ist ein deutscher Satz.";
        Annotation anno = new Annotation(sentence);
        pipeline.annotate(anno);
        for (CoreMap map : anno.get(CoreAnnotations.SentencesAnnotation.class)) {
            Tree sentenceTree = map.get(TreeCoreAnnotations.TreeAnnotation.class);
            System.out.println(sentenceTree);
        }
    }
}
