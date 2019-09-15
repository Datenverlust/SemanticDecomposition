/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.kimanufaktur.nsm.decomposition.graph.entities.nodes;

import de.kimanufaktur.nsm.decomposition.Concept;
import de.kimanufaktur.nsm.decomposition.graph.entities.links.WeightedLink;
import de.kimanufaktur.nsm.decomposition.graph.entities.marker.DoubleMarkerWithOrigin;
import de.kimanufaktur.markerpassing.Link;
import de.kimanufaktur.markerpassing.Marker;
import de.kimanufaktur.markerpassing.Node;
import de.kimanufaktur.markerpassing.SpreadingStep;

import java.nio.channels.ConnectionPendingException;
import java.util.*;

/**
 * Created by faehndrich on 19.05.15.
 */
public class DoubleNodeWithMultipleThresholds implements Node {
    public List<Marker> activationHistory = null;
    Collection<Link> links = null;
    Collection<Marker> markers = null;
    Map<Concept, Double> threshold = null;
    Concept concept = null;

    //------------
    //Elements for the distance measure
    Map<Concept, Map<Double, List<Concept>>> activation = null;

    public DoubleNodeWithMultipleThresholds(Concept concept) {
        this.links = new ArrayList<>();
        this.markers = new ArrayList<>();
        this.activation = new HashMap<>();
        this.threshold = new HashMap<>();
        this.activationHistory = new ArrayList<>();
        this.setConcept(concept);
    }
    public DoubleNodeWithMultipleThresholds() {
        this.links = new ArrayList<>();
        this.markers = new ArrayList<>();
        this.activation = new HashMap<>();
        this.threshold = new HashMap<>();
        this.activationHistory = new ArrayList<>();

    }

    //-----------
    public List<Marker> getActivationHistory() {
        return activationHistory;
    }

    public void setActivationHistory(List<Marker> activationHistory) {
        this.activationHistory = activationHistory;
    }

    public Map<Concept, Map<Double, List<Concept>>> getActivation() {
        return activation;
    }

    public void setActivation(Map<Concept, Map<Double, List<Concept>>> activation) {
        this.activation = activation;
    }

    public Map<Double, List<Concept>> getActivation(Concept originToCheck) {
        return activation.get(originToCheck);
    }

    public void addActivation(Concept origin, double activation, List<Concept> visitedConcepts) {
        Map<Double, List<Concept>> activation4Concept = this.activation.get(origin);
        if (null == activation4Concept) {
            Map<Double,List<Concept>> newActivationMap = new HashMap<Double, List<Concept>>();
            newActivationMap.put(activation,visitedConcepts);
            this.activation.put(origin, newActivationMap);
        } else {
            Map<Double, List<Concept>> currentActivationMap = this.activation.get(origin);
            Map.Entry<Double, List<Concept>> entry = currentActivationMap.entrySet().iterator().next();
            Double currentActivation = entry.getKey();
            Double newActivation = currentActivation+activation;
            currentActivationMap.remove(entry.getKey());
            currentActivationMap.put(newActivation,entry.getValue());
            this.activation.put(origin, currentActivationMap);
        }
    }

    public Concept getConcept() {
        return concept;
    }

    public void setConcept(Concept concept) {
        this.concept = concept;
    }

    public Map<Concept, Double> getThreshold() {
        return threshold;
    }

    public void setThreshold(Map<Concept, Double> threshold) {
        this.threshold = threshold;
    }

    @Override
    public void addLink(Link link) {
        if (link != null) {
            links.add(link);
        }
    }

    @Override
    public void removeLink(Link link) {
        if (link != null && links.contains(link)) {
            links.remove(link);
        }
    }

    @Override
    public Collection<Link> getLinks() {
        return links;
    }

    public void setLinks(Collection<Link> links) {
        this.links = links;
    }

    @Override
    public Collection<Marker> getMarkers() {
        return this.markers;
    }

    public void setMarkers(Collection<Marker> markers) {
        this.markers = markers;
    }

    @Override
    public void addMarker(Marker marker) {
        if (marker != null) {
            markers.add(marker);
        }
    }

    @Override
    public void removeMarker(Marker marker) {
        if (marker != null) {
            markers.remove(marker);
        }
    }

