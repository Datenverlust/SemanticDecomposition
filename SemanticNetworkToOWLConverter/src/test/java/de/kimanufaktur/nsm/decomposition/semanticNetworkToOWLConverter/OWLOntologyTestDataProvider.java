/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.kimanufaktur.nsm.decomposition.semanticNetworkToOWLConverter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.vocab.OWL2Datatype;

import de.kimanufaktur.nsm.decomposition.owlConverter.AbstractOWLOntologyFactory;
import de.kimanufaktur.nsm.decomposition.owlConverter.model.OWLConcept;
import de.kimanufaktur.nsm.decomposition.owlConverter.model.OWLProperty;
import de.kimanufaktur.nsm.decomposition.owlConverter.model.OWLRelation;
import de.kimanufaktur.nsm.decomposition.semanticNetworkToOWLConverter.model.SemanticNetworkConcept;
import de.kimanufaktur.nsm.decomposition.semanticNetworkToOWLConverter.model.SemanticNetworkProperty;
import de.kimanufaktur.nsm.decomposition.semanticNetworkToOWLConverter.model.SemanticNetworkRelation;
import de.kimanufaktur.nsm.decomposition.semanticNetworkToOWLConverter.model.SemanticNetworkRelationType;

public class OWLOntologyTestDataProvider {

    protected static final String TEMP_DATA_PREFIX = "temp";
    protected static final String TEMP_DATA_SUFFIX = ".owl";

    protected static final String CONCEPT1 = "concept1";
    protected static final String CONCEPT2 = "concept2";
    protected static final String CONCEPT3 = "concept3";

    protected static final Long DEFAULT_ID = 1L;

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

    protected static Collection<OWLProperty> getTestProperties() {
	return Arrays.asList(new SemanticNetworkProperty("10", OWL2Datatype.XSD_INT),
		new SemanticNetworkProperty("big", OWL2Datatype.XSD_STRING),
		new SemanticNetworkProperty("10.3", OWL2Datatype.XSD_DOUBLE),
		new SemanticNetworkProperty("true", OWL2Datatype.XSD_BOOLEAN));
    }

    protected static OWLConcept getDefaultConcept(String conceptName, String type) {
	return new SemanticNetworkConcept(OWLOntologyTestDataProvider.DEFAULT_ID, conceptName,
		type, new ArrayList<>(), new ArrayList<>());
    }

    protected static Collection<OWLRelation> getTestRelations(String entity1, String entity2, String entity3) {

	OWLConcept owlConcept1 = new SemanticNetworkConcept(1L, entity1, OBJECT_TYPE, new ArrayList<>(), new ArrayList<>());
	OWLConcept owlConcept2 = new SemanticNetworkConcept(2L, entity2, OBJECT_TYPE, new ArrayList<>(), new ArrayList<>());
	OWLConcept owlConcept3 = new SemanticNetworkConcept(3L, entity3, OBJECT_TYPE, new ArrayList<>(), new ArrayList<>());

	return Arrays.asList(
		new SemanticNetworkRelation("TEST_RELATION_1", owlConcept1, owlConcept2, SemanticNetworkRelationType.SYNONYM),
		new SemanticNetworkRelation("TEST_RELATION_2", owlConcept1, owlConcept2, SemanticNetworkRelationType.ANTONYM),
		new SemanticNetworkRelation("TEST_RELATION_3", owlConcept3, owlConcept1, SemanticNetworkRelationType.HYPONYM));
    }

    protected static OWLOntology getTestOntology(AbstractOWLOntologyFactory factory)
	    throws IOException, OWLOntologyStorageException, OWLOntologyCreationException {

	File tmpFile = File.createTempFile(TEMP_DATA_PREFIX, TEMP_DATA_SUFFIX);
	IRI tmpFileIRI = IRI.create(tmpFile);

	factory.saveOntology(tmpFileIRI);
	return AbstractOWLOntologyFactory.loadOntology(tmpFileIRI);
    }

}
