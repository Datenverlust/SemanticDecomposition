/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.dailab.nsm.decomposition.manualDefinition.input;

import de.dailab.nsm.decomposition.Concept;
import de.dailab.nsm.decomposition.Decomposition;
import de.dailab.nsm.decomposition.WordType;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * Created by Ghadh on 03.11.2015.
 */
public class InputController {
    //view
    public InputView inputView;
    private Stage priamaryStage;
    private Decomposition decomposition;
    private WordType wordType;
    public InputView getInputView() {
        return inputView;
    }

    public void setInputView(InputView inputView) {
        this.inputView = inputView;
    }

    public WordType getWordType() {
        return wordType;
    }

    public void setWordType(WordType wordType) {
        this.wordType = wordType;
    }

    public Decomposition getDecomposition() {
        return decomposition;
    }

    public void setDecomposition(Decomposition decomposition) {
        this.decomposition = decomposition;
    }

    public Stage getPriamaryStage() {
        return priamaryStage;
    }

    public void setPriamaryStage(Stage priamaryStage) {
        this.priamaryStage = priamaryStage;
    }



    public InputController(Stage priamaryStage) {
        this.priamaryStage = priamaryStage;
        this.inputView = new InputView();
        this.decomposition = new Decomposition();
        Decomposition.init();
        inputView.getDecomposeButton().setOnAction(new DecomposeButtonEvent());
        inputView.getComboBox().valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue observable, String oldValue, String newValue) {
                wordType = WordType.getType(newValue);
            }
        });

    }



    public void show() {
        inputView.show(priamaryStage);
    }


    /**********************
     * Events
     **********************/

    /**
     * action handler for 'decomposeButton'
     */
    class DecomposeButtonEvent implements EventHandler<ActionEvent> {

        @Override
        public void handle(ActionEvent event) {
            inputView.getConfirm().setText("");
            String word = inputView.getWord2decompse().getText();
            if (word.isEmpty() && word.equals("")) {
                inputView.getConfirm().setText("please, input a word ");
                inputView.getConfirm().setTextFill(Color.RED);
            } else {
                if (wordType == null) {
                    inputView.getConfirm().setText("please, select the word type");
                    inputView.getConfirm().setTextFill(Color.RED);
                } else {
                    Concept concept = Decomposition.createConcept(word);

//                    if (Decomposition.getKnownConcept().containsKey(concept.hashCode()) ||
//                            Decomposition.getKnownConcept().containsValue(concept)) {
                    if(Decomposition.getKnownConcept(concept).getDecompositionlevel()>=0){
                        inputView.getConfirm().setText("the concept has already a decomposition!");
                        inputView.getConfirm().setTextFill(Color.RED);
                    } else {
                        concept = decomposition.multiThreadedDecompose(word, wordType, 2);
                        inputView.getConfirm().setText("the concept had been decomposed");
                        inputView.getConfirm().setTextFill(Color.GREEN);
                        //  Delegate.definedConcepts.put(concept.hashCode(), concept);
                    }
                }
            }
        }
    }


}