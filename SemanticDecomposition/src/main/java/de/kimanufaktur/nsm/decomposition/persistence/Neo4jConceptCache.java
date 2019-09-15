/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.kimanufaktur.nsm.decomposition.persistence;

import de.kimanufaktur.nsm.decomposition.Concept;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.ogm.drivers.embedded.driver.EmbeddedDriver;
import org.neo4j.ogm.service.Components;
import org.neo4j.ogm.session.Session;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by faehndrich on 21.03.16.
 */
//@SpringBootApplication
public class Neo4jConceptCache {
    static String dBPath = System.getProperty("user.home") + File.separator + ".decomposition" + File.separator + "ConceptCache";
    GraphDatabaseService graphDb = null;
    //String propertiesPath = getClass().getClassLoader().getResource("neo4j.properties").getPath();
    private Session session = null;
    private static void registerShutdownHook(final GraphDatabaseService graphDb) {
        // Registers a shutdown hook for the Neo4j instance so that it
        // shuts down nicely when the VM exits (even if you "Ctrl-C" the
        // running application).
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                graphDb.shutdown();
            }
        });
    }

    public void init() {


        Components.setDriver(new EmbeddedDriver());
        Components.configuration()
                .driverConfiguration()
                .setDriverClassName("org.neo4j.ogm.drivers.embedded.driver.EmbeddedDriver")
                .setURI(dBPath);

//        graphDb = new GraphDatabaseFactory()
//                .newEmbeddedDatabaseBuilder(dBPath)
//                .setConfig(GraphDatabaseSettings.pagecache_memory, "512M")
//                .setConfig(GraphDatabaseSettings.string_block_size, "60")
//                .setConfig(GraphDatabaseSettings.array_block_size, "300")
//                .newGraphDatabase();
//        registerShutdownHook(graphDb);
        session = Neo4jSessionFactory.getInstance().getNeo4jSession();
    }

    public void put(Concept concept) {
//        try (Transaction tx = graphDb.beginTx()) {
            session.save(concept);

//            tx.success();
//        }
    }
    public Concept get(Long conceptID){
        Concept result = null;
        try{
            result= session.load(Concept.class, conceptID);
        }catch (NullPointerException notAKnownConcept){
           notAKnownConcept.printStackTrace();
        }
        return result;
    }


    public void putAll(ArrayList<Concept> concepts2put) {
        for(Concept con : concepts2put){
            this.put(con);
        }
    }

    public void stop(){
        registerShutdownHook(graphDb);
    }
}
