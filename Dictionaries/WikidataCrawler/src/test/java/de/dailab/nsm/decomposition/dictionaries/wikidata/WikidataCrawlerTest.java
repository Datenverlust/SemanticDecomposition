/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.dailab.nsm.decomposition.dictionaries.wikidata;

import static com.mscharhag.oleaster.matcher.Matchers.expect;
import static com.mscharhag.oleaster.runner.StaticRunnerSupport.*;

import com.mscharhag.oleaster.runner.OleasterRunner;

import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RunWith(OleasterRunner.class)
public class WikidataCrawlerTest {
    private WikidataCrawler described_instance;

    {
        /**
         * inject the database for testing to speed up the process
         */
        before(() -> {
            described_instance = WikidataCrawler.getSharedInstance();
        });

        describe("findItemByName", () -> {
            it("returns the best match for a given name that matches the title", () -> {
                WikidataItem result = described_instance.findItemByName("Germany");
                expect(result.getWdid()).toEqual("Q183");
            });

            it("returns the best match for a given name that matches an alias", () -> {
                WikidataItem result = described_instance.findItemByName("Bundesrepublik Deutschland");
                expect(result.getWdid()).toEqual("Q183");
            });
        });

        describe("findItemByWdid", () -> {
            it("returns the best match for a given name that matches the title", () -> {
                WikidataItem result = described_instance.findItemByWdid("Q183");
                expect(result.getTitle()).toEqual("Germany");
            });
            it("gracefully handles a missing item", () -> {
                WikidataItem result = described_instance.findItemByWdid("Q0");
                expect(result).toBeNull();
            });
        });

        describe("findItemById", () -> {
            it("returns the best match for a given name that matches the title", () -> {
                WikidataItem temp = described_instance.findItemByWdid("Q183");
                WikidataItem result= described_instance.findItemById(temp.getId());
                expect(result.getTitle()).toEqual("Germany");
            });

            it("gracefully handles a missing item", () -> {
                WikidataItem result = described_instance.findItemById((long) 0);
                expect(result).toBeNull();
            });
        });

        describe("an entity", () -> {
            it("is able to return the title", () -> {
                WikidataItem entity = described_instance.findItemByWdid("Q42");
                expect(entity.getTitle()).toEqual("Douglas Adams");
            });

            it("does not fail on a missing title, hence returns an empty string", () -> {
                WikidataItem entity = described_instance.findItemByWdid("Q22828603");
                expect(entity.getTitle()).toEqual(""); // missing title intended ...
            });

            it("is able to return the description", () -> {
                WikidataItem entity = described_instance.findItemByWdid("Q42");
                expect(entity.getDescription()).toEqual("English writer and humorist");
            });

            it("does not fail on a missing description, hence returns an empty string", () -> {
                WikidataItem entity = described_instance.findItemByWdid("Q22828603");
                expect(entity.getDescription()).toEqual(""); // missing description intended ...
            });

            it("is able to fetch its hypernyms through 'instance of' relations", () -> {
                WikidataItem entity = described_instance.findItemByWdid("Q23444"); // white

                List<String> expectation = new ArrayList<>();
                expectation.add("color"); // "instance of" exists

                List<String> result = entity.getHypernyms().stream().map(WikidataItem::getTitle).collect(Collectors.toList());
                expect(result.containsAll(expectation)).toBeTrue();
            });

            it("is able to fetch its hypernyms through 'subclass of' relations", () -> {
                WikidataItem entity = described_instance.findItemByWdid("Q83437"); // gemstone

                List<String> expectation = new ArrayList<>();
                expectation.add("mineral"); // "subclass of" exists
                expectation.add("jewellery"); // "subclass of" exists

                List<String> result = entity.getHypernyms().stream().map(WikidataItem::getTitle).collect(Collectors.toList());
                expect(result.containsAll(expectation)).toBeTrue();
            });

            it("is able to fetch its hyponyms through 'subclass of' relations", () -> {
                WikidataItem entity = described_instance.findItemByWdid("Q7946"); // mineral

                List<String> expectation = new ArrayList<>();
                expectation.add("gemstone"); // "subclass of" exists

                List<String> result = entity.getHyponyms().stream().map(WikidataItem::getTitle).collect(Collectors.toList());
                expect(result.containsAll(expectation)).toBeTrue();
            });

            it("is able to fetch its hyponyms through 'instance of' relations", () -> {
                WikidataItem entity = described_instance.findItemByWdid("Q6256"); // country

                List<String> expectation = new ArrayList<>();
                expectation.add("Germany"); // "instance of" exists

                List<String> result = entity.getHyponyms().stream().map(WikidataItem::getTitle).collect(Collectors.toList());
                expect(result.containsAll(expectation)).toBeTrue();
            });

            it("is able to return the meronyms through 'part of' relation", () -> {
                WikidataItem entity = described_instance.findItemByWdid("Q23640"); // head

                List<String> expectation = new ArrayList<>();
                expectation.add("ear"); // "part of" exists

                List<String> result = entity.getMeronyms().stream().map(WikidataItem::getTitle).collect(Collectors.toList());
                expect(result.containsAll(expectation)).toBeTrue();
            });

            it("is able to return the meronyms through 'facet of' relation", () -> {
                WikidataItem entity = described_instance.findItemByWdid("Q8652"); // Miami

                List<String> expectation = new ArrayList<>();
                expectation.add("climate of Miami"); // "facet of" exists

                List<String> result = entity.getMeronyms().stream().map(WikidataItem::getTitle).collect(Collectors.toList());
                expect(result.containsAll(expectation)).toBeTrue();
            });

            it("is able to return the antonyms through 'opposite of' relation", () -> {
                WikidataItem entity = described_instance.findItemByWdid("Q23445"); // black

                List<String> expectation = new ArrayList<>();
                expectation.add("white"); // "opposite of" exists

                List<String> result = entity.getAntonyms().stream().map(WikidataItem::getTitle).collect(Collectors.toList());
                expect(result.containsAll(expectation)).toBeTrue();
            });

            it("is able to return the synonyms through 'said to be the same as' relation", () -> {
                WikidataItem entity = described_instance.findItemByWdid("Q538"); // Oceania

                List<String> expectation = new ArrayList<>();
                expectation.add("Q20820245"); // "said to be the same as" exists

                List<String> result = entity.getSynonyms().stream().map(WikidataItem::getWdid).collect(Collectors.toList());
                expect(result.containsAll(expectation)).toBeTrue();
            });

            it("is able to return the synonyms through its aliases", () -> {
                WikidataItem entity = described_instance.findItemByWdid("Q183"); // Germany

                List<String> expectation = new ArrayList<>();
                expectation.add("Bundesrepublik Deutschland"); // alias exists

                List<String> result = entity.getSynonyms().stream().map(WikidataItem::getTitle).collect(Collectors.toList());
                expect(result.containsAll(expectation)).toBeTrue();
            });

            it("is able to return named relations", () -> {
                WikidataItem entity = described_instance.findItemByWdid("Q23445"); // black

                List<String> expectation = new ArrayList<>();
                expectation.add("white"); // "opposite of" exists

                List<String> result = entity.getNamedRelation("opposite of").stream().map(WikidataItem::getTitle).collect(Collectors.toList());
                expect(result.containsAll(expectation)).toBeTrue();
            });

            it("is able to return inversions of named relations", () -> {
                WikidataItem entity = described_instance.findItemByWdid("Q6256"); // country

                List<String> expectation = new ArrayList<>();
                expectation.add("Germany"); // "instance of" exists

                List<String> result = entity.getNamedRelation("instance of", true).stream().map(WikidataItem::getTitle).collect(Collectors.toList());
                expect(result.containsAll(expectation)).toBeTrue();
            });

            it("is able to return the names of present relations", () -> {
                WikidataItem entity = described_instance.findItemByWdid("Q23445"); // black

                List<String> expectation = new ArrayList<>();
                expectation.add("instance of");

                List<String> result = entity.getRelationNames().stream().collect(Collectors.toList());
                expect(result.containsAll(expectation)).toBeTrue();
            });
        });
    }
}