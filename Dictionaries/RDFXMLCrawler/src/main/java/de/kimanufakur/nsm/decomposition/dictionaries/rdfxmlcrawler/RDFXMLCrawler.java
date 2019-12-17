/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com> and Lars Borchert <lars.borchert@gmail.com>,  2011
 */

package de.kimanufaktur.nsm.decomposition.dictionaries.rdfxmlcrawler;


import de.kimanufaktur.nsm.decompostion.Dictionaries.DictUtil;
import ontology.index.document.factory.owlapi.v5.DocumentFactory;
import ontology.index.indexer.basic.OntologyIndexer;
import ontology.index.indexer.core.Fields;
import ontology.index.indexer.core.Finder;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.de.GermanAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.document.Document;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.search.EntitySearcher;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Johannes Fähndrich on 18.07.18.
 */
public class RDFXMLCrawler {

    private final String _path2DBLocation;
    private final String _source;
    String _dictFileName = "";
    private OWLOntologyManager _manager;
    private OWLOntology _ontology;
    private OntologyIndexer _ontIndexer;
    private DocumentFactory _indexDocFactory;
    private Finder _indexReader;
    private String _language;
    private Set<String> _customStopWords = null;
    /**
     * Create a RDF XML file as an dictionary.
     *
     * @param source this is a URL to the RDF XML file.
     */
    public RDFXMLCrawler(String source, String language) {

        _path2DBLocation = System.getProperty("user.home").toString() + File.separator + ".decomposition" + File.separator + "RDF";
        String[] urlParts = source.split("/");
        _dictFileName = urlParts[urlParts.length - 1];
        this._source = source;
        _manager = OWLManager.createOWLOntologyManager();
        _language = language;
        init();
    }

    public String getPath2DBLocation() {
        return _path2DBLocation;
    }

    public String getSource() {
        return _source;
    }

    public String getDictFileName() {
        return _dictFileName;
    }


    public void init() {
        File dict = new File(_path2DBLocation + File.separator + _dictFileName);
        if (!dict.exists()) {
            //save a local copy
            DictUtil.downloadFileParalell(_source, _path2DBLocation + File.separator + _dictFileName);
        }
        //load
        try {
            System.out.println("RDFCrawler attempts to load  " +dict.toString());
            _ontology = _manager.loadOntologyFromOntologyDocument(dict);
            System.out.println("RDFCrawler successfully loaded  " +dict.toString());
            //index();
        } catch (OWLOntologyCreationException e) {
            //e.printStackTrace();
            System.err.println(e.getMessage());
        }
    }

    public void index(Set<String> customStopWords){
        _customStopWords = customStopWords;
        index();
    }

    public void index(){
        if(_ontIndexer == null){
            //TODO: use shared config and fixed identifiers for detrmining language
            //values currently taken from de.kimanufaktur.nsm.decomposition.settings.Config

            Analyzer indexAnalyzer = null;
            CharArraySet customStopWords = null;
            if(_customStopWords != null){
                customStopWords = new CharArraySet(_customStopWords, true);
            }

            if(_language.equals("GER") ){
                if(customStopWords != null){
                    indexAnalyzer = new GermanAnalyzer(customStopWords);
                } else {
                    indexAnalyzer = new GermanAnalyzer();
                }

            } else{

                if(customStopWords != null){
                    indexAnalyzer = new StandardAnalyzer(customStopWords);
                } else {
                    indexAnalyzer = new StandardAnalyzer();
                }

            }
            //TODO: make this configurable to choose between in memory and persistent index storage
            Directory dir = new RAMDirectory();

            _ontIndexer = new OntologyIndexer(dir, indexAnalyzer);
            _indexDocFactory = new DocumentFactory();
        }

        _ontology.classesInSignature(Imports.INCLUDED).forEach(
                c -> {
                    try{
                        _ontIndexer.index(_indexDocFactory.createOWLClassDoc(c));
                    } catch(IOException e){
                        System.err.println(e.getMessage());
                    }
                });

        _ontology.dataPropertiesInSignature(Imports.INCLUDED).forEach(
                dp -> {
                    try{
                        _ontIndexer.index(_indexDocFactory.createOWLDataProperty(dp));
                    } catch(IOException e){
                        System.err.println(e.getMessage());
                    }
                });
        _ontology.objectPropertiesInSignature(Imports.INCLUDED).forEach(
                objp -> {
                    try{
                        _ontIndexer.index(_indexDocFactory.createOWLObjectPropertyDoc(objp));
                    } catch(IOException e){
                        System.err.println(e.getMessage());
                    }
                });
        _ontology.individualsInSignature(Imports.INCLUDED).forEach(
                namedInd -> {
                    try{
                        _ontIndexer.index(_indexDocFactory.createOWLNamedIndividualDoc(namedInd));
                    } catch(IOException e){
                        System.err.println(e.getMessage());
                    }
                });



        //TODO; index assertion axioms as well

        _ontIndexer.flush();
        _ontIndexer.closeWriter();

        //init reader
        _indexReader = new Finder(_ontIndexer.getIndex(), _ontIndexer.getAnalyzer());
    }


