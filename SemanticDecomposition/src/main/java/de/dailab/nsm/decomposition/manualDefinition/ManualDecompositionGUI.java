/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 *//*


package de.dailab.nsm.decomposition.manualDefinition;

import de.dailab.nsm.decomposition.manualDefinition.input.InputController;
import de.dailab.nsm.decomposition.manualDefinition.model.PriamaryStage;
import javafx.application.Application;
import javafx.stage.Stage;

*/
/**
 * Created by Ghadh on 03.11.2015.
 *//*

public class ManualDecompositionGUI extends Application {

    public static InputController inputC = null;
    String word2decompost = null;

    public ManualDecompositionGUI(String word) {
        word2decompost = word;
    }

    public ManualDecompositionGUI(){
        super();
    }

    public static void showInputView() {
        inputC.show();
    }

    public static void main(String[] args) {

        launch(args);


    }

    @Override
    public void start(Stage primaryStage) {

        PriamaryStage.getInstance().setPrimaryStage(primaryStage);
        Stage Pstage = PriamaryStage.getInstance().getPrimaryStage();
        inputC = new InputController(Pstage);
        inputC.getInputView().getWord2compse().setText(word2decompost);
        showInputView();
    }

}
*/
