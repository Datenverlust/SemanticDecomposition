/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.kimanufaktur.nsm.decomposition.manualDefinition.input;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Created by Ghadh on 03.11.2015.
 */
public class InputView {

    private final Label label4valid;
    private Scene scence;

    private GridPane grid;


    private LetterTextField word2compse;
    private final ComboBox comboBox;
    private Label wordType;
    private final Button decomposeButton;
    private VBox validBox;
    private VBox content;
    private Label confirm;

    public LetterTextField getWord2compse() {
        return word2compse;
    }

    public void setWord2compse(LetterTextField word2compse) {
        this.word2compse = word2compse;
    }

    public InputView() {

        grid = new GridPane();
        grid.setAlignment(Pos.TOP_LEFT);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));


        word2compse = new LetterTextField();
        word2compse.setMaxWidth(100);
        word2compse.setMaxHeight(100);

        wordType= new Label("select the word type");
        comboBox = new ComboBox();
        prepareComboBox();

        decomposeButton = new Button("multiThreadedDecompose");
        label4valid = new Label("Enter a concept");
        validBox = new VBox(10);
        confirm=new Label("");
        validBox.getChildren().addAll(label4valid, word2compse,wordType, comboBox,confirm);
        content = new VBox(10);
        content.getChildren().addAll(validBox);
        decomposeButton.setAlignment(Pos.CENTER_LEFT);
        content.getChildren().add(decomposeButton);
        grid.add(content, 0, 1);
        scence = new Scene(grid, 500, 500);

    }

    private void prepareComboBox() {
        comboBox.setPromptText("word type");
        comboBox.getItems().addAll("ADJECTIVE", "NOUN", "ADVERB", "PROPOSITION", "VERB");
    }


    public TextField getWord2decompse() {
        return word2compse;
    }

    public Button getDecomposeButton() {
        return decomposeButton;
    }

    public Label getLabel4valid() {
        return label4valid;
    }

    public ComboBox getComboBox() {
        return comboBox;
    }

    public Label getConfirm() {
        return confirm;
    }

    public void show(Stage stage) {
        stage.setTitle("semantic decomposition ");
        stage.setScene(scence);
        stage.show();
    }
}
