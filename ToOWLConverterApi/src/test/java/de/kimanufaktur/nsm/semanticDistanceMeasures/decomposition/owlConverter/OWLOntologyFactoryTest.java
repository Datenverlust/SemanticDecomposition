/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.kimanufaktur.nsm.decomposition.owlConverter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import de.kimanufaktur.nsm.decomposition.owlConverter.model.OWLConcept;
import de.kimanufaktur.nsm.decomposition.owlConverter.model.OWLProperty;
import de.kimanufaktur.nsm.decomposition.owlConverter.model.OWLRelationType;
import de.kimanufaktur.nsm.decomposition.owlConverter.util.OWLNamespace;

/**
 * Tests the {@link AbstractOWLOntologyFactory} functions and a the avoidance of
 * cyles while creating concepts.
 *
 */
public class OWLOntologyFactoryTest {

    private AbstractOWLOntologyFactory factory;

    private OWLOntologyTestDataProvider owlOntologyTestDataProvider;

    @Before
    public void init() throws OWLOntologyCreationException {
	this.factory = OWLOntologyTestDataProvider.createTestFactory();
	this.owlOntologyTestDataProvider = new OWLOntologyTestDataProvider();
    }

    @Test
    public void testAddConcept() {

	OWLNamedIndividual idv = factory.addConceptRecursivly(
		owlOntologyTestDataProvider.createOWLConcept(OWLOntologyTestDataProvider.CONCEPT1, OWLOntologyTestDataProvider.FISH_TYPE));

	try {
	    assertTrue(OWLOntologyTestDataProvider.getTestOntology(factory).containsIndividualInSignature(idv.getIRI()));
	} catch (OWLOntologyStorageException | OWLOntologyCreationException | IOException e) {
	    e.printStackTrace();
	}
    }

    @Test
    public void testAddConceptWithProperties() {

	OWLConcept concept = owlOntologyTestDataProvider.createOWLConcept(OWLOntologyTestDataProvider.CONCEPT1,
		OWLOntologyTestDataProvider.OBJECT_TYPE);
	Collection<OWLProperty> testProperties = OWLOntologyTestDataProvider.getTestProperties();
	concept.addProperties(testProperties);

	OWLNamedIndividual idv = factory.addConceptRecursivly(concept);

	try {
	    Set<OWLAxiom> axioms = idv.getReferencingAxioms(OWLOntologyTestDataProvider.getTestOntology(factory));
	    Set<OWLNamedIndividual> idvs = getAxiomIndividuals(axioms);
	    Set<String> props = getAxiomPropertyNames(axioms);

	    assertTrue(idvs.contains(idv));
	    assertTrue(props.containsAll(testProperties.stream().map(p -> p.getType().getIRI().toString()).collect(Collectors.toSet())));

	} catch (OWLOntologyStorageException | OWLOntologyCreationException | IOException e) {
	    e.printStackTrace();
	}
    }

