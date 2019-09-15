/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.kimanufaktur.nsm.decomposition.graph.edges;

/**
 * Created by tomkoenig on 03.10.16.
 */
public class ArbitraryEdge extends WeightedEdge{
    private String relationName;

    public ArbitraryEdge(){
        this.setEdgeType(EdgeType.Arbitrary);
    }

    public void setRelationName(String relationName) {
        this.relationName = relationName;
    }

    public String getRelationName() {
        return relationName;
    }
}
