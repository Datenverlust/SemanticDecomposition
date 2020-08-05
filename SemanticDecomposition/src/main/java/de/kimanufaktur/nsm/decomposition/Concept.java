/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.kimanufaktur.nsm.decomposition;

import de.kimanufaktur.nsm.decomposition.Dictionaries.BaseDictionary;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** Created by faehndrich on 11.11.14. */
// A concept is a formal conceptualization of the meaning of a word.
// @NodeEntity
// @Entity
public class Concept implements Serializable, de.kimanufaktur.nsm.decomposition.IConcept {

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
  protected HashMap<String, Set<Concept>> senseKeyToSynonymsMap;
  // @Relationship(type = "Antonyms")
  protected HashMap<String, Set<Concept>> senseKeyToAntonymsMap;
  // @Relationship(type = "Hypernyms")
  protected HashMap<String, Set<Concept>> senseKeyToHypernymsMap;
  // @Relationship(type = "Hyponyms")
  protected HashMap<String, Set<Concept>> senseKeyToHyponymsMap;
  // @Relationship(type = "Meronyms")
  protected HashMap<String, Set<Concept>> senseKeyToMeronymsMap;
  // @Relationship(type = "Definitions")
  protected HashSet<Definition> definitions;
  // @Relationship(type = "Derivations")
  protected HashSet<Concept> derivations;
  protected HashSet<Concept> arbitraryRelations;
  protected String ner = null;
  protected String lemma = null;
  protected String originatedRelationName;

  protected Set<String> assignedSenseKeys;
  protected Boolean negated;

  public Concept() {
    setDecompositionlevel(-1);
    ids = new Hashtable<>();
    senseKeyToSynonymsMap = new HashMap<>();
    senseKeyToAntonymsMap = new HashMap<>();
    senseKeyToHypernymsMap = new HashMap<>();
    senseKeyToHyponymsMap = new HashMap<>();
    senseKeyToMeronymsMap = new HashMap<>();
    derivations = new HashSet<>();
    definitions = new HashSet<>();
    arbitraryRelations = new HashSet<>();
    assignedSenseKeys = new HashSet<>();
    negated = false;
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

  public HashSet<Concept> getSynonyms() {
    HashSet<Concept> result = new HashSet<>();
    for(Set<Concept> conceptSet: senseKeyToSynonymsMap.values()){
      result.addAll(conceptSet);
    }
    return result;
  }

  public HashMap<String, Set<Concept>> getSenseKeyToSynonymsMap() {
    return senseKeyToSynonymsMap;
  }

  public void setSenseKeyToSynonymsMap(HashMap<String, Set<Concept>> senseKeyToSynonymsMap) {
    this.senseKeyToSynonymsMap = senseKeyToSynonymsMap;
  }

  public HashSet<Concept> getAntonyms() {
    HashSet<Concept> result = new HashSet<>();
    for(Set<Concept> conceptSet: senseKeyToAntonymsMap.values()){
      result.addAll(conceptSet);
    }
    return result;
  }

  public HashMap<String, Set<Concept>> getSenseKeyToAntonymsMap() {
    return senseKeyToAntonymsMap;
  }

  public void setSenseKeyToAntonymsMap(HashMap<String, Set<Concept>> senseKeyToAntonymsMap) {
    this.senseKeyToAntonymsMap = senseKeyToAntonymsMap;
  }

  public HashSet<Concept> getHypernyms() {
    HashSet<Concept> result = new HashSet<>();
    for(Set<Concept> conceptSet: senseKeyToHypernymsMap.values()){
      result.addAll(conceptSet);
    }
    return result;
  }

  public HashMap<String, Set<Concept>> getSenseKeyToHypernymsMap() {
    return senseKeyToHypernymsMap;
  }

  public void setSenseKeyToHypernymsMap(HashMap<String, Set<Concept>> senseKeyToHypernymsMap) {
    this.senseKeyToHypernymsMap = senseKeyToHypernymsMap;
  }

  public HashSet<Concept> getHyponyms() {
    HashSet<Concept> result = new HashSet<>();
    for(Set<Concept> conceptSet: senseKeyToHyponymsMap.values()){
      result.addAll(conceptSet);
    }
    return result;
  }
  public HashMap<String, Set<Concept>>
  getSenseKeyToHyponymsMap() {
    return senseKeyToHyponymsMap;
  }

  public void setSenseKeyToHyponymsMap(
      HashMap<String, Set<Concept>>
          senseKeyToHyponymsMap) {
    this.senseKeyToHyponymsMap = senseKeyToHyponymsMap;
  }

  public HashSet<Concept> getMeronyms() {
    HashSet<Concept> result = new HashSet<>();
    for(Set<Concept> conceptSet: senseKeyToMeronymsMap.values()){
      result.addAll(conceptSet);
    }
    return result;
  }

  public HashMap<String, Set<Concept>>
  getSenseKeyToMeronymsMap() {
    return senseKeyToMeronymsMap;
  }

  public void setSenseKeyToMeronymsMap(
      HashMap<String, Set<Concept>>
          senseKeyToMeronymsMap) {
    this.senseKeyToMeronymsMap = senseKeyToMeronymsMap;
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

  public void setArbitraryRelations(HashSet<Concept> arbitraryRelations) {
    this.arbitraryRelations = arbitraryRelations;
  }

  public HashSet<Concept> getArbitraryRelations() {
    return arbitraryRelations;
  }

  public Set<String> getAssignedSenseKeys() {
    return assignedSenseKeys;
  }

  public void setAssignedSenseKeys(Set<String> assignedSenseKeys) {
    this.assignedSenseKeys = assignedSenseKeys;
  }

  public Boolean getNegated() {
    return negated;
  }

  public void setNegated(Boolean negated) {
    this.negated = negated;
  }

  public int getDecompositionElementCount() {
    int tmpdecompositionElementCount = 0;
    for (Definition def : getDefinitions()) {
      tmpdecompositionElementCount += def.getDefinition().size();
    }
    tmpdecompositionElementCount += getSenseKeyToSynonymsMap().size();
    tmpdecompositionElementCount += getSenseKeyToAntonymsMap().size();
    tmpdecompositionElementCount += getSenseKeyToHypernymsMap().size();
    tmpdecompositionElementCount += getSenseKeyToHyponymsMap().size();
    tmpdecompositionElementCount += getSenseKeyToMeronymsMap().size();
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
    for (Definition def : definitions) {
      String senseKey = def.getSensekey();
      decomposition.addAll(getSenseKeyToSynonymsMap().get(senseKey));
      decomposition.addAll(getSenseKeyToHypernymsMap().get(senseKey));
      decomposition.addAll(getSenseKeyToHyponymsMap().get(senseKey));
      decomposition.addAll(getSenseKeyToAntonymsMap().get(senseKey));
    }
    decomposition.addAll(getArbitraryRelations());
    return decomposition;
  }

  public Collection<Concept> getAllFeatures() { // TODO: fix the commented lines
    Collection<Concept> features = new ArrayList<>();
    if (this.getSenseKeyToSynonymsMap() != null && this.getSenseKeyToSynonymsMap().size() > 0) {
      //      features.addAll(this.getSynonyms());
    }
    if (this.getSenseKeyToAntonymsMap() != null && this.getSenseKeyToAntonymsMap().size() > 0) {
      //      features.addAll(this.getAntonyms());
    }
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
    int hash = Objects.hash(this.litheral, this.assignedSenseKeys, this.negated);
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
