/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.dailab.nsm.semanticDistanceMeasures;

import de.dailab.nsm.decomposition.IConcept;

/**
 * Created by Sabine on 09.07.2015.
 */
public interface SemanticDistanceMeasureInterface {
    double compareConcepts(IConcept c1, IConcept c2);
}
