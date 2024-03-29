/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.kimanufaktur.nsm.decomposition;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.CollinsHeadFinder;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by Hannes on 02.04.2017.
 */
public class AnalyseUtil {

    static StanfordCoreNLP pipeline = null;
    public static StanfordCoreNLP tokenizePipeline() {

        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit");
        if(pipeline==null) {
            pipeline = new StanfordCoreNLP(props);
        }
        return pipeline;
    }

    public static Annotation getAnnotation(String text, StanfordCoreNLP pipeline){
        Annotation document = new Annotation(text);
        pipeline.annotate(document);
        return document;
    }

    /**
     * This is the full pipeline. It can be used on hight performance computers.
     *
     * @return the full stack pipeline for coreNMP o use.
     */
    public static StanfordCoreNLP getFullPipeline() {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, parse, ner");
        props.setProperty("ner.useSUTime", "false");
        props.setProperty("depparse.extradependencies", "MAXIMAL");

        if(pipeline == null) {
            pipeline = new StanfordCoreNLP(props);
        }
        return pipeline;
    }

    public static List<List<String>> tokenizeText(Annotation document){
        List<List<String>> tokens=new ArrayList<>();
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);

        for (CoreMap sentence : sentences) {
            List<String> sentTokens=new ArrayList<>();
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                String word = token.get(CoreAnnotations.TextAnnotation.class);
                sentTokens.add(word);
            }
            tokens.add(sentTokens);
        }
        return tokens;
    }

    /**
     * Should get you the head word of the main noun phrase (it didn't do that it gives you from all noun phrases the head word)
     * @return
     */
    public static List<String> getHeadWord( Annotation annotation){

        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
        List<String> list = new ArrayList<>();
        Tree tree = sentences.get(0).get(TreeCoreAnnotations.TreeAnnotation.class);
        CollinsHeadFinder headFinder = new CollinsHeadFinder();
        for (Tree subtree : tree) {
            if ((subtree.label().value().equals("NP") )) {
                Tree head = subtree.headTerminal(headFinder,null);
                if(!listContainsHead(list,head.nodeString())){
                    list.add(subtree.headTerminal(headFinder,null).nodeString());
                }
            }
        }

        return list;

    }

    public static Boolean listContainsHead(List<String> list, String headWord){
        for(String s : list){
            if(s.equals(headWord)){
                return true;
            }
        }
        return false;
    }

    public static Boolean isNoun(Tree tree, String headWord){
        for(Tree t : tree) {
            if(t.label().value().startsWith("NN")&&t.firstChild()!=null){
                System.out.println(t.nodeString());
                return t.firstChild().nodeString().equals(headWord);
            }
        }
        return false;
    }





}
