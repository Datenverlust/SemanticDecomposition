/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.kimanufaktur.nsm.graph.spreadingActivation.MarkerPassing;

/**
 * Created by root on 10.12.15.
 */
public class TypedMarkerPassingConfig {
    //###########################
    // VARS
    //###########################
    public int terminationPulseCount = 20;
    public double initialMarkerAmount = 1000.d;
    public int decompositionDepth = 2;

    // Thresholds
    public double threshold = 0.d;
    public double synThreshold = 0.d;
    public double hypoThreshold = 0.d;
    public double hyperThreshold = 0.d;
    public double antoThreshold = 0.d;
    public double defThreshold = 0.d;

    // Link weights
    public double synWeight = 1.d;
    public double hyperWeight = 1.d;
    public double hypoWeight = 1.d;
    public double antoWeight = 1.d;
    public double defWeight = 1.d;

    public TypedMarkerPassingConfig() {}

    @Override
    public String toString()  {
        String str = "Thresholds:\n";
        str += "\tthreshold = "+threshold+"\n";
        str += "\tsynthreshold = "+synThreshold+"\n";
        str += "\thypothreshold = "+hypoThreshold+"\n";
        str += "\thyperthreshold = "+hyperThreshold+"\n";
        str += "\tantothreshold = "+antoThreshold+"\n";
        str += "\tdefthreshold = "+defThreshold+"\n";
        str += "Weights:\n";
        str += "\tsynWeight = "+synWeight+"\n";
        str += "\thypernWeight = "+hyperWeight+"\n";
        str += "\thypoWeight = "+hypoWeight+"\n";
        str += "\tantoWeight = "+antoWeight+"\n";
        str += "\tdefWeight = "+defWeight+"\n";
        return str;
    }

    public TypedMarkerPassingConfig(TypedMarkerPassingConfig config) {
        this.terminationPulseCount = config.terminationPulseCount;
        this.threshold = config.threshold;
        this.synThreshold = config.synThreshold;
        this.hypoThreshold = config.hypoThreshold;
        this.hyperThreshold = config.hyperThreshold;
        this.antoThreshold = config.antoThreshold;
        this.defThreshold = config.defThreshold;
        this.synWeight = config.synWeight;
        this.hyperWeight = config.hyperWeight;
        this.hypoWeight = config.hypoWeight;
        this.antoWeight = config.antoWeight;
        this.defWeight = config.defWeight;
    }

    public double swap(double num, int index) {
        if(index==0 || index>=getNumberOfParameter())
            throw new IndexOutOfBoundsException("index it either 0 or higher than the number of parameters.");
        double tmp = 0.d;
        switch (index) {
            case 1:
                tmp = this.threshold;
                this.threshold = num;
                return tmp;
            case 2:
                tmp = this.synThreshold;
                this.synThreshold = num;
                return tmp;
            case 3:
                tmp = this.hypoThreshold;
                this.hypoThreshold = num;
                return tmp;
            case 4:
                tmp = this.hyperThreshold;
                this.hyperThreshold = num;
                return tmp;
            case 5:
                tmp = this.antoThreshold;
                this.antoThreshold = num;
                return tmp;
            case 6:
                tmp = this.defThreshold;
                this.defThreshold = num;
                return tmp;
            case 7:
                tmp = this.synWeight;
                this.synWeight = num;
                return tmp;
            case 8:
                tmp = this.hyperWeight;
                this.hyperWeight = num;
                return tmp;
            case 9:
                tmp = this.hypoWeight;
                this.hypoWeight = num;
                return tmp;
            case 10:
                tmp = this.antoWeight;
                this.antoWeight = num;
                return tmp;
            case 11:
                tmp = this.defWeight;
                this.defWeight = num;
                return tmp;
            default:
                return 0.d;
        }
    }


    public double get(int index) {
        if(index==0 || index>=getNumberOfParameter())
            throw new IndexOutOfBoundsException("index it either 0 or higher than the number of parameters.");
        double tmp = 0.d;
        switch (index) {
            case 1:
                tmp = this.threshold;
                return tmp;
            case 2:
                tmp = this.synThreshold;
                return tmp;
            case 3:
                tmp = this.hypoThreshold;
                return tmp;
            case 4:
                tmp = this.hyperThreshold;
                return tmp;
            case 5:
                tmp = this.antoThreshold;
                return tmp;
            case 6:
                tmp = this.defThreshold;
                return tmp;
            case 7:
                tmp = this.synWeight;
                return tmp;
            case 8:
                tmp = this.hyperWeight;
                return tmp;
            case 9:
                tmp = this.hypoWeight;
                return tmp;
            case 10:
                tmp = this.antoWeight;
                return tmp;
            case 11:
                tmp = this.defWeight;
                return tmp;
            default:
                return 0.d;
        }
    }


    public static int getNumberOfParameter() {
        return 12;
    }

}
