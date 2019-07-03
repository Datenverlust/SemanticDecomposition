/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.kimanufaktur.nsm.owlConflictSolver.test;

import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test if pellet already detects certain conflict cases concerning the ISA
 * relation. 
 * If pellet won't detect anything, it must hold that: 
 * 1. The only unsatisfiable class for pellet should be owl:Nothing. 
 * 2. Pellet shouldn't detect an inconsistency (pellet remains consistent).
 */
public class PelletConflictDetectionTest {

    private TestDataProvider testDataProvider = new TestDataProvider();

    // pellet doesn't detect conflict
    @Test
    public void reflexiveISAConflictDetectionTest() throws OWLOntologyCreationException {
	PelletReasoner pellet = testDataProvider.loadOntologyWithPellet(testDataProvider.REFLEXIVE_ISA_CONFLICT_ONTOLOGY);
	assertTrue(pellet.getUnsatisfiableClasses().getSize() == 1 && pellet.isConsistent());
    }

    // pellet already detects conflict
    @Test
    public void disjointISAConflictDetectionTest() throws OWLOntologyCreationException {
	PelletReasoner pellet = testDataProvider.loadOntologyWithPellet(testDataProvider.DISJOINT_ISA_CONFLICT_ONTOLOGY);
	assertFalse(pellet.getUnsatisfiableClasses().getSize() == 1 && pellet.isConsistent());
    }

    // pellet doesn't detect conflict
    @Test
    public void cyclicISAConflictDetectionTest() throws OWLOntologyCreationException {
	PelletReasoner pellet = testDataProvider.loadOntologyWithPellet(testDataProvider.CYCLIC_ISA_CONFLICT_ONTOLOGY);
	assertTrue(pellet.getUnsatisfiableClasses().getSize() == 1 && pellet.isConsistent());
    }

    // pellet doesn't detect conflict
    @Test
    public void multipleInheritanceConflictDetectionTest() throws OWLOntologyCreationException {
	PelletReasoner pellet = testDataProvider.loadOntologyWithPellet(testDataProvider.MULTIPLE_INHERITANCE_CONFLICT_ONTOLOGY);
	assertTrue(pellet.getUnsatisfiableClasses().getSize() == 1 && pellet.isConsistent());
    }

    // pellet doesn't detect conflict
    @Test
    public void inheritancePropertyConflictDetectionTest() throws OWLOntologyCreationException {
	PelletReasoner pellet = testDataProvider.loadOntologyWithPellet(testDataProvider.INHERITENCE_PROPERTY_CONFLICT_ONTOLOGY);
	assertTrue(pellet.getUnsatisfiableClasses().getSize() == 1 && pellet.isConsistent());
    }

}
