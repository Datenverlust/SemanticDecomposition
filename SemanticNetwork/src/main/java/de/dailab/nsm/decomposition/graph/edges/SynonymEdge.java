/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.dailab.nsm.decomposition.graph.edges;

/**
 * Created by faehndrich on 05.05.15.
 */
public class SynonymEdge extends WeightedEdge{

    public SynonymEdge(){
        this.setEdgeType(EdgeType.Synonym);
    }

}
