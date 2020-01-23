/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.kimanufaktur.nsm.semanticDistanceMeasures;
/**
 * Created by faehndrich on 07.05.15.
 */
public class SimilarityPair extends DataExample {
    String string1 = null;
    String string2 = null;

    public String getString1() {
        return string1;
    }

    public void setString1(String word) {
        this.string1 = word;
    }

    public String getString2() {
        return string2;
    }

    public void setString2(String string2) {
        this.string2 = string2;
    }

    public SimilarityPair(String string1, String string2, double distance){
        this.string1 = string1;
        this.string2 = string2;
        this.setTrueResult(distance);
    }

    @Override
    public Object clone(){
        return super.clone();
    }
}
