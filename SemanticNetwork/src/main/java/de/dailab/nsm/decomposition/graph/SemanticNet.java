/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.dailab.nsm.decomposition.graph;

import de.dailab.nsm.decomposition.graph.entities.relations.Relation;
import de.dailab.nsm.decomposition.graph.entities.Entity;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.ListenableDirectedGraph;

/**
 * Created by faehndrich on 01.05.15.
 */
public abstract class SemanticNet extends ListenableDirectedGraph<Entity, Relation> {
    public SemanticNet(Class<? extends Relation> edgeClass) {
        super(edgeClass);
    }

    public SemanticNet(DirectedGraph<Entity, Relation> base) {
        super(base);
    }
}
