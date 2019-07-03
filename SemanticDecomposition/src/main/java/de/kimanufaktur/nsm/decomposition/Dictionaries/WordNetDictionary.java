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
import de.kimanufaktur.nsm.decomposition.dictionaries.wordnet.WordNetCrawler;
import de.kimanufaktur.nsm.decomposition.exceptions.DictionaryDoesNotContainConceptException;
import edu.mit.jwi.item.ISenseKey;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;


/**
 * An implementation of the @see IDictionary for wordNet. For this to work wordnet needs to be installed and the path
 * int the WordNetCrawler needs to be set.
 * Created by faehndrich on 12.11.14.
 */
public class WordNetDictionary extends BaseDictionary {
    private static final Logger logger = Logger.getLogger(Decomposition.class);
    private static WordNetDictionary instance = null;
    Pattern p = Pattern.compile("\\w+(-\\d+)?");
    private WordNetCrawler wordNetCrawler = null;

    private WordNetDictionary() {
        init();
    }

    public static WordNetDictionary getInstance() {
        if (instance == null) {
            instance = new WordNetDictionary();
        }
        return instance;
    }

    @Override
    public void init() {
        if (wordNetCrawler == null) {
            wordNetCrawler = new WordNetCrawler();
            wordNetCrawler.initWordNet();
        }
    }

    @Override
    public HashSet<Concept> getSynonyms(Concept word) {
        HashSet<Concept> synonyms = new HashSet<>();
        Set<IWord> syn = new HashSet<>();
        try {
            syn = wordNetCrawler.getSynonyms(word.getLitheral(), POS.valueOf(word.getWordType().type()), 10);
        } catch (IllegalArgumentException e) {
            //e.printStackTrace();
            syn = wordNetCrawler.getSynonyms(word.getLitheral(), POS.NOUN, 10);
            if (syn.size() == 0) {
                syn = wordNetCrawler.getSynonyms(word.getLitheral(), POS.VERB, 10);
            }
            if (syn.size() == 0) {
                syn = wordNetCrawler.getSynonyms(word.getLitheral(), POS.ADJECTIVE, 10);
            }
            if (syn.size() == 0) {
                syn = wordNetCrawler.getSynonyms(word.getLitheral(), POS.ADVERB, 10);
            }
        }
        for (IWord w : syn) {
            Concept s = getConcept(w.getID());
            if (Decomposition.checkIsPrime(s)) {
                s = Decomposition.getPrimeofConcept(s);
            } else {
                Concept knownConcept = Decomposition.getKnownConcept(s);
                if (knownConcept.getDecompositionlevel() >= 0) {
                    s = knownConcept;
                }
            }
            synonyms.add(s);
        }
        //logger.debug("Synonyms for " + word.getLitheral() + " are " + synonyms.toString());
        return synonyms;
    }

    @Override
    public HashSet<Concept> getAntonyms(Concept word) {
        HashSet<Concept> antonyms = new HashSet<>();
        Set<IWord> ant = new HashSet<>();
        try {
            ant = wordNetCrawler.getAntonymes(word.getLitheral(), POS.valueOf(word.getWordType().type()), 10);
        } catch (IllegalArgumentException e) {
            //e.printStackTrace();
            ant = wordNetCrawler.getAntonymes(word.getLitheral(), POS.NOUN, 10);
            if (ant.size() == 0) {
                ant = wordNetCrawler.getAntonymes(word.getLitheral(), POS.VERB, 10);
            }
            if (ant.size() == 0) {
                ant = wordNetCrawler.getAntonymes(word.getLitheral(), POS.ADJECTIVE, 10);
            }
            if (ant.size() == 0) {
                ant = wordNetCrawler.getAntonymes(word.getLitheral(), POS.ADVERB, 10);
            }
        }
        for (IWord w : ant) {
            Concept s = getConcept(w.getID());
            antonyms.add(s);
        }
        //logger.debug("Antonyms for " + word.getLitheral() + " are " + antonyms.toString());
        return antonyms;
    }

    @Override
    public HashSet<Concept> getHypernyms(Concept word) {
        HashSet<Concept> hypernyms = new HashSet<>();
        Set<IWord> hyper = new HashSet<>();
        try {
            hyper = wordNetCrawler.getHypernyms(word.getLitheral(), POS.valueOf(word.getWordType().type()), 10);
        } catch (IllegalArgumentException e) {
            //e.printStackTrace();
            hyper = wordNetCrawler.getHypernyms(word.getLitheral(), POS.NOUN, 10);
            if (hyper.size() == 0) {
                hyper = wordNetCrawler.getHypernyms(word.getLitheral(), POS.VERB, 10);
            }
            if (hyper.size() == 0) {
                hyper = wordNetCrawler.getHypernyms(word.getLitheral(), POS.ADJECTIVE, 10);
            }
            if (hyper.size() == 0) {
                hyper = wordNetCrawler.getHypernyms(word.getLitheral(), POS.ADVERB, 10);
            }
        }
        for (IWord w : hyper) {
            if (w.getID() != null) {
                Concept s = getConcept(w.getID());
                hypernyms.add(s);
            }
        }
        //logger.debug("Hypernyms for " + word.getLitheral() + " are " + hypernyms.toString());
        return hypernyms;
    }

