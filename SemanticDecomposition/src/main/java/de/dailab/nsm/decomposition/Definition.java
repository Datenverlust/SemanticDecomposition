/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.dailab.nsm.decomposition;

import de.dailab.nsm.decomposition.Dictionaries.BaseDictionary;
import de.dailab.nsm.decomposition.nlp.GermanLemma;
import de.dailab.nsm.decomposition.nlp.LemmaToken;
import de.dailab.nsm.decomposition.settings.Config;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by faehndrich on 12.11.14.
 */
//@NodeEntity
public class Definition implements Serializable {
    private static StanfordCoreNLP pipeline = null;
    //@Relationship(type = "Term")
    private Concept term = null;
    //@Relationship(type = "Defining_concepts")
    private List<Concept> definition = null;
    //@Relationship(type = "Example_Phrase")
    private List<Concept> examplePhrase = null;
    //@GraphId
    private Long id;
    private String sensekey = null;


    public Definition() {
        if( Config.LANGUAGE.EN ==
                Config.LANGUAGE.valueOf( Config.getInstance().getUserProps().getProperty(Config.LANGUAGE_KEY) )){
            //
            //  Init English
            //
            init();
        } else if (Config.LANGUAGE.GER ==
                Config.LANGUAGE.valueOf( Config.getInstance().getUserProps().getProperty(Config.LANGUAGE_KEY) )){
            //
            //  Init German
            //
            initGerman();
        }


    }