    @Test
    public void testAddConceptWithRelations() {
	OWLConcept concept1 = owlOntologyTestDataProvider.createOWLConcept(OWLOntologyTestDataProvider.CONCEPT1,
		OWLOntologyTestDataProvider.OBJECT_TYPE);
	OWLConcept concept2 = owlOntologyTestDataProvider.createOWLConcept(OWLOntologyTestDataProvider.CONCEPT2,
		OWLOntologyTestDataProvider.ANIMAL_TYPE);
	OWLConcept concept3 = owlOntologyTestDataProvider.createOWLConcept(OWLOntologyTestDataProvider.CONCEPT3,
		OWLOntologyTestDataProvider.DEER_TYPE);

	concept1.addRelations(owlOntologyTestDataProvider.getTestRelations(concept1.getName(), concept2.getName(), concept3.getName()));

	OWLNamedIndividual idv2 = factory.addConceptRecursivly(concept2);
	OWLNamedIndividual idv1 = factory.addConceptRecursivly(concept1);

	try {

	    Map<OWLObjectPropertyExpression, Set<OWLIndividual>> objectProperties = idv1
		    .getObjectPropertyValues(OWLOntologyTestDataProvider.getTestOntology(factory));
	    Set<String> relationIris = getOWLObjectPropertyNames(objectProperties);
	    Set<OWLIndividual> relationTargets = getAllOWLIndividuals(objectProperties);

	    assertTrue(relationIris
		    .contains(OWLNamespace.OWL_RELATION_NAMESPACE + OWLOntologyTestDataProvider.SYNONYMY_TPYE));
	    assertTrue(relationIris.contains(OWLNamespace.OWL_RELATION_NAMESPACE + OWLOntologyTestDataProvider.ANTONYMY_TYPE));
	    assertFalse(relationIris.contains(OWLNamespace.OWL_RELATION_NAMESPACE + OWLOntologyTestDataProvider.HYPONYMY_TYPE));
	    assertTrue(relationTargets.contains(idv2));
	    assertFalse(relationTargets.contains(idv1));

	} catch (OWLOntologyStorageException | OWLOntologyCreationException | IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

    }

    @Test
    public void testAddCylicConcepts() {
	OWLConcept concept1 = owlOntologyTestDataProvider.createOWLConcept(OWLOntologyTestDataProvider.CONCEPT1,
		OWLOntologyTestDataProvider.TREE_TYPE);
	OWLConcept concept2 = owlOntologyTestDataProvider.createOWLConcept(OWLOntologyTestDataProvider.CONCEPT2,
		OWLOntologyTestDataProvider.TREE_TYPE);
	OWLConcept concept3 = owlOntologyTestDataProvider.createOWLConcept(OWLOntologyTestDataProvider.CONCEPT3,
		OWLOntologyTestDataProvider.TREE_TYPE);
	OWLRelationType isNextTo = OWLOntologyTestDataProvider.createOWLRelationType(OWLOntologyTestDataProvider.IS_NEXT_TO_RELATION_TYPE);

	concept1.addRelations(
		Collections.singleton(OWLOntologyTestDataProvider.createOWLRelation(OWLOntologyTestDataProvider.TEST_RELATION_1, concept1,
			concept2, isNextTo)));
	concept2.addRelations(
		Collections.singleton(OWLOntologyTestDataProvider.createOWLRelation(OWLOntologyTestDataProvider.TEST_RELATION_2, concept2,
			concept3, isNextTo)));
	concept3.addRelations(
		Collections.singleton(OWLOntologyTestDataProvider.createOWLRelation(OWLOntologyTestDataProvider.TEST_RELATION_3, concept3,
			concept1, isNextTo)));

	factory.addConceptRecursivly(concept1);
	factory.addConceptRecursivly(concept2);
	factory.addConceptRecursivly(concept3);

	// passed, if test terminates (because of cycles)

    }

    private Set<String> getOWLObjectPropertyNames(Map<OWLObjectPropertyExpression, Set<OWLIndividual>> properties) {
	return properties.keySet().stream().map(p -> p.getNamedProperty().getIRI().toString())
		.collect(Collectors.toSet());
    }

    private Set<OWLIndividual> getAllOWLIndividuals(Map<OWLObjectPropertyExpression, Set<OWLIndividual>> properties) {
	return properties.values().stream().flatMap(o -> o.stream())
		.collect(Collectors.toSet());
    }

    private Set<OWLNamedIndividual> getAxiomIndividuals(Set<OWLAxiom> axioms) {
	return axioms.stream().flatMap(a -> a.getIndividualsInSignature().stream()).collect(Collectors.toSet());
    }

    private Set<String> getAxiomPropertyNames(Set<OWLAxiom> axioms) {
	return axioms.stream()
		.flatMap(a -> a.getDataPropertiesInSignature().stream()).map(p -> p.getIRI().toString()).collect(Collectors.toSet());
    }

}
