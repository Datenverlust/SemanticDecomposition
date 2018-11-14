/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.dailab.nsm.decomposition.graph.edges;

import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.Edge;
import org.jgrapht.graph.DefaultEdgeWrapper;

import java.util.Map;

/**
 * Created by Johannes Fähndrich on 21.05.15 as part of his dissertation as part of his dissertation.
 */
public class WeightedEdge extends DefaultEdgeWrapper implements Edge {

    EdgeType edgeType; //define which kind of edge this is.

    public EdgeType getEdgeType() {
        return edgeType;
    }

    public void setEdgeType(EdgeType edgeType) {
        this.edgeType = edgeType;
    }

    public WeightedEdge() {
        this.setEdgeType(EdgeType.Unknown);
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
    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return this == null;
        }else{
            if(this == null){
                return false;
            }
        }
        if (other == null) {
            return false;
        }
        if (other != null && this == null) return false;
        if(this.getClass() != other.getClass()) return false;
        if (other == this) return true;
        if (!(other instanceof WeightedEdge)) return false;
        if(this.getSource() != null && ((WeightedEdge) other).getSource() != null && this.getTarget() != null && ((WeightedEdge) other).getTarget() != null){
            if (this.getSource().equals(((WeightedEdge) other).getSource())) {
                return this.getTarget().equals(((WeightedEdge) other).getTarget());
            } else {
                return false;
            }
        }else{
            if(this.getSource() == null && ((WeightedEdge) other).getSource() == null){
                return this.getTarget() == null && ((WeightedEdge) other).getTarget() == null;
            }else{
                return false;
            }
        }

    }
}
