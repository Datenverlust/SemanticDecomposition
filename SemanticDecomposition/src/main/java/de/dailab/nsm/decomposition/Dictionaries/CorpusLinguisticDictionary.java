//package de.dailab.nsm.decomposition.Dictionaries;
//
//import de.dailab.nsm.decomposition.Concept;
//import de.dailab.nsm.decomposition.Definition;
//import de.dailab.nsm.decomposition.WordType;
//
//import de.dailab.nsm.decomposition.dictionaries.corpuslinguistics.CorpusLinguisticStatistics;
//import de.dailab.nsm.decomposition.exceptions.DictionaryDoesNotContainConceptException;
//
//import java.util.HashSet;
//import java.util.List;
//
///**
// * Created by faehndrich on 14.04.16.
// */
//public class CorpusLinguisticDictionary extends IDictionary  {
//
//    private static CorpusLinguisticDictionary instance = null;
//    private CorpusLinguisticStatistics corpusLinguisticStatistics = null;
//    private CorpusLinguisticDictionary(){init();}
//
//    public static  CorpusLinguisticDictionary getInstance(){
//        if(instance == null){
//            instance = new CorpusLinguisticDictionary();
//        }
//        return instance;
//    }
//
//    @Override
//    public void init() {
//        super.init();
//        if (corpusLinguisticStatistics == null) {
//            corpusLinguisticStatistics = new CorpusLinguisticStatistics();
//            corpusLinguisticStatistics.init();
//         }
//    }
//
//    @Override
//    public HashSet<Concept> getSynonyms(Concept word) {
//        return null;
//    }
//
//    @Override
//    public HashSet<Concept> getAntonyms(Concept word) {
//        return null;
//    }
//
//    @Override
//    public HashSet<Concept> getHypernyms(Concept word) {
//        return null;
//    }
//
//    @Override
//    public HashSet<Concept> getHyponyms(Concept word) {
//        return null;
//    }
//
//    @Override
//    public HashSet<Concept> getMeronyms(Concept word) {
//        return null;
//    }
//
//    @Override
//    public List<Definition> getDefinitions(Concept word) {
//        return null;
//    }
//
//    @Override
//    public Concept fillConcept(Concept word, WordType wordType) throws DictionaryDoesNotContainConceptException {
//        return null;
//    }
//
//    @Override
//    Concept getLemma(String word, WordType wordType) {
//        return null;
//    }
//
//    @Override
//    public Concept getConcept(Object id) {
//        return null;
//    }
//
//    @Override
//    public Concept setPOS(Concept word) {
//        return null;
//    }
//
//    @Override
//    public Concept fillDefinition(Concept word) throws DictionaryDoesNotContainConceptException {
//        return null;
//    }
//
//    @Override
//    public Concept fillRelated(Concept concept) throws DictionaryDoesNotContainConceptException {
//        return null;
//    }
//}
