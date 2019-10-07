/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.kimanufaktur.nsm.graph.entities.links;

import de.kimanufaktur.nsm.decomposition.graph.spreadingActivation.MarkerPassing.MarkerPassingConfig;

/**
 * Created by tom koenig on 25.09.16.
 */
public class ArbitraryRelationLink extends WeightedLink {
    private String relationName;

    @Override
    public double getWeight() {
        // TODO this is the answer to how should the edges be weightened
        return MarkerPassingConfig.getLinkWeightForRelationWithName(relationName);
    }

    public String getRelationName() {
        return relationName;
    }

    public void setRelationName(String relationName) {
        this.relationName = relationName;
    }
}
