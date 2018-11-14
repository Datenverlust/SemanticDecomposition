/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.dailab.nsm.decomposition.owlConverter.util;

/**
 * Provides common OWL namespaces and some internal conventions for this
 * OWLConverter
 *
 */
public enum OWLNamespace {

    OWL_NAMESPACE_PREFIX("http://www.dailab.de/ontologies/"),
    OWL_ONTOLOGY_NAMESPACE(OWL_NAMESPACE_PREFIX + "ontology#"),
    OWL_CLASS_NAMESPACE(OWL_NAMESPACE_PREFIX + "class#"),
    OWL_RELATION_NAMESPACE(OWL_NAMESPACE_PREFIX + "relation#"),
    OWL_INDIVIDUAL_NAMESPACE(OWL_NAMESPACE_PREFIX + "individual#");

    private final String name;

    OWLNamespace(String name) {
	this.name = name;
    }

    @Override
    public String toString() {
	return this.name;
    }

    public static String removeIllegalNamespaceCharacters(String entity) {
	return entity.replaceAll("\\s+", "_").replaceAll("[^A-Za-z0-9]", "");
    }

    public static String removeNullNames(String entity) {
	return entity.equals("null") ? entity.replaceAll("null", "") : entity;
    }

}
