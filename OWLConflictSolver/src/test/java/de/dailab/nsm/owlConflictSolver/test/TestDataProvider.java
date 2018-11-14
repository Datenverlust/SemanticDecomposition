/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.dailab.nsm.owlConflictSolver.test;

import java.io.File;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;

import de.dailab.nsm.decomposition.owlConverter.AbstractOWLOntologyFactory;

/**
 * Manages loading of test datas and test ontologies.
 *
 */
public class TestDataProvider {
    protected static final String VOGEL_ONTOLOGY_ENTITY = "http://www.dailab.de/ontologies/class#Vogel";
    protected static final String PINGUIN_ONTOLOGY_ENTITY = "http://www.dailab.de/ontologies/class#Pinguin";

    private final Logger logger = Logger.getRootLogger();

    protected final String NON_CONFLICTED_ANIMALS_ONTOLOGY = getClass().getResource(File.separator + "owl" + File.separator + "pinguin_vogel_without_conflict.owl").getPath();
    protected final String REFLEXIVE_ISA_CONFLICT_ONTOLOGY = getClass().getResource( File.separator +"owl"+ File.separator +"reflexive_isa_conflict.owl").getPath();
    protected final String DISJOINT_ISA_CONFLICT_ONTOLOGY = getClass().getResource(File.separator +"owl"+ File.separator +"disjoint_conflict.owl").getPath();
    protected final String CYCLIC_ISA_CONFLICT_ONTOLOGY = getClass().getResource(File.separator +"owl"+ File.separator +"extended_cyclic_isa_conflict.owl").getPath();
    protected final String MULTIPLE_INHERITANCE_CONFLICT_ONTOLOGY = getClass().getResource(File.separator +"owl"+ File.separator +"multiple_inheritance_conflict.owl")
	    .getPath();
    protected final String INHERITENCE_PROPERTY_CONFLICT_ONTOLOGY = getClass().getResource(File.separator +"owl"+ File.separator +"property_conflict.owl").getPath();
    protected final String COMPLETE_EXTENDED_CONFLICTED_ONTOLOGY = getClass().getResource(File.separator +"owl"+ File.separator +"complete_extended_animal_ontology.owl")
	    .getPath();

    protected PelletReasoner loadOntologyWithPellet(String filePath) throws OWLOntologyCreationException {
	OWLOntology ont = TestDataProvider.loadOntology(filePath);
	long start = System.currentTimeMillis();
	logger.info("Pellet starts preparations... \n");
	PelletReasoner pellet = PelletReasonerFactory.getInstance().createReasoner(ont);
	pellet.prepareReasoner();
	logger.info("Pellet took " + (System.currentTimeMillis() - start) + " millis for preparations.\n");
	return pellet;
    }

    protected static OWLOntology loadOntology(String path) throws OWLOntologyCreationException {
	return AbstractOWLOntologyFactory.loadOntology(IRI.create(new File(path)));
    }
}