    /**
     * Retuns a list of Documents pointing to various entities from the ontology with the given name
     * @param entity
     * @return
     */
    public List<Document> findEntity(String entity){
        String[] fieldsToSearch = new String[]{Fields.ENTITY_NAME};
        return _indexReader.find(fieldsToSearch, entity);
    }

    /**
     * Search on the specified fields for the given query.
     * For valid field keys
     * @see ontology.index.indexer.core.Fields
     * @param fields
     * @param query
     * @return
     */
    public List<Document> find(String[] fields, String query){
        return _indexReader.find(fields, query);
    }

    /**
     * Retrieves IRI entries for all found documents for the given query
     * @param fields    - Fields to search on
     * @param query     - query
     * @return          - ordered list of results. first entry is best match
     */
    public List<String> findIRIs(String[] fields, String query){
        List<String> result = new ArrayList();
        List<Document> docs = find(fields, query);
        if(docs != null && docs.size() > 0){
            result = new ArrayList(docs.size());
            for(Document d : docs){
                result.add(d.get(Fields.IRI));
            }
        }
        return result;
    }

    /**
     * Returns the entity Name for given IRI.
     * @param iri - an indexed iri
     * @return  - entity name i.e. the fragment of the IRI
     */
    public String getEntityNameForIRI(String iri){
        return IRI.create(iri).getFragment();
    }

    /**
     * Retuns a list of IRIs pointing to various entities from the ontology with the given name
     * @param entity    - query
     * @return  - ordered list of IRIs identifying entities in the ontology. First element is best match.
     */
    public List<String> findIRIs(String entity){
        String[] fieldsToSearch = new String[]{Fields.ENTITY_NAME};
        return findIRIs(fieldsToSearch, entity);
    }


    /**
     * Returns all equivalent entities to the given one or empty set.
     * Supported for properties, classes and individuals.
     * @param entityIRI - dataproperty, objectproperty, class or individual IRI
     * @return  - iris of equivalent entities
     */
    public Set<String> sameAs(String entityIRI){
        IRI entIri = IRI.create(entityIRI);
        Set<String> result = new HashSet<>();
        _ontology.entitiesInSignature(entIri, Imports.INCLUDED).forEach(e -> {

            if(e instanceof OWLNamedIndividual){
                _ontology.sameIndividualAxioms((OWLNamedIndividual)e).forEach( sameIndAx ->
                {
                    sameIndAx.individuals().filter(i -> i instanceof  OWLNamedIndividual)
                                            .map(i -> ((OWLNamedIndividual)i).getIRI().toString())
                                            .forEach(result::add);
                });
            } else if(e instanceof OWLClass){
                _ontology.equivalentClassesAxioms((OWLClass)e).forEach( equClassAx ->
                {
                    equClassAx.classesInSignature().map(c -> c.getIRI().toString())
                            .forEach(result::add);
                });
            } else if(e instanceof OWLObjectProperty){
                _ontology.equivalentObjectPropertiesAxioms((OWLObjectProperty)e).forEach( equObjPropAx ->
                {
                    equObjPropAx.objectPropertiesInSignature().map(p -> p.getIRI().toString())
                            .forEach(result::add);
                });
            } else if(e instanceof OWLDataProperty){
                _ontology.equivalentDataPropertiesAxioms((OWLDataProperty)e).forEach( equDataPropAx ->
                {
                    equDataPropAx.objectPropertiesInSignature().map(p -> p.getIRI().toString())
                            .forEach(result::add);
                });
            }

        });
        return result;
    }

