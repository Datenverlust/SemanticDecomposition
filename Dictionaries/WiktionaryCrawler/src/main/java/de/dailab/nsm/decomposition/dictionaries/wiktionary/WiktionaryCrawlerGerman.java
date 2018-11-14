/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.dailab.nsm.decomposition.dictionaries.wiktionary;

import java.io.File;

public class WiktionaryCrawlerGerman extends WiktionaryCrawler {

    //german dictionary
    public WiktionaryCrawlerGerman(){
        path2DBLocation = System.getProperty("user.home").toString() + File.separator + ".decomposition" + File.separator + "wiktionary" + File.separator + "de";
        dictFileName = "dewiktionary-latest-pages-meta-current.xml.bz2";
        source = "http://dainas.dai-labor.de/~faehndrich@dai/Dictionaries/Wiktionary/";
    }
}