    /**
     * Create a Definition for the given english word. This will remove all non letters from the word.
     * Here is the first time we can select a word type with the given NLP pipeline.
     * <p>
     * TODO: fix this, here we do not want the dictionarries to be invloved. THis is just the creation of one Definition
     *
     * @param def the word as string for which a definitions should be created.
     */
    public Definition(String def) {

        if( Config.LANGUAGE.EN ==
                Config.LANGUAGE.valueOf( Config.getInstance().getUserProps().getProperty(Config.LANGUAGE_KEY) )){
            //
            //  Init English
            //
            init();
        } else if (Config.LANGUAGE.GER ==
                Config.LANGUAGE.valueOf( Config.getInstance().getUserProps().getProperty(Config.LANGUAGE_KEY) )){
            //
            //  Init German
            //
            initGerman();
        }
        //TODO Hack: We should carry around exaclty one instance at some approriate place
        //currenlntly trying to use the BaseDictionary instance (since this is most probably instantiated by now)

        if(pipeline == null) pipeline = BaseDictionary.pipeline;

        String[] words = def.split("\\s+");
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            // You may want to check for a non-word character before blindly
            // performing a replacement
            // It may also be necessary to adjust the character class
            words[i] = words[i].replaceAll("[^\\p{Alnum}]+", "");
            stringBuilder.append(words[i]);
            stringBuilder.append(" ");
        }
        definition = this.createDefinition(stringBuilder.toString());
    }

    /**
     * Initialize the Definition. For example prepare WodNet in memory.
     */
    private void init() {
        //TODO Hack: We should carry around exaclty one instance at some approriate place
        //currenlntly trying to use the BaseDictionary instance (since this is most probably instantiated by now)
        if (BaseDictionary.pipeline == null) {
            System.out.println("StanfordCoreNLP - [English]");
            // creates a StanfordCoreNLP object, with POS tagging, lemmatization, NER, parsing, and coreference resolution
            Properties props = new Properties();
//            props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
            props.put("annotators", "tokenize, ssplit, pos, lemma, parse, depparse");
            BaseDictionary.pipeline = new StanfordCoreNLP(props);
        }
    }

    private void initGerman(){
        //TODO Hack: We should carry around exaclty one instance at some approriate place
        //currenlntly trying to use the BaseDictionary instance (since this is most probably instantiated by now)

        if (BaseDictionary.pipeline == null) {
            System.out.println("StanfordCoreNLP - [German]");
            // creates a StanfordCoreNLP object, with POS tagging, lemmatization, NER, parsing, and coreference resolution
            Properties props =StringUtils.argsToProperties(
                    new String[]{"-props", "StanfordCoreNLP-german.properties"});
            //props.put("annotators", "tokenize, ssplit, pos, lemma, parse, depparse");
            BaseDictionary.pipeline = new StanfordCoreNLP(props);
        }
    }

    public String getSensekey() {
        return sensekey;
    }

    public void setSensekey(String sensekey) {
        this.sensekey = sensekey;
    }

    public Concept getTerm() {
        return term;
    }

    public void setTerm(Concept term) {
        this.term = term;
    }

    public List<Concept> getExamplePhrase() {
        return examplePhrase;
    }

    public void setExamplePhrase(List<Concept> examplePhrase) {
        this.examplePhrase = examplePhrase;
    }

    public List<Concept> getDefinition() {
        return definition;
    }

    public void setDefinition(List<Concept> definition) {
        this.definition = definition;
    }

    /**
     * Create a definition out of a string. The string is parsed, tokenized and the POS is set for each word.
     *
     * @param sentence The sentence as string which schould serve as definition.
     * @return A list of concepts making up the given sentence.
     */
    public List<Concept> createDefinition(String sentence) {
        List<Concept> def = new ArrayList<>();
        // create an empty Annotation just with the given text
        Annotation document = new Annotation(sentence);
        pipeline.annotate(document);
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);

        for (CoreMap sen : sentences) {
            // traversing the words in the current sentence
            // a CoreLabel is a CoreMap with additional token-specific methods
            for (CoreLabel token : sen.get(CoreAnnotations.TokensAnnotation.class)) {
                String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                //get lemma
                String lemma = token.lemma();
                //System.out.println("[Token] " + token.toString());

                //german
                if(lemma == null &&
                        Config.LANGUAGE.GER == Config.LANGUAGE.valueOf( Config.getInstance().getUserProps().getProperty(Config.LANGUAGE_KEY) )){
                    //lemmatisation for german not supported by coreNLP,
                    //so use another lemmatisation here.
                    //TODO: make configurable
                    GermanLemma gl = new GermanLemma();
                    List<LemmaToken> lemmas = null;
                    if(token.word() != null){
                        lemmas = gl.lemma(token.word());
                    } else {
                        lemmas = gl.lemma(token.value());
                    }

                    for(LemmaToken t: lemmas){
                        //TODO: shall the concept be the word or the lemma? since later the lemma gets set
                        Concept word = null;
                        if(t.getLemma() != null){
                            word = Decomposition.createConcept(t.getLemma());
                        }
                        if(word != null){
                            // this is the POS tag of the token
                            word.setWordType(WordType.UNKNOWN); //TODO: convert pos tag to valid value
                            // this is the NER label of the token
                            //String ne = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
                            //word.setNer(ne);
                            word.setLemma(t.getLemma());
                            def.add(word);
                        } else {
                            System.out.println("No lemmatization for " + "[Token] " + token.toString());
                        }

                    }
                    continue;
                }

                //english

                Concept word = Decomposition.createConcept(lemma);
                // this is the POS tag of the token
                word.setWordType(WordType.valueOf(pos));
                // this is the NER label of the token
                //String ne = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
                //word.setNer(ne);
                word.setLemma(lemma);
                def.add(word);
            }
            // this is the parse tree of the current sentence
            //Tree tree = sen.get(TreeCoreAnnotations.TreeAnnotation.class);

            // this is the Stanford dependency graph of the current sentence
            //SemanticGraph dependencies = sen.get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class);
        }
        return def;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Concept def : definition) {
            stringBuilder.append(def.getLitheral() + " ");
        }
        return definition.toString();
    }

    @Override
    public int hashCode() {
       int hash =0;
        for (Concept partDef : definition) {
            hash += partDef.hashCode();
        }
        if(id == null){
            id = Long.valueOf(hash);
        }
        return hash;
    }

    @Override
    public boolean equals(Object definition) {
        return this.hashCode() == definition.hashCode();
    }
}
