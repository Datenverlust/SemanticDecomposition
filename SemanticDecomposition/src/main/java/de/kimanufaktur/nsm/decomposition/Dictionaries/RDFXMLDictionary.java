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
 *
 * This class acts as an interface to OWL based ontology knowledge bases.
 *
 * Contributors:
 *
 * Lars Borchert
 *
 */
public class RDFXMLDictionary extends BaseDictionary {
    private static Object _lock  = new Object();
    private static RDFXMLDictionary _instance;

    /**
     * Data crawler
     */
    protected RDFXMLCrawler _crawler;

    /**
     * Language tag
     */
    protected String _language;

    public RDFXMLDictionary(RDFXMLCrawler datasource){
        init(datasource);
    }

    /**
     * Instantiates the dictionary by downlaoding the given ontology from the given document iri.
     * @param source
     */
    public RDFXMLDictionary(String source){
        init(source);
    }

    /**
     * Constructs an english measurmement ontology as knowledge base (or dictionary)
     */
    public RDFXMLDictionary(){
        //TODO: remove DAI-Lab URL and re-host ontology or load locally
       init("http://kimanufaktur.de/Dictionaries/RDF/om-2.0.rdf");
    }

    private void init(String source){
        _language = Config.getInstance().getUserProps().getProperty(Config.LANGUAGE_KEY);
        init (new RDFXMLCrawler(source, _language));
    }

    private void init(RDFXMLCrawler crawler){
        _language = crawler.getLanguage();
        _crawler = crawler;
        //config returns the stopwords from the configuration.
        Set<String> stopWords = Config.getInstance().stopWords();
        _crawler.index(stopWords);
    }

    /**
     * Helper for retrieving IRIs of found entries for given word.
     * @param word
     * @return
     */
    protected List<String> findIRIsForTerm(String word){
        String[] fields = new String[]{Fields.ENTITY_NAME};
        List<String> result =  _crawler.findIRIs(fields, word);
        return result;
    }

    /**
     * Returns the entity name for the given IRI
     * @param entityIRI
     * @return - entity name or NULL
     */
    protected String entityFromIRI(String entityIRI){
        return _crawler.getEntityNameForIRI(entityIRI);
    }

    @Override
    public HashSet<Concept> getSynonyms(Concept word) {
        //get equivalent classes (and try not to use a reasoner, because a reasoner would never classif the default ontology)
        HashSet<Concept> syns = new HashSet<>();
        if(word == null || word.getLitheral() == null){
            System.err.println("concept or concept's literal is null");
            return syns;
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
        // for some entities maybe DisjointClasses / DisjointProperty ?
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
        if (word.getLemma() == null) {
            // if a lemma was not set yet by another dictionary we try to set it here
            setLemma(word);
        }

        if (word.getWordType() == null) {
            word.setWordType(wordType);
        }

        word.getSynonyms().addAll(this.getSynonyms(word));
        word.getAntonyms().addAll(this.getAntonyms(word));
        word.getHypernyms().addAll(this.getHypernyms(word));
        word.getHyponyms().addAll(this.getHyponyms(word));
        word.getMeronyms().addAll(this.getMeronyms(word));

        //This word (or concept) may be a value of an owl annotation property
        //in this case we add the property name as hypernym to refelct
        //that this concept relates to a certain annotation property.
        //For example "Ø" is_a "annotation:symbol"
        //Furthermore iff this concept is a annotation value, we add the
        //entity which is annotated by this concepts annotation as meronym thus
        //we interprete this concept as a part of a detailed description of
        // another thing.

        String[] fields = new String[]{Fields.ANNOTATION_VALUE};
        List<org.apache.lucene.document.Document> docs = _crawler.find(fields,word.getLitheral());
        for(org.apache.lucene.document.Document d: docs){
            String annotationName = d.get(Fields.ENTITY_NAME);
            String annotatedEntity = _crawler.getEntityNameForIRI(d.get(Fields.ANNOTATED_ENTITY_IRI));
            word.getHypernyms().add(getConcept(annotationName));
            word.getMeronyms().add(getConcept(annotatedEntity));
        }


        //i don't get the following part. what shall be accomplished here?
//        List<String> entities = _crawler.findIRIs(word.getLitheral());
//        if (entities == null) {
//            throw new DictionaryDoesNotContainConceptException(word.getLitheral());
//        } else {
//            fillDefinition(word);
//        }
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

    public RDFXMLCrawler getCrawler(){
        return _crawler;
    }
}
