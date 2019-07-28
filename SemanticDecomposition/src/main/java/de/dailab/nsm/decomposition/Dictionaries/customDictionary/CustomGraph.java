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

    private final static boolean MERGE_CONCEPTS = false;

    private final boolean disableStorage;

    public static boolean disableTests = true;

    public CustomGraph() {
        this(false);
    }
    public CustomGraph(boolean disableStorage) {
        this.disableStorage = disableStorage;

        connectionsFilePath = getConnectionFilePath();
        connectedIndexFilePath = getIndexFilePath();

        try {
            connectionsFile = new RandomAccessFile(connectionsFilePath, "r");
            loadLookupMap();
        }
        catch (IOException ex) {
            logger.error("Could not open graph file: " + ex.getLocalizedMessage());
            ex.printStackTrace();
        }

    }

    private static String getConnectionFilePath() {
        if( Config.LANGUAGE.GER ==
                Config.LANGUAGE.valueOf( Config.getInstance().getUserProps().getProperty(Config.LANGUAGE_KEY) )){

            return Config.CUSTOM_ARCHIVE_GER_PATH + File.separator +
                    Config.CUSTOM_ARCHIVE_CONNECTIONS_NAME;
        }
        else {
            return Config.CUSTOM_ARCHIVE_EN_PATH + File.separator +
                    Config.CUSTOM_ARCHIVE_CONNECTIONS_NAME;
        }
    }
    private static String getIndexFilePath() {
        if( Config.LANGUAGE.GER ==
                Config.LANGUAGE.valueOf( Config.getInstance().getUserProps().getProperty(Config.LANGUAGE_KEY) )){

            return Config.CUSTOM_ARCHIVE_GER_PATH + File.separator +
                    Config.CUSTOM_ARCHIVE_INDEX_NAME;
        }
        else {
            return Config.CUSTOM_ARCHIVE_EN_PATH + File.separator +
                    Config.CUSTOM_ARCHIVE_INDEX_NAME;
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
        if (entry.index <= 0) {
            entry.index = generateEntryIndex();
        }
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
                logger.info("Don't need to load: " + entry);
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

        assert entry != null;
        assert entry.concept != null;

        return entry;
    }

    public CustomEntry addConcept(Concept concept) {
        logger.info("Adding concept: " + concept);
        String word = concept.getLitheral();
        WordType type = concept.getWordType();

        if(word == null) {
            //what to do here?
            return null;
        }
        if (type == null) {
            //Failsafe, the type should never be null
            type = WordType.UNKNOWN;
            concept.setWordType(WordType.UNKNOWN);
        }

        if(entryLookup.containsKey(word)
            && entryLookup.get(word).contains(type)) {
            // word and type already exists, might be a mistake, or loop
            CustomEntry entry = entryLookup.get(word).get(type);
            return mergeConcept(entry, concept);
        }
        else {
            logger.info("Actually adding concept");
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
    }

    private CustomEntry mergeConcept(CustomEntry entry, Concept concept) {
        assert entry != null;
        assert concept != null;

        Concept target = entry.concept;

        //If merging is forced, or the target is empty (was not already set correctly)
        // everything should have at least 1 definition if it was loaded
        if(MERGE_CONCEPTS || (target.getDefinitions().size() == 0 && concept.getDefinitions().size() > 0) ) {
            //TODO: make sure that this does not lead to duplications?
            target.getDefinitions().addAll(concept.getDefinitions());
            target.getMeronyms().addAll(concept.getMeronyms());
            target.getHypernyms().addAll(concept.getHypernyms());
            target.getHyponyms().addAll(concept.getHyponyms());
            target.getAntonyms().addAll(concept.getAntonyms());
            target.getSynonyms().addAll(concept.getSynonyms());
            target.getAlternativeSyn().addAll(concept.getAlternativeSyn());
            target.getAlternativeAnt().addAll(concept.getAlternativeAnt());
        }
        return entry;
    }

    private void addEntryToLookup(CustomEntry entry) {
        if (!entryLookup.containsKey(entry.word)) {
            entryLookup.put(entry.word, new Hashtable<>());
        }
        logger.info("Adding entry to lookup: " + entry);

        //FIXME: Not a fitting null check here. This should be solved somewhere general
        if(entry.type == null) {
            entry.type = WordType.UNKNOWN;
        }
        entryLookup.get(entry.word).put(entry.type, entry);
    }


    private void storeLookupMap() {
        logger.info("Storing entryLookup");
        try {
            Path p = Path.of(connectedIndexFilePath);
            if (Files.exists(p)) {
                Files.move(p, Path.of(connectedIndexFilePath + "_" + new Date()));
            }

            FileOutputStream connectedIndexFile;
            connectedIndexFile = new FileOutputStream(connectedIndexFilePath, false);
            ObjectOutputStream out = new ObjectOutputStream(connectedIndexFile);
            out.writeObject(entryLookup);
            logger.info("done storing entryLookup");
        }
        catch (Exception ex) {
            logger.error("Could not store entryLookup: " + ex.getLocalizedMessage());
        }
    }

    private void loadLookupMap() {
        logger.info("Loading entryLookup");

        if (disableStorage) {
            entryLookup = new Hashtable<>();
            entries = new ArrayList<>();
            entriesById = new Hashtable<>();
            logger.debug("Did not load lookup map");
        }

        try {
            FileInputStream connectedIndexFile;
            connectedIndexFile = new FileInputStream(connectedIndexFilePath);
            ObjectInputStream in = new ObjectInputStream(connectedIndexFile);
            entryLookup = (Hashtable<String, Hashtable<WordType, CustomEntry>>) in.readObject();
            logger.info("Loaded lookup map");
            logger.info("Creating entries list");

            for (Hashtable<WordType, CustomEntry> types : entryLookup.values()) {
                for (CustomEntry entry : types.values()) {
                    addEntry(entry);
                }
            }
            nextInternalIndex = entries.size() + 1;

            logger.info("Created entries list");

            testLookup();
            testConnections();
        }
        catch (Exception ex) {
            entryLookup = new Hashtable<>();
            entries = new ArrayList<>();
            entriesById = new Hashtable<>();
            logger.error("Could not store entryLookup: " + ex.getLocalizedMessage());
        }
    }
    private void testLookup() {
        if (disableTests) {
            return;
        }
        System.out.println("------Starting lookup test-------");
        System.out.println("Entries: " + entryLookup.size());
        int limit = Math.min(1000000, entryLookup.size());
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
        System.out.println("------Finished lookup test-------");
    }

    private void testConnections() {
        if(disableTests) {
            return;
        }
        System.out.println("------Starting connections test-------");

        int limit = Math.min(1000000, entryLookup.size());
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

        if (disableStorage) {
            logger.debug("skipping the storing part");
            return;
        }

        try {
            System.out.println("Storing entries: " + entries.size());

            Path p = Path.of(connectionsFilePath);
            if (Files.exists(p)) {
                Files.move(p, Path.of(connectionsFilePath + "_" + new Date()));
            }

            RandomAccessFile connectionsFile;
            connectionsFile =
                    new RandomAccessFile(connectionsFilePath, "rw");
            //connectionsFile.setLength(0);

            logger.info("Storing graph");
            iterator = entries.listIterator();
            while(iterator.hasNext()) {
                CustomEntry entry = iterator.next();
                if(entry.fileIndex < 0) {
                    entry.fileIndex = connectionsFile.getFilePointer();
                    writeEntry(connectionsFile, entry);
                }
            }
            iterator = null;
        }
        catch (IOException ex) {
            logger.error("Could not store everything: " + ex.getLocalizedMessage());
            ex.printStackTrace();
        }
    }


    private void writeEntry(RandomAccessFile file, CustomEntry entry) {
        //this will write a single entries references into the connections file

        //This only loads when needed
        loadEntry(entry);

        Concept concept = entry.concept;

        logger.debug("Logging everything about: " + entry);
        logger.debug("Logging everything about: " + concept);
        logger.debug("Definitions: " + concept.getDefinitions());
        logger.debug("Definitions num: " + concept.getDefinitions().size());


        if (disableStorage) {
            logger.debug("Skipping the storing part");
            return;
        }
        try {

            for (Definition def : concept.getDefinitions()) {
                logger.debug("Got definition");
                storeDefinition(file, def);
            }

            for (Concept connection : concept.getAlternativeSyn()) {
                logger.debug("Got 1");
                storeConnection(file, connection, 1);
            }

            for (Concept connection : concept.getAlternativeAnt()) {
                logger.debug("Got 2");
                storeConnection(file, connection, 2);
            }

            for (Concept connection : concept.getSynonyms()) {
                logger.debug("Got 3");
                storeConnection(file, connection, 3);
            }

            for (Concept connection : concept.getAntonyms()) {
                logger.debug("Got 4");
                storeConnection(file, connection, 4);
            }

            for (Concept connection : concept.getHypernyms()) {
                logger.debug("Got 5");
                storeConnection(file, connection, 5);
            }

            for (Concept connection : concept.getHyponyms()) {
                logger.debug("Got 6");
                storeConnection(file, connection, 6);
            }

            for (Concept connection : concept.getDerivations()) {
                logger.debug("Got 7");
                storeConnection(file, connection, 7);
            }

            for (Concept connection : concept.getArbitraryRelations()) {
                logger.debug("Got 8");
                storeConnection(file, connection, 8);
            }
            //Delimiter, for the list of connections
            file.writeInt(-1);
        }
        catch (IOException ex) {
            logger.error("Could not write into connections file: " + ex.getLocalizedMessage());
        }
    }

    private void storeConnection(RandomAccessFile file, Concept concept, int type) throws IOException {
        storeConnection(file, getEntryFromConcept(concept), type);
    }

    private void storeConnection(RandomAccessFile file, CustomEntry entry, int type) throws IOException {
        if (disableStorage) {
            logger.debug("skipping the storing part");
            return;
        }

        int internalIndex = entry.index;
        logger.info("Storing in " + file.getFilePointer() + ": "+type+", "+internalIndex);
        // store the type of the connection
        file.writeInt(type);

        //Store the connection reference
        file.writeInt(internalIndex);
    }

    private void storeDefinition(RandomAccessFile file, Definition def) throws IOException {
        if (disableStorage) {
            logger.debug("skipping the storing part");
            return;
        }

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
        logger.info("Loading entry: " + entry);

        if (disableStorage) {
            logger.debug("skipping the loading part");
            Concept concept = new Concept(entry.word, entry.type);
            entry.concept = concept;
            return concept;
        }

        if(entry.fileIndex < 0) {
            logger.warn("No file index given for : " + entry);
            Concept concept = new Concept(entry.word, entry.type);
            entry.concept = concept;
            return concept;
        }

        long fileIndex = entry.fileIndex;
        Concept concept = new Concept(entry.word);
        concept.setWordType(entry.type);

        try {
            logger.debug("Seeking in connections file: " + fileIndex);
            connectionsFile.seek(fileIndex);
            boolean definitionLoaded = false;
            Definition def = new Definition();
            List<Concept> defList= new ArrayList<>();
            mainloop:
            while (true) {
                //read stuff from the file
                int connectionType = connectionsFile.readInt();
                logger.debug("Type: " + connectionType);
                //-1 is the end of the current words connections
                if (connectionType == -1) {
                    break mainloop;
                }
                //-2 is the break between definitions
                if(connectionType == -2) {
                    definitionLoaded = false;
                }
                int connectionTarget = connectionsFile.readInt();
                logger.debug("Target: " + connectionTarget);

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
                logger.info("File pointer: " + file.getFilePointer());
                int connectionType = file.readInt();
                logger.info("Type: " + connectionType);
                //-1 is the end of the current words connections
                if (connectionType == -1) {
                    continue ;
                }
                int connectionTarget = file.readInt();
                logger.info("Target: " + connectionTarget);
            }
        }
        catch (EOFException ex) {
            //TODO: This is just a test, but still, this should be checked differently...
            logger.info("End of file!");
        }
        catch (IOException ex) {
            logger.warn("IOError,Unexpected IOException while dumping connections");
            ex.printStackTrace();
        }

    }

    public static void deleteArchive() {
        try {
            Files.deleteIfExists(Paths.get(getIndexFilePath()));
        }
        catch (Exception ex) {
            logger.error("could not delete index file");
            ex.printStackTrace();
        }

        try {
            Files.deleteIfExists(Paths.get(getConnectionFilePath()));
        }
        catch (Exception ex) {
            logger.error("could not delete connection file");
            ex.printStackTrace();
        }

    }
}
