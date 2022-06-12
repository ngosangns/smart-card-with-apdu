package com.naapp.naapp;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class NaApp {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException ex) {
        }

        NaAppScreen f = new NaAppScreen();
        f.manHinhKetNoiDenThe();
    }
}