    /**
     * Retrieves IRIs of all properties under the domain of entityIRI.
     * The result is empty if entity is not a class or indidivual and if no properties
     * exist for the given class or individual.
     * @param entityIRI - a class or individual IRI
     * @return
     */
    public Set<String> propertiesFor(String entityIRI){
        IRI entIri = IRI.create(entityIRI);
        Set<String> result = new HashSet<>();
        _ontology.entitiesInSignature(entIri, Imports.INCLUDED).forEach(e -> {
            if(e instanceof OWLNamedIndividual){
                _ontology.dataPropertyAssertionAxioms((OWLNamedIndividual)e)
                        .map(dpax ->
                             dpax.getProperty().asOWLDataProperty().getIRI().toString() )
                        .forEach(result::add);
                _ontology.objectPropertyAssertionAxioms((OWLNamedIndividual)e)
                        .map(objpax ->
                                objpax.getProperty().asOWLObjectProperty().getIRI().toString() )
                        .forEach(result::add);

            } else if(e instanceof OWLClass){
                //TODO: check whether this works as intentioned
                e.dataPropertiesInSignature().map(d -> d.getIRI().toString()).forEach(result::add);
                e.objectPropertiesInSignature().map(o -> o.getIRI().toString()).forEach(result::add);
            }

            //e.annotationPropertiesInSignature().map(a->a.getIRI().toString()).forEach(result::add);
            //result.addAll( annotationsFor(entityIRI));
            EntitySearcher.getAnnotationAssertionAxioms(e, _ontology)
                    .map(a -> annotation2IRI(a))
                    .forEach(result::add);
        });

        return result;
    }

    /**
     * Retrieves all children for the given entity.
     * TODO: also instances of classes and properties?
     * @param entityIRI - either class, individual or a property
     * @return
     */
    public Set<String> children(String entityIRI){
        IRI entIri = IRI.create(entityIRI);
        Set<String> result = new HashSet<>();
        _ontology.entitiesInSignature(entIri, Imports.INCLUDED).forEach(e -> {
            e.getEntityType().getIRI();
            if(e instanceof OWLClass){
                //TODO: currently we use only class declaration axioms , i.e. no OBjectHasValue or ObjectAllValuesFrom etc
                _ontology.subClassAxiomsForSuperClass((OWLClass)e)
                        .filter(subClAx -> subClAx.getSuperClass().isClassExpressionLiteral())
                        .map(subClAx -> subClAx.getSubClass().asOWLClass().getIRI().toString())
                        .forEach(result::add);
                //TODO: also instances?
                result.addAll(instances(entityIRI));
            } else if(e instanceof OWLObjectProperty ){
                _ontology.objectSubPropertyAxiomsForSuperProperty((OWLObjectProperty)e)
                        .map(subpropax -> subpropax.getSubProperty().asOWLObjectProperty().getIRI().toString())
                        .forEach(result::add);
            } else if(e instanceof OWLDataProperty){
                _ontology.dataSubPropertyAxiomsForSuperProperty((OWLDataProperty)e)
                        .map(subpropax -> subpropax.getSubProperty().asOWLDataProperty().getIRI().toString())
                        .forEach(result::add);
            }

        });
        return result;
    }

