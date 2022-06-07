package com.naapp.naapp;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class NaApp {
    public static void main(String[] args) {
        QuanLyThe quanLyThe = new QuanLyThe();
        byte[] AID = {(byte)0X11, (byte)0X22, (byte)0X33, (byte)0X44, (byte)0X55, (byte)0X66};
        The the1 = new The(AID);
        quanLyThe.themThe(the1);
        
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException ex) {
        }
        
        NaAppScreen f = new NaAppScreen(quanLyThe);
        f.manHinhKetNoiDenThe();
    }
}
