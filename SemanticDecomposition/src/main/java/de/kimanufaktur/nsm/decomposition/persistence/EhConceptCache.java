/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.kimanufaktur.nsm.decomposition.persistence;

import de.kimanufaktur.nsm.decomposition.Concept;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;

import java.util.ArrayList;

/**
 * Created by faehndrich on 24.03.16.
 */
public class EhConceptCache {
    private static EhConceptCache instance;

    private static CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
            .withCache("ConceptCache",
                    CacheConfigurationBuilder.newCacheConfigurationBuilder(Long.class, Concept.class)
                            .build())
            .build(true);


    private static Cache<Long, Concept> conceptCache = null;

    private EhConceptCache() {
    }

    private static void init() {
        instance = new EhConceptCache();
        //CacheConfiguration<Long, Concept> cacheConfigurationBuilder = CacheConfigurationBuilder.newCacheConfigurationBuilder(Long.class, Concept.class).build();
        conceptCache = cacheManager.getCache("ConceptCache", Long.class, Concept.class);
    }

    public static synchronized EhConceptCache getInstance() {
        if (instance == null) {
            init();
        }
        return instance;
    }

    public void put(Concept concept) {
        if (concept != null) {
            synchronized (conceptCache) {
                Concept oldConcept = conceptCache.get(Long.valueOf(concept.hashCode()));
                if (oldConcept != null) {
                    if (concept.getDecompositionlevel() > oldConcept.getDecompositionlevel()) {
                        conceptCache.put(Long.valueOf(concept.hashCode()), concept);
                    }
                } else {
                    conceptCache.put(Long.valueOf(concept.hashCode()), concept);
                }
            }
        }
    }

    public void remove(Concept concept) {
        conceptCache.remove(concept.getId());
    }

    public void putAll(ArrayList<Concept> concepts2put) {
        for (Concept con : concepts2put) {
            this.put(con);
        }
    }

    public Concept get(Long id) {
        return conceptCache.get(id);
    }

    //cacheManager.close();

}
