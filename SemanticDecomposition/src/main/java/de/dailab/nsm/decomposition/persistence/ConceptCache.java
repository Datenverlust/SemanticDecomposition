/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.dailab.nsm.decomposition.persistence;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import de.dailab.nsm.decomposition.Concept;
import de.dailab.nsm.decomposition.DecompositionConfig;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by faehndrich on 28.03.16.
 */
public class ConceptCache {
    static DecompositionConfig decompositionConfig = new DecompositionConfig();

    static RemovalListener<Long, Concept> removalListener = removal -> {
        Concept concept = removal.getValue();
        if (concept.getId() != null) {
            String conceptCacheLocation = System.getProperty("user.home") + File.separator + ".decomposition" + File.separator + "ConceptCache" + File.separator + concept.getId() + ".decompGraph";
            writeFile(conceptCacheLocation, concept);
        }
    };
    private static ExecutorService executor = Executors.newCachedThreadPool();
    private static LoadingCache<Long, Concept> conceptCache = null;
    private static Logger logger = Logger.getLogger(ConceptCache.class);
    private static ConceptCache instance;

    private ConceptCache() {
    }

    public static void init() {
        instance = new ConceptCache();

        //conceptCache = CacheBuilder.newBuilder().maximumSize(decompositionConfig.getCacheSize()).removalListener(RemovalListeners.asynchronous(removalListener, executor)).build(new CacheLoader<Long, Concept>() {
        conceptCache = CacheBuilder.newBuilder().maximumSize(decompositionConfig.getCacheSize()).build(new CacheLoader<Long, Concept>() {
                                                                                                           @Override
                                                                                                           public Concept load(Long key) {
                                                                                                               return new Concept();//oadConcept(key);
                                                                                                           }
                                                                                                       }
        );
    }

    public static void cleanUp(){
        conceptCache.cleanUp();
    }

    public static synchronized ConceptCache getInstance() {
        if (instance == null) {
            init();
        }
        return instance;
    }

    /**
     * Save the JGraph object of the decomposition into a file.
     *
     * @param concept the decomposition of a concept transformed into a Graph
     * @throws IOException
     */
    private static void saveConcept(Long id, Concept concept) throws IOException {
        String path2write2 = System.getProperty("user.home") + File.separator + ".decomposition" + File.separator + id + ".decompGraph";
        Concept result = null;
        File graphFile = new File(path2write2);
        if (!graphFile.exists()) {
            if (!graphFile.getParentFile().exists()) {
                graphFile.getParentFile().mkdirs();
            }
        }
        logger.info("Saving decomposition to " + path2write2);
        FileOutputStream fileOut = new FileOutputStream(path2write2, false);
        ObjectOutputStream out = new ObjectOutputStream(fileOut);
        out.writeObject(concept);
        out.flush();
        fileOut.flush();
        out.close();
        fileOut.close();
        return;
    }

    /**
     * Load the given graph form the user home directory in .decomposition with the ednign .decompGraph
     *
     * @param id the id of the concept to load
     * @throws IOException
     * @throws ClassNotFoundException
     * @returnthe JGraph loaded from the given path.
     */
    private static Concept loadConcept(Long id) {

        String conceptCacheLocation = System.getProperty("user.home") + File.separator + ".decomposition" + File.separator + "ConceptCache" + File.separator + id + ".decompGraph";
        Concept result = new Concept();
        File graphFile = new File(conceptCacheLocation);
        if (!graphFile.exists()) {
            return result;
        }
        try {
            FileInputStream fileIn = new FileInputStream(graphFile);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            result = (Concept) in.readObject();
            in.close();
            fileIn.close();
        } catch (Exception nofiel) {
            nofiel.printStackTrace();
        }
        System.out.println("Read concept " + (result.getLitheral()) + " from disk");
        return result;
    }

    /**
     * Write the concept into a file with the given path. This is used as persisitent cache. If wanted this can be
     * replaced by a data base or equivalent.
     *
     * @param path          the path to write the concept to.
     * @param concept2write the concept to be persisted at the given path.
     */
    private static void writeFile(String path, Concept concept2write) {

        File graphFile = new File(path);
        if (!graphFile.exists()) {
            if (!graphFile.getParentFile().exists()) {
                graphFile.getParentFile().mkdirs();
            }
        } else {
            return;
        }
        System.out.println("Write concept to disk " + concept2write.toString());
        try {
            FileOutputStream fileIn = new FileOutputStream(graphFile);
            ObjectOutputStream in = new ObjectOutputStream(fileIn);
            in.writeObject(concept2write);
            in.close();
            fileIn.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public Concept get(Long key) {
        try {
            Concept cachedConcept = conceptCache.get(key);
//            System.out.println("Found a valid concept: " + (cachedConcept.getLitheral() != null));
            //logger.info("Getting " + cachedConcept.getLitheral() + "from cache.");
            return cachedConcept;
        } catch (Exception irgnored) {
            irgnored.printStackTrace();
        }
        return null;
    }

    public void put(Concept concept) {

        try {
            Concept oldConcept = conceptCache.get(Long.valueOf(concept.hashCode()));
            if (oldConcept.getDecompositionlevel() < concept.getDecompositionlevel()) {
                conceptCache.put(Long.valueOf(concept.hashCode()), concept);
                //logger.info("Putting " + concept.getLitheral() + "into cache.");
//                if (concept.getId() != null && concept.getDecompositionlevel()>1) {
//                    String conceptCacheLocation = System.getProperty("user.home") + File.separator + ".decomposition" + File.separator + "ConceptCache" + File.separator + concept.getId() + ".decompGraph";
//                    executor.execute(new Runnable() {
//                        @Override
//                        public void run() {
//                            writeFile(conceptCacheLocation, concept);
//                        }
//                    });
//                }
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

    }

    public void putAll(List<Concept> conceptList) {
        for (Concept c : conceptList) {
            put(c);
        }
    }
}