    /**
     * Retrieves all parents for given entity.
     * @param entityIRI - either class, individual or a property
     * @return
     */
    public Set<String> parents(String entityIRI){
        IRI entIri = IRI.create(entityIRI);
        Set<String> result = new HashSet<>();
        _ontology.entitiesInSignature(entIri, Imports.INCLUDED).forEach(e -> {
            e.getEntityType().getIRI();
            if(e instanceof OWLNamedIndividual){
                _ontology.classAssertionAxioms((OWLNamedIndividual)e)
                        .filter(classAssertionAxiom -> classAssertionAxiom.getClassExpression().isClassExpressionLiteral())
                        .map(classAssertionAxiom -> classAssertionAxiom.getClassExpression().asOWLClass().getIRI().toString())
                        .forEach(result::add);
            } else if(e instanceof OWLClass){
                //TODO: currently we use only class declaration axioms , i.e. no OBjectHasValue or ObjectAllValuesFrom etc
                _ontology.subClassAxiomsForSubClass((OWLClass)e)
                        .filter(subClAx ->
                            subClAx.getSuperClass().isClassExpressionLiteral())
                        .map(subclax -> subclax.getSuperClass().asOWLClass().getIRI().toString())
                        .forEach(result::add);
            } else if(e instanceof OWLObjectProperty ){
                _ontology.objectSubPropertyAxiomsForSubProperty((OWLObjectProperty)e)
                        .map(subpropax -> subpropax.getSuperProperty().asOWLObjectProperty().getIRI().toString())
                        .forEach(result::add);
            } else if(e instanceof OWLDataProperty){
                _ontology.dataSubPropertyAxiomsForSubProperty((OWLDataProperty)e)
                        .map(subpropax -> subpropax.getSuperProperty().asOWLDataProperty().getIRI().toString())
                        .forEach(result::add);
            }

        });
        return result;
    }

    /**
     * Retrieves all instances of the given entity
     * @param entityIRI - class
     * @return
     */
    public Set<String> instances(String entityIRI){
        IRI entIri = IRI.create(entityIRI);
        Set<String> result = new HashSet<>();
        _ontology.entitiesInSignature(entIri, Imports.INCLUDED).forEach(e -> {
            if(e instanceof OWLClass){
                _ontology.classAssertionAxioms((OWLClassExpression) e)
                        .map(classAssertionAxiom -> classAssertionAxiom.getIndividual())
                        .filter(ind -> ind.isNamed())
                        .map(namedInd -> namedInd.asOWLNamedIndividual().getIRI().toString())
                        .forEach(result::add);
            }
//            if(e instanceof OWLClass) {
//                ((OWLClass) e).individualsInSignature().filter(i -> i instanceof OWLNamedIndividual)
//                        .map(i -> ((OWLNamedIndividual) i).getIRI().toString())
//                        .forEach(result::add);
//            }

            //property instances have no unique name
//            } else if(e instanceof OWLObjectProperty ){
//                EntitySearcher.getDomains(((OWLObjectProperty)e).asObjectPropertyExpression(), _ontology.importsClosure())
//                        .map(ce -> ce.asOWLClass().individualsInSignature() )
//                        .filter(i -> i instanceof OWLNamedIndividual)
//                        .flatMap(i -> _ontology.objectPropertyAssertionAxioms((OWLNamedIndividual)i))
//                        .filter(propAx -> propAx.getProperty().equals(((OWLObjectProperty) e).asObjectPropertyExpression()))
//                        .map(propAx -> propAx.getProperty());
//            } else if(e instanceof OWLDataProperty){
//               //TODO:
//            }

        });
        return result;
    }

    /**
     * Rettrieves the type (class) of thew given instance
     * @param entityIRI
     * @return
     */
    public Set<String> typeOf(String entityIRI){
        IRI entIri = IRI.create(entityIRI);
        Set<String> result = new HashSet<>();
        _ontology.entitiesInSignature(entIri, Imports.INCLUDED).forEach(e -> {
            if(e instanceof OWLNamedIndividual){
                _ontology.classAssertionAxioms((OWLNamedIndividual)e)
                        .map(ca -> ca.getClassExpression().asOWLClass().getIRI().toString())
                        .forEach(result::add);
            }
        });
        return result;
    }

    public Set<String> annotationsFor(String entityIRI){
        Set<String> result = new HashSet<>();
        //assert one entity
        _ontology.entitiesInSignature(IRI.create( entityIRI ), Imports.INCLUDED).forEach(e -> {
            EntitySearcher.getAnnotationAssertionAxioms(e, _ontology)
                    .map(a -> annotation2IRI(a))
                    .forEach(result::add);
        });

        return result;
    }

    /**
     * Helper method for retrieving/constructing an IRI identifying the given
     * annotation
     * @param ax
     * @return
     */
    private String annotation2IRI(OWLAnnotationAssertionAxiom ax){
        String annoIRI = "";
        if( ax.iriValue().isPresent() ){
            annoIRI = ax.iriValue().get().toString();
            return annoIRI;
        }
        return ax.getProperty().getIRI().toString();
    }


}
