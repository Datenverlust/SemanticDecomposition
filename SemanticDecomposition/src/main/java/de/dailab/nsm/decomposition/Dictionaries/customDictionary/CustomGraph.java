package de.dailab.nsm.decomposition.Dictionaries.customDictionary;

import com.google.common.collect.Iterables;
import de.dailab.nsm.decomposition.Concept;
import de.dailab.nsm.decomposition.Definition;
import de.dailab.nsm.decomposition.WordType;
import de.dailab.nsm.decomposition.exceptions.DictionaryDoesNotContainConceptException;
import de.dailab.nsm.decomposition.settings.Config;
import org.apache.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class CustomGraph {

    private static final Logger logger = Logger.getLogger(CustomGraph.class);

    private final String connectionsFilePath;
    private final String connectedIndexFilePath;

    private RandomAccessFile connectionsFile;

    ArrayList<CustomEntry> entries = new ArrayList<>();
    Hashtable<Integer, CustomEntry> entriesById = new Hashtable<>();

    private int nextInternalIndex = 1;

    ListIterator<CustomEntry> iterator = null;
    // <word, <type, entry>>
    protected Hashtable<String, Hashtable<WordType, CustomEntry>> entryLookup = new Hashtable<>();

    private Object indexLock = new Object();

    public CustomGraph() {
        if( Config.LANGUAGE.GER ==
                Config.LANGUAGE.valueOf( Config.getInstance().getUserProps().getProperty(Config.LANGUAGE_KEY) )){

            connectionsFilePath = Config.CUSTOM_ARCHIVE_GER_PATH + File.separator +
                    Config.CUSTOM_ARCHIVE_CONNECTIONS_NAME;
            connectedIndexFilePath = Config.CUSTOM_ARCHIVE_GER_PATH + File.separator +
                    Config.CUSTOM_ARCHIVE_INDEX_NAME;
        }
        //else if( Config.LANGUAGE.EN ==
        //       Config.LANGUAGE.valueOf( Config.getInstance().getUserProps().getProperty(Config.LANGUAGE_KEY) )){
        else {
            connectionsFilePath = Config.CUSTOM_ARCHIVE_EN_PATH + File.separator +
                    Config.CUSTOM_ARCHIVE_CONNECTIONS_NAME;
            connectedIndexFilePath = Config.CUSTOM_ARCHIVE_EN_PATH + File.separator +
                    Config.CUSTOM_ARCHIVE_INDEX_NAME;
        }


        try {
            connectionsFile = new RandomAccessFile(connectionsFilePath, "r");
            loadLookupMap();
        }
        catch (IOException ex) {
            System.out.println("Could not open graph file: " + ex.getLocalizedMessage());
            ex.printStackTrace();
        }

    }

    private void addEntry(CustomEntry entry) {
        if(iterator == null) {
            entries.add(entry);
        }
        else {
            iterator.add(entry);
        }
        entriesById.put(getIdForEntry(entry), entry);
    }

    private int generateEntryIndex() {
        synchronized (this.indexLock) {
            return nextInternalIndex++;
        }
    }

    private int getIdForEntry(CustomEntry entry) {
        if (entry.index <= 0) entry.index = generateEntryIndex();
        return entry.index;
    }


    private CustomEntry getEntryForId(int id) throws DictionaryDoesNotContainConceptException{
        if(!entriesById.containsKey(id)) {
            throw new DictionaryDoesNotContainConceptException();
        }
        CustomEntry entry = entriesById.get(id);

        loadEntry(entry);

        return entry;
    }

    private void loadEntry(CustomEntry entry) {
        synchronized (entry) {
            if(entry.loaded) {
                System.out.println("Don't need to load: " + entry);
                return;
            }

            loadConceptFromFile(entry);
        }
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

    public Concept getConceptForWord(String word) throws DictionaryDoesNotContainConceptException {
        return getEntryForWord(word).concept;
    }

    public CustomEntry getEntryForWord(String word) throws DictionaryDoesNotContainConceptException {
        //Will this be used outside of tests?
        if(!entryLookup.containsKey(word)) {
            throw new DictionaryDoesNotContainConceptException();
        }
        //If it contains the word, it contains at least 1 entry. (position 0)
        //FIXME: What to do with typless words? Iterate over the known ones?
        CustomEntry entry = Iterables.get(entryLookup.get(word).values(), 0);
        loadEntry(entry);
        return entry;
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

        CustomEntry entry = entryLookup.get(word).get(type);
        loadEntry(entry);

        return entry;
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
        int index = generateEntryIndex();
        CustomEntry entry = new CustomEntry(index, concept);
        addEntry(entry);
        addEntryToLookup(entry);

        concept.getDecomposition().forEach(connectedConcept -> addConcept(connectedConcept));
        concept.getDefinitions().forEach(
                definition -> definition.getDefinition().forEach(
                        connectedConcept -> addConcept(connectedConcept)
                )
        );
        return entry;
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
            System.out.println("done storing entryLookup");
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
            System.out.println("Loaded lookup map");
            System.out.println("Creating entries list");

            for (Hashtable<WordType, CustomEntry> types : entryLookup.values()) {
                for (CustomEntry entry : types.values()) {
                    addEntry(entry);
                }
            }
            nextInternalIndex = entries.size() + 1;

            System.out.println("Created entries list");

            testLookup();
            testConnections();
        }
        catch (Exception ex) {
            entryLookup = new Hashtable<>();
            entries = new ArrayList<>();
            entriesById = new Hashtable<>();
            System.out.println("Could not store entryLookup: " + ex.getLocalizedMessage());
        }
    }
    private void testLookup() {
        System.out.println("------Starting lookup test-------");
        System.out.println("Entries: " + entryLookup.size());
        int limit = Math.min(1100, entryLookup.size());
        Enumeration<String> e = entryLookup.keys();
        for (int i = 0; i < limit; i++) {
            String word = e.nextElement();
            System.out.println("Got word: " + word);
            Hashtable typesList = entryLookup.get(word);
            System.out.println("Has types: " + typesList.size());
            Iterator<WordType> it = typesList.keys().asIterator();
            while (it.hasNext()) {
                WordType type = it.next();
                System.out.println("Type: " + type);
                System.out.println("Entry: " + typesList.get(type));
            }
        }
        System.out.println("Checking Monday, id: 87, fileIndex: 348");
        try {
            loadConcept(87);
        }
        catch (Exception ex) {
            System.out.println("Error while getting monday: ");
            ex.printStackTrace();
        }

        System.out.println("------Finished lookup test-------");
    }

    private void testConnections() {
        System.out.println("------Starting connections test-------");

        int limit = Math.min(110000, entryLookup.size());
        Enumeration<String> e = entryLookup.keys();
        for (int i = 0; i < limit; i++) {
            String word = e.nextElement();
            Hashtable<WordType, CustomEntry> typesList = entryLookup.get(word);
            Iterator<WordType> it = typesList.keys().asIterator();
            while (it.hasNext()) {
                WordType type = it.next();
                CustomEntry entry = typesList.get(type);
                System.out.println("Word: " + word);
                System.out.println("Type: " + type);
                System.out.println("Entry: " + entry);
                loadEntry(entry);
                System.out.println("Concept: " + entry.concept);

            }
        }

        System.out.println("------Finished connections test-------");

    }

    private void storeGraph() {
        try {
            System.out.println("Storing entries: " + entries.size());

            RandomAccessFile connectionsFile;
            connectionsFile =
                    new RandomAccessFile(connectionsFilePath, "rw");
            connectionsFile.setLength(0);

            System.out.println("Storing graph");
            iterator = entries.listIterator();
            while(iterator.hasNext()) {
                CustomEntry entry = iterator.next();
                entry.fileIndex = connectionsFile.getFilePointer();
                writeEntry(connectionsFile, entry);
            }
            iterator = null;
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
        System.out.println(("Definitions: " + concept.getDefinitions()));
        System.out.println(("Definitions num: " + concept.getDefinitions().size()));

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
        System.out.println("Storing in " + file.getFilePointer() + ": "+type+", "+internalIndex);
        // store the type of the connection
        file.writeInt(type);

        //Store the connection reference
        file.writeInt(internalIndex);
    }

    private void storeDefinition(RandomAccessFile file, Definition def) throws IOException {
        for(Concept concept : def.getDefinition()) {
            storeConnection(file, concept, 27);
        }
        file.writeInt(-2);
    }


    private Concept loadConcept(int index) throws DictionaryDoesNotContainConceptException {
        if(!entriesById.containsKey(index)) {
            throw new DictionaryDoesNotContainConceptException();
        }
        return loadConceptFromFile(entriesById.get(index));
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

        if(entry.fileIndex < 0) {
            System.out.println("No file index given for : " + entry);
        }

        long fileIndex = entry.fileIndex;
        Concept concept = new Concept(entry.word);
        concept.setWordType(entry.type);

        try {
            System.out.println("Seeking in connections file: " + fileIndex);
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
        concept.setDecompositionlevel(1);
        entry.concept = concept;
        entry.loaded = true;
        return concept;
    }

    public void save() {
        storeGraph();
        storeLookupMap();
    }

    public void dumpConnections() {
        try {
            RandomAccessFile file = new RandomAccessFile(connectionsFilePath, "r");

            mainloop:
            while (true) {
                //read stuff from the file
                System.out.println("File pointer: " + file.getFilePointer());
                int connectionType = file.readInt();
                System.out.println("Type: " + connectionType);
                //-1 is the end of the current words connections
                if (connectionType == -1) {
                    continue ;
                }
                int connectionTarget = file.readInt();
                System.out.println("Target: " + connectionTarget);
            }
        }
        catch (EOFException ex) {
            //TODO: This is just a test, but still, this should be checked differently...
            System.out.println("End of file!");
        }
        catch (IOException ex) {
            System.out.println("IOError,Unexpected IOException while dumping connections");
            ex.printStackTrace();
        }

    }

}
