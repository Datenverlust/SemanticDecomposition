/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.kimanufaktur.nsm.decomposition.graph.spreadingActivation;

import de.kimanufaktur.markerpassing.ProcessingStep;

/**
 * Created by faehndrich on 19.05.15.
 */
public class countTerminationCondition implements ProcessingStep {
    int pulsecount = 0;

    public void setPulscount(int count){
        this.pulsecount = count;
    }

    @Override
    public void execute() {
        pulsecount++;
    }
}
