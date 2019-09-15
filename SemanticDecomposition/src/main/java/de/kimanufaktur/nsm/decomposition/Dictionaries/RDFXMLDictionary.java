/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.kimanufaktur.nsm.decomposition.Dictionaries;

import de.kimanufaktur.nsm.decomposition.Concept;
import de.kimanufaktur.nsm.decomposition.Decomposition;
import de.kimanufaktur.nsm.decomposition.Definition;
import de.kimanufaktur.nsm.decomposition.WordType;
import de.kimanufaktur.nsm.decomposition.dictionaries.rdfxmlcrawler.RDFXMLCrawler;
import de.kimanufaktur.nsm.decomposition.exceptions.DictionaryDoesNotContainConceptException;
import de.kimanufaktur.nsm.decomposition.settings.Config;
import ontology.index.indexer.core.Fields;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Johannes Fähndrich on 18.07.18 as part of his dissertation.
 */
public class RDFXMLDictionary extends BaseDictionary {
    private static Object _lock  = new Object();
    private static RDFXMLDictionary _instance;

    /**
     * Data crawler
     */
    private RDFXMLCrawler _crawler;

    /**
     * Language tag
     */
    private String _language;

    /**
     * Default ontology
     */
    //private String _source = "http://dainas.dai-labor.de/~faehndrich@dai/Dictionaries/RDF/om-2.0.rdf";

//    public static RDFXMLDictionary getInstance(){
//        //default: Measurement Ontology
//        return getInstance("http://dainas.dai-labor.de/~faehndrich@dai/Dictionaries/RDF/om-2.0.rdf");
//    }
//
//    public static RDFXMLDictionary getInstance(String customSource){
//        if(_instance == null){
//            //grap lock only if needed, meaning only when we actually need to instantiate
//            synchronized (_lock){
//                //While we are waiting for the lock, another thread might have
//                //caused the instantiation already
//                if(_instance == null){
//                    _instance = new RDFXMLDictionary(customSource);
//                }
//            }
//        }
//        return _instance;
//    }

//    private RDFXMLDictionary(String source){
//        if (_crawler == null) {
//            _language = Config.getInstance().getUserProps().getProperty(Config.LANGUAGE_KEY);
//            _crawler = new RDFXMLCrawler(source, _language);
//            //config returns the stopwords from the configuration.
//            Set<String> stopWords = Config.getInstance().stopWords();
//            _crawler.init();
//            _crawler.index(stopWords);
//        }
//
//        System.out.println(String.format("RDFXMLDictionary [%s] [%s]", source, _language));
//
//    }

    public RDFXMLDictionary(String source){
        init(source);

    }
    public RDFXMLDictionary(){
       init("http://dainas.dai-labor.de/~faehndrich@dai/Dictionaries/RDF/om-2.0.rdf");

    }

    private void init(String source){
        _language = Config.getInstance().getUserProps().getProperty(Config.LANGUAGE_KEY);
        _crawler = new RDFXMLCrawler(source, _language);
        //config returns the stopwords from the configuration.
        Set<String> stopWords = Config.getInstance().stopWords();
        _crawler.index(stopWords);
    }

    /**
     * Helper for retrieving IRIs of found entries for given word.
     * @param word
     * @return
     */
    private List<String> findIRIsForTerm(String word){
        String[] fields = new String[]{Fields.ENTITY_NAME};
        List<String> result =  _crawler.findIRIs(fields, word);
        return result;
    }

    /**
     * Returns the entity name for the given IRI
     * @param entityIRI
     * @return - entity name or NULL
     */
    private String entityFromIRI(String entityIRI){
        return _crawler.getEntityNameForIRI(entityIRI);
    }

