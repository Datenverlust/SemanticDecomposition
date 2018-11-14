/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com> and Lars Borchert <lars.borchert@gmail.com>,  2011
 */
package ontology.index.indexer.core;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;

import java.io.IOException;

/**
 * Created by borchert on 19.07.2018.
 * A simple full text indexer abstract class for wrapping some initialization code.
 */
public abstract class Indexer {

    //the actual index
    protected Directory index;
    // A stop word ananlyzer used for filtering out words which should not get indexed.
    protected Analyzer anaylzer;
    //An index writer. Writes documents to the index
    protected IndexWriter writer;
    //Further configuration. No special configuration is done here.
    protected IndexWriterConfig config;

    /**
     * Constructs and intiliazes an index.
     *
     * @param dir
     * @param analyzer
     */
    public Indexer(Directory dir, Analyzer analyzer) {
        this.anaylzer = analyzer;
        this.index = dir;
        initIndex();
    }

    /**
     * Call this before you change the index.
     */
    public void initIndex() {
        try {
            if (this.writer == null) {
                this.config = new IndexWriterConfig(this.anaylzer);
                this.writer = new IndexWriter(index, config);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Flush all changes and make the visible to readers.
     */
    public void flush() {
        try {
            if (this.writer != null) {
                this.writer.commit();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Closes the index writer
     */
    public void closeWriter() {
        try {
            if (this.writer != null) {
                this.writer.close();
                this.writer = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks whether a index writer is opened or not.
     *
     * @return
     */
    public boolean isWriterClosed() {
        return this.writer == null;
    }

}
