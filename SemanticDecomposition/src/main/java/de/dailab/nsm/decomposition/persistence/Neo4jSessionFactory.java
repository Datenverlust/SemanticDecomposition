/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.dailab.nsm.decomposition.persistence;

import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.springframework.context.annotation.Bean;

import java.io.File;

/**
 * Created by faehndrich on 21.03.16.
 */
public class Neo4jSessionFactory {

    private static Neo4jSessionFactory factory = new Neo4jSessionFactory();
    private static Configuration config = null;
    private final static SessionFactory sessionFactory = new SessionFactory(getConfiguration(), "de.dailab.nsm.decomposition");

    private Neo4jSessionFactory() {
        getConfiguration();

    }

    @Bean
    private static Configuration getConfiguration() {
        String dBPath = "File:" + File.separator + File.separator + System.getProperty("user.home") + File.separator + ".decomposition" + File.separator + "ConceptCache";
        config = new Configuration();
        config.driverConfiguration()
                .setDriverClassName
                        ("org.neo4j.ogm.drivers.embedded.driver.EmbeddedDriver")
                .setURI(dBPath);

        return config;
    }

    public static Neo4jSessionFactory getInstance() {
        if (config == null) {
            getConfiguration();
        }
        return factory;
    }

    public Session getNeo4jSession() {
        return sessionFactory.openSession();
    }
}