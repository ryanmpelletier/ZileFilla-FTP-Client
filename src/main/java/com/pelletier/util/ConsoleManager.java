package com.pelletier.util;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * Created by ryanb on 4/3/2016.
 *
 * I would like any of my components to be able to use this to write to the console.
 *
 */
public class ConsoleManager {

    private static BooleanProperty hasNewText = new SimpleBooleanProperty();

    private static String newText;

    private static ConsoleManager consoleInstance = new ConsoleManager();

    public static ConsoleManager getInstance() {
        return consoleInstance;
    }

    private ConsoleManager() {
    }

    public static void writeText(String text){
        newText = text;
        ConsoleManager.hasNewText.setValue(true);
    }

    public static boolean getHasNewText() {
        return hasNewText.get();
    }

    public static BooleanProperty hasNewTextProperty() {
        return hasNewText;
    }

    public static void setHasNewText(boolean hasNewText) {
        ConsoleManager.hasNewText.set(hasNewText);
    }

    public static String getNewText() {
        ConsoleManager.hasNewText.setValue(false);
        return newText;
    }
}
