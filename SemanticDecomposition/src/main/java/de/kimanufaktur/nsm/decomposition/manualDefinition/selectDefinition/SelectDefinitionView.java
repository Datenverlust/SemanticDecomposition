/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.kimanufaktur.nsm.decomposition.manualDefinition.selectDefinition;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * Created by Ghadh on 08.11.2015.
 */
public class SelectDefinitionView {
    private GridPane grid;
    private Label label;
    private Label label2;
    private ToggleGroup toggleGroup;
    private ObservableList<RadioButton> observableListRadiobutton;
    private Button ok ;
    private Button manuelDef;
    private VBox box;
    private ListView listView;
    private Scene sc;
    private Stage stage;

    public SelectDefinitionView() {

        grid= new GridPane();

        label=new Label();
        label2= new Label();

        box= new VBox();
        observableListRadiobutton = FXCollections.observableArrayList();
        ok= new Button("ok");
        manuelDef= new Button("manuel definition");
        sc = new Scene(grid, 500, 500);
        toggleGroup = new ToggleGroup();
        listView = new ListView();

        grid.setAlignment(Pos.TOP_LEFT);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        box.setSpacing(15);
        box.getChildren().addAll(label2, label, listView, ok,manuelDef);
        listView.setPrefWidth(400);
        grid.add(box, 0, 1);

        stage=new Stage();
        stage.setScene(sc);
        stage.setTitle("choose one definition");
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent e) {
              //  e.consume();
            }
        });

    }

    public ToggleGroup getToggleGroup() {
        return toggleGroup;
    }

    public ListView getListView() {
        return listView;
    }

    public Button getOk() {
        return ok;
    }

    public ObservableList<RadioButton> getObservableListRadiobutton() {
        return observableListRadiobutton;
    }

    public Stage getStage() {
        return stage;
    }

    public Label getLabel() {
        return label;
    }

    public Label getLabel2() {
        return label2;
    }

    public Button getManuelDef() {
        return manuelDef;
    }
}
