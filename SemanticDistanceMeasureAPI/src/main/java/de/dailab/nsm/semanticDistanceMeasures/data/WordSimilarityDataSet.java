/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.dailab.nsm.semanticDistanceMeasures.data;
import de.dailab.nsm.semanticDistanceMeasures.SynonymPair;

import java.util.Collection;
import java.util.List;

/**
 * Created by faehndrich on 31.07.15.
 */
public interface WordSimilarityDataSet extends DataSet{

    Collection<SynonymPair> Normalize(List<SynonymPair> list2Normlize);

}
