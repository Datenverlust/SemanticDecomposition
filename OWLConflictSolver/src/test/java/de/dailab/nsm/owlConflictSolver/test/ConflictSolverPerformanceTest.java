/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.dailab.nsm.owlConflictSolver.test;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import com.clarkparsia.pellet.owlapiv3.PelletReasoner;

import de.dailab.nsm.owlConflictSolver.impl.ConflictSolver;

/**
 * Configure by changing the AMOUNT_TEST_CYCLES value.
 * 
 * The test assumes that the regarding ontology with the given amount of cycles
 * was already created by the {@link CyclicOntologyGenerator} and can be found
 * in the same directory the generator saved it in.
 *
 */
public class ConflictSolverPerformanceTest {

    private final static long AMOUNT_TEST_CYCLES = 4000;
    private final static String START_MESSAGE = "Start solving conflicts... .\n";
    private final static String END_MESSAGE = " millis needed to resolve conflicts.\n";

    private final TestDataProvider testDataProvider = new TestDataProvider();
    private final Logger logger = Logger.getRootLogger();

    @Test
    public void reflexiveISAConflictDetectionPerformanceTest() throws OWLOntologyCreationException {
	PelletReasoner pellet = testDataProvider
		.loadOntologyWithPellet(new CyclicOntologyGenerator().REFLEXIVE_ISA_CONFLICT_GENERATED_ONTOLOGY(AMOUNT_TEST_CYCLES));
	logger.info(START_MESSAGE);
	long start = System.currentTimeMillis();
	new ConflictSolver().resolveConflicts(pellet);
	logger.info((System.currentTimeMillis() - start) + END_MESSAGE);
    }

}
