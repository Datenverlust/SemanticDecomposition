/*package de.dailab.nsm.decomposition.Dictionaries.customDictionary;

import de.dailab.nsm.decomposition.Concept;
import de.dailab.nsm.decomposition.Decomposition;
import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Hashtable;

public class LoadGraph {

    private RandomAccessFile connectionsFile;
    private FileInputStream indexFile;


    private Hashtable<Integer, Concept> loadedConcepts = new Hashtable<>();

    // <word, <type, [internal index, file index]
    protected Hashtable<String, Hashtable<Integer, ArrayList<Integer>>> indexLookup = new Hashtable<>();
    //<index, [type, word]>
    public Hashtable<Integer, ArrayList<Object>> wordLookup = new Hashtable<>();

    private static final Logger logger = Logger.getLogger(Decomposition.class);

    LoadGraph() {
        try {
            connectionsFile = new RandomAccessFile("/home/patrick/.decomposition/Custom/EN/connections", "r");
            indexFile = new FileInputStream("/home/patrick/.decomposition/Custom/EN/index");
            ObjectInputStream in = new ObjectInputStream(indexFile);
            indexLookup = (Hashtable) in.readObject();

            System.out.println("entryLookup: " + indexLookup);
            System.out.println("entryLookup class: " + indexLookup.getClass());
            System.out.println("entryLookup size: " + indexLookup.size());
            System.out.println("entryLookup keys: " + indexLookup.keys());
            System.out.println("entryLookup contains Monday: " + indexLookup.containsKey("Monday"));
            System.out.println("entryLookup Monday: " + indexLookup.get("Monday"));
            System.out.println("entryLookup Monday 2: " + indexLookup.get("Monday").get(2));

            generateLookup();

            System.out.println("entryLookup: " + indexLookup);
            System.out.println("entryLookup class: " + indexLookup.getClass());
            System.out.println("entryLookup size: " + indexLookup.size());
            System.out.println("entryLookup keys: " + indexLookup.keys());
            System.out.println("entryLookup contains Monday: " + indexLookup.containsKey("Monday"));
            System.out.println("entryLookup Monday: " + indexLookup.get("Monday"));
            System.out.println("entryLookup Monday 2: " + indexLookup.get("Monday").get(2));

            System.out.println("word for 4068: " + wordLookup.get(4068));
        }
        catch (final Exception ex) {
            System.out.println("Could not open connectionsFile: " + ex.getLocalizedMessage());
            ex.printStackTrace();
        }
    }

    private void generateLookup() {
        wordLookup = new Hashtable<>();
        indexLookup.forEach((word, integerIntegerHashtable) -> {
            integerIntegerHashtable.forEach((type, index) -> {
                ArrayList a = new ArrayList<>();
                a.add(type);
                a.add(word);
                wordLookup.put(index, a);
            });
        });
    }
}
*/