/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.kimanufaktur.nsm.graph.entities.nodes;

import de.kimanufaktur.nsm.graph.entities.links.WeightedLink;
import de.kimanufaktur.nsm.graph.entities.marker.StringDoubleMarkerWithOrigin;
import de.kimanufaktur.markerpassing.Link;
import de.kimanufaktur.markerpassing.Marker;
import de.kimanufaktur.markerpassing.Node;
import de.kimanufaktur.markerpassing.SpreadingStep;

import java.util.*;

public class StringDoubleNodeWithMultipleThresholds implements Node {
    public List<Marker> activationHistory = null;
    Collection<Link> links = null;
    Collection<Marker> markers = null;
    Map<String, Double> threshold = null;
    String name = null;

    //------------
    //Elements for the distance measure
    Map<String, Map<Double, List<String>>> activation = null;

    public StringDoubleNodeWithMultipleThresholds(String name) {
        this.links = new ArrayList<>();
        this.markers = new ArrayList<>();
        this.activation = new HashMap<>();
        this.threshold = new HashMap<>();
        this.activationHistory = new ArrayList<>();
        this.setName(name);
    }
    public StringDoubleNodeWithMultipleThresholds() {
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

    public Map<String, Map<Double, List<String>>> getActivation() {
        return activation;
    }

    public void setActivation(Map<String, Map<Double, List<String>>> activation) {
        this.activation = activation;
    }

    public Map<Double, List<String>> getActivation(String originToCheck) {
        return activation.get(originToCheck);
    }

    public void addActivation(String origin, double activation, List<String> visitedStrings) {
        Map<Double, List<String>> activation4String = this.activation.get(origin);
        if (null == activation4String) {
            Map<Double, List<String>> newActivationMap = new HashMap<Double, List<String>>();
            newActivationMap.put(activation,visitedStrings);
            this.activation.put(origin, newActivationMap);
        } else {
            Map<Double, List<String>> currentActivationMap = this.activation.get(origin);
            Map.Entry<Double, List<String>> entry = currentActivationMap.entrySet().iterator().next();
            Double currentActivation = entry.getKey();
            Double newActivation = currentActivation+activation;
            currentActivationMap.remove(entry.getKey());
            currentActivationMap.put(newActivation,entry.getValue());
            this.activation.put(origin, currentActivationMap);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, Double> getThreshold() {
        return threshold;
    }

    public void setThreshold(Map<String, Double> threshold) {
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
    public boolean checkThresholds(Object originString) {
        //collect all activation from the not yet processed markers.
        List<Marker> marker2remove = new ArrayList<>();
        for (Marker marker : this.getMarkers()) {
            //TODO: we do not check for sprecial markers  yet.
            String origin = ((StringDoubleMarkerWithOrigin) marker).getOrigin();
            Double activation = ((StringDoubleMarkerWithOrigin) marker).getActivation();
            List<String> visitedStrings = ((StringDoubleMarkerWithOrigin) marker).getVisitedStrings();
            addActivation(origin, activation, visitedStrings);
            activationHistory.add(marker);
            marker2remove.add(marker);
        }

        //we remove the markers we have allready looked at.
        this.getMarkers().removeAll(marker2remove);
        //check threshold for each origin marker separately
        for (String activeStrings : this.activation.keySet()) {
            Map<Double, List<String>> activeStringActivationMap = this.activation.get(activeStrings);
            Map.Entry<Double, List<String>> entry = activeStringActivationMap.entrySet().iterator().next();
            Double currentActivation = entry.getKey();
            if (currentActivation >= this.getThreshold().get(activeStrings)) {
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
//    public boolean checkThresholds(Object originString) {
//
//        List<Marker> marker2remove = new ArrayList<>();
//        for (Marker marker : this.getMarkers()) {
//            // if (((StringDoubleMarkerWithOrigin) marker).getOrigin().equals(originString)) { TODO: we do not sheck for sprecial markers  yet.
//            addActivation(((StringDoubleMarkerWithOrigin) marker).getOrigin(), ((StringDoubleMarkerWithOrigin) marker).getActivation());
//            marker2remove.add(marker);
//            // }
//
//        }
//        //we remove the markers we have allready looked at.
//        this.getMarkers().removeAll(marker2remove);
//        if(this.activation.keySet().size()>1){ //if this node have been activated from multiple origins
//           for(String activeStrings : this.activation.keySet()){
//                this.activationHistory.put(activeStrings, this.activation.get(activeStrings));
//           }
//        }
//
//        for (String activeStrings : this.activation.keySet()) {
//            if (this.activation.get(activeStrings) >= this.getThreshold().get(activeStrings)) {
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
//        if (this.activation.keySet().size() > 1) { //if this node have been activated from multiple origins
            //TODO: do something if two activation meet. For now we do nothing... thus the node does not continue firing. We should end up with all activation being in such nodes.
//            double doubleactivation = 0.0;
//            double sumOfThresholds = 0.0;
//            for (String origin : this.activation.keySet()) {
//                doubleactivation += this.activation.get(origin);
//                sumOfThresholds += this.getThreshold().get(origin);
//            }
//            //if (doubleactivation >= sumOfThresholds) { //We do not have a threshhold. If this is a double activation, we activate with a spark.
//            String sparkString = new String(this.getLitheral());
//            StringDoubleMarkerWithOrigin sparkMarker = new StringDoubleMarkerWithOrigin();
//            sparkMarker.setOrigin(sparkString);
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
//        } else {//this node is activated by only one origin
            for (String origin : this.activation.keySet()) {
                Map<Double, List<String>> nodeActivationMap = this.activation.get(origin);
                Map.Entry<Double, List<String>> entry = nodeActivationMap.entrySet().iterator().next();
                Double currentActivation = entry.getKey();
                List<String> currentVisitedStrings = entry.getValue();
                if (currentActivation >= this.getThreshold().get(origin)) {
                    double activation2Pass = currentActivation / getLinks().size();
                    for (Link link : getLinks()) {
                        if (link instanceof WeightedLink) {
                            SpreadingStep spreadingStep = new SpreadingStep();
                            spreadingStep.setLink(link);
                            spreadingStep.setMarkings(getMarkersForLink(link, activation2Pass, origin, currentVisitedStrings));
                            spreadingStep.setInDirection(true);
                            spreadingSteps.add(spreadingStep);
                        }
                    }
                }
            }
//        }
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
     public Collection<Marker> getMarkersForLink(Link link, double activation2Pass, String origin, List<String> visitedStrings) {
        Collection<Marker> markers = new ArrayList<>();
        StringDoubleMarkerWithOrigin marker4link = new StringDoubleMarkerWithOrigin();
        //calculate different activation depending on link type
        if (link instanceof WeightedLink) {
            marker4link.setOrigin(origin);
            marker4link.setActivation(((WeightedLink) link).getWeight() * activation2Pass); //Send the double of the normal activation to synonyms
            marker4link.setVisitedStrings(visitedStrings);
            markers.add(marker4link);
        } else {
            marker4link.setOrigin(origin);
            marker4link.setActivation(activation2Pass); //Send the double of the normal activation to synonyms
            marker4link.setVisitedStrings(visitedStrings);
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
         Map<Double, List<String>> oldActivationMap = activation.get(origin);
         Map.Entry<Double, List<String>> entry = oldActivationMap.entrySet().iterator().next();
         Double currentActivation = entry.getKey();
         List<String> currentVisitedStrings = entry.getValue();
         Map<Double, List<String>> newActivationMap = new HashMap<Double, List<String>>();
         newActivationMap.put(currentActivation - activation2Pass, currentVisitedStrings);
        setActivation(origin, newActivationMap); //TODO: this could be done in an post processing set.

        return markers;
    }

    public void setActivation(String origin, Map<Double, List<String>> activationMap) {
        this.activation.put(origin, activationMap);
    }

    public double getDoubleActivation() {
        double result = 0;
        Set<String> sources = new HashSet<>();
        for (Marker m : this.activationHistory) {
            if (m instanceof StringDoubleMarkerWithOrigin) {
                StringDoubleMarkerWithOrigin markerWithOrigin = (StringDoubleMarkerWithOrigin) m;
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
