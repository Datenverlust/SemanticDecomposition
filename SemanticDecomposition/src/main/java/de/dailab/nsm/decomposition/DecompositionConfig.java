/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.dailab.nsm.decomposition;


/**
 * Created by faehndrich on 15.06.16.
 */
public class DecompositionConfig implements Cloneable {
    static int threadCount = 4;


    int cacheSize = 100;


    public static int getThreadCount() {
        return threadCount;
    }

    public void setThreadCount(int threadCount) {
        DecompositionConfig.threadCount = threadCount;
    }

    public int getCacheSize() {
        return cacheSize;
    }

    public void setCacheSize(int cacheSize) {
        this.cacheSize = cacheSize;
    }

}
