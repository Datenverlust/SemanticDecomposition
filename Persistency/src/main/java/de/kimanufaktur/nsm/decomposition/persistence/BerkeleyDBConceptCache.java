package de.kimanufaktur.nsm.decomposition.persistence;///*
// * Copyright (C) Johannes Fähndrich - All Rights Reserved.
// * Unauthorized copying of this file, via any medium is strictly
// * prohibited Proprietary and confidential.
// * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
// */
//
//package de.kimanufaktur.nsm.decomposition.persistence;
//
//
//import com.sleepycat.je.*;
//import com.sleepycat.persist.EntityStore;
//import com.sleepycat.persist.PrimaryIndex;
//import com.sleepycat.persist.StoreConfig;
//import de.kimanufaktur.nsm.decomposition.Concept;
//
//import java.io.File;
//import java.util.ArrayList;
//
///**
// * Created by faehndrich on 20.03.16.
// */
//public class BerkeleyDBConceptCache {
//
//    static Environment myDbEnvironment = null;
//    static String dBPath = System.getProperty("user.home") + File.separator + ".decomposition" + File.separator + "ConceptCache";
//    private EntityStore store;
//    private PrimaryIndex primaryIndex = null;
//    private Database db = null;
//
//    public BerkeleyDBConceptCache() {
//        if (myDbEnvironment == null) {
//            init();
//        }
//    }
//
//    public void put(Concept concept) {
//        if (concept != null) {
//            primaryIndex.put(concept);
//        }
//    }
//
//    public void sync() {
//        myDbEnvironment.sync();
//    }
//
//    public void putAll(ArrayList<Concept> conceptCollection) {
//        conceptCollection.forEach(this::put);
//    }
//
//    public Concept get(long conceptHash) {
//        return (Concept) primaryIndex.get(conceptHash);
//    }
//
//    private void init() {
//        try {
//            /*Config the environment*/
//            EnvironmentConfig envConfig = new EnvironmentConfig();
//            envConfig.setAllowCreate(true);
//            envConfig.setTransactional(true);
//            File dbFile = new File(dBPath);
//
//            try {
//                myDbEnvironment = new Environment(dbFile, envConfig);
//            } catch (IllegalArgumentException noDirE) {
//                dbFile.mkdirs();
//                myDbEnvironment = new Environment(dbFile, envConfig);
//            }
//            EnvironmentMutableConfig envMutableConfig = new EnvironmentMutableConfig();
////            envMutableConfig.setTxnNoSync(true);
//            envMutableConfig.setCacheMode(CacheMode.DEFAULT);
//            myDbEnvironment.setMutableConfig(envMutableConfig);
//
//            /*config Database*/
//            DatabaseConfig dbconf = new DatabaseConfig();
//            dbconf.setAllowCreate(true);
//            dbconf.setSortedDuplicates(false);
//            db = myDbEnvironment.openDatabase(null, "SampleDB", dbconf);
//            /*Config the store*/
//            StoreConfig storeConfig = new StoreConfig();
//            storeConfig.setAllowCreate(true);
//            store = new EntityStore(myDbEnvironment, "EntityStore", storeConfig);
//            primaryIndex = store.getPrimaryIndex(Long.class, Concept.class);
//
//        } catch (DatabaseException dbe) {
//            dbe.printStackTrace();
//            System.exit(1);
//        }
//
//
//    }
//
//    public void close() {
//        try {
//            if (store != null) {
//                store.close();
//            }
//            if(db != null){
//                db.close();
//            }
//            if (myDbEnvironment != null) {
//                myDbEnvironment.close();
//            }
//        } catch (DatabaseException dbe) {
//            dbe.printStackTrace();
//            System.exit(1);
//        }
//    }
//
//
//    public void basAPI(Concept concept) {
//
////        DatabaseEntry searchEntry = new DatabaseEntry();
////        DatabaseEntry dataValue = new DatabaseEntry(concept);
////        DatabaseEntry keyValue = new DatabaseEntry("key content".getBytes("UTF-8"));
////        db.put(null, keyValue, dataValue);//inserting an entry
////
////
////        db.get(null, keyValue, searchEntry, LockMode.DEFAULT);//retrieving record
////        String foundData = new String(searchEntry.getData(), "UTF-8");
////        dataValue = new DatabaseEntry("updated data content". getBytes("UTF-8"));
////        db.put(null, keyValue, dataValue);//updating an entry
////        db.delete(null, keyValue);//delete operation
//
//
//    }
//
//}
