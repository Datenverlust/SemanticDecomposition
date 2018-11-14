/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.dailab.nsm.decomposition.Dictionaries;


import de.dailab.nsm.decomposition.Concept;
import de.dailab.nsm.decomposition.Decomposition;
import de.dailab.nsm.decomposition.Definition;
import de.dailab.nsm.decomposition.WordType;
import de.dailab.nsm.decomposition.dictionaries.wiktionary.WiktionaryCrawler;
import de.dailab.nsm.decomposition.dictionaries.wiktionary.WiktionaryCrawlerGerman;
import de.dailab.nsm.decomposition.exceptions.DictionaryDoesNotContainConceptException;
import de.dailab.nsm.decomposition.settings.Config;
import de.tudarmstadt.ukp.jwktl.api.*;
import de.tudarmstadt.ukp.jwktl.api.util.ILanguage;
import de.tudarmstadt.ukp.jwktl.api.util.Language;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Impementation of the Wikitionary as an dictionary used during the decomposition. This is based on the implementation
 * of the @see de.dailab.nsm.decomposition.dictionaries.wiktionary.WiktionaryCrawler.
 * The initial call of this function will unpack a wikipedia dump and create a database in
 * the de.dailab.nsm.decomposition.dictionaries.wiktionary target folder. If this database should be reused, please do not
 * mvn clean before the install.
 * Created by faehndrich on 12.11.14.
 */
public class WiktionaryDictionary extends BaseDictionary {
    private static WiktionaryDictionary instance = null;
    //private static final StanfordCoreNLP pipeline = null;
    private static final Logger logger = Logger.getLogger(Decomposition.class);
    private WiktionaryCrawler wiktionaryCrawler;
    Pattern p = Pattern.compile("\\w+(-\\d+)?");

    private WiktionaryDictionary(){
    }

    public static  WiktionaryDictionary getInstance(){
        if(instance == null){
           instance = new WiktionaryDictionary();
            System.out.println("Wiktionary intitialised [" + instance.language.getName() + "]");
        }
        return instance;
    }

    public  List<Concept> createDefinition(String sentence) {
        List<Concept> def = new ArrayList<>();
        // create an empty Annotation just with the given text
        Annotation document = new Annotation(sentence);
        pipeline.annotate(document);
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);

