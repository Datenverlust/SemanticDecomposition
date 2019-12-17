/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.kimanufaktur.nsm.graph.entities.marker;

import de.kimanufaktur.markerpassing.Marker;

/**
 * Created by faehndrich on 19.05.15.
 */
public class DoubleMarker implements Marker {
    double activation = Double.NaN;

    public double getActivation() {
        return activation;
    }

    public void setActivation(double activation) {
        this.activation = activation;
    }
}