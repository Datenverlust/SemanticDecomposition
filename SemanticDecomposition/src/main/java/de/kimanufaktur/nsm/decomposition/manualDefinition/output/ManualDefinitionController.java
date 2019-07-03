/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.kimanufaktur.nsm.decomposition.manualDefinition.output;

import de.kimanufaktur.nsm.decomposition.Concept;
import de.kimanufaktur.nsm.decomposition.Definition;
import de.kimanufaktur.nsm.decomposition.manualDefinition.model.Delegate;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

/**
 * model
 * Created by Ghadh on 03.11.2015.
 */
public class ManualDefinitionController {

    private Concept concept;
    private ManualDefinitionView view;
    private Delegate delegate;
    private Stage stage;


    public ManualDefinitionController(Concept concept) {
        this.concept = concept;

        this.stage = new Stage();

        Platform.runLater(new Runnable() {
            public ManualDefinitionView view;
            public ManualDefinitionView getView() {
                return view;
            }

            public void setView(ManualDefinitionView view) {
                this.view = view;
            }

            public Runnable init(ManualDefinitionView view){
                this.setView(view);
                return this;
            }

            @Override
            public void run() {
                this.view = new ManualDefinitionView();
                initialize();
            }
        }.init(this.view));
             /* Events for GUI-elements*/
    }


