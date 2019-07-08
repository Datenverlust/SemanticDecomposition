package de.dailab.nsm.decomposition.Dictionaries.customDictionary;

import de.dailab.nsm.decomposition.Concept;
import de.dailab.nsm.decomposition.Definition;
import de.dailab.nsm.decomposition.WordType;
import de.dailab.nsm.decomposition.exceptions.DictionaryDoesNotContainConceptException;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.*;

public class CustomGraph {

    private static final Logger logger = Logger.getLogger(CustomGraph.class);

    private String connectionsPlaceholderFilePath = "/home/patrick/.decomposition/Custom/EN/connectionsPlaceholder";
    private String connectionsFilePath = "/home/patrick/.decomposition/Custom/EN/connections";
    private String connectedIndexFilePath = "/home/patrick/.decomposition/Custom/EN/index";

    private RandomAccessFile connectionsFile;

    ArrayList<CustomEntry> entries = new ArrayList<>();

    public CustomGraph() {
        try {
            connectionsFile = new RandomAccessFile(connectionsFilePath, "r");
        }
        catch (IOException ex) {
            System.out.println("Could not open graph file: " + ex.getLocalizedMessage());
            ex.printStackTrace();
        }

    }

    // <word, <type, entry>>
    protected Hashtable<String, Hashtable<WordType, CustomEntry>> entryLookup = new Hashtable<>();


    private CustomEntry getEntryForId(int id) throws DictionaryDoesNotContainConceptException{
        if(!entries.contains(id)) {
            throw new DictionaryDoesNotContainConceptException();
        }
        CustomEntry entry = entries.get(id);

        loadEntry(entry);

        return entry;
    }

    private void loadEntry(CustomEntry entry) {
        if(entry.loaded) {
            return;
        }

        loadConceptFromFile(entry);

    }

    protected CustomEntry getEntryFromConcept(Concept concept) {
        try {
            CustomEntry entry = getEntryForWordAndType(concept.getLitheral(), concept.getWordType());
            return entry;
        }
        catch (DictionaryDoesNotContainConceptException ex) {
            CustomEntry entry = addConcept(concept);
            return entry;
        }
    }

    public Concept getConceptForId(int id) throws DictionaryDoesNotContainConceptException {
        return getEntryForId(id).concept;
    }

    public Concept getConceptForWordAndType(String word, WordType type) throws DictionaryDoesNotContainConceptException {
        return getEntryForWordAndType(word, type).concept;
    }

    public CustomEntry getEntryForWordAndType(String word, WordType type) throws DictionaryDoesNotContainConceptException {
        if(word == null || type == null) {
            throw new DictionaryDoesNotContainConceptException();
        }

        if(!entryLookup.containsKey(word)) {
            throw new DictionaryDoesNotContainConceptException();
        }
        if(!entryLookup.get(word).containsKey(type)) {
            throw new DictionaryDoesNotContainConceptException();
        }

        return entryLookup.get(word).get(type);
    }

    public CustomEntry addConcept(Concept concept) {
        System.out.println("Adding concept: " + concept);
        String word = concept.getLitheral();
        WordType type = concept.getWordType();

        if(word == null) {
            //what to do here?
            return null;
        }
        if (type == null) {
            type = WordType.UNKNOWN;
            concept.setWordType(WordType.UNKNOWN);
        }

        if(entryLookup.containsKey(word)
        && entryLookup.get(word).contains(type)) {
            // word and type already exists, might be a mistake, or loop
            return null;
        }
        int index = entries.size();
        CustomEntry entry = new CustomEntry(index, concept);
        entries.add(entry);
        addEntryToLookup(entry);

        concept.getDecomposition().forEach(connectedConcept -> addConcept(connectedConcept));
        concept.getDefinitions().forEach(
                definition -> definition.getDefinition().forEach(
                        connectedConcept -> addConcept(connectedConcept)
                )
        );
        return entry;
    }

    private void addEntry(CustomEntry entry) {

    }

    private void addEntry(ListIterator<CustomEntry> it, CustomEntry entry) {
        it.add(entry);
    }


    private void addEntryToLookup(CustomEntry entry) {
        if (!entryLookup.containsKey(entry.word)) {
            entryLookup.put(entry.word, new Hashtable<>());
        }
        System.out.println("Adding entry to lookup: " + entry);

        //FIXME: Not a fitting null check here. This should be solved somewhere general
        if(entry.type == null) {
            entry.type = WordType.UNKNOWN;
        }
        entryLookup.get(entry.word).put(entry.type, entry);
    }


    private void storeLookupMap() {
        System.out.println("Storing entryLookup");
        try {
            FileOutputStream connectedIndexFile;
            connectedIndexFile = new FileOutputStream(connectedIndexFilePath, false);
            ObjectOutputStream out = new ObjectOutputStream(connectedIndexFile);
            out.writeObject(entryLookup);
        }
        catch (Exception ex) {
            System.out.println("Could not store entryLookup: " + ex.getLocalizedMessage());
        }
    }

    private void loadLookupMap() {
        System.out.println("Loading entryLookup");
        try {
            FileInputStream connectedIndexFile;
            connectedIndexFile = new FileInputStream(connectedIndexFilePath);
            ObjectInputStream in = new ObjectInputStream(connectedIndexFile);
            entryLookup = (Hashtable<String, Hashtable<WordType, CustomEntry>>) in.readObject();
        }
        catch (Exception ex) {
            System.out.println("Could not store entryLookup: " + ex.getLocalizedMessage());
        }
    }

