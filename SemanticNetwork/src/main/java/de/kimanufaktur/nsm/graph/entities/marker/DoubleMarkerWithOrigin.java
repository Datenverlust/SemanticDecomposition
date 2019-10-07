/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.kimanufaktur.nsm.graph.entities.marker;

import de.kimanufaktur.nsm.decomposition.Concept;
import de.kimanufaktur.markerpassing.Link;
import de.kimanufaktur.markerpassing.Marker;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by faehndrich on 19.05.15.
 */
public class DoubleMarkerWithOrigin implements Marker {
    double activation = Double.NaN;
    Concept origin = null;
    Class<? extends Link> linkType = null;
    List<Concept> visitedConcepts = new LinkedList<Concept>();
    List<Link> visitedLinks = new LinkedList<>();
    List<Concept> answers = new LinkedList<>();

    public List<Link> getVisitedLinks() {return visitedLinks;}
    public void setVisitedLinks(List<Link> visitedLinks) {this.visitedLinks = visitedLinks;}
    public Class<? extends Link> getLinkType() {
        return linkType;
    }
    public void setLinkType(Class<? extends Link> linkType) {
        this.linkType = linkType;
    }
    public Concept getOrigin() {
        return origin;
    }
    public void setOrigin(Concept origin) {
        this.origin = origin;
    }
    public List<Concept> getAnswers() {return answers; }

    public void setAnswers(List<Concept> answers) {
        this.answers = answers;
    }

    public double getActivation() {
        return activation;
    }
    public void setActivation(double activation) {this.activation = activation;}
    public List<Concept> getVisitedConcepts() {return visitedConcepts;}
    public void setVisitedConcepts(List<Concept> visitedConcepts) {this.visitedConcepts = visitedConcepts;}

    public void addConcept (Concept concept){
        visitedConcepts.add(concept);
    }

    public void addLink (Link link){
        visitedLinks.add(link);
    }


}