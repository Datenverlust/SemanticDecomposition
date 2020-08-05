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
import de.kimanufaktur.nsm.decomposition.dictionaries.wikidata.WikidataCrawler;
import de.kimanufaktur.nsm.decomposition.dictionaries.wikidata.WikidataItem;
import de.kimanufaktur.nsm.decomposition.exceptions.DictionaryDoesNotContainConceptException;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.function.Function;

/**
 * Created by tkoenig on 14/06/16.
 */
public class WikidataDictionary extends BaseDictionary {
    private static WikidataDictionary instance = null;
    private WikidataCrawler wikidataCrawler;
    private HashMap<String, WikidataItem> entityCache;
    private HashMap<String, HashSet<Concept>> relationsCache;

    private WikidataDictionary() {
        init();
    }

    /**
     * lazily get an dictionary instance
     * @return instance of WikidataDictionary
     */
    public static WikidataDictionary getInstance() {
        if (instance == null) {
            instance = new WikidataDictionary();
            instance.init();
        }
        return instance ;
    }

    /**
     * initialize the dictionary instance
     */
    @Override
    public void init() {
        super.init();
        if (wikidataCrawler == null) {
            wikidataCrawler = WikidataCrawler.getSharedInstance();
        }
    }

    /**
     * Get the Wikidata Entity from the Wikidata Database
     * TODO: choose the definition. We do return the first definition, always.
     *
     * @param word     the literal value we are looking for.
     * @param wordType the @WordType we want to look up.
     * @return The Wikidata Entity for the given word
     */
    private WikidataItem getWord(String word, WordType wordType) {
        return wikidataCrawler.findItemByName(word); // TODO introduct a "searchEntity" method, since we don't have the id here
                                                     // and also pass wordType to make selection more accurate
    }

    /**
     * Get the word with the given ID.
     *
     * @param id Wikidata Entity Id which identifies the word we search.
     * @return the conceptualization of the word with the given Wikidata Entity Id.
     */
    @Override
    public Concept getConcept(Object id) {
        WikidataItem entity = wikidataCrawler.findItemById((long) id);
        Concept concept = Decomposition.createConcept(entity.getTitle(), WordType.NN);
        concept.setId(entity.getId());
        return concept;
    }

    public HashSet<Concept> getArbitraryRelations(Concept word) {
        return getRelations(word, "arbitraryRelations", WikidataItem::getArbitraryRelations);
    }

    //TODO: fix return values of concept methods
    @Override
    public HashMap<String, Set<Concept>> getSynonyms(Concept word) {
//        return getRelations(word, "synonyms", WikidataItem::getSynonyms);
        return new HashMap<>();
    }

    @Override
    public HashMap<String, Set<Concept>> getAntonyms(Concept word) {
//        return getRelations(word, "antonyms", WikidataItem::getAntonyms);
        return new HashMap<>();
    }

    @Override
    public HashMap<String, Set<Concept>> getHypernyms(Concept word) {
//        return getRelations(word, "hypernyms", WikidataItem::getHypernyms);
        return new HashMap<>();
    }

    @Override
    public HashMap<String, Set<Concept>> getHyponyms(Concept word) {
//        return getRelations(word, "hyponyms", WikidataItem::getHyponyms);
        return new HashMap<>();
    }

    @Override
    public HashMap<String, Set<Concept>> getMeronyms(Concept word) {
//        return getRelations(word, "meronyms", WikidataItem::getMeronyms);
        return new HashMap<>();
    }

    @Override
    public List<Definition> getDefinitions(Concept word) {
        List<Definition> retval = new ArrayList<>();

        WikidataItem entity = entityForConcept(word);
        Definition definition = new Definition(entity.getDescription());
        retval.add(definition);

        return retval;
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

        WikidataItem entity = entityForConcept(word);
        if (entity == null) return word;

//        System.out.println("Processing \"" + word.getLitheral() + "\"");

        word.getSenseKeyToSynonymsMap().putAll(getSynonyms(word));
        word.getSenseKeyToAntonymsMap().putAll(getAntonyms(word));
        word.getSenseKeyToHypernymsMap().putAll(getHypernyms(word));
        word.getSenseKeyToHyponymsMap().putAll(getHyponyms(word));
        word.getSenseKeyToMeronymsMap().putAll(getMeronyms(word));
        //word.getArbitraryRelations().addAll(getArbitraryRelations(word));

        if (entity == null) {
            throw new DictionaryDoesNotContainConceptException(word.getLitheral());
        } else {
//            TODO somehow, definitions are also decomposed which results in non-cached entities being processes, which
//            basically makes the program unusable due to runtime ... so ... don't use it?
//            fillDefinition(word);
        }
//        System.out.println("Processed \"" + word.getLitheral() + "\" " + word.getDecompositionlevel());

        return word;
    }