        for (CoreMap sen : sentences) {
            // traversing the words in the current sentence
            // a CoreLabel is a CoreMap with additional token-specific methods
            for (CoreLabel token : sen.get(CoreAnnotations.TokensAnnotation.class)) {
                Concept word = Decomposition.createConcept(token.lemma()); //new Concept(token.lemma());
                // this is the POS tag of the token
                String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                word.setWordType(WordType.valueOf(pos));
                // this is the NER label of the token
                String ne = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
                word.setNer(ne);

                //get lemma
                String lemma = token.get(CoreAnnotations.LemmaAnnotation.class);
                word.setLemma(lemma);
                def.add(word);
            }
        }
        return def;
    }

    @Override
    public void init() {
        super.init();
        if (wiktionaryCrawler == null) {
            if( Config.LANGUAGE.GER ==
                    Config.LANGUAGE.valueOf( Config.getInstance().getUserProps().getProperty(Config.LANGUAGE_KEY) )){

                String path2DB = Config.getInstance().getUserProps().getProperty(Config.WIKTIONARY_DB_PATH_GER_KEY);
                String archiveName = Config.getInstance().getUserProps().getProperty(Config.WIKTIONARY_DB_PATH_GER_KEY);
                String sourceURI = Config.getInstance().getUserProps().getProperty(Config.WIKTIONARY_DB_ARCHIVE_SOURCE_URI_KEY);
                //System.out.println("Init Wiktionary: DB Path:  " + path2DB +", "+ "Archive Path" + archiveName + " Source Path" + sourceURI);
                this.wiktionaryCrawler = new WiktionaryCrawler(path2DB, archiveName, sourceURI);

            } else if( Config.LANGUAGE.EN ==
                    Config.LANGUAGE.valueOf( Config.getInstance().getUserProps().getProperty(Config.LANGUAGE_KEY) )){

                //this.language = Language.ENGLISH;
                //default constructor initilaizes english based crawler
                this.wiktionaryCrawler = new WiktionaryCrawler();
            }

            this.wiktionaryCrawler.init();
        }
    }

    @Override
    public HashSet<Concept> getSynonyms(Concept word) {
        HashSet<Concept> synonyms = new HashSet<>();
        Set<IWiktionaryRelation> syn = wiktionaryCrawler.getRelations(word.getLitheral(), RelationType.SYNONYM);
        for (IWiktionaryRelation w : syn) {
            IWiktionaryPage spage = wiktionaryCrawler.getPage(w.getTarget());
            if (spage != null) {
                for (IWiktionaryEntry synonym : spage.getEntries()) {
                    Concept s = getConcept(synonym.getPage().getId());
                        synonyms.add(s);
                }
            }
        }
        //logger.debug("Synonyms for " + word.getLitheral() + " are " + synonyms.toString());
        return synonyms;
    }

    public HashSet<Concept> getAntonyms(Concept word) {
        HashSet<Concept> antonyms = new HashSet<>();
        Set<IWiktionaryRelation> ant = wiktionaryCrawler.getRelations(word.getLitheral(), RelationType.ANTONYM);
        addSynset(ant, antonyms);
        //logger.debug("Antonyms for " + word.getLitheral() + " are " + antonyms.toString());
        return antonyms;
    }

    @Override
    public HashSet<Concept> getHypernyms(Concept word) {
        HashSet<Concept> hypernyms = new HashSet<>();
        Set<IWiktionaryRelation> hyp = wiktionaryCrawler.getRelations(word.getLitheral(), RelationType.HYPERNYM);
        addSynset(hyp, hypernyms);
        //logger.debug(RelationType.HYPERNYM.toString() + " for " + word.getLitheral() + " are " + hypernyms.toString());
        return hypernyms;
    }

    @Override
    public HashSet<Concept> getHyponyms(Concept word) {
        HashSet<Concept> hyponyms = new HashSet<>();
        Set<IWiktionaryRelation> hyp = wiktionaryCrawler.getRelations(word.getLitheral(), RelationType.HYPONYM);
        addSynset(hyp, hyponyms);
        //logger.debug(RelationType.HYPONYM.toString() + " for " + word.getLitheral() + " are " + hyponyms.toString());
        return hyponyms;
    }

    private void addSynset(Set<IWiktionaryRelation> synset, HashSet<Concept> toAdd2) {
        for (IWiktionaryRelation w : synset) {
            IWiktionaryPage hpage = wiktionaryCrawler.getPage(w.getTarget());
            if (hpage != null) {
                for (IWiktionaryEntry hyper : hpage.getEntries()) {
                    Concept h = getConcept(hyper.getPage().getId());
                    if (Decomposition.checkIsPrime(h)) {
                        h = Decomposition.getPrimeofConcept(h);
                    } else {
                        Concept knownConcept = Decomposition.getKnownConcept(h);
                        if (knownConcept.getDecompositionlevel()>=0) {
                            h = knownConcept;
                        }
                    }
                    toAdd2.add(h);
                }
            }
        }
    }

    @Override
    public HashSet<Concept> getMeronyms(Concept word) {
        HashSet<Concept> meronyms = new HashSet<>();
        Set<IWiktionaryRelation> mer = wiktionaryCrawler.getRelations(word.getLitheral(), RelationType.MERONYM);
        for (IWiktionaryRelation w : mer) {
            IWiktionaryPage spage = wiktionaryCrawler.getPage(w.getTarget());
            if (spage != null) {
                for (IWiktionaryEntry synonym : spage.getEntries()) {
                    Concept m = null;
                    m = getConcept(synonym.getPage().getId());
                    meronyms.add(m);
                }
            }
        }
        //logger.debug("Meronyms for " + word.getLitheral() + " are " + meronyms.toString());
        return meronyms;
    }

    /**
     * get all definitions of the concept. Here not only is-a sentences are returned but also example phrases.
     *
     * @param word the concept to took up the definitions for.
     * @return the list fo definitions found in WordNet.
     */
    @Override
    public List<Definition> getDefinitions(Concept word) {
        List<Definition> result = new ArrayList<>();
        Set<IWiktionaryEntry> entries = null;
        if (word.getWordType() == null) {
            entries = wiktionaryCrawler.getEntries(wiktionaryCrawler.getPage(word.getLitheral()));
        } else {
            entries = wiktionaryCrawler.getEntries(wiktionaryCrawler.getPage(word.getLitheral()), PartOfSpeech.valueOf(word.getWordType().type()));
        }
        for (IWiktionaryEntry entry : entries) {
            for (String gloss : wiktionaryCrawler.getGlosses(entry)) {
                Definition def = new Definition(gloss);
                result.add(def);
            }
        }
        return result;
    }

    @Override
    public Concept fillConcept(Concept word, WordType wordType) throws DictionaryDoesNotContainConceptException {
        if (word.getLemma() != null) {
            setLemma(word);
        }

        if (word.getWordType() == null) {
            if (wordType != null) {
                word.setWordType(wordType);
            } else {
                setPOS(word);
            }
        }
        word.getSynonyms().addAll(this.getSynonyms(word));
        word.getAntonyms().addAll(this.getAntonyms(word));
        word.getHypernyms().addAll(this.getHypernyms(word));
        word.getHyponyms().addAll(this.getHyponyms(word));
        word.getMeronyms().addAll(this.getMeronyms(word));
        IWiktionaryPage w = getWord(word.getLitheral(), word.getWordType());
        if (w == null) {
            throw new DictionaryDoesNotContainConceptException(word.getLitheral());
        } else {
            fillDefinition(word);
        }
        //logger.debug("filling concept: " + word.getLitheral() + " with POS: " + word.getWordType());
        return word;


    }

    @Override
    public Concept getLemma(String word, WordType wordType) {
        //logger.debug("Filling: " + word + " with word type: " + wordType.type());
        return Decomposition.createConcept(word, wordType);

    }

    /**
     * Get the word with the given ID.
     *
     * @param id Wikitionary page key which identifies the word we search.
     * @return the conceptualization of the word with the given WordNet id.
     */
    @Override
    public Concept getConcept(Object id) {
        IWiktionaryPage page = null;
        Concept result = null;
        page = WiktionaryCrawler.wkt.getPageForId(Long.valueOf(id.toString()));
        result = Decomposition.createConcept(page.getTitle()); //new Concept(page.getTitle()); //Decomposition.createConcept(page.getTitle(),WordType.getType(page.getEntry(0).getWordForms().get(0).getWordForm()));
//        try {
//            fillDefinition(result);
//        } catch (DictionaryDoesNotContainConceptException e) {
//            e.printStackTrace();
//        }
        return result;
    }

    /**
     * Related is here interpreted as all word forms known to wikipedia.
     *
     * @param concept the concept to get the realted concepts for.
     * @return the given concept but the related filled with all word forms known to the wikipedia as concepts.
     */
    @Override
    public Concept fillRelated(Concept concept) {
        IWiktionaryPage word = null;
        try {
            word = getWord(concept.getLitheral(), concept.getWordType());

            for (IWiktionaryEntry w : word.getEntries()) {
                for (IWiktionaryWordForm wordForm : w.getWordForms()) {
                    Concept related = Decomposition.createConcept(wordForm.getWordForm(), concept.getWordType());
                    concept.getDerivations().add(related);
                }
            }
        } catch (DictionaryDoesNotContainConceptException e) {
            e.printStackTrace();
        }
        return concept;
    }

    @Override
    public Concept fillDefinition(Concept word) {
        IWiktionaryPage page = wiktionaryCrawler.getPage(word.getLitheral());
        if (page != null) {
            word.getIds().put(this, page.getId());
            for (IWiktionaryEntry entry : page.getEntries()) { //look up every entry of the page as definition
                if (entry.getWordLanguage() != null) {
                    //TODO:DISCUSS: Really language check necessery here?!
                    //if (entry.getWordLanguage().equals(language)) {//if we have a language tag we only want the english definition
                        lookupDefinition(word, entry);
                    //}
                } else {//look up entries which have no language set
                    lookupDefinition(word, entry);
                }
            }
        }
        return word;
    }

    /**
     * This looks up the given word in the dictionary. This adds all definitions found on the wiki page.
     * TODO: here we should select the right definition for the decomposed word.
     * @param word  the word to look up
     * @param entry the entry to search for the given word.
     */
    private void lookupDefinition(Concept word, IWiktionaryEntry entry) {
        for (IWikiString gloss : entry.getGlosses()) {
            if (!gloss.getPlainText().equals("")) {
                Definition definition = null;
                definition = new Definition(gloss.getPlainText());//TODO: choos definition
                definition.setTerm(word);
                if (!word.getDefinitions().contains(definition)) {
                    word.getDefinitions().add(definition);
                }
            }
        }
    }

    /**
     * Get the IWord from the WordNet Database. This is only used to check if a page exists so that we can continue
     *
     * @param word     the literal value we are looking for.
     * @param wordType the @WordType we want to look up.
     * @return The wikipedia page for the given word
     */
    private IWiktionaryPage getWord(String word, WordType wordType) throws DictionaryDoesNotContainConceptException {
        IWiktionaryPage page = wiktionaryCrawler.getPage(word);
        PartOfSpeech wordPOS = null;
        IWiktionaryPage result = null;
        if (page != null) {
            try {
                wordPOS = PartOfSpeech.valueOf(wordType.type());
                result = wiktionaryCrawler.getEntries(page, wordPOS).iterator().next().getPage(); //TODO: Indeterministically choose here
                //logger.error("there are many concepts of type: " + wordType.type() + " of word: " + word + " choosing the first one found.");
                if (result == null) {
                    result = wiktionaryCrawler.getEntries(page).iterator().next().getPage();//TODO: Indeterministically choose here
                }
            } catch (IllegalArgumentException e) {
                //e.printStackTrace();
                result = page;
                return result;
            } catch (NoSuchElementException e) {
                try {
                    result = wiktionaryCrawler.getEntries(page).iterator().next().getPage();//TODO: Indeterministically choose here
                } catch (NoSuchElementException e1) {
                    logger.error("There no definition for concepts: " + page.getTitle());
                    throw new DictionaryDoesNotContainConceptException(word);
                }

            }
        }
        return result;
    }

    /**
     * Get the IWord from the Wiktionary Database. Here the Wikipedia page is returned withoug pictures or annimations.
     *
     * @param word the literal value we are looking for.
     * @return wikipedia page
     */
    private IWiktionaryPage getiWiktionaryPage(String word) {
        IWiktionaryPage result;
        result = wiktionaryCrawler.getEntries(wiktionaryCrawler.getPage(word)).iterator().next().getPage();
        return result;
    }


    @Override
    public Concept setPOS(Concept word) {
        Annotation document = new Annotation(word.getLitheral());
        pipeline.annotate(document);
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        for (CoreMap sen : sentences) {
            // traversing the words in the current sentence
            // a CoreLabel is a CoreMap with additional token-specific methods
            for (CoreLabel token : sen.get(CoreAnnotations.TokensAnnotation.class)) {

                // this is the POS tag of the token
                String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                if (!pos.equals("") && word.getWordType() == null) {
                    WordType t = null;
                    try {
                        t = WordType.valueOf(pos);
                        word.setWordType(t);
                    } catch (IllegalArgumentException notype) {
                        notype.printStackTrace();
                        word.setWordType(WordType.UNKNOWN);
                    }
                }
                // this is the NER label of the token
                String ne = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
                word.setNer(ne);

                //get lemma
                String lemma = token.get(CoreAnnotations.LemmaAnnotation.class);
                word.setLemma(lemma);
            }
        }
        return word;
    }


}
