/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.dailab.nsm.decomposition.graph.entities.links;

import de.dailab.nsm.decomposition.graph.spreadingActivation.MarkerPassing.MarkerPassingConfig;

/**
 * Created by faehndrich on 13.08.15.
 */
public class AntonymLink extends WeightedLink   {
    @Override
    public double getWeight() {
        return MarkerPassingConfig.getAntonymLinkWeight();
    }

}
