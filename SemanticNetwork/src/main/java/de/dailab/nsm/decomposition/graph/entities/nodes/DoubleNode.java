/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.dailab.nsm.decomposition.graph.entities.nodes;

import de.dailab.nsm.decomposition.graph.entities.links.WeightedLink;
import de.dailab.nsm.decomposition.graph.entities.marker.DoubleMarker;
import de.tuberlin.spreadalgo.Link;
import de.tuberlin.spreadalgo.Marker;
import de.tuberlin.spreadalgo.Node;
import de.tuberlin.spreadalgo.SpreadingStep;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by faehndrich on 19.05.15.
 */
public class DoubleNode implements Node {
    Collection<Link> links = null;
    Collection<Marker> markers = null;
    double threshold = 0.0d;
    String litheral = null;
    double activation = 0.0d;

    public double getActivation() {
        return activation;
    }

    public void setActivation(double activation) {
        this.activation = activation;
    }


    public DoubleNode(String litheral) {
        this.links = new ArrayList<>();
        this.markers = new ArrayList<>();
        this.setLitheral(litheral);
    }

    public String getLitheral() {
        return litheral;
    }

    public void setLitheral(String litheral) {
        this.litheral = litheral;
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
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
        return markers;
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
        if (marker != null && markers.contains(marker)) {
            markers.remove(marker);
        }
    }

    @Override
    public boolean checkThresholds(Object o) {
        return false;
    }

//    @Override
//    public boolean checkThresholds(Collection<Class<? extends Marker>> collection) {
//        return false;
//    }


//    @Override
//    public boolean checkThresholds(Object markerClasses) {
//        List<Marker> marker2remove = new ArrayList<>();
//        for (Marker marker : this.getMarkers()) {
//                this.activation += ((DoubleMarker) marker).getActivation();
//                marker2remove.add(marker);
//        }
//        //we remove the markers we have allready looked at.
//        this.getMarkers().removeAll(marker2remove);
//        if (this.activation >= this.getThreshold()) {
//            return true;
//        } else {
//            return false;
//        }
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
     * @return the activation step for the next node.
     */
    public Collection<SpreadingStep> out() {
        Collection<SpreadingStep> spreadingSteps = new ArrayList<>();
        double activation2Pass = activation / getLinks().size();
        for (Link link : getLinks()) {
            if (link instanceof WeightedLink) {
                SpreadingStep spreadingStep = new SpreadingStep();
                spreadingStep.setLink(link);
                spreadingStep.setMarkings(getMarkersForLink(link,activation2Pass));
                spreadingStep.setInDirection(true);
                spreadingSteps.add(spreadingStep);
            }
        }
        return spreadingSteps;
    }

    /**
     * get Markers for the given link. Here we use the activation with activationlvl/#marker
     * TODO: differentiate the link types.
     * TODO: choose a output weight for the different link types.
     * @param link the link to get the marker for
     * @return list of markers to be bast to the given link.
     */
    private Collection<Marker> getMarkersForLink(Link link,double activation2Pass) {
        Collection<Marker> markers = new ArrayList<>();
        DoubleMarker marker4link = new DoubleMarker();

        //Reduce the current activation given to the link.
        setActivation(activation-activation2Pass); //TODO: this could be done in an post processing set.
        marker4link.setActivation(activation2Pass);
        markers.add(marker4link);
        return markers;
    }
}
