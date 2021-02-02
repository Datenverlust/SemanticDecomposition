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
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

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

        _path2DBLocation = System.getProperty("user.home") + File.separator + ".decomposition" + File.separator + "RDF";
        String[] urlParts = source.split("/");
        String tmp = urlParts[urlParts.length - 1];
        _dictFileName = "om-2.0.owl";//urlParts[urlParts.length - 1];
        this._source = source;
        _manager = OWLManager.createOWLOntologyManager();
        _language = language;
        init();
    }

    /**
     * Use an Ontology as dicitonary. Use this constructor if you have your ontology
     * is located in-memory.
     * @param ont
     * @param language
     */
    public RDFXMLCrawler(OWLOntology ont, String language){
        _ontology = ont;
        _manager = ont.getOWLOntologyManager();
        _language = language;
        _source = null;
        _path2DBLocation = System.getProperty("user.home") + File.separator + ".decomposition" + File.separator + "RDF";
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

    public String getLanguage(){
        return _language;
    }

    public void init() {
        if(_source != null){
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


        for( OWLClass c : _ontology.getClassesInSignature(true)){
            try {
                _ontIndexer.index(_indexDocFactory.createOWLClassDoc(c));
                indexAnnotationfor(c);
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }

        for(OWLDataProperty dp :  _ontology.getDataPropertiesInSignature(true)){
            try {
                _ontIndexer.index(_indexDocFactory.createOWLDataProperty(dp));
                indexAnnotationfor(dp);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        for(OWLObjectProperty objp: _ontology.getObjectPropertiesInSignature(true)){
            try {
                _ontIndexer.index(_indexDocFactory.createOWLObjectPropertyDoc(objp));
                indexAnnotationfor(objp);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        for(OWLNamedIndividual i: _ontology.getIndividualsInSignature(true)){
            try {
                _ontIndexer.index(_indexDocFactory.createOWLNamedIndividualDoc(i));
                indexAnnotationfor(i);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        _ontIndexer.flush();
        _ontIndexer.closeWriter();

        //init reader
        _indexReader = new Finder(_ontIndexer.getIndex(), _ontIndexer.getAnalyzer());
    }

    private void indexAnnotationfor(OWLEntity entity){

        Set<OWLAnnotationAssertionAxiom> annos =
                _ontology.getAnnotationAssertionAxioms((OWLAnnotationSubject)entity.getIRI());
        for(OWLAnnotationAssertionAxiom a : annos){
            try {
                Document annoDoc = _indexDocFactory.createOWLAnnotationAssertionDoc(a);
                annoDoc.add(new TextField(Fields.ANNOTATED_ENTITY_IRI, entity.getIRI().toString(), Field.Store.YES));
                _ontIndexer.index(annoDoc);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
     * Returns all equivalent entities (sameAs axioms) to the given one or empty set.
     * Supported for properties, classes and individuals.
     * @param entityIRI - dataproperty, objectproperty, class or individual IRI
     * @return  - iris of equivalent entities
     */
    public Set<String> sameAs(String entityIRI){
        IRI entIri = IRI.create(entityIRI);
        Set<String> result = new HashSet<>();

        for (OWLEntity e : _ontology.getEntitiesInSignature(entIri, true)){

            if(e instanceof OWLNamedIndividual){
                e.getReferencingAxioms(_ontology, true).stream()
                    .filter(axiom ->  (axiom instanceof OWLSameIndividualAxiom))
                    .forEach( sameIndAx -> {
                        ((OWLSameIndividualAxiom) sameIndAx).getIndividuals().stream()
                                .filter(i -> i instanceof  OWLNamedIndividual)
                                .map(i -> ((OWLNamedIndividual)i).getIRI().toString())
                                .forEach(result::add);
                    });
            } else if(e instanceof OWLClass){
                e.getReferencingAxioms(_ontology, true).stream()
                        .filter(axiom ->  (axiom instanceof OWLEquivalentClassesAxiom))
                        .forEach( equClassAx -> {
                            equClassAx.getClassesInSignature().stream()
                                .map(c -> c.getIRI().toString())
                                .forEach(result::add);
                });
            } else if(e instanceof OWLObjectProperty){
                e.getReferencingAxioms(_ontology, true).stream()
                        .filter(axiom ->  (axiom instanceof OWLEquivalentObjectPropertiesAxiom))
                        .forEach( equObjPropAx -> {
                            equObjPropAx.getObjectPropertiesInSignature().stream()
                                .map(p -> p.getIRI().toString())
                                .forEach(result::add);
                });
            } else if(e instanceof OWLDataProperty){
                e.getReferencingAxioms(_ontology, true).stream()
                        .filter(axiom ->  (axiom instanceof OWLEquivalentDataPropertiesAxiom))
                        .forEach( equDataPropAx -> {
                            equDataPropAx.getObjectPropertiesInSignature().stream()
                                .map(p -> p.getIRI().toString())
                                .forEach(result::add);
                });
            }

        }
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
        _ontology.getEntitiesInSignature(entIri, true).forEach(e -> {
            if(e instanceof OWLNamedIndividual){
                //dataproperties
                e.getReferencingAxioms(_ontology, true).stream()
                        .filter(axiom ->  (axiom instanceof OWLDataPropertyAssertionAxiom))
                        .map(dpax -> ((OWLDataPropertyAssertionAxiom) dpax).getProperty().asOWLDataProperty().getIRI().toString() )
                        .forEach(result::add);
                //object properties
                e.getReferencingAxioms(_ontology, true).stream()
                        .filter(axiom ->  (axiom instanceof OWLObjectPropertyAssertionAxiom))
                        .map(dpax -> ((OWLObjectPropertyAssertionAxiom) dpax).getProperty().asOWLObjectProperty().getIRI().toString() )
                        .forEach(result::add);

            } else if(e instanceof OWLClass){
                e.getReferencingAxioms(_ontology, true).stream()
                        .filter(ax -> ax instanceof OWLPropertyDomainAxiom)
                        .map(propdomax -> ((OWLPropertyDomainAxiom)propdomax).getProperty())
                        .forEach(prop -> {
                            if(prop instanceof OWLDataProperty){
                                result.add( ((OWLDataProperty)prop).getIRI().toString());
                            }else
                            if(prop instanceof OWLObjectProperty){
                                result.add( ((OWLObjectProperty)prop).getIRI().toString() );
                            }
                        });
            }

            _ontology.getImportsClosure().stream()
                    .map(ontology -> e.getAnnotationAssertionAxioms(ontology))
                    .flatMap(annotationassertionaxioms -> annotationassertionaxioms.stream()) //flatten stream of sets of axioms to stream of axioms
                    .map(a ->  annotation2IRI(a))
                    .forEach(result::add);
        });

        return result;
    }

    /**
     * Retrieves all children for the given entity.
     * TODO: currently imports closure is not used
     * @param entityIRI - either class or a property
     * @return
     */
    public Set<String> children(String entityIRI){
        IRI entIri = IRI.create(entityIRI);
        Set<String> result = new HashSet<>();
        _ontology.getEntitiesInSignature(entIri, true).forEach(e -> {

            if(e instanceof OWLClass){
                //TODO: currently we use only class declaration axioms , i.e. no OBjectHasValue or ObjectAllValuesFrom etc
                _ontology.getSubClassAxiomsForSuperClass((OWLClass)e).stream()
                        .filter(subClAx -> subClAx.getSuperClass().isClassExpressionLiteral())
                        .map(subClAx -> subClAx.getSubClass().asOWLClass().getIRI().toString())
                        .forEach(result::add);
                //also instances of given class entity
                result.addAll(instances(entityIRI));
            } else if(e instanceof OWLObjectProperty ){
                _ontology.getObjectSubPropertyAxiomsForSuperProperty((OWLObjectProperty)e).stream()
                        .map(subpropax -> subpropax.getSubProperty().asOWLObjectProperty().getIRI().toString())
                        .forEach(result::add);
            } else if(e instanceof OWLDataProperty){
                _ontology.getDataSubPropertyAxiomsForSuperProperty((OWLDataProperty)e).stream()
                        .map(subpropax -> subpropax.getSubProperty().asOWLDataProperty().getIRI().toString())
                        .forEach(result::add);
            }

        });
        return result;
    }

    /**
     * Retrieves all parents for given entity.
     * TODO: imports closure not used
     * @param entityIRI - either class, individual or a property
     * @return
     */
    public Set<String> parents(String entityIRI){
        IRI entIri = IRI.create(entityIRI);
        Set<String> result = new HashSet<>();
        _ontology.getEntitiesInSignature(entIri, true).forEach(e -> {

            if(e instanceof OWLNamedIndividual){
                _ontology.getClassAssertionAxioms((OWLNamedIndividual)e).stream()
                        .filter(classAssertionAxiom -> classAssertionAxiom.getClassExpression().isClassExpressionLiteral())
                        .map(classAssertionAxiom -> classAssertionAxiom.getClassExpression().asOWLClass().getIRI().toString())
                        .forEach(result::add);
            } else if(e instanceof OWLClass){
                //TODO: currently we use only class declaration axioms , i.e. no OBjectHasValue or ObjectAllValuesFrom etc
                _ontology.getSubClassAxiomsForSubClass((OWLClass)e).stream()
                        .filter(subClAx ->
                            subClAx.getSuperClass().isClassExpressionLiteral())
                        .map(subclax -> subclax.getSuperClass().asOWLClass().getIRI().toString())
                        .forEach(result::add);
            } else if(e instanceof OWLObjectProperty ){
                _ontology.getObjectSubPropertyAxiomsForSubProperty((OWLObjectProperty)e).stream()
                        .map(subpropax -> subpropax.getSuperProperty().asOWLObjectProperty().getIRI().toString())
                        .forEach(result::add);
            } else if(e instanceof OWLDataProperty){
                _ontology.getDataSubPropertyAxiomsForSubProperty((OWLDataProperty)e).stream()
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
        _ontology.getEntitiesInSignature(entIri, true).forEach(e -> {
            if(e instanceof OWLClass){
                _ontology.getClassAssertionAxioms((OWLClassExpression) e).stream()
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
        _ontology.getEntitiesInSignature(entIri, true).forEach(e -> {
            if(e instanceof OWLNamedIndividual){
                _ontology.getClassAssertionAxioms((OWLNamedIndividual)e).stream()
                        .map(ca -> ca.getClassExpression().asOWLClass().getIRI().toString())
                        .forEach(result::add);
            }
            if(e instanceof OWLClass){
                //simply add this class (type)
                result.add(e.getIRI().toString());
            }
        });
        return result;
    }

    public Set<String> annotationsFor(String entityIRI){
        Set<String> result = new HashSet<>();
        IRI entIRI = IRI.create(entityIRI);
        //assert one entity
        _ontology.getEntitiesInSignature(entIRI, true).forEach(e -> {
            e.getAnnotationAssertionAxioms(_ontology).stream()
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
        if( ax.getValue() != null){
            if( ax.getValue() instanceof OWLLiteral){
                annoIRI = ((OWLLiteral)ax.getValue()).getLiteral();//TODO: check whether literal is a fully qualified IRI
                return annoIRI;
            }
        }
        return ax.getProperty().getIRI().toString();
    }

    public OWLOntology getOntology(){
        return _ontology;
    }

}
