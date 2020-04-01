/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.kimanufaktur.nsm.decomposition;

import de.kimanufaktur.nsm.decomposition.Dictionaries.BaseDictionary;
import de.kimanufaktur.nsm.decomposition.IConcept;
import java.io.Serializable;
import java.util.*;

/** Created by faehndrich on 11.11.14. */
// A concept is a formal conceptualization of the meaning of a word.
// @NodeEntity
// @Entity
public class Concept implements Serializable, IConcept {

  transient Hashtable<BaseDictionary, Object> ids;
  // @Relationship(type = "AlternativeSynonyms")
  protected List<Concept> alternativeSyn = new ArrayList<>();
  // @Relationship(type = "AlternativeAntonyms")
  protected List<Concept> alternativeAnt = new ArrayList<>();

  protected String litheral = null;
  // @Relationship(type = "Part-of-Speech")
  protected WordType wordType = null;
  // @GraphId
  // @PrimaryKey
  protected long id = -1;

  protected int decompositionElementCount = 0;
  protected int decompositionlevel = 0;
  // @Relationship(type = "Synonyms")
  // @SecondaryKey(relate = Relationship.ONE_TO_MANY, relatedEntity = Concept.class)
  protected HashSet<Concept> synonyms;
  // @Relationship(type = "Antonyms")
  protected HashSet<Concept> antonyms;
  // @Relationship(type = "Hypernyms")
  protected HashSet<Concept> hypernyms;
  // @Relationship(type = "Hyponyms")
  protected HashSet<Concept> hyponyms;
  // @Relationship(type = "Meronyms")
  protected HashSet<Concept> meronyms;
  // @Relationship(type = "Definitions")
  protected HashSet<Definition> definitions;
  // @Relationship(type = "Derivations")
  protected HashSet<Concept> derivations;
  protected HashSet<Concept> arbitraryRelations;
  protected String ner = null;
  protected String lemma = null;
  protected String originatedRelationName;

  protected HashMap<String, Set<Concept>> sensekeyToSynonymsMap;
  protected HashMap<String, Set<Concept>> sensekeyToAntonymsMap;
  protected HashMap<String, Set<Concept>> sensekeyToHypernymsMap;
  protected HashMap<String, Set<Concept>> sensekeyToHyponymsMap;
  protected HashMap<String, Set<Concept>> sensekeyToMeronymsMap;
  protected HashMap<String, Definition> sensekeyToDefinitionsMap;

  public Concept() {
    setDecompositionlevel(-1);
    ids = new Hashtable<>();
    synonyms = new HashSet<>();
    antonyms = new HashSet<>();
    hypernyms = new HashSet<>();
    hyponyms = new HashSet<>();
    meronyms = new HashSet<>();
    derivations = new HashSet<>();
    definitions = new HashSet<>();
    arbitraryRelations = new HashSet<>();

    sensekeyToSynonymsMap = new HashMap<>();
    sensekeyToAntonymsMap = new HashMap<>();
    sensekeyToHypernymsMap = new HashMap<>();
    sensekeyToHyponymsMap = new HashMap<>();
    sensekeyToMeronymsMap = new HashMap<>();
    sensekeyToDefinitionsMap = new HashMap<>();
  }

  public Concept(String litheral) {
    this();
    setLitheral(litheral);
    id = Long.valueOf(this.hashCode());
  }