    private void initialize() {
        new JFXPanel();
        view.getCheck().setOnAction(new CheckButtonEvent());
        view.getListView().setOnMouseClicked(new ListViewEvent());
        view.getOk().setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                stage.close();
            }
        });
    }

    //reset the listview for suggested words
    private void resetListView2() {
        if (view.getObservableListRadioButton().size() > 1) {
            view.getObservableListRadioButton().clear();
            view.getObservableListRadioButton().removeAll();
        }
        if (view.getListViewConcept().getItems() != null) {
            view.getListViewConcept().getItems().clear();
            view.getListViewConcept().getItems().removeAll();
            view.getListViewConcept().setItems(null);
        }

        view.getListViewConcept().setVisible(false);
        view.getLabelForListViewConcept().setVisible(false);
    }

    private void showConceptDetails(Concept concept1) {
        resetListView2();
        if (concept1 != null) {
            List<Concept> conceptsSyn = new ArrayList<>();
            List<Concept> conceptsAnt = new ArrayList<>();
            if (concept1.getAlternativeSyn() != null && concept1.getAlternativeSyn().size() > 0)
                conceptsSyn.addAll(concept1.getAlternativeSyn());
            if (concept1.getAlternativeAnt() != null && concept1.getAlternativeAnt().size() > 0)
                conceptsAnt.addAll(concept1.getAlternativeAnt());
            ToggleGroup tg = new ToggleGroup();
            for (Concept c : conceptsSyn) {
                RadioButton radio = new RadioButton(c.getLitheral());
                radio.setUserData(c);
                radio.setToggleGroup(tg);
                view.getObservableListRadioButton().add(radio);
            }
            for (Concept c : conceptsAnt) {
                RadioButton radio = new RadioButton("not " + c.getLitheral());
                radio.setUserData(c);
                radio.setToggleGroup(tg);
                view.getObservableListRadioButton().add(radio);
            }
            view.getListViewConcept().setItems(view.getObservableListRadioButton());
            view.getLabelForListViewConcept().setVisible(true);
            view.getListViewConcept().setVisible(true);
            int flag = concept.getDefinitions().iterator().next().getDefinition().indexOf(concept1);
            tg.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                public void changed(ObservableValue<? extends Toggle> ov,
                                    Toggle old_toggle, Toggle new_toggle) {
                    List<Concept> def = concept.getDefinitions().iterator().next().getDefinition();
                    if (tg.getSelectedToggle() != null) {
                        Concept selectedAlternative = (Concept) tg.getSelectedToggle().getUserData();
                        if (conceptsSyn.contains(selectedAlternative)){
                            def.set(flag, selectedAlternative);
                            resetListView2();
                            showListView();
                        }
                        if (conceptsAnt.contains(selectedAlternative)) {
                            def.set(flag, new Concept("not"));
                            def.add(flag + 1, selectedAlternative);
                            resetListView2();
                            showListView();
                        }

                        view.getConfirm().setText("the created definition is: " + def.toString());
                    }
                }

            });
        }

    }

    private void set(Concept concept) {
        this.concept=concept;
    }
    private Concept getConcept(){
        return this.concept;

    }


    public Concept show() {
        view.getWord().setText("please, create a definition for " + concept.getLitheral());
        view.show(stage);
        return concept;
    }

    protected void resetListView() {
        if (view.getListView() != null && view.getListView().getItems() != null) {
            view.getListView().getItems().clear();
            view.getListView().setVisible(false);
            view.getLabelForListView().setVisible(false);

        }
    }


    private void showListView() {
        resetListView();
        if (concept != null && delegate != null) {
            //  concepts in the Definition added to observable list
            if (delegate.noOfConceptsInDefinition(concept) > 0) {
                List<Concept> conceptInDef = concept.getDefinitions().iterator().next().getDefinition();
                for (int i = 0; i < conceptInDef.size(); i++) {
                    if (view.getObservableList() != null) {

                        if (delegate.createIgnoredConcepts(conceptInDef).contains(conceptInDef.get(i)))
                            continue;
                        if (conceptInDef.get(i).getAlternativeSyn() != null) {
                            if (conceptInDef.get(i).getAlternativeSyn().size() > 0) {
                                view.getObservableList().add(conceptInDef.get(i).getLitheral());
                                view.getLabelForListView().setVisible(true);
                                view.getListView().setVisible(true);
                            }
                        }
                        if (conceptInDef.get(i).getAlternativeAnt() != null) {
                            if (conceptInDef.get(i).getAlternativeAnt().size() > 0) {
                                view.getObservableList().add(conceptInDef.get(i).getLitheral());
                                view.getLabelForListView().setVisible(true);
                                view.getListView().setVisible(true);
                            }
                        }
                    }
                }
            }
        }
    }

    public void setDelegate(Delegate delegate) {
        this.delegate = delegate;
    }
    /*
    **********************
        Events
    **********************
     */

    class CheckButtonEvent implements EventHandler<ActionEvent> {

        @Override
        public void handle(ActionEvent event) {
            //clear old items from the listView
            resetListView();
            resetListView2();
            String string = view.getDescriptionInput().getText();

            if (string != "" && !string.isEmpty() && delegate != null) {
                concept = delegate.createDefinitionFromString(concept, string);
                Definition def = concept.getDefinitions().iterator().next();
                if (delegate.checkCycle(concept, def, 3)) {
                    Concept cycle = delegate.getConceptWithCycle(concept, def, 3);
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Warning ");
                    alert.setHeaderText("");
                    alert.setContentText("the definition has a cycle with the following concept: "
                            + cycle.getLitheral());
                    concept.getDefinitions().clear();
                    alert.showAndWait();
                    return;
                }
                delegate.checkDefinition(concept);
                view.getConfirm().setText("Done !" +
                        " the created definition is: " + def.toString());
                view.getConfirm().setTextFill(Color.GREEN);
                showListView();

            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Warning ");
                alert.setHeaderText("");
                alert.setContentText("the definition can not be empty!");
                alert.showAndWait();
            }
        }

    }


    class ListViewEvent implements EventHandler<MouseEvent> {

        @Override
        public void handle(MouseEvent event) {
            String selectedItem = view.getListView().getSelectionModel().getSelectedItem();
            Concept selectedConcept = new Concept(selectedItem);
            List<Concept> conceptInDef = concept.getDefinitions().iterator().next().getDefinition();
            for (int i = 0; i < conceptInDef.size(); i++) {

                if (selectedConcept.equals(conceptInDef.get(i))) {
                    selectedConcept = conceptInDef.get(i);
                    /*only the concepts with alternative words will be showed*/
                    showConceptDetails(selectedConcept);
                }
            }
        }
    }
}