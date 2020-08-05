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

import java.util.*;
import java.util.regex.Pattern;

/**
 * An implementation of the @see IDictionary for wordNet. For this to work wordnet needs to be
 * installed and the path int the WordNetCrawler needs to be set. Created by faehndrich on 12.11.14.
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
    super.init();
    if (wordNetCrawler == null) {
      wordNetCrawler = new WordNetCrawler();
      wordNetCrawler.initWordNet();
    }
  }

  @Override
  public HashMap<String, Set<Concept>> getSynonyms(Concept word) {
    HashMap<String, Set<Concept>> sensekeyToSynonymsMap = new HashMap<>();
    Map<String, Set<IWord>> synonymsMap;
    try {
      synonymsMap =
          wordNetCrawler.getSynonyms(
              word.getLitheral(), POS.valueOf(word.getWordType().type()), 10);
    } catch (IllegalArgumentException e) {
      // e.printStackTrace();
      synonymsMap = wordNetCrawler.getSynonyms(word.getLitheral(), POS.NOUN, 10);
      if (synonymsMap.size() == 0) {
        synonymsMap = wordNetCrawler.getSynonyms(word.getLitheral(), POS.VERB, 10);
      }
      if (synonymsMap.size() == 0) {
        synonymsMap = wordNetCrawler.getSynonyms(word.getLitheral(), POS.ADJECTIVE, 10);
      }
      if (synonymsMap.size() == 0) {
        synonymsMap = wordNetCrawler.getSynonyms(word.getLitheral(), POS.ADVERB, 10);
      }
    }
    for (String senseKey : synonymsMap.keySet()) {
      Set<Concept> conceptSet = new HashSet<>();
      for (IWord w : synonymsMap.get(senseKey)) {
        Concept s = getConcept(w.getID());
        if (Decomposition.checkIsPrime(s)) {
          s = Decomposition.getPrimeofConcept(s);
        } else {
          Concept knownConcept = Decomposition.getKnownConcept(s);
          if (knownConcept.getDecompositionlevel() >= 0) {
            s = knownConcept;
          }
        }
        if(!s.equals(word)) conceptSet.add(s);
      }
      if (!conceptSet.isEmpty()) sensekeyToSynonymsMap.put(senseKey, conceptSet);
    }
    return sensekeyToSynonymsMap;
  }

  @Override
  public HashMap<String, Set<Concept>> getAntonyms(Concept word) {
    HashMap<String, Set<Concept>> sensekeyToAntonymsMap = new HashMap<>();
    Map<String, Set<IWord>> antonymsMap;
    try {
      antonymsMap =
          wordNetCrawler.getSensekeyToAntonymsMap(
              word.getLitheral(), POS.valueOf(word.getWordType().type()), 10);
    } catch (IllegalArgumentException e) {
      antonymsMap = wordNetCrawler.getSensekeyToAntonymsMap(word.getLitheral(), POS.NOUN, 10);
      if (antonymsMap.size() == 0) {
        antonymsMap = wordNetCrawler.getSensekeyToAntonymsMap(word.getLitheral(), POS.VERB, 10);
      }
      if (antonymsMap.size() == 0) {
        antonymsMap =
            wordNetCrawler.getSensekeyToAntonymsMap(word.getLitheral(), POS.ADJECTIVE, 10);
      }
      if (antonymsMap.size() == 0) {
        antonymsMap = wordNetCrawler.getSensekeyToAntonymsMap(word.getLitheral(), POS.ADVERB, 10);
      }
    }
    for (String senseKey : antonymsMap.keySet()) {
      Set<Concept> conceptSet = new HashSet<>();
      for (IWord w : antonymsMap.get(senseKey)) {
        Concept s = getConcept(w.getID());
        conceptSet.add(s);
      }
      if (!conceptSet.isEmpty()) sensekeyToAntonymsMap.put(senseKey, conceptSet);
    }
    return sensekeyToAntonymsMap;
  }

  @Override
  public HashMap<String, Set<Concept>> getHypernyms(Concept word) {
    HashMap<String, Set<Concept>> sensekeyToHypernymsMap = new HashMap<>();
    Map<String, Set<IWord>> hypernymsMap;
    try {
      hypernymsMap =
          wordNetCrawler.getSensekeyToHypernymsMap(
              word.getLitheral(), POS.valueOf(word.getWordType().type()), 10);
    } catch (IllegalArgumentException e) {
      // e.printStackTrace();
      hypernymsMap = wordNetCrawler.getSensekeyToHypernymsMap(word.getLitheral(), POS.NOUN, 10);
      if (hypernymsMap.size() == 0) {
        hypernymsMap = wordNetCrawler.getSensekeyToHypernymsMap(word.getLitheral(), POS.VERB, 10);
      }
      if (hypernymsMap.size() == 0) {
        hypernymsMap =
            wordNetCrawler.getSensekeyToHypernymsMap(word.getLitheral(), POS.ADJECTIVE, 10);
      }
      if (hypernymsMap.size() == 0) {
        hypernymsMap = wordNetCrawler.getSensekeyToHypernymsMap(word.getLitheral(), POS.ADVERB, 10);
      }
    }
    for (String senseKey : hypernymsMap.keySet()) {
      Set<Concept> conceptSet = new HashSet<>();
      for (IWord w : hypernymsMap.get(senseKey)) {
        if (w.getID() != null) {
          Concept s = getConcept(w.getID());
          conceptSet.add(s);
        }
      }
      if (!conceptSet.isEmpty()) sensekeyToHypernymsMap.put(senseKey, conceptSet);
    }
    // logger.debug("Hypernyms for " + word.getLitheral() + " are " + hypernyms.toString());
    return sensekeyToHypernymsMap;
  }

  @Override
  public HashMap<String, Set<Concept>> getHyponyms(Concept word) {
    HashMap<String, Set<Concept>> sensekeyToHyponymsMap = new HashMap<>();
    Map<String, Set<IWord>> hyponymsMap;
    try {
      hyponymsMap =
          wordNetCrawler.getSensekeyToHyponymsMap(
              word.getLitheral(), POS.valueOf(word.getWordType().type()), 10);
    } catch (IllegalArgumentException e) {
      // e.printStackTrace();
      hyponymsMap = wordNetCrawler.getSensekeyToHyponymsMap(word.getLitheral(), POS.NOUN, 10);
      if (hyponymsMap.size() == 0) {
        hyponymsMap = wordNetCrawler.getSensekeyToHyponymsMap(word.getLitheral(), POS.VERB, 10);
      }
      if (hyponymsMap.size() == 0) {
        hyponymsMap =
            wordNetCrawler.getSensekeyToHyponymsMap(word.getLitheral(), POS.ADJECTIVE, 10);
      }
      if (hyponymsMap.size() == 0) {
        hyponymsMap = wordNetCrawler.getSensekeyToHyponymsMap(word.getLitheral(), POS.ADVERB, 10);
      }
    }
    for (String senseKey : hyponymsMap.keySet()) {
      Set<Concept> conceptSet = new HashSet<>();
      for (IWord w : hyponymsMap.get(senseKey)) {
        Concept s = getConcept(w.getID());
        conceptSet.add(s);
      }
      if (!conceptSet.isEmpty()) sensekeyToHyponymsMap.put(senseKey, conceptSet);
    }
    return sensekeyToHyponymsMap;
  }

  @Override
  public HashMap<String, Set<Concept>> getMeronyms(Concept word) {
    HashMap<String, Set<Concept>> sensekeyToMeronymsMap = new HashMap<>();
    Map<String, Set<IWord>> meronymsMap;
    try {
      meronymsMap =
          wordNetCrawler.getSensekeyToMeronymsMap(
              word.getLitheral(), POS.valueOf(word.getWordType().type()), 10);
    } catch (IllegalArgumentException e) {
      meronymsMap = wordNetCrawler.getSensekeyToMeronymsMap(word.getLitheral(), POS.NOUN, 10);
      if (meronymsMap.size() == 0) {
        meronymsMap = wordNetCrawler.getSensekeyToMeronymsMap(word.getLitheral(), POS.VERB, 10);
      }
      if (meronymsMap.size() == 0) {
        meronymsMap =
            wordNetCrawler.getSensekeyToMeronymsMap(word.getLitheral(), POS.ADJECTIVE, 10);
      }
      if (meronymsMap.size() == 0) {
        meronymsMap = wordNetCrawler.getSensekeyToMeronymsMap(word.getLitheral(), POS.ADVERB, 10);
      }
    }
    for (String senseKey : meronymsMap.keySet()) {
      Set<Concept> conceptSet = new HashSet<>();
      for (IWord w : meronymsMap.get(senseKey)) {
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
        conceptSet.add(m);
      }
      if (!conceptSet.isEmpty()) sensekeyToMeronymsMap.put(senseKey, conceptSet);
    }
    return sensekeyToMeronymsMap;
  }

  /**
   * get all definitions of the concept. Here not only is-a sentences are returned but also example
   * phrases.
   *
   * @param word the concept to took up the definitions for.
   * @return the list fo definitions found in WordNet.
   */
  @Override
  public List<Definition> getDefinitions(Concept word) {
    List<Definition> result = new ArrayList<>();
    Map<String, IWord> wMap = getSenseKeyToWordMap(word.getLitheral(), word.getWordType());
    for (String senseKey : wMap.keySet()) {
      IWord w = wMap.get(senseKey);
      String gloss = w.getSynset().getGloss();
      String deftxt = gloss.substring(gloss.indexOf(';') + 1);
      Definition definition = new Definition(deftxt);
      definition.setSensekey(senseKey);
      result.add(definition);
    }
    return result;
  }

  @Override
  public Concept fillConcept(Concept word, WordType wordType)
      throws DictionaryDoesNotContainConceptException {
    if (word.getWordType() == null) {
      if (wordType != null) {
        word.setWordType(wordType);
      } else {
        setPOS(word);
      }
    }
    if (word.getSenseKeyToSynonymsMap() == null || word.getSenseKeyToSynonymsMap().size() < 1) {
      word.setSenseKeyToSynonymsMap(this.getSynonyms(word));
    } else {
      word.getSenseKeyToSynonymsMap().putAll(this.getSynonyms(word));
    }

    word.getSenseKeyToAntonymsMap().putAll(this.getAntonyms(word));

    word.getSenseKeyToHypernymsMap().putAll(this.getHypernyms(word));

    word.getSenseKeyToHyponymsMap().putAll(this.getHyponyms(word));

    word.getSenseKeyToMeronymsMap().putAll(this.getMeronyms(word));
    // fillRelated(word)
        fillDefinition(word);
    return word;
  }

  @Override
  public Concept getLemma(String word, WordType wordType) {
    // logger.debug("Filling: " + word + " with word type: " + wordType.type());
    return Decomposition.createConcept(word, wordType);
  }

  /**
   * Get the word with the given ID. This is a shortcut for WordNet. Sometimes we have a concept and
   * its ID and can find it using the id. The Ids are stored in the concepts ids properties. Key is
   * the dictionary you want the id for, since the same word hast different ids in different
   * dictionaries.
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
      throw new DictionaryDoesNotContainConceptException(
          "WordNet does not contain the concept " + concept.getLitheral());
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
    Map<String, IWord> wMap = getSenseKeyToWordMap(word.getLitheral(), word.getWordType());
    if (wMap == null || wMap.size() < 1) {
      throw new DictionaryDoesNotContainConceptException(word.getLitheral());
    }

    for (String senseKey : wMap.keySet()) {
      IWord w = wMap.get(senseKey);
      if (w.getPOS().name().equals(word.getWordType().type())) {
        String gloss = w.getSynset().getGloss();
        String deftxt = null;
        try {
          deftxt =
              gloss.substring(
                  0,
                  gloss.indexOf(
                      ';')); // get the first definition. This is done to cut of the example
          // sentences. For more fuzzyness remove this.
        } catch (StringIndexOutOfBoundsException noSemicolonException) {
          deftxt = gloss; // there was no semicolon in the definition so we take the howl sentence.
        }
//         Definition tmp = new Definition(deftxt);
        // TODO: analyze definition, on where a e.g. a example sentence begins.
        Definition definition = new Definition(deftxt);
        definition.setTerm(word);
        definition.setSensekey(senseKey);
        if (!word.getDefinitions().contains(definition)) {
          word.getDefinitions().add(definition);
        }
      }
    }
    return word;
  }

  /**
   * Get the IWord from the WordNet Database
   *
   * @param word the literal value we are looking for.
   * @param wordType the @WordType we want to look up.
   * @return the IWord from WordNet to the given word.
   */
  private List<IWord> getWord(String word, WordType wordType) {
    POS wordPOS = null;
    try {
      wordPOS = POS.valueOf(wordType.type());
    } catch (IllegalArgumentException e) {
      // e.printStackTrace();
      List<IWord> result = wordNetCrawler.getWord(word, POS.NOUN);
      // logger.debug("Concept " + word + " with WordType: " + wordType.type() + "is unknown. Using
      // " + POS.NOUN + ".");
      if (result == null) {
        result = wordNetCrawler.getWord(word, POS.VERB);
        // logger.debug("Concept " + word + " with WordType: " + wordType.type() + "is unknown.
        // Using " + POS.VERB + ".");
      }
      if (result == null) {
        result = wordNetCrawler.getWord(word, POS.ADJECTIVE);
        // logger.debug("Concept " + word + " with WordType: " + wordType.type() + "is unknown.
        // Using " + POS.ADJECTIVE + ".");
      }
      if (result == null) {
        result = wordNetCrawler.getWord(word, POS.ADVERB);
        // logger.debug("Concept " + word + " with WordType: " + wordType.type() + "is unknown.
        // Using " + POS.ADVERB + ".");
      }
      return result;
    }
    return wordNetCrawler.getWord(word, wordPOS);
  }

  private Map<String, IWord> getSenseKeyToWordMap(String word, WordType wordType) {
    POS wordPOS = null;
    try {
      wordPOS = POS.valueOf(wordType.type());
    } catch (IllegalArgumentException e) {
      // e.printStackTrace();
      Map<String, IWord> result = wordNetCrawler.getSenseKeyToWordMap(word, POS.NOUN);
      // logger.debug("Concept " + word + " with WordType: " + wordType.type() + "is unknown. Using
      // " + POS.NOUN + ".");
      if (result == null) {
        result = wordNetCrawler.getSenseKeyToWordMap(word, POS.VERB);
        // logger.debug("Concept " + word + " with WordType: " + wordType.type() + "is unknown.
        // Using " + POS.VERB + ".");
      }
      if (result == null) {
        result = wordNetCrawler.getSenseKeyToWordMap(word, POS.ADJECTIVE);
        // logger.debug("Concept " + word + " with WordType: " + wordType.type() + "is unknown.
        // Using " + POS.ADJECTIVE + ".");
      }
      if (result == null) {
        result = wordNetCrawler.getSenseKeyToWordMap(word, POS.ADVERB);
        // logger.debug("Concept " + word + " with WordType: " + wordType.type() + "is unknown.
        // Using " + POS.ADVERB + ".");
      }
      return result;
    }
    return wordNetCrawler.getSenseKeyToWordMap(word, wordPOS);
  }

  @Override
  public Concept setPOS(Concept word) {
    //        MaxentTagger tagger = new MaxentTagger("taggers/left3words-distsim-wsj-0-18.tagger");
    //        String tag = tagger.tagString(word.getLitheral());
    // Properties props = new Properties();
    // props.put("annotators", "pos, ner");
    // StanfordCoreNLP pipelinePOS = new StanfordCoreNLP(props);

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
            //                            System.out.println("Type which is missing is: " +
            // String.valueOf((int) car));
            //                        }
            // notype.printStackTrace();
            word.setWordType(WordType.UNKNOWN);
          }
        }

        // this is the NER label of the token
        String ne = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
        if (ne != null) {
          word.setNer(ne);
        }
        // get lemma
        String lemma = token.get(CoreAnnotations.LemmaAnnotation.class);
        word.setLemma(lemma);
      }
    }
    return word;
  }
}
