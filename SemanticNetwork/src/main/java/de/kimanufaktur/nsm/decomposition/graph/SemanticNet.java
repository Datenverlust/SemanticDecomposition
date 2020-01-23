/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.kimanufaktur.nsm.decomposition.graph;


import de.kimanufaktur.nsm.graph.entities.Entity;

import de.kimanufaktur.nsm.graph.entities.relations.Relation;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultListenableGraph;


/**
 * Created by faehndrich on 01.05.15.
 */
public abstract class SemanticNet extends DefaultListenableGraph<Entity, Relation> {
    public SemanticNet(Class<? extends Relation> edgeClass) {
        super(new DefaultDirectedGraph<>(edgeClass));
    }

    public SemanticNet(DefaultDirectedGraph<Entity, Relation> base) {
        super(base);
    }
}