    @Override
    public boolean checkThresholds(Object originConcept) {
        //collect all activation from the not yet processed markers.
        List<Marker> marker2remove = new ArrayList<>();
        for (Marker marker : this.getMarkers()) {
            //TODO: we do not check for sprecial markers  yet.
            Concept origin = ((DoubleMarkerWithOrigin) marker).getOrigin();
            Double activation = ((DoubleMarkerWithOrigin) marker).getActivation();
            List<Concept> visitedConcepts = ((DoubleMarkerWithOrigin) marker).getVisitedConcepts();
            addActivation(origin, activation, visitedConcepts);
            activationHistory.add(marker);
            marker2remove.add(marker);
        }

        //we remove the markers we have allready looked at.
        this.getMarkers().removeAll(marker2remove);
        //check threshold for each origin marker separately
        for (Concept activeConcepts : this.activation.keySet()) {
            Map<Double, List<Concept>> activeConceptActivationMap = this.activation.get(activeConcepts);
            Map.Entry<Double, List<Concept>> entry = activeConceptActivationMap.entrySet().iterator().next();
            Double currentActivation = entry.getKey();
            if (currentActivation >= this.getThreshold().get(activeConcepts)) {
                return true;
            }
        }
        return false;
    }

//    @Override
//    public boolean checkThresholds(Collection<Class<? extends Marker>> collection) {
//        return false;
//    }

//    @Override
//    public boolean checkThresholds(Object originConcept) {
//
//        List<Marker> marker2remove = new ArrayList<>();
//        for (Marker marker : this.getMarkers()) {
//            // if (((DoubleMarkerWithOrigin) marker).getOrigin().equals(originConcept)) { TODO: we do not sheck for sprecial markers  yet.
//            addActivation(((DoubleMarkerWithOrigin) marker).getOrigin(), ((DoubleMarkerWithOrigin) marker).getActivation());
//            marker2remove.add(marker);
//            // }
//
//        }
//        //we remove the markers we have allready looked at.
//        this.getMarkers().removeAll(marker2remove);
//        if(this.activation.keySet().size()>1){ //if this node have been activated from multiple origins
//           for(Concept activeConcepts : this.activation.keySet()){
//                this.activationHistory.put(activeConcepts, this.activation.get(activeConcepts));
//           }
//        }
//
//        for (Concept activeConcepts : this.activation.keySet()) {
//            if (this.activation.get(activeConcepts) >= this.getThreshold().get(activeConcepts)) {
//                return true;
//            }
//        }
//        return false;
//    }

    /**
     * in function of the double node. This is an first glance at the implementation where we do not use weights yet.
     * TODO: adapt to different edge types and include weights.
     *
     * @param stepts the processing setps which are to be included into the node.
     */
    public void in(Collection<SpreadingStep> stepts) {
        for (SpreadingStep step : stepts) {
            if (step.getTargetNode().equals(this)) {
                //the SpreadingStep contains the link the marker has been passed on.
                for (Marker m : step.getMarkings()) {
                    this.getMarkers().add(m);
                }
            }
        }
    }

    /**
     * The out function of the double node. Here the activation is splitt amoun the links of the node.
     * The activation is reduced with each firing link. Thus after the out-function has been acitvated,
     * the activation of the node should be 0.0.
     * The links are weighted equally thus the activation of each link is activation/#links
     *
     * @return the activation step for the next node.
     */
    public Collection<SpreadingStep> out() {
        Collection<SpreadingStep> spreadingSteps = new ArrayList<>();
        if (this.activation.keySet().size() > 1) { //if this node have been activated from multiple origins
            //TODO: do something if two activation meet. For now we do nothing... thus the node does not continue firing. We should end up with all activation being in such nodes.
//            double doubleactivation = 0.0;
//            double sumOfThresholds = 0.0;
//            for (Concept origin : this.activation.keySet()) {
//                doubleactivation += this.activation.get(origin);
//                sumOfThresholds += this.getThreshold().get(origin);
//            }
//            //if (doubleactivation >= sumOfThresholds) { //We do not have a threshhold. If this is a double activation, we activate with a spark.
//            Concept sparkConcept = new Concept(this.getLitheral());
//            DoubleMarkerWithOrigin sparkMarker = new DoubleMarkerWithOrigin();
//            sparkMarker.setOrigin(sparkConcept);
//            for (Link link : getLinks()) {
//                if (link instanceof WeightedLink) {
//                    SpreadingStep spreadingStep = new SpreadingStep();
//                    spreadingStep.setLink(link);
//                    sparkMarker.setActivation(sumOfThresholds * ((WeightedLink) link).getWeight());
//                    spreadingStep.getMarkings().add(sparkMarker);
//                    spreadingStep.setInDirection(true);
//                    spreadingSteps.add(spreadingStep);
//                }
//            }
//            //}
        //We tryed sparking and this yiels worst results.
        } else {//this node is activated by only one origin
            for (Concept origin : this.activation.keySet()) {
                Map<Double, List<Concept>> nodeActivationMap = this.activation.get(origin);
                Map.Entry<Double, List<Concept>> entry = nodeActivationMap.entrySet().iterator().next();
                Double currentActivation = entry.getKey();
                List<Concept> currentVisitedConcepts = entry.getValue();
                if (currentActivation >= this.getThreshold().get(origin)) {
                    double activation2Pass = currentActivation / getLinks().size();
                    for (Link link : getLinks()) {
                        if (link instanceof WeightedLink) {
                            SpreadingStep spreadingStep = new SpreadingStep();
                            spreadingStep.setLink(link);
                            spreadingStep.setMarkings(getMarkersForLink(link, activation2Pass, origin, currentVisitedConcepts));
                            spreadingStep.setInDirection(true);
                            spreadingSteps.add(spreadingStep);
                        }
                    }
                }
            }
        }
        return spreadingSteps;
    }

