/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com> and Lars Borchert <lars.borchert@gmail.com>,  2011
 */

package ontology.index.indexer.basic;

import ontology.index.indexer.core.Indexer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.store.Directory;

import java.io.IOException;

/**
 * A basic ontology indexer
 *
 */
public class OntologyIndexer extends Indexer {


    public OntologyIndexer(Directory dir, Analyzer analyzer){
        super(dir, analyzer);
    }

    public void index(Document doc) throws IOException {
        this.writer.addDocument(doc);
    }

    public Directory getIndex(){
        return this.index;
    }

    public Analyzer getAnalyzer(){
        return this.anaylzer;
    }

}
