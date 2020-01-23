/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.kimanufaktur.nsm.decomposition.dictionaries.wikidata;

/**
 * TODO: description for WikidataCrawler
 */
public class WikidataCrawler {
    private static WikidataCrawler sharedInstance;

    /**
     * shared instance
     *
     * @return shared instance of WikidataCrawler
     */
    public static WikidataCrawler getSharedInstance() {
        if (sharedInstance == null) {
            sharedInstance = new WikidataCrawler();
        }
        return sharedInstance;
    }

    public WikidataItem findItemByName(String name) {
        return WikidataExtractionProcessor.getSharedInstance().findItemByName(name);
    }

    public WikidataItem findItemById(Long id) {
        return WikidataExtractionProcessor.getSharedInstance().findItemById(id);
    }

    public WikidataItem findItemByWdid(String id) {
        return WikidataExtractionProcessor.getSharedInstance().findItemByWdid(id);
    }
}
