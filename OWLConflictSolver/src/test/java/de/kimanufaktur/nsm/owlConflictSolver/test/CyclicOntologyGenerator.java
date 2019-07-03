/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.kimanufaktur.nsm.owlConflictSolver.test;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * Generates an ontology with [CYLES_AMOUNT] reflexive class membership (is_a)
 * cycles. Each created owl concept contains only one reflexive is_a relation.
 * 
 * Configure the CYLES_AMOUNT value and run the main() to start.
 * 
 * The resulting ontology will be saved as [FILE_NAME_SAVING_PREFIX] +
 * [CYLES_AMOUNT] + [FILE_NAME_SUFFIX].
 * 
 * You need to prepare the file name prefix a bit (see
 * [FILE_NAME_LOADING_PREFIX]) to load the ontology again because of the
 * building configuraton in the pom.xml.
 * 
 * The concepts will be created as: 
 * Entity0;
 * Entity1 is_a Entity1 && is_a Entity0;
 * Entity2 is_a Entity2 && is_a Entity1;
 * ...
 *
 */
public class CyclicOntologyGenerator {

    private static final long CYLES_AMOUNT = 4000;
    private static final String FILE_NAME_SAVING_PREFIX = "./src/main/resources/owl/testOnt_";
    private static final String FILE_NAME_LOADING_PREFIX = "/owl/testOnt_";
    private static final String FILE_NAME_SUFFIX = "_cyles.owl";
    private static final String LINE_BREAK = "\n";

    protected final String REFLEXIVE_ISA_CONFLICT_GENERATED_ONTOLOGY(long cyles) {
	return getClass().getResource(FILE_NAME_LOADING_PREFIX + cyles + FILE_NAME_SUFFIX).getPath();
    }

    public static void main(String[] args) {
	try {
	    generateCylicOwlontology(CYLES_AMOUNT, FILE_NAME_SAVING_PREFIX + CYLES_AMOUNT + FILE_NAME_SUFFIX);
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    private static void generateCylicOwlontology(long amountCyles, String filePath) throws IOException {
	FileUtils.writeStringToFile(new File(filePath), getCylicOWLOntology(amountCyles));
    }

    private static String getCylicOWLOntology(long amountCyles) {
	return OWL_PREAMBLE + BASIC_OWL_CLASSES + CYLIC_CLASSES_NOTE + generateCyclicOWLClasses(amountCyles) + OWL_END_TAG;
    }

    private final static String OWL_PREAMBLE = "<?xml version=\"1.0\"?><rdf:RDF xmlns=\"http://www.dailab.de/ontologies/ontology#2#\""
	    + LINE_BREAK
	    + "xml:base=\"http://www.dailab.de/ontologies/ontology#2\"" + LINE_BREAK
	    + "xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"" + LINE_BREAK
	    + "xmlns:owl=\"http://www.w3.org/2002/07/owl#\"" + LINE_BREAK
	    + "xmlns:xml=\"http://www.w3.org/XML/1998/namespace\"" + LINE_BREAK
	    + "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\"" + LINE_BREAK
	    + "xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\">" + LINE_BREAK
	    + "<owl:Ontology rdf:about=\"http://www.dailab.de/ontologies/ontology#2\"/>" + LINE_BREAK + LINE_BREAK;

    private final static String BASIC_OWL_CLASSES = "<owl:Class rdf:about=\"http://www.dailab.de/ontologies/class#Vogel0\">"
	    + "</owl:Class> \n\n";

    private final static String CYLIC_CLASSES_NOTE = "<!-- generated cyclic classes -->" + LINE_BREAK + LINE_BREAK;
    private final static String OWL_END_TAG = "</rdf:RDF>";

    private static String generateCyclicOWLClasses(long amount) {
	String result = "";

	for (int i = 1; i <= amount; ++i) {
	    result += "<owl:Class rdf:about=\"http://www.dailab.de/ontologies/class#Vogel" + i + "\">" + LINE_BREAK
		    + "<rdfs:subClassOf rdf:resource=\"http://www.dailab.de/ontologies/class#Vogel" + (i - 1) + "\"/>" + LINE_BREAK
		    + "<rdfs:subClassOf rdf:resource=\"http://www.dailab.de/ontologies/class#Vogel" + i + "\"/>" + LINE_BREAK
		    + "</owl:Class>" + LINE_BREAK + LINE_BREAK;
	}

	return result;
    }

}
