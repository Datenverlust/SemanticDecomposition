/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

import de.kimanufaktur.nsm.decomposition.dictionaries.rdfxmlcrawler.RDFXMLCrawler;
import ontology.index.indexer.core.Fields;
import org.apache.lucene.document.Document;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.List;
import java.util.Set;

/**
 * Created by Johannes Fähndrich on 18.07.18 as part of his dissertation.
 */
public class RDFXMLCrawlerTest {
    RDFXMLCrawler rdfxmlCrawler;

    @Before
   public void test(){
        rdfxmlCrawler = new RDFXMLCrawler("http://dainas.dai-labor.de/~faehndrich@dai/Dictionaries/RDF/om-2.0.rdf", "GER");

        //local file test
        //rdfxmlCrawler = new RDFXMLCrawler("file:/Users/borchert/Repositories/git/audamac/oaf/oaf/ontologymapper/som/target/classes/client_source.owl", "GER");

        //rdfxmlCrawler = new RDFXMLCrawler("http://127.0.0.1/ontologies/exampleOnt-f.owl", "GER");
        File localFile = new File(rdfxmlCrawler.getPath2DBLocation() + File.separator +  rdfxmlCrawler.getDictFileName());
        Assert.assertTrue(localFile.exists());

    }

    @Test
    public void testIndexSimple(){
//        RDFXMLCrawler rdfxmlCrawler = new RDFXMLCrawler("http://dainas.dai-labor.de/~faehndrich@dai/Dictionaries/RDF/om-2.0.rdf", "GER");
//        File localFile = new File(rdfxmlCrawler.getPath2DBLocation() + File.separator +  rdfxmlCrawler.getDictFileName());
//        Assert.assertTrue(localFile.exists());
        long start = System.currentTimeMillis();
        //test index
        rdfxmlCrawler.index();
        System.out.println("Indexing took " + (System.currentTimeMillis() - start) + " ms");
        String query = "milli*"; //search for prefix(milli) on entityName field

        List<Document> results = rdfxmlCrawler.findEntity(query);
        System.out.println(results.size() + " hits for " +query+ " on EntityName.");
        Assert.assertEquals(results.isEmpty(), false);

        String[] fields = new String[]{Fields.ENTITY_NAME, Fields.ENTITY_TYPE};
        query = "OWLClass, Kunde~"; //search for OWLClass OR like(Kunde) in the two fields
        results = rdfxmlCrawler.find(fields, query);
        System.out.println(results.size() + " hits for " +query+ " on " +fields[0]+", " + fields[1] );
        Assert.assertEquals(results.isEmpty(), false);

        System.out.println("Empty Query Testing");
        query = "";
        rdfxmlCrawler.findEntity(query);



    }

    @Test
    public void testCrawler(){

        rdfxmlCrawler.index();

        String query = "SIPrefix"; //search for prefix(milli) on entityName field
        //String query = "Volumen";
        //String query = "SIPrefix";
        List<Document> results = rdfxmlCrawler.findEntity(query);

        Document d = results.get(0);
        String entityIRI = d.get(Fields.IRI);
        Set<String> similar = rdfxmlCrawler.sameAs(entityIRI);

        Set<String> type =  rdfxmlCrawler.typeOf(entityIRI);
        Set<String> properties = rdfxmlCrawler.propertiesFor(entityIRI);

        Set<String> parents = rdfxmlCrawler.parents(entityIRI);
        Set<String> annotations = rdfxmlCrawler.annotationsFor(entityIRI);
        Set<String> children = rdfxmlCrawler.children(entityIRI);


        printSet(type, "Type of " +entityIRI );
        printSet(properties, "Properties of " +entityIRI );
        printSet(similar, entityIRI + " similar to" );
        printSet(parents, "Parents of " +entityIRI );
        printSet(children, "Children of " +entityIRI );
        printSet(annotations, "Annotations for  " +entityIRI );



        System.out.println();
    }

//    @Test
    public void testproductOntology(){
        rdfxmlCrawler.index();

        String query = "http://www.semanticweb.org/borchert/ontologies/2018/6/beispielAkkuSchrauberOntologieFinal#drehmoment"; //search for prefix(milli) on entityName field
        //String query = "Volumen";
        //String query = "SIPrefix";
        List<Document> results = rdfxmlCrawler.findEntity(query);

        Document d = results.get(0);
        String entityIRI = d.get(Fields.IRI);
        Set<String> similar = rdfxmlCrawler.sameAs(entityIRI);

        Set<String> type =  rdfxmlCrawler.typeOf(entityIRI);
        Set<String> properties = rdfxmlCrawler.propertiesFor(entityIRI);

        Set<String> parents = rdfxmlCrawler.parents(entityIRI);
        Set<String> annotations = rdfxmlCrawler.annotationsFor(entityIRI);
        Set<String> children = rdfxmlCrawler.children(entityIRI);


        printSet(type, "Type of " +entityIRI );
        printSet(properties, "Properties of " +entityIRI );
        printSet(similar, entityIRI + " similar to" );
        printSet(parents, "Parents of " +entityIRI );
        printSet(children, "Children of " +entityIRI );
        printSet(annotations, "Annotations for  " +entityIRI );

        System.out.println("IRI: " + entityIRI + " -> " +rdfxmlCrawler.getEntityNameForIRI(entityIRI));



    }

    private void printSet(Set<String> aSet, String title){
        System.out.println(title);
        System.out.println("================");
        for (String s : aSet){
            System.out.println(s);
        }
        System.out.println("----------------");
    }



}
