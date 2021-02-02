/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com> and Lars Borchert <lars.borchert@gmail.com>,  2011
 */

package ontology.index.document.factory.owlapi.v5;

import ontology.index.indexer.core.Fields;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.semanticweb.owlapi.model.*;

/*
 *  Create by borchert on 23.07.18
 *
 * Factory for constructing various lucene index documents based on owl entity types.
 *
 */
public class DocumentFactory {

    //private OWLOntologyManager _manager;


    public Document createOWLClassDoc(OWLClass aClass){
        Document doc = createBaseDoc(aClass.getIRI().toString(), aClass.getIRI().getFragment());
        doc.add(new TextField(Fields.ENTITY_TYPE, "OWLClass", Field.Store.YES));
        return doc;
    }

    public Document createOWLObjectPropertyDoc(OWLObjectProperty objp){
        Document doc = createBaseDoc(objp.getIRI().toString(), objp.getIRI().getFragment());
        doc.add(new TextField(Fields.ENTITY_TYPE, "OWLObjectProperty", Field.Store.YES));
        return doc;
    }

    public Document createOWLDataProperty(OWLDataProperty dp){
        Document doc = createBaseDoc(dp.getIRI().toString(), dp.getIRI().getFragment());
        doc.add(new TextField(Fields.ENTITY_TYPE, "OWLDataProperty", Field.Store.YES));
        return doc;
    }

    public Document createOWLNamedIndividualDoc(OWLNamedIndividual ind){
        Document doc = createBaseDoc(ind.getIRI().toString(), ind.getIRI().getFragment());
        doc.add(new TextField(Fields.ENTITY_TYPE, "OWLNamedIndividual", Field.Store.YES));
        return doc;
    }

    public Document createOWLObjectPropertyAssertionDoc(OWLObjectPropertyAssertionAxiom objpa) {
        return createOWLObjectPropertyAssertionDocument(objpa, "OWLObjectPropertyAssertion");
    }

    public Document createOWLNegativeObjectPropertyAssertionDoc(OWLNegativeObjectPropertyAssertionAxiom negObjpa) {
        return createOWLObjectPropertyAssertionDocument(negObjpa, "OWLNegativeObjectPropertyAssertion");
    }

    public Document createOWLDataPropertyAssertionDoc(OWLDataPropertyAssertionAxiom dpa){
        return createOWLDataPropertyAssertionDocument(dpa, "OWLDataPropertyAssertion");
    }

    public Document createOWLNegativeDataPropertyAssertionDoc(OWLNegativeDataPropertyAssertionAxiom negDpa){
        return createOWLDataPropertyAssertionDocument(negDpa, "OWLNegativeDataPropertyAssertion");
    }

    public Document createOWLAnnotationDoc(OWLAnnotation a){
        Document doc = createBaseDoc(a.getProperty().getIRI().toString(), a.getProperty().getIRI().getFragment());
        doc.add(new TextField(Fields.ENTITY_TYPE, "OWLAnnotation", Field.Store.YES));

        OWLAnnotationValue val = a.getValue();
        if(val != null){
            if (val instanceof OWLLiteral) {
                doc.add(new TextField(Fields.ANNOTATION_VALUE, ((OWLLiteral) val).getLiteral(), Field.Store.YES));
            } else {
                doc.add(new StringField(Fields.ANNOTATION_VALUE, val.toString(), Field.Store.YES ));
            }
        }
        return doc;
    }

    public Document createOWLAnnotationAssertionDoc(OWLAnnotationAssertionAxiom aa){
        OWLAnnotationValue val = aa.getValue();
        if(val != null){
            return createOWLAnnotationDoc(aa.getAnnotation());
        }
        //TODO: support only literals currently
        return null;
    }


    /*
     #
     #      Helper methods
     #
     */

    /**
     * Helper method for creating a document containing values shared over all document types.
     * @param entityName
     * @param iri
     * @return
     */
    private Document createBaseDoc(String iri, String entityName){
        Document doc = new Document();
        if(iri == null || entityName == null ||
        iri.length() == 0 || entityName == null){
            System.err.println("RDFXMLCrawler Doc Factory: IRI or EntityName null! ->  "  + iri + "#" + entityName);
            return doc;
        }
        //string fields are "atomic" ; their content is not analyzed by the registered analyzer, but stored anyway.
        doc.add(new StringField(Fields.IRI, iri, Field.Store.YES));
        doc.add(new TextField(Fields.ENTITY_NAME, entityName, Field.Store.YES));

        return doc;
    }

    /**
     * Common code for creating positive and negative OWLObjectProperty assertion index documents
     * @param obja
     * @param entityTypeFieldValue
     * @return
     */
    private Document createOWLObjectPropertyAssertionDocument(OWLPropertyAssertionAxiom<OWLObjectPropertyExpression, OWLIndividual> obja, String entityTypeFieldValue ){
        Document doc = null;
        IRI iri = obja.getProperty().getNamedProperty().getIRI();
        doc = createBaseDoc(iri.toString(), iri.getFragment());
        doc.add(new TextField(Fields.ENTITY_TYPE, entityTypeFieldValue, Field.Store.YES));
        OWLIndividual subjInd = obja.getSubject();
        OWLIndividual objInd = obja.getObject();
        if(subjInd.isNamed()){
            IRI indIRI = ((OWLNamedIndividual)subjInd).getIRI();
            doc.add(new TextField(Fields.PROPERTY_DOMAIN, indIRI.getFragment(), Field.Store.YES));
        }
        if(objInd.isNamed()){
            IRI indIRI = ((OWLNamedIndividual)objInd).getIRI();
            doc.add(new TextField(Fields.PROPERTY_RANGE, indIRI.getFragment(), Field.Store.YES));
        }

        return doc;
    }

    /**
     * Common code for creating positive and negative OWLDataProperty assertion index documents
     * @param dpa
     * @param entityTypeFieldValue
     * @return
     */
    private Document createOWLDataPropertyAssertionDocument(OWLPropertyAssertionAxiom<OWLDataPropertyExpression, OWLLiteral> dpa,String entityTypeFieldValue){
        Document doc = null;
        IRI iri = dpa.getProperty().asOWLDataProperty().getIRI();
        doc = createBaseDoc(iri.toString(), iri.getFragment());
        doc.add(new TextField(Fields.ENTITY_TYPE, entityTypeFieldValue, Field.Store.YES));
        OWLIndividual subjInd = dpa.getSubject();
        OWLLiteral value = dpa.getObject();

        if(subjInd.isNamed()){
            IRI indIRI = ((OWLNamedIndividual)subjInd).getIRI();
            doc.add(new TextField(Fields.PROPERTY_DOMAIN, indIRI.getFragment(), Field.Store.YES));
            doc.add(new TextField(Fields.PROPERTY_RANGE, value.getLiteral(), Field.Store.YES));

        }

        return doc;
    }


}