  public Concept(String literal, WordType POS) {
    this(literal);
    this.setWordType(POS);
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public HashSet<Concept> getMeronyms() {
    return meronyms;
  }

  public void setMeronyms(HashSet<Concept> meronyms) {
    this.meronyms = meronyms;
  }

  public HashSet<Concept> getDerivations() {
    return derivations;
  }

  public void setDerivations(HashSet<Concept> derivations) {
    this.derivations = derivations;
  }

  public HashSet<Definition> getDefinitions() {
    return definitions;
  }

  public void setDefinitions(HashSet<Definition> definitions) {
    this.definitions = definitions;
  }

  public HashSet<Concept> getSynonyms() {
    return synonyms;
  }

  public void setSynonyms(HashSet<Concept> synonyms) {
    this.synonyms = synonyms;
  }

  public HashSet<Concept> getAntonyms() {
    return antonyms;
  }

  public void setAntonyms(HashSet<Concept> antonyms) {
    this.antonyms = antonyms;
  }

  public HashSet<Concept> getHypernyms() {
    return hypernyms;
  }

  public void setHypernyms(HashSet<Concept> hypernyms) {
    this.hypernyms = hypernyms;
  }

  public HashSet<Concept> getHyponyms() {
    return hyponyms;
  }

  public void setArbitraryRelations(HashSet<Concept> arbitraryRelations) {
    this.arbitraryRelations = arbitraryRelations;
  }

  public HashSet<Concept> getArbitraryRelations() {
    return arbitraryRelations;
  }

  public void setHyponyms(HashSet<Concept> hyponyms) {
    this.hyponyms = hyponyms;
  }

  public HashMap<String, Set<Concept>> getSensekeyToSynonymsMap() {
    return sensekeyToSynonymsMap;
  }

  public void setSensekeyToSynonymsMap(HashMap<String, Set<Concept>> sensekeyToSynonymsMap) {
    this.sensekeyToSynonymsMap = sensekeyToSynonymsMap;
  }

  public HashMap<String, Set<Concept>> getSensekeyToAntonymsMap() {
    return sensekeyToAntonymsMap;
  }

  public void setSensekeyToAntonymsMap(HashMap<String, Set<Concept>> sensekeyToAntonymsMap) {
    this.sensekeyToAntonymsMap = sensekeyToAntonymsMap;
  }

  public HashMap<String, Set<Concept>> getSensekeyToHypernymsMap() {
    return sensekeyToHypernymsMap;
  }

  public void setSensekeyToHypernymsMap(HashMap<String, Set<Concept>> sensekeyToHypernymsMap) {
    this.sensekeyToHypernymsMap = sensekeyToHypernymsMap;
  }

  public HashMap<String, Set<Concept>> getSensekeyToHyponymsMap() {
    return sensekeyToHyponymsMap;
  }

  public void setSensekeyToHyponymsMap(HashMap<String, Set<Concept>> sensekeyToHyponymsMap) {
    this.sensekeyToHyponymsMap = sensekeyToHyponymsMap;
  }

  public HashMap<String, Set<Concept>> getSensekeyToMeronymsMap() {
    return sensekeyToMeronymsMap;
  }

  public void setSensekeyToMeronymsMap(HashMap<String, Set<Concept>> sensekeyToMeronymsMap) {
    this.sensekeyToMeronymsMap = sensekeyToMeronymsMap;
  }

  public HashMap<String, Definition> getSensekeyToDefinitionsMap() {
    return sensekeyToDefinitionsMap;
  }

  public void setSensekeyToDefinitionsMap(HashMap<String, Definition> sensekeyToDefinitionsMap) {
    this.sensekeyToDefinitionsMap = sensekeyToDefinitionsMap;
  }

  public int getDecompositionElementCount() {
    int tmpdecompositionElementCount = 0;
    for (Definition def : getDefinitions()) {
      tmpdecompositionElementCount += def.getDefinition().size();
    }
    tmpdecompositionElementCount += getSynonyms().size();
    tmpdecompositionElementCount += getAntonyms().size();
    tmpdecompositionElementCount += getHypernyms().size();
    tmpdecompositionElementCount += getHyponyms().size();
    tmpdecompositionElementCount += getMeronyms().size();
    tmpdecompositionElementCount += getArbitraryRelations().size();
    return decompositionElementCount;
  }

  public void setDecompositionElementCount(int decompositionElementCount) {
    this.decompositionElementCount = decompositionElementCount;
  }

  public String getNer() {
    return ner;
  }

  public void setNer(String ner) {
    this.ner = ner;
  }

  public int getDecompositionlevel() {
    return decompositionlevel;
  }

  public void setDecompositionlevel(int decompositionlevel) {
    this.decompositionlevel = decompositionlevel;
  }

  public Dictionary<BaseDictionary, Object> getIds() {
    if (ids == null) {
      ids = new Hashtable<>();
    }
    return ids;
  }

  public Object getId4Dictionary(BaseDictionary dict) {
    return ids.get(dict);
  }

  /**
   * Add a id for a given Dictionary for faster access.
   *
   * @param dict the dict in which the concept is described
   * @param id the id of the concept in the dict.
   */
  public void AddId(BaseDictionary dict, Object id) {
    this.ids.put(dict, id);
  }

  public WordType getWordType() {
    return wordType;
  }

  public void setWordType(WordType wordType) {
    try {
      this.wordType = wordType;
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
    }
  }

  public Collection<Concept> getDecomposition() {
    Set<Concept> decomposition = new HashSet<>();
    for (Definition def : getDefinitions()) {
      decomposition.addAll(def.getDefinition());
    }
    decomposition.addAll(getSynonyms());
    decomposition.addAll(getHypernyms());
    decomposition.addAll(getHyponyms());
    decomposition.addAll(getAntonyms());
    decomposition.addAll(getArbitraryRelations());
    return decomposition;
  }

  public Collection<Concept> getAllFeatures() {
    Collection<Concept> features = new ArrayList<>();
    if (this.getSynonyms() != null && this.getSynonyms().size() > 0)
      features.addAll(this.getSynonyms());
    if (this.getAntonyms() != null && this.getAntonyms().size() > 0)
      features.addAll(this.getAntonyms());
    return features;
  }

  public List<Concept> getAlternativeSyn() {
    return alternativeSyn;
  }

  public void setAlternativeSyn(List<Concept> alternativeSyn) {
    this.alternativeSyn = alternativeSyn;
  }

  public List<Concept> getAlternativeAnt() {
    return alternativeAnt;
  }

  public void setAlternativeAnt(List<Concept> alternativeAnt) {
    this.alternativeAnt = alternativeAnt;
  }

  public String getLitheral() {
    return litheral;
  }

  protected void setLitheral(String litheral) {
    this.litheral = litheral;
  }

  /**
   * identifies the concept.
   *
   * @return an integer identifying the concept. TODO: For now this is not dependent of the
   *     definition used. One concept per word, with all definitions. TODO: We use the decomposition
   *     depth in the id. Thus the same concept decomposed in multiple levels are stored
   *     differently.
   */
  @Override
  public int hashCode() {
    int hash = this.litheral.hashCode();
    if (this.id == -1) {
      this.setId(
          Long.valueOf(
              hash)); // TODO: fix this, here the concepts should be seperated by their definitions
                      // (Word sens etc....)
    }
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == null || obj == null) {
      return false;
    }
    return this.hashCode() == obj.hashCode();
  }

