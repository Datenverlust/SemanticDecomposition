/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.kimanufaktur.nsm.decomposition.Dictionaries;

import de.kimanufaktur.nsm.decomposition.Concept;
import de.kimanufaktur.nsm.decomposition.Definition;
import de.kimanufaktur.nsm.decomposition.WordType;
import de.kimanufaktur.nsm.decomposition.exceptions.DictionaryDoesNotContainConceptException;
import de.kimanufaktur.nsm.decomposition.nlp.GermanLemma;
import de.kimanufaktur.nsm.decomposition.nlp.LemmaToken;
import de.kimanufaktur.nsm.decomposition.settings.Config;
import de.tudarmstadt.ukp.jwktl.api.util.ILanguage;
import de.tudarmstadt.ukp.jwktl.api.util.Language;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

/**
 * Created by faehndrich on 11.11.14.
 */
public abstract class BaseDictionary implements Serializable, Dictionary {
    public static StanfordCoreNLP pipeline = null;
    protected static ILanguage language = null;
    private String _dictName;

    public BaseDictionary() {
        init();
    }

    /**
     * Initialize the dictionary. For example prepare WodNet in memory.
     */
    public synchronized void init() {
        //TODO: Discuss: Is a new instantiation of StanfordNLP really needed here? Why not use the DecompositionUtil version?
        //The Decomposition class could carry around an instance and could share that instance
        if (pipeline == null) {

            if (Config.LANGUAGE.GER ==
                    Config.LANGUAGE.valueOf(Config.getInstance().getUserProps().getProperty(Config.LANGUAGE_KEY))) {
                initGerman();

            } else if (Config.LANGUAGE.EN ==
                    Config.LANGUAGE.valueOf(Config.getInstance().getUserProps().getProperty(Config.LANGUAGE_KEY))) {
                initEnglish();
            }

        }
    }

    private void initEnglish() {
        System.out.println("StanfordCoreNLP - [English]");
        // creates a StanfordCoreNLP object, with POS tagging, lemmatization, NER, parsing, and coreference resolution
        Properties props = new Properties();
        //props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
        props.put("annotators", "tokenize, ssplit, pos, lemma");
        pipeline = new StanfordCoreNLP(props);
        language = Language.ENGLISH;
    }

    private void initGerman() {
        System.out.println("StanfordCoreNLP - [German]");
        Properties props = StringUtils.argsToProperties(
                "-props", "StanfordCoreNLP-german.properties");
        //props.put("annotators", "tokenize, ssplit, pos, parse, depparse");
        props.setProperty("ner.useSUTime", "0");
        pipeline = new StanfordCoreNLP(props);
        language = Language.GERMAN;
    }

    /**
     * Flush the cache of a dictionary to start clean when decomposing a new concept
     */
    public void flushCache() {
    }

    /**
     * fill the concept with the synonyms out of the dictionary.
     *
     * @param word the concept to be filled. Here the Wordtype and the literal needs to be filled.
     * @return the set of synonyms of concepts. Here no recursion is done, so the synonyms need to be filled again.
     */
    public abstract HashSet<Concept> getSynonyms(Concept word);

    /**
     * fill the concept with the antonyms out of the dictionary.
     *
     * @param word the concept to be filled. Here the Wordtype and the literal needs to be filled.
     * @return the set of antonyms of concepts. Here no recursion is done, so the antonyms need to be filled again.
     */
    public abstract HashSet<Concept> getAntonyms(Concept word);

    /**
     * fill the concept with the Hypernyms out of the dictionary.
     *
     * @param word the concept to be filled. Here the Wordtype and the literal needs to be filled.
     * @return the set of Hypernyms of concepts. Here no recursion is done, so the Hypernyms need to be filled again.
     */
    public abstract HashSet<Concept> getHypernyms(Concept word);

    /**
     * fill the concept with the Hyponyms out of the dictionary.
     *
     * @param word the concept to be filled. Here the Wordtype and the literal needs to be filled.
     * @return the set of Hyponyms of concepts. Here no recursion is done, so the Hyponyms need to be filled again.
     */
    public abstract HashSet<Concept> getHyponyms(Concept word);

    /**
     * fill the concept with the Meronyms out of the dictionary. The Meronym relation is ruffly speaking the part-of relation.
     *
     * @param word the concept to be filled. Here the Wordtype and the literal needs to be filled.
     * @return the set of Meronyms of concepts. Here no recursion is done, so the Meronyms need to be filled again.
     */
    public abstract HashSet<Concept> getMeronyms(Concept word);


    /**
     * Get possible definitions of the concept form the dictionary. Here a definitions is a list of words. We argue
     * that the grammar of the definitions is at first irrelevant to its decomposition. So we keep the order of concepts
     * in the definitions and decompose each of its concepts.
     *
     * @param word The concept to get the definitions for. Here at least the literal and the Wordtype need to be set beforehand.
     * @return a list of possible definitions from the dictionary.
     */
    public abstract List<Definition> getDefinitions(Concept word);

