/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.dailab.nsm.semanticDistanceMeasures;




/**
 * Created by faehndrich on 07.05.15.
 */
public class SynonymPair extends DataExample {
    String word = null;
    String synonym = null;


    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getSynonym() {
        return synonym;
    }

    public void setSynonym(String synonym) {
        this.synonym = synonym;
    }


    public SynonymPair(String word, String synonym, double distance){
        this.word = word;
        this.synonym = synonym;
        this.trueResult =distance;
    }


}