    @Override
    public Concept fillDefinition(Concept word) {
        WikidataItem entity = entityForConcept(word);
        String description = entity.getDescription();
        if (description != null) {
            Definition definition = new Definition(description);
            word.getDefinitions().add(definition);
        }
        return word;
    }

    @Override
    public Concept fillRelated(Concept word) {
        WikidataItem entity = entityForConcept(word);
        entity.getRelatedItems().stream().forEach(relatedItem -> {
            Concept relatedConcept = new Concept(relatedItem.getTitle());
            relatedConcept.setId(relatedItem.getId());
            word.getDerivations().add(relatedConcept);
        });
        return word;
    }

    @Override
    public Concept getLemma(String word, WordType wordType) {
        return Decomposition.createConcept(word, wordType);
    }

    @Override
    public Concept setPOS(Concept word) {
        Annotation document = new Annotation(word.getLitheral());
        pipeline.annotate(document);
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        for (CoreMap sentence : sentences) {
            // traversing the words in the current sentence
            // a CoreLabel is a CoreMap with additional token-specific methods
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                // this is the POS tag of the token
                String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                if (!pos.equals("") && word.getWordType() == null) {
                    WordType t = null;
                    try {
                        t = WordType.valueOf(pos);
                        word.setWordType(t);
                    } catch (IllegalArgumentException notype) {
                        notype.printStackTrace();
                        word.setWordType(null);
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

    @Override
    public void flushCache() {
//        getEntityCache().clear();
//        getRelationsCache().clear();
    }

    /**
     * Delegate the retrieval of relations of an entity to the appropriate getter method
     *
     * @param word the concept that is used for as the source of the relations
     * @param getter an instance method of WikidataItem that is used to retrieve the relations
     * @return a hashset of concepts that were retrieved by the getter
     */
    private HashSet<Concept> getRelations(Concept word, String relationName, Function<WikidataItem, HashSet<WikidataItem>> getter) {
        WikidataItem entity = entityForConcept(word);
        HashSet<Concept> retval = new HashSet<>();

        HashSet<Concept> cachedRelations = getCachedRelations(entity, word.getDecompositionlevel(), relationName);
        if (cachedRelations != null) return cachedRelations;

        if (entity != null) {
            getter.apply(entity).stream().forEach(synonym -> {
                // TODO this excludes values and only includes entities
                if (synonym.getWdid().startsWith("Q") && synonym.getTitle().length() > 0) {
                    Concept concept = new Concept(synonym.getTitle());
                    concept.setId(synonym.getId());
                    concept.setOriginatedRelationName(synonym.getOriginatedRelationName());
                    concept.setWordType(word.getWordType());
                    concept.setDecompositionlevel(word.getDecompositionlevel() - 1);
                    retval.add(concept);
                    cacheEntity(synonym, concept);
                    if (concept.getDecompositionlevel() > 0) {
                        try {
                            fillConcept(concept, concept.getWordType());
                        } catch (DictionaryDoesNotContainConceptException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            cacheRelations(entity, word.getDecompositionlevel(), relationName, retval);
        }
        return retval;
    }

    /**
     * Return the entity that matches a given concept best.
     *
     * @param word the concept that is used to search for a matching entity
     * @return the best matching wikidata entity
     */
    private WikidataItem entityForConcept(Concept word) {
        WikidataItem entity = getCachedEntity(word);
        if (entity != null) return entity;
        entity = wikidataCrawler.findItemByName(word.getLitheral());
        if (entity == null) return null;
        System.out.println("entity for concept: " + entity.getTitle() + " (" + entity.getWdid() + ") was found for query string: \"" + word.getLitheral() + "\"");
        cacheEntity(entity, word);
        return entity;
    }

    private void cacheEntity(WikidataItem entity, Concept word) {
        getEntityCache().put(word.getLitheral(), entity);
    }

    private WikidataItem getCachedEntity(Concept word) {
        return getEntityCache().get(word.getLitheral());
    }

    private HashMap<String, WikidataItem> getEntityCache() {
        if (entityCache == null) {
            entityCache = new HashMap<>();
        }
        return entityCache;
    }

    private void cacheRelations(WikidataItem entity, int level, String relationName, HashSet<Concept> relations) {
        getRelationsCache().put(entity.getWdid() + "::" + level + "::" + relationName, relations);
    }

    private HashSet<Concept> getCachedRelations(WikidataItem entity, int level, String relationName) {
        if (entity == null) return null;
        return getRelationsCache().get(entity.getWdid() + "::" + level + "::" + relationName);
    }

    private HashMap<String, HashSet<Concept>> getRelationsCache() {
        if (relationsCache == null) {
            relationsCache  = new HashMap<>();
        }
        return relationsCache ;
    }
}
