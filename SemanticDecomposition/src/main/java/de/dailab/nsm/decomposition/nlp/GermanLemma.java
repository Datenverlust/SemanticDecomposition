/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.dailab.nsm.decomposition.nlp;

import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.tagging.de.GermanTagger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GermanLemma implements Lemmatisation {

    public static final String LEMMA_UNKNOWN = "LEMMA_UNKNOWN";

    @Override
    public List<LemmaToken> lemma(String word) {
        List<LemmaToken> result = new ArrayList<>();
        try {
            GermanTagger tagger = new GermanTagger();
            AnalyzedTokenReadings readings = tagger.lookup(word);
            if(readings != null){
                List<AnalyzedToken> token = readings.getReadings();
                for(AnalyzedToken t: token){
                    GermanLemmaToken glt = new GermanLemmaToken();
                    glt.setLemma(t.getLemma());
                    glt.setPosTag(t.getPOSTag());
                    glt.setWord(t.getToken());
                    result.add(glt);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}