    @Override
    public HashSet<Concept> getHyponyms(Concept word) {
        HashSet<Concept> hyponyms = new HashSet<>();
        Set<IWord> hypo = new HashSet<>();
        try {
            hypo = wordNetCrawler.getHyponyms(word.getLitheral(), POS.valueOf(word.getWordType().type()), 10);
        } catch (IllegalArgumentException e) {
            //e.printStackTrace();
            hypo = wordNetCrawler.getHypernyms(word.getLitheral(), POS.NOUN, 10);
            if (hypo.size() == 0) {
                hypo = wordNetCrawler.getHypernyms(word.getLitheral(), POS.VERB, 10);
            }
            if (hypo.size() == 0) {
                hypo = wordNetCrawler.getHypernyms(word.getLitheral(), POS.ADJECTIVE, 10);
            }
            if (hypo.size() == 0) {
                hypo = wordNetCrawler.getHypernyms(word.getLitheral(), POS.ADVERB, 10);
            }
        }
        for (IWord w : hypo) {
            Concept s = getConcept(w.getID());
            hyponyms.add(s);
        }
        //logger.debug("Hyponyms for " + word.getLitheral() + " are " + hyponyms.toString());
        return hyponyms;
    }

    @Override
    public HashSet<Concept> getMeronyms(Concept word) {
        HashSet<Concept> meronyms = new HashSet<>();
        Set<IWord> mero = new HashSet<>();
        try {
            mero = wordNetCrawler.getMeronyms(word.getLitheral(), POS.valueOf(word.getWordType().type()), 10);
        } catch (IllegalArgumentException e) {
            //e.printStackTrace();
            mero = wordNetCrawler.getMeronyms(word.getLitheral(), POS.NOUN, 10);
            if (mero.size() == 0) {
                mero = wordNetCrawler.getMeronyms(word.getLitheral(), POS.VERB, 10);
            }
            if (mero.size() == 0) {
                mero = wordNetCrawler.getMeronyms(word.getLitheral(), POS.ADJECTIVE, 10);
            }
            if (mero.size() == 0) {
                mero = wordNetCrawler.getMeronyms(word.getLitheral(), POS.ADVERB, 10);
            }
        }
        for (IWord w : mero) {
            Concept m = getConcept(w.getID());
            m.setWordType(WordType.getType(w.getPOS().toString()));
            if (Decomposition.checkIsPrime(m)) {
                m = Decomposition.getPrimeofConcept(m);
            } else {
                Concept knownConcept = Decomposition.getKnownConcept(m);
                if (knownConcept.getDecompositionlevel() >= 0) {
                    m = knownConcept;
                }
            }
            meronyms.add(m);
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
        List<IWord> wlist = getWord(word.getLitheral(), word.getWordType());
        for (IWord w : wlist) {
            String gloss = w.getSynset().getGloss();
            String deftxt = gloss.substring(gloss.indexOf(';') + 1);
            result.add(new Definition(deftxt));
        }
        return result;
    }

    @Override
    public Concept fillConcept(Concept word, WordType wordType) throws DictionaryDoesNotContainConceptException {
        if (word.getWordType() == null) {
            if (wordType != null) {
                word.setWordType(wordType);
            } else {
                setPOS(word);
            }
        }
        if (word.getSynonyms() == null || word.getSynonyms().size() < 1) {
            word.setSynonyms(this.getSynonyms(word));
        } else {
            word.getSynonyms().addAll(this.getSynonyms(word));
        }
        word.getAntonyms().addAll(this.getAntonyms(word));
        word.getHypernyms().addAll(this.getHypernyms(word));
        word.getHyponyms().addAll(this.getHyponyms(word));
        word.getMeronyms().addAll(this.getMeronyms(word));
        //fillRelated(word);
        fillDefinition(word);
        return word;
    }

    @Override
    public Concept getLemma(String word, WordType wordType) {
        //logger.debug("Filling: " + word + " with word type: " + wordType.type());
        return Decomposition.createConcept(word, wordType);
    }

    /**
     * Get the word with the given ID. This is a shortcut for WordNet. Sometimes we have a concept and its ID and
     * can find it using the id. The Ids are stored in the concepts ids properties. Key is the dictionary you want
     * the id for, since the same word hast different ids in  different dictionaries.
     *
     * @param id IWordID which identifies the word we search.
     * @return the conceptualization of the word with the given WordNet id.
     */
    @Override
    public Concept getConcept(Object id) {
        IWord w = null;
        IWordID wid = null;
        Concept result = null;
        try {
            wid = (IWordID) id;
            w = wordNetCrawler.getWord(wid);
            if (w != null) {
                result = Decomposition.createConcept(w.getLemma());
                result.getIds().put(this, id);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public Concept fillRelated(Concept concept) throws DictionaryDoesNotContainConceptException {
        List<IWord> wordlist = getWord(concept.getLitheral(), concept.getWordType());
        if (wordlist == null || wordlist.size() == 0) {
            throw new DictionaryDoesNotContainConceptException("WordNet does not contain the concept " + concept.getLitheral());
        }
        for (IWord word : wordlist) {
            for (IWordID w : word.getRelatedWords()) {
                Concept related = Decomposition.createConcept(w.getLemma(), concept.getWordType());
                related.setWordType(WordType.getType(w.getSynsetID().getPOS().getTag()));
                concept.getDerivations().add(related);
            }
        }
        return concept;
    }

    @Override
    public Concept fillDefinition(Concept word) throws DictionaryDoesNotContainConceptException {
        List<IWord> wlist = getWord(word.getLitheral(), word.getWordType());
        if (wlist == null || wlist.size() < 1) {
            throw new DictionaryDoesNotContainConceptException(word.getLitheral());
        }

        for (IWord w : wlist) {
            //String deftxt = w.getSynset().getGloss().replace(';', '.');
            if (w.getPOS().name().equals(word.getWordType().type())) {
                String gloss = w.getSynset().getGloss();
                String deftxt = null;
                try {
                    deftxt = gloss.substring(0, gloss.indexOf(';')); //get the first definition. This is done to cut of the example sentences. For more fuzzyness remove this.
                } catch (StringIndexOutOfBoundsException noSemicolonException) {
                    deftxt = gloss; //there was no semicolon in the definition so we take the howl sentence.
                }
                //Definition tmp = new Definition(deftxt);
                //TODO: analyze definition, on where a e.g. a example sentence begins.
                Definition definition = new Definition(deftxt);
                definition.setTerm(word);
                for (IWord synsetWord : w.getSynset().getWords()) {
                    ISenseKey senskey = synsetWord.getSenseKey();
                    definition.setSensekey(senskey.toString());
                }
                word.getDefinitions().add(definition);
            }
        }
        return word;
    }

    /**
     * Get the IWord from the WordNet Database
     *
     * @param word     the literal value we are looking for.
     * @param wordType the @WordType we want to look up.
     * @return the IWord from WordNet to the given word.
     */
    private List<IWord> getWord(String word, WordType wordType) {
        POS wordPOS = null;
        try {
            wordPOS = POS.valueOf(wordType.type());
        } catch (IllegalArgumentException e) {
            //e.printStackTrace();
            List<IWord> result = wordNetCrawler.getWord(word, POS.NOUN);
            //logger.debug("Concept " + word + " with WordType: " + wordType.type() + "is unknown. Using " + POS.NOUN + ".");
            if (result == null) {
                result = wordNetCrawler.getWord(word, POS.VERB);
                //logger.debug("Concept " + word + " with WordType: " + wordType.type() + "is unknown. Using " + POS.VERB + ".");
            }
            if (result == null) {
                result = wordNetCrawler.getWord(word, POS.ADJECTIVE);
                //logger.debug("Concept " + word + " with WordType: " + wordType.type() + "is unknown. Using " + POS.ADJECTIVE + ".");
            }
            if (result == null) {
                result = wordNetCrawler.getWord(word, POS.ADVERB);
                //logger.debug("Concept " + word + " with WordType: " + wordType.type() + "is unknown. Using " + POS.ADVERB + ".");
            }
            return result;
        }
        return wordNetCrawler.getWord(word, wordPOS);
    }


    @Override
    public Concept setPOS(Concept word) {
//        MaxentTagger tagger = new MaxentTagger("taggers/left3words-distsim-wsj-0-18.tagger");
//        String tag = tagger.tagString(word.getLitheral());
        // Properties props = new Properties();
        //props.put("annotators", "pos, ner");
        //StanfordCoreNLP pipelinePOS = new StanfordCoreNLP(props);

        Annotation document = new Annotation(word.getLitheral());
        pipeline.annotate(document);
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);

        for (CoreMap sen : sentences) {
            // traversing the words in the current sentence
            // a CoreLabel is a CoreMap with additional token-specific methods
            for (CoreLabel token : sen.get(CoreAnnotations.TokensAnnotation.class)) {

                // this is the POS tag of the token
                String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                if (!pos.equals("") && word.getWordType() == null && !pos.isEmpty()) {
                    WordType t = null;
                    try {
                        t = WordType.valueOf(pos);
                        if (t == null) {
                            word.setWordType(WordType.UNKNOWN);
                        } else {
                            word.setWordType(t);
                        }
                    } catch (IllegalArgumentException notype) {
//                        for(char car : pos.toCharArray()){
//                            System.out.println("Type which is missing is: " +  String.valueOf((int) car));
//                        }
                        //notype.printStackTrace();
                        word.setWordType(WordType.UNKNOWN);
                    }
                }

                // this is the NER label of the token
                String ne = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
                if (ne != null) {
                    word.setNer(ne);
                }
                //get lemma
                String lemma = token.get(CoreAnnotations.LemmaAnnotation.class);
                word.setLemma(lemma);

            }
        }
        return word;
    }
}
