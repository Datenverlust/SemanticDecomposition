/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.dailab.nsm.decomposition.exceptions;

/**
 * Created by faehndrich on 19.01.15.
 */
public class DictionaryDoesNotContainConceptException extends Exception {
    private final static String _MESSAGE = "Dictionary does not contain: ";
    private String concept = null;
    public DictionaryDoesNotContainConceptException(){
         super();
    }
    public DictionaryDoesNotContainConceptException(String concept){
        super("Dictionary does not contain: " + concept);

        this.concept = concept;
    }
    public DictionaryDoesNotContainConceptException(String message, Throwable cause) { super(message, cause); }
    public DictionaryDoesNotContainConceptException(Throwable cause) { super(cause); }
    public String getMessage() {
        return DictionaryDoesNotContainConceptException._MESSAGE + concept;
    }
}
