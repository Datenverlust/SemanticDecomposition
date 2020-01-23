/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.kimanufaktur.nsm.decomposition.nlp;

public class GermanLemmaToken implements LemmaToken{

    private String lemma;
    private String posTag;
    private String word;

    public void setLemma(String lemma) {
        this.lemma = lemma;
    }

    public void setPosTag(String posTag) {
        this.posTag = posTag;
    }

    public void setWord(String word) {
        this.word = word;
    }

    @Override
    public String getPosTag() {
        return this.posTag;
    }

    @Override
    public String getLemma() {
        return this.lemma;
    }

    @Override
    public String getWord() {
        return this.word;
    }

    public String toString(){
        StringBuilder builder = new StringBuilder();
        if(this.lemma != null){
            builder.append("[GermanLemmaToken]Lemma: ");
            builder.append(this.lemma);
            builder.append("  ");
        }

        if(this.posTag != null){
            builder.append("POS: ");
            builder.append(this.posTag);
            builder.append("  ");
        }

        if(this.word != null){
            builder.append("Word: ");
            builder.append(this.word);
            builder.append("  ");
        }
        builder.append("\n");
        return builder.toString();
    }
}
