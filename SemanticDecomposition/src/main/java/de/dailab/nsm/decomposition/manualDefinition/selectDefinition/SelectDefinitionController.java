/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.dailab.nsm.decomposition.manualDefinition.selectDefinition;

import de.dailab.nsm.decomposition.Concept;
import de.dailab.nsm.decomposition.Definition;
import de.dailab.nsm.decomposition.manualDefinition.model.Delegate;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.*;
import javafx.scene.paint.Color;

import java.util.Iterator;
import java.util.List;

/**
 * Created by Ghadh on 01.11.2015.
 */
public class SelectDefinitionController {

    private static Concept concept;
    private SelectDefinitionView view;
    private Delegate delegate = null;


    public SelectDefinitionController(Concept concept) {
        SelectDefinitionController.concept = concept;
        this.view = new SelectDefinitionView();
    }

    private void showError(Concept concept){
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning ");
        alert.setHeaderText("");
        alert.setContentText("the definition has a cycle with the concept: "
                + concept.getLitheral()+". please, choose another one ");
        concept.getDefinitions().clear();
        alert.showAndWait();
    }
    public Concept getSelectedDefinition() {
        view.getLabel().setText("choose a definition for: " + concept.getLitheral());
        int k = nrDefinition(concept);
        Iterator<Definition> definitionIterator = concept.getDefinitions().iterator();
        while (definitionIterator.hasNext()){
            Definition d = definitionIterator.next();
            final RadioButton radio = new RadioButton(d.toString());
            radio.setToggleGroup(view.getToggleGroup());
            radio.setUserData(d);
            view.getObservableListRadiobutton().add(radio);

        }

        view.getListView().setItems(view.getObservableListRadiobutton());
        view.getToggleGroup().selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            public void changed(ObservableValue<? extends Toggle> ov,
                                Toggle old_toggle, Toggle new_toggle) {
                if (view.getToggleGroup().getSelectedToggle() != null && delegate != null) {
                    view.getLabel().setText("");
                    Definition d = (Definition) view.getToggleGroup().getSelectedToggle().getUserData();
                    concept.getDefinitions().clear();
                    concept.getDefinitions().add(d);
                    // use the benefit of the prime, syn, ant and known concepts
                    concept = delegate.checkDefinitionForSelection(concept);
                    Definition definition = concept.getDefinitions().iterator().next();
                    boolean cycle = delegate.checkCycle(concept, definition, 3);

                    if (cycle) {
                        Concept conceptCyc = delegate.getConceptWithCycle(concept, definition, 3);
                        showError(conceptCyc);
                    } else {
                        view.getLabel().setText(concept.getDefinitions().toString());
                        view.getLabel().setTextFill(Color.GREEN);
                    }

                }
            }
        });

        view.getOk().setOnAction(e -> {
            if (view.getToggleGroup().getSelectedToggle() != null) {
                view.getStage().close();

            } else {
                view.getLabel2().setText("you must choose a definition");
                view.getLabel2().setTextFill(Color.RED);
            }

        });
        view.getManuelDef().setOnAction(e -> {
            concept.getDefinitions().clear();
           concept= delegate.getManualDefinition(concept);
            view.getStage().close();

        });
        view.getStage().showAndWait();

        return concept;
    }

    private int nrDefinition(Concept concept) {
        if (concept != null) {
            if (concept.getDefinitions() != null) {
                if (!concept.getDefinitions().isEmpty()) {
                    return concept.getDefinitions().size();
                }
            }
        }
        return 0;
    }

    public void setDelegate(Delegate delegate) {
        this.delegate = delegate;
    }

    public static void main(String[] args) {

    }

}