  @Override
  public String toString() {
    return this.litheral;
  }

  public String getLemma() {
    return lemma;
  }

  public void setLemma(String lemma) {
    this.lemma = lemma;
  }

  public String getOriginatedRelationName() {
    return originatedRelationName;
  }

  public void setOriginatedRelationName(String originatedRelationName) {
    this.originatedRelationName = originatedRelationName;
  }

  //    private void writeObject(ObjectOutputStream oos) throws IOException {
  //        // default serialization
  //        oos.defaultWriteObject();
  //        // write the object
  //        oos.writeUTF(litheral);
  //        oos.writeObject(ids);
  //        oos.writeObject(wordType);
  //        oos.writeObject(synonyms);
  //        oos.writeObject(antonyms);
  //        oos.writeObject(hypernyms);
  //        oos.writeObject(hyponyms);
  //        oos.writeObject(decomposition);
  //        oos.writeObject(definitions);
  //        oos.writeObject(derivations);
  //        if(ner !=null){
  //            oos.writeUTF(ner);
  //        }
  //
  //    }
  //
  //    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
  //        // default deserialization
  //        ois.defaultReadObject();
  //        Concept concept = (Concept)ois.readObject();
  //        //concept = new Locconceptation(ois.readInt(), ois.readInt(), ois.readInt(),
  // ois.readInt());
  //        // ... more code
  //
  //    }

  //    @Override
  //    public void writeExternal(ObjectOutput oo) throws IOException
  //    {
  //        oo.writeObject(userName);
  //        oo.writeObject(roll);
  //    }
  //
  //    @Override
  //    public void readExternal(ObjectInput oi) throws IOException, ClassNotFoundException
  //    {
  //        userName = (String)oi.readObject();
  //        roll = (Integer)oi.readObject();
  //    }

}