    /**
     * get Markers for the given link. Here we use the activation with activationlvl/#marker
     * TODO: differentiate the link types.
     * TODO: choose a output weight for the different link types.
     *
     * @param link the link to get the marker for
     * @return list of markers to be bast to the given link.
     */
     public Collection<Marker> getMarkersForLink(Link link, double activation2Pass, Concept origin, List<Concept> visitedConcepts) {
        Collection<Marker> markers = new ArrayList<>();
        DoubleMarkerWithOrigin marker4link = new DoubleMarkerWithOrigin();
        //calculate different activation depending on link type
        if (link instanceof WeightedLink) {
            marker4link.setOrigin(origin);
            marker4link.setActivation(((WeightedLink) link).getWeight() * activation2Pass); //Send the double of the normal activation to synonyms
            marker4link.setVisitedConcepts(visitedConcepts);
            markers.add(marker4link);
        } else {
            marker4link.setOrigin(origin);
            marker4link.setActivation(activation2Pass); //Send the double of the normal activation to synonyms
            marker4link.setVisitedConcepts(visitedConcepts);
            marker4link.setLinkType(link.getClass());
            markers.add(marker4link);
        }

//        if(link instanceof SynonymLink){
//            marker4link.setOrigin(origin);
//            marker4link.setActivation(4.0*activation2Pass); //Send the double of the normal activation to synonyms
//            markers.add(marker4link);
//        }
//        else if(link instanceof HyponymLink){
//            marker4link.setOrigin(origin);
//            marker4link.setActivation(4.0*activation2Pass); //Send the double of the normal activation to synonyms
//            markers.add(marker4link);
//        }
//        else if(link instanceof HypernymLink){
//            marker4link.setOrigin(origin);
//            marker4link.setActivation(4.0*activation2Pass); //Send the double of the normal activation to synonyms
//            markers.add(marker4link);
//        }
//        else if(link instanceof DefinitionLink){
//            marker4link.setOrigin(origin);
//            marker4link.setActivation(0.1*activation2Pass);
//            markers.add(marker4link);
//        }
//        else{
//            marker4link.setOrigin(origin);
//            marker4link.setActivation(activation2Pass);
//            markers.add(marker4link);
//        }


        //Reduce the current activation given to the link.
         Map<Double, List<Concept>> oldActivationMap = activation.get(origin);
         Map.Entry<Double, List<Concept>> entry = oldActivationMap.entrySet().iterator().next();
         Double currentActivation = entry.getKey();
         List<Concept> currentVisitedConcepts = entry.getValue();
         Map<Double, List<Concept>> newActivationMap = new HashMap<Double, List<Concept>>();
         newActivationMap.put(currentActivation - activation2Pass, currentVisitedConcepts);
        setActivation(origin, newActivationMap); //TODO: this could be done in an post processing set.

        return markers;
    }

    public void setActivation(Concept origin, Map<Double, List<Concept>> activationMap) {
        this.activation.put(origin, activationMap);
    }

    public double getDoubleActivation() {
        double result = 0;
        Set<Concept> sources = new HashSet<>();
        for (Marker m : this.activationHistory) {
            if (m instanceof DoubleMarkerWithOrigin) {
                DoubleMarkerWithOrigin markerWithOrigin = (DoubleMarkerWithOrigin) m;
                sources.add(markerWithOrigin.getOrigin());
                result += markerWithOrigin.getActivation();
            }
        }
        if (sources.size() < 2) {
            result = 0;
        }
        return result;
    }
}
