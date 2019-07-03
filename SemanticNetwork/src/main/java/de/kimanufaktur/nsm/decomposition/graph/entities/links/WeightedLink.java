/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.kimanufaktur.nsm.decomposition.graph.entities.links;

import de.kimanufaktur.nsm.decomposition.graph.spreadingActivation.MarkerPassing.MarkerPassingConfig;
import de.kimanufaktur.spreadalgo.Link;
import de.kimanufaktur.spreadalgo.Node;

/**
 * Created by faehndrich on 19.05.15.
 */
public class WeightedLink implements Link {
    Node source = null;
    Node target = null;
    double weight = 0.0d;
    public MarkerPassingConfig markerPassingConfig = new MarkerPassingConfig();

    public Node getTarget() {
        return target;
    }

    public void setTarget(Node target) {this.target = target;}

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public Node getSource() {
        return source;
    }

    public void setSource(Node sourceNode) {
        source = sourceNode;
    }


    @Override
    public boolean equals(Object other) {
        if (other == null && this == null) return true;
        if (other == null && this != null) return false;
        if (other != null && this == null) return false;
        if(this.getClass() != other.getClass()) return false;
        if (other == this) return true;
        if (!(other instanceof WeightedLink)) return false;
        if(this.source != null && ((WeightedLink) other).source != null && this.target != null && ((WeightedLink) other).target != null){
            if (this.source.equals(((WeightedLink) other).source)) {
                    return this.target.equals(((WeightedLink) other).target);
            } else {
                return false;
            }
        }else{
            if(this.source == null && ((WeightedLink) other).source == null){
                return this.getTarget() == null && ((WeightedLink) other).target == null;
            }else{
                return false;
            }
        }

    }

}
