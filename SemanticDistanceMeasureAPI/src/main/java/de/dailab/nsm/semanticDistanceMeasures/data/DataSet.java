/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.dailab.nsm.semanticDistanceMeasures.data;

import de.dailab.nsm.semanticDistanceMeasures.DataExample;

import java.util.List;

/**
 * Created by Johannes Fähndrich on 10.06.18 as part of his dissertation.
 */
public interface DataSet {
    List<DataExample> ReadExampleDataSet();
}
