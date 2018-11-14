/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.dailab.nsm.decomposition.owlConverter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.vocab.OWL2Datatype;

import de.dailab.nsm.decomposition.owlConverter.model.OWLConcept;
import de.dailab.nsm.decomposition.owlConverter.model.OWLProperty;
import de.dailab.nsm.decomposition.owlConverter.model.OWLRelation;
import de.dailab.nsm.decomposition.owlConverter.model.OWLRelationType;
import de.dailab.nsm.decomposition.owlConverter.util.OWLNamespace;

/**
 * Provides test data for the ToOWLConverterAPI tests.
 */
public class OWLOntologyTestDataProvider {

    protected static final String TEMP_DATA_PREFIX = "temp";
    protected static final String TEMP_DATA_SUFFIX = ".owl";

    protected static final String CONCEPT1 = "concept1";
    protected static final String CONCEPT2 = "concept2";
    protected static final String CONCEPT3 = "concept3";

    protected static final String OBJECT_TYPE = "object";
    protected static final String FISH_TYPE = "fish";
    protected static final String TREE_TYPE = "tree";
    protected static final String ANIMAL_TYPE = "animal";
    protected static final String DEER_TYPE = "deer";

    protected static final String IS_NEXT_TO_RELATION_TYPE = "is_next_to";
    protected static final String SYNONYMY_TPYE = "synonymy";
    protected static final String ANTONYMY_TYPE = "antonymy";
    protected static final String HYPONYMY_TYPE = "hyponymy";

    protected static final String TEST_RELATION_1 = "testRelation1";
    protected static final String TEST_RELATION_2 = "testRelation2";
    protected static final String TEST_RELATION_3 = "testRelation3";

    protected static AbstractOWLOntologyFactory createTestFactory() throws OWLOntologyCreationException {
	return new AbstractOWLOntologyFactory() {

	    @Override
	    protected OWLAxiom addProperty(OWLProperty property, OWLIndividual idv) {

		OWLDataProperty owlDataProperty = factory.getOWLDataProperty(property.getType().getIRI());
		OWLLiteral owlLiteral = factory.getOWLLiteral(property.getValueAsString(), property.getType());

		return factory.getOWLDataPropertyAssertionAxiom(owlDataProperty, idv,
			owlLiteral);
	    }

	    @Override
	    protected OWLAxiom addRelation(OWLRelation relation) {
		OWLObjectProperty role = factory
			.getOWLObjectProperty(IRI.create(OWLNamespace.OWL_RELATION_NAMESPACE + relation.getType().getName()));

		OWLNamedIndividual source = factory
			.getOWLNamedIndividual(IRI.create(OWLNamespace.OWL_INDIVIDUAL_NAMESPACE + relation.getSource().getName()));
		OWLNamedIndividual target = factory
			.getOWLNamedIndividual(IRI.create(OWLNamespace.OWL_INDIVIDUAL_NAMESPACE + relation.getTarget().getName()));

		return factory.getOWLObjectPropertyAssertionAxiom(role, source,
			target);
	    }

	};

    }

    protected OWLConcept createOWLConcept(String name, String type) {
	return new OWLTestConcept(name, type, new ArrayList<>(), new ArrayList<>());
    }

    protected static OWLProperty createOWLDataProperty(String valueAsString, OWL2Datatype type) {
	return new OWLProperty() {

	    @Override
	    public String getValueAsString() {
		return valueAsString;
	    }

	    @Override
	    public OWL2Datatype getType() {
		return type;
	    }
	};
    }

    protected static OWLRelation createOWLRelation(String name, OWLConcept source, OWLConcept target, OWLRelationType type) {
	return new OWLRelation() {

	    @Override
	    public String getName() {
		return name;
	    }

	    @Override
	    public OWLRelationType getType() {
		return type;
	    }

	    @Override
	    public OWLConcept getTarget() {
		return target;
	    }

	    @Override
	    public OWLConcept getSource() {
		return source;
	    }
	};

    }

