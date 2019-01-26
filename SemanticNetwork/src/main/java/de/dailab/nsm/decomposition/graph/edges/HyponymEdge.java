/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.dailab.nsm.decomposition.graph.edges;


/**
 * Created by faehndrich on 30.07.15.
 */
public class HyponymEdge extends WeightedEdge {

    public HyponymEdge(){
        this.setEdgeType(EdgeType.Hyponym);
    }
}
