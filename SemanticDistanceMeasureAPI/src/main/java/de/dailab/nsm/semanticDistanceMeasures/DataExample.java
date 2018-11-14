/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.dailab.nsm.semanticDistanceMeasures;

/**
 * Created by Johannes Fähndrich on 10.06.18 as part of his dissertation.
 */
public class DataExample implements Cloneable {
    double result = Double.NaN;
    double trueResult = Double.NaN;
    public double getResult() {
        return result;
    }
    public void setResult(double result) {
        this.result = result;
    }

    public double getTrueResult() {
        return trueResult;
    }
    public void setTrueResult(double trueResult) {
        this.trueResult = trueResult;
    }

    @Override
    public Object clone(){
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }

}