    private void storeGraph() {
        try {
            System.out.println("Storing entries: " + entries.size());

            RandomAccessFile connectionsPlaceholderFile;
            connectionsPlaceholderFile =
                    new RandomAccessFile(connectionsPlaceholderFilePath, "rw");
            connectionsPlaceholderFile.setLength(0);
            RandomAccessFile connectionsFile;
            connectionsFile =
                    new RandomAccessFile(connectionsFilePath, "rw");
            connectionsFile.setLength(0);


            System.out.println("Storing graph placeholder");

            //TODO: calculate index, instead of running everything twice
            ListIterator it = entries.listIterator();
            while(it.hasNext()) {
                CustomEntry entry = (CustomEntry) it.next();
                writeEntry(connectionsPlaceholderFile, entry);
            }


            System.out.println("Storing graph");
            for (CustomEntry entry : entries) {
                writeEntry(connectionsFile, entry);
            }
        }
        catch (IOException ex) {
            System.out.println("Could not store everything: " + ex.getLocalizedMessage());
            ex.printStackTrace();
        }
    }


    private void writeEntry(RandomAccessFile file, CustomEntry entry) {
        //this will write a single entries references into the connections file

        Concept concept = entry.concept;

        System.out.println("Logging everything about: " + entry);
        System.out.println(("Definitions: " + concept.getDefinitions().size()));

        try {

            for (Definition def : concept.getDefinitions()) {
                System.out.println("Got definition");
                storeDefinition(file, def);
            }

            for (Concept connection : concept.getAlternativeSyn()) {
                System.out.println("Got 1");
                storeConnection(file, connection, 1);
            }

            for (Concept connection : concept.getAlternativeAnt()) {
                System.out.println("Got 2");
                storeConnection(file, connection, 2);
            }

            for (Concept connection : concept.getSynonyms()) {
                System.out.println("Got 3");
                storeConnection(file, connection, 3);
            }

            for (Concept connection : concept.getAntonyms()) {
                System.out.println("Got 4");
                storeConnection(file, connection, 4);
            }

            for (Concept connection : concept.getHypernyms()) {
                System.out.println("Got 5");
                storeConnection(file, connection, 5);
            }

            for (Concept connection : concept.getHyponyms()) {
                System.out.println("Got 6");
                storeConnection(file, connection, 6);
            }

            for (Concept connection : concept.getDerivations()) {
                System.out.println("Got 7");
                storeConnection(file, connection, 7);
            }

            for (Concept connection : concept.getArbitraryRelations()) {
                System.out.println("Got 8");
                storeConnection(file, connection, 8);
            }
            //Delimiter, for the list of connections
            file.writeInt(-1);
        }
        catch (IOException ex) {
            System.out.println("Could not write into connections file: " + ex.getLocalizedMessage());
        }
    }

    private void storeConnection(RandomAccessFile file, Concept concept, int type) throws IOException {
        storeConnection(file, getEntryFromConcept(concept), type);
    }

    private void storeConnection(RandomAccessFile file, CustomEntry entry, int type) throws IOException {

        int internalIndex = entry.index;
        int fileIndex = entry.fileIndex;
        System.out.println("Storing: "+type+", "+internalIndex+ ", " + fileIndex);
        // store the type of the connection
        file.writeInt(type);

        //Store the connection reference
        file.writeInt(internalIndex);

        //Store the connection reference
        file.writeInt(fileIndex);
    }

    private void storeDefinition(RandomAccessFile file, Definition def) throws IOException {
        for(Concept concept : def.getDefinition()) {
            storeConnection(file, concept, 27);
        }
        file.writeInt(-2);
    }


    private Concept loadConcept(int index) throws DictionaryDoesNotContainConceptException {
        if(!entries.contains(index)) {
            throw new DictionaryDoesNotContainConceptException();
        }
        return loadConceptFromFile(entries.get(index));
    }

    private Concept loadConcept(String word, WordType type) throws DictionaryDoesNotContainConceptException{
        if(!entryLookup.containsKey(word)) {
            throw new DictionaryDoesNotContainConceptException();
        }
        if(!entryLookup.get(word).containsKey(type)) {
            throw new DictionaryDoesNotContainConceptException();
        }
        return loadConceptFromFile(entryLookup.get(word).get(type));
    }


    private Concept loadConceptFromFile(CustomEntry entry) {
        System.out.println("Loading entry: " + entry);

        int fileIndex = entry.fileIndex;
        Concept concept = entry.concept;

        try {
            connectionsFile.seek(fileIndex);
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
                System.out.println("Target: " + connectionTarget);

                CustomEntry connectedEntry = getEntryForId(connectionTarget);

                //check if the definition is done

                if(!definitionLoaded && connectionType == 27) {
                    defList.add(connectedEntry.concept);
                }
                else if(!definitionLoaded && connectionType != 27) {
                    def.setDefinition(defList);
                    concept.getDefinitions().add(def);
                }
                else {
                    switch (connectionType) {
                        case -1:
                            break mainloop;
                        case 1:
                            concept.getAlternativeSyn().add(connectedEntry.concept);
                            break;
                        case 2:
                            concept.getAlternativeAnt().add(connectedEntry.concept);
                            break;
                        case 3:
                            concept.getSynonyms().add(connectedEntry.concept);
                            break;
                        case 4:
                            concept.getAntonyms().add(connectedEntry.concept);
                            break;
                        case 5:
                            concept.getHypernyms().add(connectedEntry.concept);
                            break;
                        case 6:
                            concept.getHyponyms().add(connectedEntry.concept);
                            break;
                        case 7:
                            concept.getDerivations().add(connectedEntry.concept);
                            break;
                        case 8:
                            concept.getArbitraryRelations().add(connectedEntry.concept);
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
        entry.loaded = true;
        return entry.concept;
    }


    public void save() {
        storeGraph();
        storeLookupMap();
    }

}
