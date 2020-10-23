/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.kimanufaktur.nsm.decomposition.dictionaries.wikidata;

import com.opencsv.CSVReader;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class WikidataExtractionProcessor {
    private static WikidataExtractionProcessor sharedInstance;
    private        Driver                      driver;

    private HashMap<String, Long> propertyNodeIds;
    private HashMap<String, String> wdidMappings;
    private Integer numberOfSimilarMatches = 2;
    private Integer maximumNumberFetchedEdges = 2;

    public WikidataExtractionProcessor() {
        driver = GraphDatabase.driver( "bolt://localhost", AuthTokens.basic( "neo4j", "neo4j" ) );

    }

    public void teardown() {

        driver.close();
    }

    /**
     * lazily get the shared instance
     * @return shared instance of the WikidataExtractionProcessor
     */
    public static WikidataExtractionProcessor getSharedInstance() {
        if (sharedInstance == null) {
            sharedInstance = new WikidataExtractionProcessor();
        }
        return sharedInstance;
    }

    public List<Record> query(Session session, String query, Object... parameters) {
        query = String.format(query, parameters);

        StatementResult result =  session.run( query);

        List<Record> retval = new ArrayList<>();
        while (result.hasNext()) {
            retval.add(result.next());
        }

        return retval;
    }

    public WikidataItem queryItem(String query, Object... parameters) {
        Session session = driver.session();
        List<Record> result = query(session, query, parameters);

        if (result.size() > 0) {
            WikidataItem resultItem = new WikidataItem(result.remove(0), result);

            session.close();
            return resultItem;
        }
        session.close();
        return null;
    }

    public List<WikidataItem> queryItems(String query, Object... parameters) {
        Session session =  driver.session();

        List<Record> records = query(session, query, parameters);
        List<WikidataItem> result = records.stream().map(record -> new WikidataItem(record)).collect(Collectors.toList());
        session.close();
        return result;
    }

    public List<String> queryRelations(String query, Object... parameters) {
        Session session = driver.session();

        List<Record> records = query(session, query, parameters);
        List<String> result = records.stream().map(record -> record.get("p").get("title").asString()).collect(Collectors.toList());
        session.close();
        return result;
    }

    public WikidataItem findItemByName(String name) {
        // TODO can i use this map?
        String wdid = getWdidMappings().get(name);
        if (wdid == null) return null;
        WikidataItem item = findItemByWdid(wdid);
        return item;
//        TODO dont ever use for the name due to performance reasons
//        if (item != null) { return item; }
//        return queryItem("MATCH (n:Item) WHERE n.title =~ \"(?i)%s\" OR single(x IN n.aliases WHERE x = \"%s\") RETURN n LIMIT %d;", name, name, numberOfSimilarMatches);
    }

    public WikidataItem findItemById(Long id) {
        return queryItem("MATCH (n:Item) WHERE ID(n) = %d RETURN n LIMIT 1;", id);
    }

    public WikidataItem findItemByWdid(String wdid) {
        return queryItem("MATCH (n:Item) WHERE n.wdid = \"%s\" RETURN n LIMIT 1;", wdid);
    }

    /**
     * finds all values that are related to the given item via the given propertyNames
     *
     * @param sourceId of the item that originates the statements
     * @param propertyNames names of properties that are searched for
     */
    public List<WikidataItem> valueNodesForProperties(WikidataItem source, String... propertyNames) {
        ArrayList<WikidataItem> retval = new ArrayList<>();
        if (source.getIsNotExpandable()) return retval;
        ArrayList<WikidataItem> sources = new ArrayList<>();
        sources.add(source);
        sources.addAll(source.getRelatedItems());

        sources.forEach(sourceItem -> {
            Arrays.stream(propertyNames).forEach(propertyName -> {
                retval.addAll(valueNodesForPropertyName(sourceItem.getId(), propertyName));
            });
        });

        return retval;
    }

    public List<WikidataItem> valueNodesForInvertedProperties(WikidataItem source, String... propertyNames) {
        ArrayList<WikidataItem> retval = new ArrayList<>();
        if (source.getIsNotExpandable()) return retval;
        ArrayList<WikidataItem> sources = new ArrayList<>();
        sources.add(source);
        sources.addAll(source.getRelatedItems());

        sources.forEach(sourceItem -> {
            Arrays.stream(propertyNames).forEach(propertyName -> {
                retval.addAll(valueNodesForInvertedPropertyName(sourceItem.getId(), propertyName));
            });
        });

        return retval;
    }

    public List<WikidataItem> valueNodesExcludingProperties(WikidataItem source, String... propertyNames) {
        ArrayList<WikidataItem> retval = new ArrayList<>();
        if (source.getIsNotExpandable()) return retval;
        String propertyIds = getExcludedPropertyIds(propertyNames);
        ArrayList<WikidataItem> sources = new ArrayList<>();
        sources.add(source);
        sources.addAll(source.getRelatedItems());

        sources.forEach(sourceItem -> {
            Arrays.stream(propertyNames).forEach(propertyName -> {
                retval.addAll(valueNodesExcludingPropertyIds(sourceItem.getId(), propertyIds));
            });
        });

        return retval;
    }

    public List<WikidataItem> valueNodesForInvertedPropertiesExcludingProperties(WikidataItem source, String... propertyNames) {
        ArrayList<WikidataItem> retval = new ArrayList<>();
        if (source.getIsNotExpandable()) return retval;
        String propertyIds = getExcludedPropertyIds(propertyNames);
        ArrayList<WikidataItem> sources = new ArrayList<>();
        sources.add(source);
        sources.addAll(source.getRelatedItems());

        sources.forEach(sourceItem -> {
            Arrays.stream(propertyNames).forEach(propertyName -> {
                retval.addAll(valueNodesForInvertedPropertiesExcludingPropertyIds(sourceItem.getId(), propertyIds));
            });
        });

        return retval;
    }

    public List<String> namesOfPresentRelations(WikidataItem source) {
        ArrayList<String> retval = new ArrayList<>();
        if (source.getIsNotExpandable()) return retval;
        ArrayList<WikidataItem> sources = new ArrayList<>();
        sources.add(source);
        sources.addAll(source.getRelatedItems());

        sources.forEach(sourceItem -> {
            retval.addAll(queryRelations(
                    "START n=node(%d)\n" +
                            "MATCH (n)-[CLAIMS]->(c:Claim),\n" +
                            "      (c)-[TYPE_OF]->(p)\n" +
                            "RETURN p",
                    sourceItem.getId()
            ));
        });

        return retval;
    }

    private String getExcludedPropertyIds(String[] propertyNames) {
        List<String> exclusions = Arrays.stream(propertyNames).collect(Collectors.toList());
        exclusions.addAll(getGloballyExcludedProperties());
        List<Long> excludedProperties = exclusions.stream().map(this::getPropertyNodeId).collect(Collectors.toList());
        return excludedProperties.toString();
    }

    private List<WikidataItem> valueNodesExcludingPropertyIds(Long sourceId, String propertyIds) {
        return queryItems(
                "START n=node(%d)\n" +
                        "MATCH (n)-[r1:CLAIMS]->(c:Claim),\n" +
                        "      (c)-[r2:HAS_VALUE]->(v:Value),\n" +
                        "      (c)-[r3:TYPE_OF]->(p)\n" +
                        "WHERE NOT(ID(p) IN %s)\n" +
                        "RETURN v, p",
                sourceId,
                propertyIds
        );
    }

    private List<WikidataItem> valueNodesForInvertedPropertiesExcludingPropertyIds(Long sourceId, String propertyIds) {
        return queryItems(
                "START v=node(%d)\n" +
                        "MATCH (n)-[r1:CLAIMS]->(c:Claim),\n" +
                        "      (c)-[r2:HAS_VALUE]->(v:Value),\n" +
                        "      (c)-[r3:TYPE_OF]->(q)\n" +
                        "WHERE NOT(ID(q) IN %s)\n" +
                        "RETURN n, q LIMIT %d",
                sourceId,
                propertyIds,
                maximumNumberFetchedEdges
        );
    }

    private List<WikidataItem> valueNodesForPropertyName(Long sourceId, String propertyName) {
        Long propertyNodeId = getPropertyNodeId(propertyName);
        return queryItems(
                "START n=node(%d)\n" +
                        "MATCH (n)-[r1:CLAIMS]->(c:Claim),\n" +
                        "      (c)-[r2:HAS_VALUE]->(v:Value),\n" +
                        "      (c)-[r3:TYPE_OF]->(p)\n" +
                        "WHERE ID(p) = %d\n" +
                        "RETURN v, p",
                sourceId,
                propertyNodeId
        );
    }

    private List<WikidataItem> valueNodesForInvertedPropertyName(Long sourceId, String propertyName) {
        Long propertyNodeId = getPropertyNodeId(propertyName);
        return queryItems(
                "START v=node(%d)\n" +
                        "MATCH (n)-[r1:CLAIMS]->(c:Claim),\n" +
                        "      (c)-[r2:HAS_VALUE]->(v:Value),\n" +
                        "      (c)-[r3:TYPE_OF]->(q)\n" +
                        "WHERE ID(q) = %d\n" +
                        "RETURN n, q LIMIT %d",
                sourceId,
                propertyNodeId,
                maximumNumberFetchedEdges
        );
    }

    private Long getPropertyNodeId(String propertyName) {
        Long nodeId = getPropertyNodeIds().get(propertyName);
        Session session = null;
        if (nodeId == null) {
            session = driver.session();

            nodeId = query(session, "MATCH (p:Property) WHERE p.title= \"%s\" RETURN ID(p) as id LIMIT 1;", propertyName)
                        .get(0)
                        .get("id")
                        .asLong();
            getPropertyNodeIds().put(propertyName, nodeId);
            session.close();
        }
        return nodeId;
    }

    private HashMap<String, Long> getPropertyNodeIds() {
        if (propertyNodeIds == null) {
            propertyNodeIds = new HashMap<>();
        }
        return propertyNodeIds;
    }

    private HashMap<String, String> getWdidMappings() {
        if (wdidMappings == null) {
            wdidMappings = readWikidataEntitymap();
        }
        return wdidMappings;
    }

    private HashMap<String, String> readWikidataEntitymap()  {
        String u = this.getClass().getResource("/entitymap.csv").getPath();
        HashMap<String, String> retval = new HashMap<>();
        CSVReader reader = null;
        try {
            reader = new CSVReader(new FileReader(u));
            String[] nextLine;
            while ((nextLine = reader.readNext()) != null) {
                retval.put(nextLine[0], nextLine[1]);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return retval;
    }

    private Collection<? extends String> getGloballyExcludedProperties() {
        return Arrays.asList(
                "AllMovie artist ID",
                "AlloCiné person ID",
                "BiblioNet author ID",
                "BIBSYS ID",
                "BNE ID",
                "BnF ID",
                "CANTIC-ID",
                "CiNii author ID",
                "ČSFD person ID",
                "Discogs artist ID",
                "dmoz ID",
                "DNF person ID",
                "Encyclopædia Britannica Online ID",
                "FAST-ID",
                "Freebase ID",
                "GND ID",
                "IMDb ID",
                "ISFDB author ID",
                "ISNI",
                "image",
                "Kinopoisk person ID",
                "LAC ID",
                "LCAuth ID",
                "LNB ID",
                "Munzinger IBA",
                "MusicBrainz artist ID",
                "MusicBrainz label ID",
                "National Library of Israel ID",
                "National Portrait Gallery (London) person ID",
                "National Thesaurus for Author Names ID",
                "NDL ID",
                "NILF author id",
                "NKCR AUT ID",
                "NLA (Australia) ID",
                "NLP ID",
                "NLR (Romania) ID",
                "NNDB people ID",
                "NSK ID",
                "NUKAT (WarsawU) authorities",
                "official website",
                "Open Library ID",
                "Oxford Biography Index Number",
                "People Australia ID",
                "Perlentaucher ID",
                "PORT person ID",
                "PTBNP ID",
                "Rotten Tomatoes ID",
                "RSL ID (person)",
                "SBN ID",
                "SELIBR",
                "SFDb person ID",
                "SUDOC authorities",
                "TED speaker ID",
                "VIAF ID",
                "contributor",
                "described by source",
                "depicts"
        );
    }
}
