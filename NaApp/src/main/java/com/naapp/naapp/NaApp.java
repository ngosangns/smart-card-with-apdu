package com.naapp.naapp;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class NaApp {

    public static void main(String[] args) throws Exception {
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException ex) {
        }
        
        QuanLyThe.loadKhoThe();

        NaAppScreen f = new NaAppScreen();
        f.manHinhKetNoiDenThe();
    }
}
