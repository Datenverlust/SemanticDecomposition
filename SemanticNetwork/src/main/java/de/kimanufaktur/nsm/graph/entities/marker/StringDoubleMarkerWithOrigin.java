/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.kimanufaktur.nsm.graph.entities.marker;

import de.kimanufaktur.markerpassing.Link;
import de.kimanufaktur.markerpassing.Marker;

import java.util.LinkedList;
import java.util.List;

public class StringDoubleMarkerWithOrigin implements Marker {
    double activation = Double.NaN;
    String origin = null;
    Class<? extends Link> linkType = null;
    List<String> visitedStrings = new LinkedList<String>();
    List<Link> visitedLinks = new LinkedList<>();
    List<String> answers = new LinkedList<>();

    public List<Link> getVisitedLinks() {return visitedLinks;}
    public void setVisitedLinks(List<Link> visitedLinks) {this.visitedLinks = visitedLinks;}
    public Class<? extends Link> getLinkType() {
        return linkType;
    }
    public void setLinkType(Class<? extends Link> linkType) {
        this.linkType = linkType;
    }
    public String getOrigin() {
        return origin;
    }
    public void setOrigin(String origin) {
        this.origin = origin;
    }
    public List<String> getAnswers() {return answers; }

    public void setAnswers(List<String> answers) {
        this.answers = answers;
    }

    public double getActivation() {
        return activation;
    }
    public void setActivation(double activation) {this.activation = activation;}
    public List<String> getVisitedStrings() {return visitedStrings;}
    public void setVisitedStrings(List<String> visitedStrings) {this.visitedStrings = visitedStrings;}

    public void addString (String name){
        visitedStrings.add(name);
    }

    public void addLink (Link link){
        visitedLinks.add(link);
    }


}