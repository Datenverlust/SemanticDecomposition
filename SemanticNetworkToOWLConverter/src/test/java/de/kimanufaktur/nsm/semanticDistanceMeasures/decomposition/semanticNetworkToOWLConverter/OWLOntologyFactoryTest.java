/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.kimanufaktur.nsm.decomposition.semanticNetworkToOWLConverter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
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

import de.kimanufaktur.nsm.decomposition.owlConverter.AbstractOWLOntologyFactory;
import de.kimanufaktur.nsm.decomposition.owlConverter.model.OWLConcept;
import de.kimanufaktur.nsm.decomposition.owlConverter.model.OWLProperty;
import de.kimanufaktur.nsm.decomposition.owlConverter.util.OWLNamespace;
import de.kimanufaktur.nsm.decomposition.semanticNetworkToOWLConverter.model.SemanticNetworkConcept;
import de.kimanufaktur.nsm.decomposition.semanticNetworkToOWLConverter.model.SemanticNetworkRelation;
import de.kimanufaktur.nsm.decomposition.semanticNetworkToOWLConverter.model.SemanticNetworkRelationType;

public class OWLOntologyFactoryTest {

    private AbstractOWLOntologyFactory factory;

    @Before
    public void init() throws OWLOntologyCreationException {
	this.factory = new OWLOntologyFactory();
    }

    @Test
    public void testAddConcept() {

	OWLNamedIndividual idv = factory.addConceptRecursivly(
		OWLOntologyTestDataProvider.getDefaultConcept(OWLOntologyTestDataProvider.CONCEPT1, OWLOntologyTestDataProvider.FISH_TYPE));

	try {
	    assertTrue(OWLOntologyTestDataProvider.getTestOntology(factory).containsIndividualInSignature(idv.getIRI()));
	} catch (OWLOntologyStorageException | OWLOntologyCreationException | IOException e) {
	    e.printStackTrace();
	}
    }

    @Test
    public void testAddConceptWithProperties() {

	OWLConcept concept = OWLOntologyTestDataProvider.getDefaultConcept(OWLOntologyTestDataProvider.CONCEPT1,
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
	OWLConcept concept1 = OWLOntologyTestDataProvider.getDefaultConcept(OWLOntologyTestDataProvider.CONCEPT1,
		OWLOntologyTestDataProvider.OBJECT_TYPE);
	OWLConcept concept2 = OWLOntologyTestDataProvider.getDefaultConcept(OWLOntologyTestDataProvider.CONCEPT2,
		OWLOntologyTestDataProvider.ANIMAL_TYPE);
	OWLConcept concept3 = OWLOntologyTestDataProvider.getDefaultConcept(OWLOntologyTestDataProvider.CONCEPT3,
		OWLOntologyTestDataProvider.DEER_TYPE);

	concept1.addRelations(OWLOntologyTestDataProvider.getTestRelations(concept1.getName(), concept2.getName(), concept3.getName()));

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
	OWLConcept concept1 = new SemanticNetworkConcept(OWLOntologyTestDataProvider.DEFAULT_ID, OWLOntologyTestDataProvider.CONCEPT1,
		OWLOntologyTestDataProvider.TREE_TYPE, new ArrayList<>(), new ArrayList<>());
	OWLConcept concept2 = new SemanticNetworkConcept(OWLOntologyTestDataProvider.DEFAULT_ID, OWLOntologyTestDataProvider.CONCEPT2,
		OWLOntologyTestDataProvider.TREE_TYPE, new ArrayList<>(), new ArrayList<>());
	OWLConcept concept3 = new SemanticNetworkConcept(OWLOntologyTestDataProvider.DEFAULT_ID, OWLOntologyTestDataProvider.CONCEPT3,
		OWLOntologyTestDataProvider.TREE_TYPE, new ArrayList<>(), new ArrayList<>());

	concept1.addRelations(
		Collections.singleton(new SemanticNetworkRelation(OWLOntologyTestDataProvider.TEST_RELATION_1, concept1,
			concept2, SemanticNetworkRelationType.SYNONYM)));
	concept2.addRelations(
		Collections.singleton(new SemanticNetworkRelation(OWLOntologyTestDataProvider.TEST_RELATION_2, concept2,
			concept3, SemanticNetworkRelationType.SYNONYM)));
	concept3.addRelations(
		Collections.singleton(new SemanticNetworkRelation(OWLOntologyTestDataProvider.TEST_RELATION_3, concept3,
			concept1, SemanticNetworkRelationType.SYNONYM)));

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
