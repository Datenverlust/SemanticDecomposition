/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.kimanufaktur.nsm.decomposition.Dictionaries;

import de.kimanufaktur.nsm.decomposition.Concept;
import de.kimanufaktur.nsm.decomposition.Definition;
import de.kimanufaktur.nsm.decomposition.WordType;
import de.kimanufaktur.nsm.decomposition.exceptions.DictionaryDoesNotContainConceptException;

import java.util.HashSet;
import java.util.List;

public interface Dictionary {
    /**
     * fill the concept with the synonyms out of the dictionary.
     *
     * @param word the concept to be filled. Here the Wordtype and the literal needs to be filled.
     * @return the set of synonyms of concepts. Here no recursion is done, so the synonyms need to be filled again.
     */
      HashSet<Concept> getSynonyms(Concept word);

    /**
     * fill the concept with the antonyms out of the dictionary.
     *
     * @param word the concept to be filled. Here the Wordtype and the literal needs to be filled.
     * @return the set of antonyms of concepts. Here no recursion is done, so the antonyms need to be filled again.
     */
      HashSet<Concept> getAntonyms(Concept word);

    /**
     * fill the concept with the Hypernyms out of the dictionary.
     *
     * @param word the concept to be filled. Here the Wordtype and the literal needs to be filled.
     * @return the set of Hypernyms of concepts. Here no recursion is done, so the Hypernyms need to be filled again.
     */
      HashSet<Concept> getHypernyms(Concept word);

    /**
     * fill the concept with the Hyponyms out of the dictionary.
     *
     * @param word the concept to be filled. Here the Wordtype and the literal needs to be filled.
     * @return the set of Hyponyms of concepts. Here no recursion is done, so the Hyponyms need to be filled again.
     */
      HashSet<Concept> getHyponyms(Concept word);

    /**
     * fill the concept with the Meronyms out of the dictionary. The Meronym relation is ruffly speaking the part-of relation.
     *
     * @param word the concept to be filled. Here the Wordtype and the literal needs to be filled.
     * @return the set of Meronyms of concepts. Here no recursion is done, so the Meronyms need to be filled again.
     */
      HashSet<Concept> getMeronyms(Concept word);


    /**
     * Get possible definitions of the concept form the dictionary. Here a definitions is a list of words. We argue
     * that the grammar of the definitions is at first irrelevant to its decomposition. So we keep the order of concepts
     * in the definitions and decompose each of its concepts.
     *
     * @param word The concept to get the definitions for. Here at least the literal and the Wordtype need to be set beforehand.
     * @return a list of possible definitions from the dictionary.
     */
      List<Definition> getDefinitions(Concept word);

    /**
     * Filling the concepts with all known elements of the dictionary, like synonyms.
     *
     * @param word     the concept to be billed.
     * @param wordType POS of the word to be decomposed
     * @return the same concept but hopefully filled with all that is known about it in the dictionary.
     * @throws DictionaryDoesNotContainConceptException
     */
      Concept fillConcept(Concept word, WordType wordType) throws DictionaryDoesNotContainConceptException;

     Concept getLemma(String word, WordType wordType);

    /**
     * get the concept from the dictionary using the given ID of the dicrionary. Here for example we can get a
     * WordNet Concept by giving the WordNet ID. This is done so that we can find a word with its exact word sense.
     * Different dictionaries will have different IDs. All Ids of a concept are sorted in its concept.ids.
     *
     * @param id the dictionary specific id of a word sense.
     * @return the concept for the given word in the dictionary.
     */
      Concept getConcept(Object id);


    /**
     * get the lemma of a concept.  A lemma is word which stands at the head of a definitions in a dictionary.
     * All the head words in a dictionary are lemmas. Technically, it is "a base word and its inflections".
     * @param word to ge the lemma for. E.g. walking will return to walk
     */
//     Concept getLemma(Concept word);

    /**
     * Get the part of speech of a given concept using Stanford NLP.
     *
     * @param word the concept of which the literal is used to identify the POS.
     * @return the concept with the POS set.
     */
      Concept setPOS(Concept word);


    /**
     * This function fills the definition of a given concept. Here all definitions are added to the concept.definitions
     *
     * @param word the concept to get the definition for.
     * @return the given concept but with a filled definition.
     * @throws DictionaryDoesNotContainConceptException
     */
      Concept fillDefinition(Concept word) throws DictionaryDoesNotContainConceptException;

    /**
     * This function fills the related elements of a concept. Related terms are relative and differ in their
     * definition depending on the used dictionary. Synonyms are excluded here.
     *
     * @param concept the concept to get the realted concepts for.
     * @return the given concept with a filled concept.getRelated property.
     */
      Concept fillRelated(Concept concept) throws DictionaryDoesNotContainConceptException;

    /**
     * Get the lemma of the given concept using Stanford NLP.
     * Here we want a basic form of the concept, which is reduced to the minimum. Is this the stem
     * of the word? How about different word types? Are they considered?
     * <p>
     * //TODO: this will not work for german words because Stanford german models do not contain lemmas for german words
     *
     * @param word concept to get the Lemma vor
     * @return a concept with the lemma filled.
     */
    Concept setLemma(Concept word);

}
