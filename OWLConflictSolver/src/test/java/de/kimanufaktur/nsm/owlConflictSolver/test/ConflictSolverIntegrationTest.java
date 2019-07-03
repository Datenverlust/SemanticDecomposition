/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.kimanufaktur.nsm.owlConflictSolver.test;

import com.clarkparsia.owlapiv3.OWL;
import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import de.kimanufaktur.nsm.owlConflictSolver.impl.ConflictSolver;
import de.kimanufaktur.nsm.owlConflictSolver.local.OWLOntologyChangeListenerImpl;
import org.junit.Test;
import org.semanticweb.owlapi.model.*;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * Tests the {@link ConflictSolver} concerning the implemented ISA relation
 * conflict cases. There are two tests for each conflict case: For manual
 * invocation of the {@link ConflictSolver} and for invocation by the
 * {@link OWLOntologyChangeListenerImpl}.
 * 
 * The resolveConflictInExtendedOntologyIntegrationTest takes an ontology,
 * solves conflicts and saves the result as a temp owl file. You can compare
 * before and result in an ontology editor of your choice (e.g. Prot�g�).
 *
 */
public class ConflictSolverIntegrationTest {

    private final TestDataProvider testDataProvider = new TestDataProvider();

    @Test
    public void reflexiveISAConflictDetectionTest() throws OWLOntologyCreationException {
	PelletReasoner pellet = testDataProvider.loadOntologyWithPellet(testDataProvider.REFLEXIVE_ISA_CONFLICT_ONTOLOGY);
	OWLOntology ont = pellet.getRootOntology();

	// ontology including two reflexive ISA cycles
	// System.out.println(ont.getAxioms());
	checkForCyles(ont, true);

	// invoke conflict solver manually
	new ConflictSolver().resolveConflicts(pellet);

	// the two conflicts should have been removed
	// System.out.println(ont.getAxioms());
	checkForCyles(ont, false);

	// another conflict resolution would nothing change
	new ConflictSolver().resolveConflicts(pellet);
	checkForCyles(ont, false);
    }

    @Test
    public void reflexiveISAConflictDetectionByListenerTest() throws OWLOntologyCreationException {
	OWLOntology ont = TestDataProvider.loadOntology(testDataProvider.NON_CONFLICTED_ANIMALS_ONTOLOGY);
	OWLOntologyManager manager = ont.getOWLOntologyManager();
	// attach listener to ontology
	manager.addOntologyChangeListener(new OWLOntologyChangeListenerImpl());

	// ontology should have no conflicts
	// System.out.println(ont.getAxioms());
	checkForCyles(ont, false);

	// add conflict manually to ontology (vogel is sub class of vogel)
	manager.addAxiom(ont, getSubClassOfAxiom(ont, TestDataProvider.VOGEL_ONTOLOGY_ENTITY));

	// conflict procedure should remove the conflict automatically so the
	// ontology looks like before the conflict
	checkForCyles(ont, false);
    }

    @Test
    public void resolveConflictInExtendedOntologyIntegrationTest()
	    throws OWLOntologyCreationException, OWLOntologyStorageException, IOException {
	PelletReasoner pellet = testDataProvider.loadOntologyWithPellet(testDataProvider.COMPLETE_EXTENDED_CONFLICTED_ONTOLOGY);
	OWLOntology ont = pellet.getRootOntology();

	// invoke conflict solver manually
	new ConflictSolver().resolveConflicts(pellet);

	File tmpFile = File.createTempFile("COMPLETE_EXTENDED_CONFLICTED_ONTOLOGY_WITHOUT_CONFLICT", ".owl");
	IRI tmpFileIRI = IRI.create(tmpFile);
	ont.getOWLOntologyManager().saveOntology(ont, tmpFileIRI);
    }

    private void checkForCyles(OWLOntology ont, Boolean cylesExpected) {
	String vogel = TestDataProvider.VOGEL_ONTOLOGY_ENTITY;
	OWLSubClassOfAxiom vogelSubClassOfVogel = getSubClassOfAxiom(ont, vogel);
	Set<OWLSubClassOfAxiom> vogelSubClasses = ont.getSubClassAxiomsForSubClass(OWL.Class(vogel));
	Set<OWLSubClassOfAxiom> vogelSuperClasses = ont.getSubClassAxiomsForSuperClass(OWL.Class(vogel));

	String pinguin = TestDataProvider.PINGUIN_ONTOLOGY_ENTITY;
	OWLSubClassOfAxiom pinguinSubClassOfVogel = getSubClassOfAxiom(ont, pinguin);
	Set<OWLSubClassOfAxiom> pinguinSubClasses = ont.getSubClassAxiomsForSubClass(OWL.Class(pinguin));
	Set<OWLSubClassOfAxiom> vpinguinSuperClasses = ont
		.getSubClassAxiomsForSuperClass(OWL.Class(pinguin));

	// since subClassOf equals superClassOf^(-1) for the same entity, we can
	// use the subClassAxiom as a superClassAxiom too
	Boolean vogelCyles = vogelSubClasses.contains(vogelSubClassOfVogel) || vogelSuperClasses.contains(vogelSubClassOfVogel);
	Boolean pinguinCyles = pinguinSubClasses.contains(pinguinSubClassOfVogel) || vpinguinSuperClasses.contains(pinguinSubClassOfVogel);

	// check for cyles of both enitites
	assertEquals(cylesExpected, vogelCyles || pinguinCyles);
    }

    private OWLSubClassOfAxiom getSubClassOfAxiom(OWLOntology ont, String entity) {
	OWLDataFactory factory = ont.getOWLOntologyManager().getOWLDataFactory();
	OWLClass entityClass = OWL.Class(entity);
	return factory.getOWLSubClassOfAxiom(entityClass, entityClass);
    }

}
