/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.kimanufaktur.nsm.decomposition.nlp;

import java.util.List;

/**
 * Class for creating word lemmas.
 * Uses LanguageTools German lemmatisation.
 * lemmatisations? If so, how to integrate with coreNLP or others?
 */
public interface Lemmatisation {

    List<LemmaToken> lemma(String word);

}