    protected static OWLRelationType createOWLRelationType(String name) {
	return new OWLRelationType() {

	    @Override
	    public String getName() {
		return name;
	    }
	};
    }

    protected static Collection<OWLProperty> getTestProperties() {
	return Arrays.asList(OWLOntologyTestDataProvider.createOWLDataProperty("10", OWL2Datatype.XSD_INT),
		OWLOntologyTestDataProvider.createOWLDataProperty("big", OWL2Datatype.XSD_STRING),
		OWLOntologyTestDataProvider.createOWLDataProperty("10.3", OWL2Datatype.XSD_DOUBLE),
		OWLOntologyTestDataProvider.createOWLDataProperty("true", OWL2Datatype.XSD_BOOLEAN));
    }

    protected Collection<OWLRelation> getTestRelations(String entity1, String entity2, String entity3) {
	OWLOntologyTestDataProvider owlOntologyTestDataProvider = new OWLOntologyTestDataProvider();

	OWLConcept owlConcept1 = owlOntologyTestDataProvider.createOWLConcept(entity1, OBJECT_TYPE);
	OWLConcept owlConcept2 = owlOntologyTestDataProvider.createOWLConcept(entity2, OBJECT_TYPE);
	OWLConcept owlConcept3 = owlOntologyTestDataProvider.createOWLConcept(entity3, OBJECT_TYPE);

	OWLRelationType synonymType = OWLOntologyTestDataProvider.createOWLRelationType(SYNONYMY_TPYE);
	OWLRelationType antonymType = OWLOntologyTestDataProvider.createOWLRelationType(ANTONYMY_TYPE);
	OWLRelationType hyponymType = OWLOntologyTestDataProvider.createOWLRelationType(HYPONYMY_TYPE);

	return Arrays.asList(
		OWLOntologyTestDataProvider.createOWLRelation("TEST_RELATION_1", owlConcept1, owlConcept2, synonymType),
		OWLOntologyTestDataProvider.createOWLRelation("TEST_RELATION_2", owlConcept1, owlConcept2, antonymType),
		OWLOntologyTestDataProvider.createOWLRelation("TEST_RELATION_3", owlConcept3, owlConcept1, hyponymType));
    }

    protected static OWLOntology getTestOntology(AbstractOWLOntologyFactory factory)
	    throws IOException, OWLOntologyStorageException, OWLOntologyCreationException {

	File tmpFile = File.createTempFile(TEMP_DATA_PREFIX, TEMP_DATA_SUFFIX);
	IRI tmpFileIRI = IRI.create(tmpFile);

	factory.saveOntology(tmpFileIRI);
	return AbstractOWLOntologyFactory.loadOntology(tmpFileIRI);

    }

    protected class OWLTestConcept implements OWLConcept {
	private final String name;
	private final String type;
	private Collection<OWLProperty> properties;
	private Collection<OWLRelation> relations;

	protected OWLTestConcept(String name, String type, Collection<OWLProperty> properties,
		Collection<OWLRelation> relations) {
	    this.name = name;
	    this.type = type;
	    this.properties = properties != null ? properties : new HashSet<>();
	    this.relations = relations != null ? relations : new HashSet<>();

	}

	@Override
	public String getName() {
	    return name;
	}

	@Override
	public Collection<OWLProperty> getProperties() {
	    return properties;
	}

	@Override
	public Collection<OWLRelation> getRelations() {
	    return relations;
	}

	@Override
	public String getType() {
	    return type;
	}

	@Override
	public void addProperties(Collection<OWLProperty> properties) {
	    this.properties.addAll(properties);
	}

	@Override
	public void addRelations(Collection<OWLRelation> relations) {
	    for (OWLRelation relation : relations) {
		if (relation.getSource().getName().equals(this.getName())) {
		    this.relations.add(relation);
		}
	    }
	}

    }

}
