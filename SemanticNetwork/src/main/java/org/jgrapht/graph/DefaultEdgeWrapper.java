/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package org.jgrapht.graph;

import org.jgraph.graph.AttributeMap;

import java.util.Map;

/**
 * Created by faehndrich on 21.05.15.
 */
public class DefaultEdgeWrapper extends DefaultWeightedEdge{


    @Override
    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }


    public void setTarget(Object target){
        this.target = target;
    }
    public void setSource(Object source){
        this.source = source;
    }

    public Object getSource() {
        return this.source;
    }
    public Object getTarget() {
        return this.target;
    }
}
