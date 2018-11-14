/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.dailab.nsm.decomposition.graph.entities.nodes;

import de.dailab.nsm.decomposition.Concept;
import de.dailab.nsm.decomposition.graph.entities.links.WeightedLink;

import de.dailab.nsm.decomposition.graph.entities.marker.TypedMarker;
import de.tuberlin.spreadalgo.Link;
import de.tuberlin.spreadalgo.Marker;
import de.tuberlin.spreadalgo.Node;
import de.tuberlin.spreadalgo.SpreadingStep;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by root on 20.12.15.
 */
public class TypedNode implements Node {

    //###########################
    // VARS
    //###########################
    private Collection<Link> links = null;
    //private Collection<Marker> markers = null;
    private double threshold = 0.d;
    private HashMap<Concept, Double> thresholds;
    private String literal = null;
    //private Map<Integer, Double> activation = null;
    private double c_t = 100000.d; // used in Berger et al. 2004, should be the # of nodes in the graph.
    private int pulseCount = 0;
    private HashMap<Concept, Marker> activation = null;

    //###########################
    // FUNCTIONS
    //###########################
    public TypedNode() {
        links = new ArrayList<>();
        literal = "";
        activation = new HashMap<>();
    }

    public TypedNode(String literal) {
        links = new ArrayList<>();
        this.literal = literal;
        activation = new HashMap<>();
    }

    @Override
    public void removeLink(Link link) {
        if(link != null) {
            links.remove(link);
        }
    }

    public String getLiteral() {
        return literal;
    }

    @Override
    public void addLink(Link link) {
        if(link != null) {
            if(!links.contains(link))
                links.add(link);
        }
    }

    @Override
    public Collection<Link> getLinks() {
        return this.links;
    }

    @Override
    public Collection<Marker> getMarkers() {
        return this.markers;
    }

    public void setLinks(Collection<Link> links) {
        this.links = links;
    }

    //public void setMarkers(Collection<Marker> markers) {
    //    this.markers = markers;
    //}

    public double getCumulativeActivation() {
        double activation = 0.d;
        for(Map.Entry<Concept, Marker> e : this.activation.entrySet()) {
            TypedMarker marker = (TypedMarker)e.getValue();
            activation += marker.getValue();
        }
        return activation;
    }

    public double getCumulativeWeightedActivation() {
        double activation = getCumulativeActivation();
        activation = activation * (1.d + (0.3d * this.activation.size()));
        return activation;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public void addThreshold(Concept concept, double threshold) {
        this.thresholds.put(concept,threshold);
    }

    @Override
    public void addMarker(Marker marker) {
        if(marker != null) {
            if(marker instanceof TypedMarker) {
                TypedMarker tm = ((TypedMarker)activation.get(((TypedMarker) marker).getOrigin()));
                if(tm == null) {
                    this.activation.put(((TypedMarker)marker).getOrigin(),marker);
                }
                else {
                    double newValue = tm.getValue() + ((TypedMarker) marker).getValue();
                    tm.setValue(((newValue > 0.d) ? newValue : 0.d)); // we currently don't allow negative activation
                }
            }
        }
    }

    @Override
    public void removeMarker(Marker marker) {
        if(marker != null) {
            if (marker instanceof TypedMarker) {
                TypedMarker tm = ((TypedMarker) activation.get(((TypedMarker) marker).getOrigin()));
                if(tm != null) {
                    double newValue = tm.getValue() - ((TypedMarker) marker).getValue();
                    tm.setValue(((newValue > 0.d) ? newValue : 0.d)); // we currently don't allow negative activation
                }
            }
        }
    }

    @Override
    public boolean checkThresholds(Object originConcept) {
        double activationSum = 0.d;
        for(Map.Entry<Concept, Marker> entry : activation.entrySet()) {
            activationSum+=((TypedMarker)entry.getValue()).getValue();
        }
        return activationSum >= threshold;
    }

    /***
     * Adds all activations(SpreadingSteps) with its Markers to the Node.
     * @param steps
     */
    public void in(Collection<SpreadingStep>  steps) {
        for(SpreadingStep step : steps) {
            if(step.getTargetNode().equals(this)) {
                for(Marker marker : step.getMarkings()) {
                    this.addMarker(marker);
                }
            }
        }
    }

    public Collection<SpreadingStep> out() {
        Collection<SpreadingStep> spreadingSteps = new ArrayList<SpreadingStep>();
        double f = 1.d - (getLinks().size() / this.c_t);
        double out = (f / (this.pulseCount + 1.d));

        for(Link link : getLinks()) {
            SpreadingStep spreadingStep = new SpreadingStep();
            spreadingStep.setLink(link);
            spreadingStep.setMarkings(getMarkersForLink(link,out,false));
            spreadingStep.setInDirection(true);
            spreadingSteps.add(spreadingStep);
        }

        return spreadingSteps;
    }

    public Collection<Marker> getMarkersForLink(Link link, double out, boolean useSpread) {
        Collection<Marker> markers = new ArrayList<>();
        //double weight = link instanceof WeightedLink ? ((WeightedLink) link).getWeight() : 1.d;
        WeightedLink wl = (WeightedLink)link;
        double weight = wl.getWeight();
        double spread = useSpread ? getLinks().size() : 1.d;
        for(Map.Entry<Concept, Marker> entry : activation.entrySet()) {
            if(entry.getValue() instanceof TypedMarker) {
                TypedMarker m = (TypedMarker)entry.getValue();
                double value = weight * m.getValue() * out / spread;
                markers.add(new TypedMarker(value, m.getOrigin(), m.getType()));
            }
        }
        return markers;
    }

}
