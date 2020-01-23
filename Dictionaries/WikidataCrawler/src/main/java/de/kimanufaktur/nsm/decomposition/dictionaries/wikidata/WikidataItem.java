/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.kimanufaktur.nsm.decomposition.dictionaries.wikidata;

import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Value;
import org.neo4j.driver.v1.types.Node;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by tkoenig on 14/06/16.
 */
public class WikidataItem {
    private Node node;
    private List<WikidataItem> relatedItems;
    private String enforcedTitle;
    private String originatedRelationName;
    private Boolean isNotExpandable = false;

    public WikidataItem(Record record) {
        init(record);
    }

    public WikidataItem(Node node, String enforcedTitle) {
        this.node = node;
        this.enforcedTitle = enforcedTitle;
        this.isNotExpandable = true;
    }

    public WikidataItem(Record record, List<Record> result) {
        init(record);
        relatedItems = result.stream().map(WikidataItem::new).collect(Collectors.toList());
    }

    private void init(Record record) {
        List<String> keys = record.keys();
        if (keys.size() > 0) {
            String nodeName = keys.get(0);
            node = record.get(nodeName).asNode();

            Value incomingRelation = record.get("p");
            Value outgoingRelation = record.get("q");

            if (!incomingRelation.isNull()) {
                originatedRelationName = incomingRelation.asNode().get("title").toString();
            } else if (!outgoingRelation.isNull()) {
                originatedRelationName = "inverse of " + outgoingRelation.asNode().get("title").toString();
            }

            if (originatedRelationName != null) {
                originatedRelationName = originatedRelationName.replace("\"", "");
            }
        }
    }

    public Long getId() {
        return node.id();
    }

    public String getOriginatedRelationName() {
        return originatedRelationName;
    }

    public String getWdid() {
        return getFieldStringValue("wdid");
    }

    public String getTitle() {
        if (enforcedTitle != null) return enforcedTitle;
        return getFieldStringValue("title");
    }

    public String getDescription() {
        return getFieldStringValue("description");
    }

    private String getFieldStringValue(String fieldName) {
        Value value = node.get(fieldName);
        if (value.isNull()) return "";
        return value.asString();
    }

    public List<WikidataItem> getRelatedItems() {
        if (relatedItems == null) {
            relatedItems = new ArrayList<>();
        }
        return relatedItems;
    }

    public HashSet<WikidataItem> getSynonyms() {
        HashSet<WikidataItem> retval = new HashSet<>();
        if (getIsNotExpandable()) return retval;

        Value aliases = node.get("aliases");
        if (!aliases.isNull()) {
            aliases.asList().stream().forEach(alias -> {
                WikidataItem synonym = new WikidataItem(node, alias.toString());
                retval.add(synonym);
            });
        }
        retval.addAll(WikidataExtractionProcessor.getSharedInstance().valueNodesForProperties(this, "said to be the same as"));

        return retval;
    }

    public HashSet<WikidataItem> getAntonyms() {
        HashSet<WikidataItem> retval = new HashSet<>();

        retval.addAll(WikidataExtractionProcessor.getSharedInstance().valueNodesForProperties(this, "opposite of"));

        return retval;
    }

    public HashSet<WikidataItem> getHypernyms() {
        HashSet<WikidataItem> retval = new HashSet<>();

        retval.addAll(WikidataExtractionProcessor.getSharedInstance().valueNodesForProperties(this, "instance of", "subclass of"));

        return retval;
    }

    public HashSet<WikidataItem> getHyponyms() {
        HashSet<WikidataItem> retval = new HashSet<>();

        retval.addAll(WikidataExtractionProcessor.getSharedInstance().valueNodesForInvertedProperties(this, "instance of", "subclass of"));

        return retval;
    }

    public HashSet<WikidataItem> getMeronyms() {
        HashSet<WikidataItem> retval = new HashSet<>();

        retval.addAll(WikidataExtractionProcessor.getSharedInstance().valueNodesForInvertedProperties(this, "part of", "facet of"));

        return retval;
    }

    public HashSet<WikidataItem> getArbitraryRelations() {
        HashSet<WikidataItem> retval = new HashSet<>();

        retval.addAll(WikidataExtractionProcessor.getSharedInstance().valueNodesExcludingProperties(this, "said to be the same as", "opposite of", "instance of", "subclass of"));
        retval.addAll(WikidataExtractionProcessor.getSharedInstance().valueNodesForInvertedPropertiesExcludingProperties(this, "instance of", "subclass of", "part of", "facet of"));
//        retval.addAll(WikidataExtractionProcessor.getSharedInstance().valueNodesExcludingProperties(this, "IMDb ID"));
//        retval.addAll(WikidataExtractionProcessor.getSharedInstance().valueNodesForInvertedPropertiesExcludingProperties(this, "IMDb ID"));

        return retval;
    }

    public HashSet<WikidataItem> getNamedRelation(String relationName, boolean isCounterRelation) {
        HashSet<WikidataItem> retval = new HashSet<>();

        if (isCounterRelation) {
            retval.addAll(WikidataExtractionProcessor.getSharedInstance().valueNodesForInvertedProperties(this, relationName));
        } else {
            retval.addAll(WikidataExtractionProcessor.getSharedInstance().valueNodesForProperties(this, relationName));
        }

        return retval;
    }

    public HashSet<WikidataItem> getNamedRelation(String relationName) {
        return getNamedRelation(relationName, false);
    }

    public HashSet<String> getRelationNames() {
        HashSet<String> retval = new HashSet<>();

        retval.addAll(WikidataExtractionProcessor.getSharedInstance().namesOfPresentRelations(this));

        return retval;
    }

    public Boolean getIsNotExpandable() {
        return isNotExpandable;
    }
}
