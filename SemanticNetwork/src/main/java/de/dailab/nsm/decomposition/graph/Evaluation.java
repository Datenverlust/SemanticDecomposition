/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.dailab.nsm.decomposition.graph;

import de.dailab.nsm.semanticDistanceMeasures.DataExample;
import de.dailab.nsm.semanticDistanceMeasures.SimilarityPair;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;

import java.util.Collection;
import java.util.Iterator;

/**
 * Created by faehndrich on 11.03.16.
 */
public class Evaluation {

    /**
     * Calculats the pearson correlation coefficient {@link https://de.wikipedia.org/wiki/Korrelationskoeffizient} between the
     * test result and the human semantic distance measure.
     *
     * @param experimantResult the collection of synonym pairs we want to correlation
     * @return the Pearson correlation coefficient
     */
    static public double PearsonCorrelation(Collection<? extends DataExample> experimantResult) {
        double correlation = 0.0d;
        double[] human = new double[experimantResult.size()];
        double[] result = new double[experimantResult.size()];
        Iterator iterator = experimantResult.iterator();
        for (int i = 0; i < experimantResult.size(); i++) {
            SimilarityPair pair = (SimilarityPair) iterator.next();
            human[i] = pair.getTrueResult();
            result[i] = pair.getResult();
//            System.out.println(pair.getString1() + "," + pair.getString2() + "," + pair.getDistance() + "," + pair.getResult());
        }
        PearsonsCorrelation pearsonsCorrelation = new PearsonsCorrelation();
        correlation = pearsonsCorrelation.correlation(human, result);
        return correlation;
    }

    /**
     * Calculats the  Spearman correlation coefficient {@link https://de.wikipedia.org/wiki/Rangkorrelationskoeffizient} between the
     * test result and the human semantic distance measure.
     *
     * @param experimantResult the collection of synonym pairs we want to correlation
     * @return the  Spearman correlation coefficient
     */
    static public double SpearmanCorrelation(Collection<? extends DataExample> experimantResult) {
        double correlation = 0.0d;

        double[] human = new double[experimantResult.size()];
        double[] result = new double[experimantResult.size()];
        Iterator iterator = experimantResult.iterator();
        for (int i = 0; i < experimantResult.size(); i++) {
            SimilarityPair pair = (SimilarityPair) iterator.next();
            human[i] = pair.getTrueResult();
            result[i] = pair.getResult();
        }
        SpearmansCorrelation spearmansCorrelation = new SpearmansCorrelation();
        correlation = spearmansCorrelation.correlation(human, result);
        if (Double.isNaN(correlation)) {
            correlation = 0.0d;
        }
        return correlation;
    }

    /**
     * normalize the results of the test collection of Synonym paris {@see SynonymPair}.
     *
     * @param experimantResult the collection of synonym pairs which needs normalization.
     * @return a normalized collection of synonym pairs.
     */
    static public Collection<? extends DataExample> normalize(Collection<? extends DataExample> experimantResult) {
        double max = -1.0;
        for (DataExample pair : experimantResult) {
            if (pair.getResult() > max) {
                max = pair.getResult();
            }
        }
        if (max > 0) {
            for (DataExample pair : experimantResult) {
                pair.setResult(pair.getResult() / max);
            }
        }
        return experimantResult;
    }


}