    @Override
    public HashSet<Concept> getSynonyms(Concept word) {
        //get equivalent classes (and try not to use a reasoner, because a reasoner would never classif the default ontology)
        HashSet<Concept> syns = new HashSet<>();
        if(word == null || word.getLitheral() == null){
            System.out.println("word is null");
        }
        List<String> iris = findIRIsForTerm(word.getLitheral());
        //assuming best match is iri for searched entity

        if(iris.size() > 0){
            String entityIRI = iris.get(0);
            Set<String> equivalentEntities = _crawler.sameAs(entityIRI);
            for(String entIRI : equivalentEntities){
                syns.add(getConcept(entityFromIRI(entIRI)));
            }
        }
        return syns;
    }

    @Override
    public HashSet<Concept> getAntonyms(Concept word) {
        //difficult for this dictionary. what would antonyms be in owl terms?
        return new HashSet<>();
    }

    @Override
    public HashSet<Concept> getHypernyms(Concept word) {
        //parent classes or super classes
        HashSet<Concept> hypers = new HashSet<>();
        List<String> iris = findIRIsForTerm(word.getLitheral());
        //assuming best match is iri for searched entity
        if(iris.size() > 0){
            String entityIRI = iris.get(0);
            Set<String> equivalentEntities = _crawler.parents(entityIRI);
            for(String entIRI : equivalentEntities){
                hypers.add(getConcept(entityFromIRI(entIRI)));
            }
        }
        return hypers;
    }

    @Override
    public HashSet<Concept> getHyponyms(Concept word) {
        // child classes or sub classes
        HashSet<Concept> hypos = new HashSet<>();
        List<String> iris = findIRIsForTerm(word.getLitheral());
        //assuming best match is iri for searched entity
        if(iris.size() > 0){
            String entityIRI = iris.get(0);
            Set<String> equivalentEntities = _crawler.children(entityIRI);
            for(String entIRI : equivalentEntities){
                hypos.add(getConcept(entityFromIRI(entIRI)));
            }
        }
        return hypos;
    }

    @Override
    public HashSet<Concept> getMeronyms(Concept word) {
        //all properties in the domain of the concept
        //hm or all properties in which the concept occurs? Discuss
        HashSet<Concept> merons = new HashSet<>();
        List<String> iris = findIRIsForTerm(word.getLitheral());
        //assuming best match is iri for searched entity
        if(iris.size() > 0){
            String entityIRI = iris.get(0);
            Set<String> equivalentEntities = _crawler.propertiesFor(entityIRI);
            for(String entIRI : equivalentEntities){
                merons.add(getConcept(entityFromIRI(entIRI)));
            }
        }
        return merons;
    }

    @Override
    public List<Definition> getDefinitions(Concept word) {
        //perhaps we can use annotations here or so or we use a more general dicitonary like
        //Wiktionary
        return null;
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
        List<String> entities = _crawler.findIRIs(word.getLitheral());
        if (entities == null) {
            throw new DictionaryDoesNotContainConceptException(word.getLitheral());
        } else {
            fillDefinition(word);
        }
        //logger.debug("filling concept: " + word.getLitheral() + " with POS: " + word.getWordType());
        return word;
    }

    @Override
    public Concept getLemma(String word, WordType wordType) {
        //not possible for this dictionary
        return Decomposition.createConcept(word, wordType);

    }

    /**
     * Constructs a concept.
     * In this case the id is the word itself.
     * @param id the dictionary specific id of a word sense.
     * @return  - empty concept with the given word set as literal.
     */
    @Override
    public Concept getConcept(Object id) {
        String word = "";
        if(id != null){
            word = (String)id;
        }
        Concept c = Decomposition.createConcept(word);
        return c;
    }

    @Override
    public Concept setPOS(Concept word) {
        //not possible for this dictionary
        return word;
    }

    @Override
    public Concept fillDefinition(Concept word) throws DictionaryDoesNotContainConceptException {
        //TODO: where to get a deffinition from?
        return word;
    }

    @Override
    public Concept fillRelated(Concept concept) throws DictionaryDoesNotContainConceptException {
        //TODO: what to do here?
        return concept;
    }
}