    /**
     * Filling the concepts with all known elements of the dictionary, like synonyms.
     *
     * @param word     the concept to be billed.
     * @param wordType POS of the word to be decomposed
     * @return the same concept but hopefully filled with all that is known about it in the dictionary.
     * @throws DictionaryDoesNotContainConceptException
     */
    public abstract Concept fillConcept(Concept word, WordType wordType) throws DictionaryDoesNotContainConceptException;

    public abstract Concept getLemma(String word, WordType wordType);

    /**
     * get the concept from the dictionary using the given ID of the dicrionary. Here for example we can get a
     * WordNet Concept by giving the WordNet ID. This is done so that we can find a word with its exact word sense.
     * Different dictionaries will have different IDs. All Ids of a concept are sorted in its concept.ids.
     *
     * @param id the dictionary specific id of a word sense.
     * @return the concept for the given word in the dictionary.
     */
    public abstract Concept getConcept(Object id);


    /**
     * get the lemma of a concept.  A lemma is word which stands at the head of a definitions in a dictionary.
     * All the head words in a dictionary are lemmas. Technically, it is "a base word and its inflections".
     * @param word to ge the lemma for. E.g. walking will return to walk
     */
//    public Concept getLemma(Concept word);

    /**
     * Get the part of speech of a given concept using Stanford NLP.
     *
     * @param word the concept of which the literal is used to identify the POS.
     * @return the concept with the POS set.
     */
    public abstract Concept setPOS(Concept word);


    /**
     * This function fills the definition of a given concept. Here all definitions are added to the concept.definitions
     *
     * @param word the concept to get the definition for.
     * @return the given concept but with a filled definition.
     * @throws DictionaryDoesNotContainConceptException
     */
    public abstract Concept fillDefinition(Concept word) throws DictionaryDoesNotContainConceptException;

    /**
     * This function fills the related elements of a concept. Related terms are relative and differ in their
     * definition depending on the used dictionary. Synonyms are excluded here.
     *
     * @param concept the concept to get the realted concepts for.
     * @return the given concept with a filled concept.getRelated property.
     */
    public abstract Concept fillRelated(Concept concept) throws DictionaryDoesNotContainConceptException;

    /**
     * Get the lemma of the given concept using Stanford NLP.
     * Here we want a basic form of the concept, which is reduced to the minimum. Is this the stem
     * of the word? How about different word types? Are they considered?
     * <p>
     * NOTE: For german word lemmas org.languagetool is used,  because Stanford german models do not contain lemmas for german words
     *
     * @param word concept to get the Lemma vor
     * @return a concept with the lemma filled.
     */
    public synchronized Concept setLemma(Concept word) {
        Annotation document = new Annotation(word.getLitheral());
        pipeline.annotate(document);
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);

        for (CoreMap sen : sentences) {
            // traversing the words in the current sentence
            // a CoreLabel is a CoreMap with additional token-specific methods
            for (CoreLabel token : sen.get(CoreAnnotations.TokensAnnotation.class)) {

                //get lemma
                String lemma = token.get(CoreAnnotations.LemmaAnnotation.class);

                //GERMAN
                List<LemmaToken> lemmas = null;
                if (lemma == null &&
                        Config.LANGUAGE.GER == Config.LANGUAGE.valueOf(Config.getInstance().getUserProps().getProperty(Config.LANGUAGE_KEY))) {
                    //lemmatisation for german not supported by coreNLP,
                    //so use another lemmatisation here.
                    if (token.word() != null) {
                        lemmas = germanLemma(token.word());
                    } else {
                        lemmas = germanLemma(token.value());
                    }
                }
                //TODO: DISCUSS: multiple lemmas possible !
                if (lemmas != null && lemmas.size() > 0) {
                    //best first for the moment. See above comment.
                    lemma = lemmas.get(0).getLemma();
                }
                //TODO: what if lemma == Null?
                if(lemma == null){
                    System.err.println("[LEMMATIZATION] " + word.getLitheral() + " has no known LEMMA!");
                }
                word.setLemma(lemma);
            }
        }
        return word;
    }

    protected List<LemmaToken> germanLemma(String word) {

        if (word == null || word.equals("")) return new ArrayList<>();

        GermanLemma gl = new GermanLemma();

        //a word can have multiple lemmas (see LanguageToolLemmatisationTest)
        List<LemmaToken> lemmas = gl.lemma(word);
        return lemmas;
//        for(LemmaToken t: lemmas){
//            //TODO: shall the concept be the word or the lemma? since later the lemma gets set
//            Concept word = null;
//            if(t.getLemma() != null){
//                word = Decomposition.createConcept(t.getLemma());
//            }
//            if(word != null){
//                // this is the POS tag of the token
//                word.setWordType(WordType.UNKNOWN); //TODO: convert pos tag to valid value
//                // this is the NER label of the token
//                //String ne = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
//                //word.setNer(ne);
//                word.setLemma(t.getLemma());
//                def.add(word);
//            } else {
//                System.out.println("No lemmatization for " + "[Token] " + token.toString());
//            }
//
//        }
    }

    public String getDictName() {
        return _dictName;
    }

    public void setDictName(String dictName) {
        this._dictName = dictName;
    }
}
