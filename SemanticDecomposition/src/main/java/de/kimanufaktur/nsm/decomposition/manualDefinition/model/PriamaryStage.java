/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.kimanufaktur.nsm.decomposition.manualDefinition.model;

import javafx.stage.Stage;

/**
 * Created by Ghadh on 10.11.2015.
 */
public class PriamaryStage {

    //single pattern, allow once to instance

    private static PriamaryStage instance;
    private Stage primaryStage;

    private PriamaryStage() {

    }

    public synchronized static PriamaryStage getInstance()
    {
        if (instance == null)
        {
            instance = new PriamaryStage();
        }
        return instance;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }
}
