/*package de.dailab.nsm.decomposition.Dictionaries.customDictionary;


import de.dailab.nsm.decomposition.Concept;
import de.dailab.nsm.decomposition.Definition;
import de.dailab.nsm.decomposition.WordType;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.util.*;

/**
 * Created by Patrick on 05.07.2019.

public class StoreGraph {
    private RandomAccessFile connectionsPlaceholderFile;
    private RandomAccessFile connectionsFile;
    private FileOutputStream connectedIndexFile;


    private int currentIndex = 0;

    protected HashSet<CustomEntry> entries = new HashSet<>();
    // <word, <type, [internal index, file index]
    protected Hashtable<String, Hashtable<WordType, CustomEntry>> entryLookup = new Hashtable<>();


    StoreGraph() {
        try {
            connectionsPlaceholderFile = new RandomAccessFile("/home/patrick/.decomposition/Custom/EN/connectionsPlaceholder", "rw");
            connectionsPlaceholderFile.setLength(0);
            connectionsFile = new RandomAccessFile("/home/patrick/.decomposition/Custom/EN/connections", "rw");
            connectionsFile.setLength(0);
            connectedIndexFile = new FileOutputStream("/home/patrick/.decomposition/Custom/EN/index", false);
        }
        catch (final Exception ex) {
            System.out.println("Could not open connectionsFile: " + ex.getLocalizedMessage());
        }
    }

    private CustomEntry getEntryForConcept(Concept concept) {
        String word = concept.getLitheral();
        WordType type = concept.getWordType();
        if(!entryLookup.containsKey(word)) {
            entryLookup.put(word, new Hashtable<>());
        }
        if(!entryLookup.get(word).containsKey(type)) {
            CustomEntry entry = new CustomEntry(getNextIndex(), concept);

            entryLookup.get(word).put(type, entry);
        }
        return entryLookup.get(word).get(type);
    }


    public void storeEntry(Concept concept) {
        //System.out.println("Storing entry: " + concept.getLitheral());
        //System.out.println("Connections: " + concept.getDecomposition());

        System.out.println("Adding concept to list: " + concept.getLitheral());

        CustomEntry entry = getEntryForConcept(concept);

        entries.add(entry);
    }

    public void storeConceptsInFile() {
        //TODO: implement

        System.out.println("Storing entries: " + entries.size());

        System.out.println("Storing graph placeholder");
        //TODO: calculate index, instead of running everything twice
        for (CustomEntry entry: entries) {
            writeConcept(entry, true);
        }
        System.out.println("Storing graph");

        for (CustomEntry entry : entries) {
            writeConcept(entry, false);
        }

        storeHashMap();

    }

    private void storeHashMap() {
        System.out.println("Storing entryLookup");
        try {
            ObjectOutputStream out = new ObjectOutputStream(connectedIndexFile);
            out.writeObject(entryLookup);
        }
        catch (Exception ex) {
            System.out.println("Could not store entryLookup: " + ex.getLocalizedMessage());
        }
    }

    private void writeConcept(CustomEntry entry, boolean usePlaceholderIndex) {
        //this will write a single entries references into the connections file

        Concept concept = entry.concept;

        System.out.println("Logging everything about: " + entry);
        System.out.println(("Definitions: " + concept.getDefinitions().size()));

        RandomAccessFile file = connectionsFile;
        if(usePlaceholderIndex) {
            file = connectionsPlaceholderFile;
        }

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

        int internalIndex = concept.placeholderIndex;
        int fileIndex = concept.index;
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

    private void updateLinks() {
        //TODO: implement
        //replace the placeholder references in the connections file with the real indexes
    }

    private void writeIndexFile() {
        //TODO: implement
        //will write the hashmap containing each existing word,
        // and its type, to look up the index in the connections file
    }

    private int getNextIndex() {
        return currentIndex++;
    }


    private int getPlaceholderIndexForConcept(Concept concept) {
        if(concept.placeholderIndex == 0) {
            concept.placeholderIndex = getNextIndex();
        }

        return concept.placeholderIndex;
    }
}

*/