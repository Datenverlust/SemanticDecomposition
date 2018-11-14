/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.dailab.nsm.decomposition.graph.entities.marker;

import de.dailab.nsm.decomposition.Concept;
import de.tuberlin.spreadalgo.Marker;

/**
 * Created by root on 10.12.15.
 */
public class TypedMarker implements Marker {

    //###########################
    // VARS
    //###########################
    private double value;
    private Concept origin;
    private int type;

    //###########################
    // FUNCTIONS
    //###########################
    public TypedMarker(double value, Concept origin, int type) {
        this.value = value;
        this.origin = origin;
        this.type = type;
    }

    public TypedMarker(TypedMarker marker, double value) {
        this.value = value;
        this.origin = marker.getOrigin();
        this.type = marker.getType();
    }


    public void  setValue(double value) {
        this.value = value;
    }

    public void setOrigin(Concept origin) {
        this.origin = origin;
    }

    public void setType(int type) {
        this.type = type;
    }

    public double getValue() {
        return this.value;
    }

    public Concept getOrigin() {
        return this.origin;
    }

    public int getType() {
        return this.type;
    }



}
