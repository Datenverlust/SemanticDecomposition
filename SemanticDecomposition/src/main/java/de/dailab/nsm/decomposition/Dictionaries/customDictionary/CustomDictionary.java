/*package de.dailab.nsm.decomposition.Dictionaries.customDictionary;

import de.dailab.nsm.decomposition.Concept;
import de.dailab.nsm.decomposition.Decomposition;
import de.dailab.nsm.decomposition.Definition;
import de.dailab.nsm.decomposition.Dictionaries.BaseDictionary;
import de.dailab.nsm.decomposition.WordType;
import de.dailab.nsm.decomposition.exceptions.DictionaryDoesNotContainConceptException;
import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.RandomAccessFile;
import java.util.*;

public class CustomDictionary extends BaseDictionary {

    private static final Logger logger = Logger.getLogger(Decomposition.class);

    private static CustomDictionary instance = null;

    CustomDictionary() {
        init();
    }

    public static CustomDictionary getInstance(){
        if(instance == null){
            instance = new CustomDictionary();
            System.out.println("CustomDictionary intitialised [" + instance.language.getName() + "]");
        }
        return instance;
    }


    private void loadConcept(Concept word) throws DictionaryDoesNotContainConceptException {
        System.out.println("Loading concept: " + word);
        System.out.println("concept is Monday?: " + ("Monday".equals(word.getLitheral())));
        System.out.println("entryLookup contains Monday?: " + indexLookup.containsKey("Monday"));

        if(!indexLookup.containsKey(word.getLitheral())) {
            System.out.println("custom dict does not contain " + word.getLitheral());
            throw new DictionaryDoesNotContainConceptException();
        }
        else
            if(!indexLookup.get(word.getLitheral()).containsKey(word.getWordType().ordinal())) {
                System.out.println("custom dict does not contain " + word.getLitheral() + " " + word.getWordType().ordinal());
                throw new DictionaryDoesNotContainConceptException();
        }
        int internalIndex = indexLookup.get(word.getLitheral()).get(word.getWordType().ordinal()).get(0);
        int fileIndex = indexLookup.get(word.getLitheral()).get(word.getWordType().ordinal()).get(1);

        word.getIds().put(this, internalIndex);

        try {
            boolean definitionLoaded = false;
            Definition def = new Definition();
            List<Concept> defList= new ArrayList<>();
            mainloop:
            while (true) {
                //read stuff from the file
                int connectionType = connectionsFile.readInt();
                System.out.println("Type: " + connectionType);
                //-1 is the end of the current words connections
                if (connectionType == -1) {
                    break mainloop;
                }
                //-2 is the break between definitions
                if(connectionType == -2) {
                    definitionLoaded = false;
                }
                int connectionTarget = connectionsFile.readInt();
                System.out.println(", Target: " + connectionTarget);

                //entryLookup the word and create or load the concept
                ArrayList a  = wordLookup.get(connectionTarget);
                int connectionWordType = (int)a.get(0);
                String connectionWord = (String)a.get(1);

                Concept connectedConcept;

                if (loadedConcepts.containsKey(connectionTarget)) {
                    connectedConcept = loadedConcepts.get(connectionTarget);
                }
                else {
                    connectedConcept = new Concept(connectionWord);
                    connectedConcept.getIds().put(this, fileIndex);
                    connectedConcept.setWordType(WordType.values()[connectionWordType]);
                }


                //check if the definition is done

                if(!definitionLoaded && connectionType == 27) {
                    defList.add(connectedConcept);
                }
                else if(!definitionLoaded && connectionType != 27) {
                    def.setDefinition(defList);
                    word.getDefinitions().add(def);
                }
                else {
                    switch (connectionType) {
                        case -1:
                            break mainloop;
                        case 1:
                            word.getAlternativeSyn().add(connectedConcept);
                            break;
                        case 2:
                            word.getAlternativeAnt().add(connectedConcept);
                            break;
                        case 3:
                            word.getSynonyms().add(connectedConcept);
                            break;
                        case 4:
                            word.getAntonyms().add(connectedConcept);
                            break;
                        case 5:
                            word.getHypernyms().add(connectedConcept);
                            break;
                        case 6:
                            word.getHyponyms().add(connectedConcept);
                            break;
                        case 7:
                            word.getDerivations().add(connectedConcept);
                            break;
                        case 8:
                            word.getArbitraryRelations().add(connectedConcept);
                            break;
                         default:
                             logger.warn("Could not identify connection type");
                    }
                }
            }
        }
        catch (Exception ex) {
            logger.error("Could not load: " + ex.getLocalizedMessage());
        }
    }


    @Override
    public HashSet<Concept> getSynonyms(Concept word) {
        if(!indexLookup.containsKey(word.getLitheral()) ||
                !indexLookup.get(word.getLitheral()).containsKey(word.getWordType().ordinal())) {

            logger.warn("custom dict does not contain " + word.getLitheral());
            return new HashSet<>();
        }
        int index = indexLookup.get(word.getLitheral()).get(word.getWordType().ordinal());
        if(!loadedConcepts.containsKey(index)) {
            try {
                loadConcept(word);
            }
            catch (DictionaryDoesNotContainConceptException ex) {
                logger.warn("custom dict does not contain " + word.getLitheral());
                logger.warn(ex.getLocalizedMessage());
            }
        }
        return loadedConcepts.get(index).getSynonyms();
    }


    @Override
    public HashSet<Concept> getAntonyms(Concept word) {
        if(!indexLookup.containsKey(word.getLitheral()) ||
                !indexLookup.get(word.getLitheral()).containsKey(word.getWordType().ordinal())) {

            logger.warn("custom dict does not contain " + word.getLitheral());
            return new HashSet<>();
        }
        int index = indexLookup.get(word.getLitheral()).get(word.getWordType().ordinal());
        if(!loadedConcepts.containsKey(index)) {
            try {
                loadConcept(word);
            }
            catch (DictionaryDoesNotContainConceptException ex) {
                logger.warn("custom dict does not contain " + word.getLitheral());
                logger.warn(ex.getLocalizedMessage());
            }
        }
        return loadedConcepts.get(index).getAntonyms();
    }

    @Override
    public HashSet<Concept> getHypernyms(Concept word) {
        if(!indexLookup.containsKey(word.getLitheral()) ||
                !indexLookup.get(word.getLitheral()).containsKey(word.getWordType().ordinal())) {

            logger.warn("custom dict does not contain " + word.getLitheral());
            return new HashSet<>();
        }
        int index = indexLookup.get(word.getLitheral()).get(word.getWordType().ordinal());
        if(!loadedConcepts.containsKey(index)) {
            try {
                loadConcept(word);
            }
            catch (DictionaryDoesNotContainConceptException ex) {
                logger.warn("custom dict does not contain " + word.getLitheral());
                logger.warn(ex.getLocalizedMessage());
            }
        }
        return loadedConcepts.get(index).getHypernyms();
    }

    @Override
    public HashSet<Concept> getHyponyms(Concept word) {
        if(!indexLookup.containsKey(word.getLitheral()) ||
                !indexLookup.get(word.getLitheral()).containsKey(word.getWordType().ordinal())) {

            logger.warn("custom dict does not contain " + word.getLitheral());
            return new HashSet<>();
        }
        int index = indexLookup.get(word.getLitheral()).get(word.getWordType().ordinal());
        if(!loadedConcepts.containsKey(index)) {
            try {
                loadConcept(word);
            }
            catch (DictionaryDoesNotContainConceptException ex) {
                logger.warn("custom dict does not contain " + word.getLitheral());
                logger.warn(ex.getLocalizedMessage());
            }
        }
        return loadedConcepts.get(index).getHyponyms();
    }

    @Override
    public HashSet<Concept> getMeronyms(Concept word) {
        if(!indexLookup.containsKey(word.getLitheral()) ||
                !indexLookup.get(word.getLitheral()).containsKey(word.getWordType().ordinal())) {

            logger.warn("custom dict does not contain " + word.getLitheral());
            return new HashSet<>();
        }
        int index = indexLookup.get(word.getLitheral()).get(word.getWordType().ordinal());
        if(!loadedConcepts.containsKey(index)) {
            try {
                loadConcept(word);
            }
            catch (DictionaryDoesNotContainConceptException ex) {
                logger.warn("custom dict does not contain " + word.getLitheral());
                logger.warn(ex.getLocalizedMessage());
            }
        }
        return loadedConcepts.get(index).getMeronyms();
    }

    @Override
    public List<Definition> getDefinitions(Concept word) {
        if(tryToLoadConcept(word)) {

            return new ArrayList<>();
        }
        int index = indexLookup.get(word.getLitheral()).get(word.getWordType().ordinal());
        if(!loadedConcepts.containsKey(index)) {
            try {
                loadConcept(word);
            }
            catch (DictionaryDoesNotContainConceptException ex) {
                logger.warn("custom dict does not contain " + word.getLitheral());
                logger.warn(ex.getLocalizedMessage());
            }
        }
        return new ArrayList<>(loadedConcepts.get(index).getDefinitions());
    }

    @Override
    public Concept fillConcept(Concept word, WordType wordType) throws DictionaryDoesNotContainConceptException {
        loadConcept(word);
        return word;
    }

    @Override
    public Concept getLemma(String word, WordType wordType) {
        return Decomposition.createConcept(word, wordType);
    }

    @Override
    public Concept getConcept(Object id) {
        return null;
    }

    @Override
    public Concept setPOS(Concept word) {
        return word;
    }

    @Override
    public Concept fillDefinition(Concept word) throws DictionaryDoesNotContainConceptException {
        loadConcept(word);
        return word;
    }

    @Override
    public Concept fillRelated(Concept concept) throws DictionaryDoesNotContainConceptException {
        loadConcept(concept);
        return concept;
    }

    private boolean tryToLoadConcept(Concept word) {
        if(!indexLookup.containsKey(word.getLitheral()) ||
                !indexLookup.get(word.getLitheral()).containsKey(word.getWordType().ordinal())) {

            logger.warn("custom dict does not contain " + word.getLitheral());
            return false;
        }
        int internalIndex = indexLookup.get(word.getLitheral()).get(word.getWordType().ordinal()).get(0);
        if(!loadedConcepts.containsKey(internalIndex)) {
            try {
                loadConcept(word);
                return true;
            }
            catch (DictionaryDoesNotContainConceptException ex) {
                logger.warn("custom dict does not contain " + word.getLitheral());
                logger.warn(ex.getLocalizedMessage());
                return false;
            }
        }
        return loadedConcepts.containsKey(internalIndex);
    }


    public void init() {
        super.init();
    }


}
*/