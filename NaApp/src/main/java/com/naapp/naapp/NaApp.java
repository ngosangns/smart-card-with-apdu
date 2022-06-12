package com.naapp.naapp;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class NaApp {

    public static void main(String[] args) throws Exception {
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException ex) {
        }
        
        // Load dữ liệu thẻ đã lưu trên máy
        try {
            QuanLyThe.loadKhoThe();
        } catch(Exception e) {
            JOptionPane.showMessageDialog(null, new JLabel("Có lỗi xảy ra khi load dữ liệu", JLabel.CENTER), "Lỗi", JOptionPane.PLAIN_MESSAGE);
            System.exit(0);
        }
        
        NaAppScreen f = new NaAppScreen();
        f.manHinhKetNoiDenThe();
    }
}
