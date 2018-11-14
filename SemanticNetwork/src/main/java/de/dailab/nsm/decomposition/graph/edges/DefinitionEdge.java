/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.dailab.nsm.decomposition.graph.edges;

import org.jgraph.graph.AttributeMap;

import java.util.Map;

/**
 * Created by faehndrich on 05.05.15.
 */
public class DefinitionEdge extends WeightedEdge{
    public DefinitionEdge(){
        this.setEdgeType(EdgeType.Definition);
    }

    @Override
    public AttributeMap getAttributes() {
        return null;
    }

    @Override
    public Map changeAttributes(Map map) {
        return null;
    }

    @Override
    public void setAttributes(AttributeMap attributeMap) {

    }

}
