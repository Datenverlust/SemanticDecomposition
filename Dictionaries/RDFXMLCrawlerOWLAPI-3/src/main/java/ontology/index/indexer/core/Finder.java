/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com> and Lars Borchert <lars.borchert@gmail.com>,  2011
 */

package ontology.index.indexer.core;


import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by borchert on 17.07.2018.
 * A simple searcher class for the index.
 */
public class Finder {
    //Defines the amount of retrieved documents per query.
    public static final int RESULT_COUNT_DEFAULT = 250;

    //The actual index
    protected Directory index;

    //index searcher class
    protected IndexSearcher searcher;

    //analyzer used for stop words - keywords which are filtered out before indexing and when querying
    protected Analyzer analyzer;

    /**
     * The Fineder needs an index to search on and a stop word analyzer.
     * This should be the same as was used by the indexer.
     * @param index
     * @param analyzer
     */
    public Finder(Directory index, Analyzer analyzer){
        this.index = index;
        this.analyzer = analyzer;
        try {
            IndexReader reader = DirectoryReader.open(index);
            this.searcher = new IndexSearcher(reader);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Convinient constructor. Takes an index to search upon and uses the StandardAnalyzer for stop words.
     * @param index
     */
    public Finder(Directory index){
        this.index = index;
        this.analyzer = new StandardAnalyzer();
        try {
            IndexReader reader = DirectoryReader.open(index);
            this.searcher = new IndexSearcher(reader);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Search the index for content(query) in the given documents fields
     * Finder.RESULT_COUNT_DEFAULT is used to restrict the retrieved documents count.
     * @param fields
     * @param query
     * @return
     */
    public List<Document> find(String[] fields, String query){
        return this.find(fields, query, RESULT_COUNT_DEFAULT);
    }

    /**
     * Search the index for content(query) in the given documents fields.
     * Restrict the amount of retrieved documents to resultCount.
     * @param fields
     * @param query
     * @param resultCount
     * @return
     */
    public List<Document> find(String[] fields, String query, int resultCount){
        List<Document> result = new ArrayList<>();
        try {
            ScoreDoc[] hits = getScoreDocs(fields, query, resultCount);
            if(hits == null) return result;
            for(int i=0;i<hits.length;++i) {
                int docId = hits[i].doc;
                Document d = searcher.doc(docId);
                result.add(d);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Search the index for content(query) in the given documents fields.
     * Restrict the amount of retrieved documents to resultCount.
     *
     * @param fields            document fields to search on
     * @param query             search terms
     * @param resultCount       retrieved amount of result documents
     * @return                  result documents and their respective score.
     */
    public LinkedHashMap<Document, Float> findWithScore(String[] fields, String query, int resultCount){
        LinkedHashMap<Document,Float> result = new LinkedHashMap<>();
        try {
            ScoreDoc[] hits = getScoreDocs(fields, query, resultCount);
            if(hits == null) return result;
            for(int i=0;i<hits.length;++i) {
                int docId = hits[i].doc;
                Document d = searcher.doc(docId);
                result.put(d,hits[i].score);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }



    /**
     * After closing you need to re-instantiate a new Finder
     */
    public void close(){
        try {
            this.searcher.getIndexReader().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates the query parser and triggers the index search.
     * Helper. Shared by find() methods.
     * @param fields
     * @param query
     * @param resultCount
     * @return
     */
    private ScoreDoc[] getScoreDocs(String[] fields, String query, int resultCount){
        //TODO: play around with SpellChecker or other classes for handling typos more robustly
        //in org.apache.lucene.search.spell
        //DirectSpellChecker sc = new DirectSpellChecker();
        ScoreDoc[] result = null;
        if(query == null || query.equals("")) return result;
        try {
            MultiFieldQueryParser parser = new MultiFieldQueryParser(fields, analyzer);
            parser.setAllowLeadingWildcard(true);
            //System.out.println("Query: " + query);
            Query q = parser.parse(parser.escape( query) );
            TopDocs docs = searcher.search(q, resultCount);
            result = docs.scoreDocs;
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //TODO: clarify what this is doing? Or better what it should do.
        if(result != null && result.length > 0){
            try {
                Document d = searcher.doc(result[0].doc);
                //System.out.println( "First Hit: " + (d.getField(Fields.ENTITY_NAME)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;

    }
}
