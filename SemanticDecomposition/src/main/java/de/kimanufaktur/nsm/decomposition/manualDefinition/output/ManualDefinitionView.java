/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.kimanufaktur.nsm.decomposition.manualDefinition.output;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * Created by Ghadh on 03.11.2015.
 */
public class ManualDefinitionView  {

    private Text text;
    private Scene scence;
    private GridPane grid;
    private Button ok;


    private Label word;//it was Label instead of Text

    private TextArea descriptionInput;

    private VBox wordBox;
    private VBox defBox;

    private Button check;

    private HBox hbBtn;
    private VBox content;
    @FXML
    private  ListView<String> listView;
    ObservableList<String> observableList;

    private ListView listViewConcept;
    private ObservableList<RadioButton> observableListRadioButton;
    private HBox hBox;

    private Label labelForListView;
    private Label labelForListViewConcept;
    private Label confirm;
    private VBox boxForListView;
    private VBox boxForListViewConcept;
    private ToggleGroup tg ;

    public ManualDefinitionView() {
        super();
        grid = new GridPane();
        grid.setAlignment(Pos.TOP_LEFT);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        text = new Text("	Manual Definition	");

        word = new Label("");


        descriptionInput = new TextArea();
        descriptionInput.setMaxSize(400, 100);
        wordBox = new VBox();
        defBox = new VBox();

        check = new Button("check");
        ok=new Button("ok ");
        confirm= new Label();
        observableList = FXCollections.observableArrayList();
        listView=new ListView<String>(observableList);
        listView.setMaxSize(200,200);
        listView.setVisible(false);
        observableListRadioButton = FXCollections.observableArrayList();
        listViewConcept = new ListView(observableListRadioButton);
        listViewConcept.setMaxSize(200, 200);
        listViewConcept.setVisible(false);

        labelForListView= new Label(" words in the definition");
        labelForListViewConcept = new Label("suggested words: ");
        labelForListView.setVisible(false);
        labelForListViewConcept.setVisible(false);

        boxForListView = new VBox(5);
        boxForListViewConcept= new VBox(5);

        boxForListView.getChildren().addAll(labelForListView,listView);
        boxForListViewConcept.getChildren().addAll(labelForListViewConcept, listViewConcept);

        hbBtn= new HBox(5);
        hBox=new HBox(10);
        hBox.getChildren().addAll(boxForListView,boxForListViewConcept);

        content = new VBox(5);

        text.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));

        wordBox.getChildren().addAll(word);
        defBox.getChildren().addAll(descriptionInput);
        hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
        hbBtn.getChildren().addAll(check, ok);
        tg = new ToggleGroup();
        content.getChildren().addAll(wordBox, defBox, hbBtn,confirm, hBox);

        grid.add(text, 0, 0, 2, 1);
        grid.add(content, 0, 1);
        scence = new Scene(grid,500,500);


    }

    public void show(Stage stage){
        stage.setTitle("manual definition");
      stage.setScene(scence);
        stage.showAndWait();
    }

    public ListView<String> getListView() {
        return listView;
    }

    public ListView getListViewConcept() {
        return listViewConcept;
    }

    public void setListViewConcept(ListView listViewConcept) {
        listViewConcept = listViewConcept;
    }

    public Button getCheck() {
        return check;
    }

    public Button getOk() {
        return ok;
    }
    public Label getWord() {
        return word;
    }

    public TextArea getDescriptionInput() {
        return descriptionInput;
    }

    public Label getLabelForListView() {
        return labelForListView;
    }

    public Label getLabelForListViewConcept() {
        return labelForListViewConcept;
    }

    public Label getConfirm() {
        return confirm;
    }

    public ObservableList<String> getObservableList() {
        return observableList;
    }

    public ObservableList<RadioButton> getObservableListRadioButton() {
        return observableListRadioButton;
    }

    public ToggleGroup getTg() {
        return tg;
    }
}
