/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.dailab.nsm.decomposition.dictionaries.wikidata;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class TestHelpers {
    public static Field mockPrivateField(Object described_instance, String fieldName, Object value) {
        try {
            Field field = described_instance.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(described_instance, value);
            return field;
        } catch (Exception ignored) {}
        return null;
    }

    public static Method makePrivateMethodAccessible(Object described_instance, String methodName) {
        try {
            Method method = described_instance.getClass().getDeclaredMethod(methodName);
            method.setAccessible(true);
            return method;
        } catch (Exception ignored) {}
        return null;
    }
}
