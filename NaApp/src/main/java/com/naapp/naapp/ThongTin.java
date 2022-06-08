package com.naapp.naapp;

import java.io.Serializable;

public class ThongTin implements Serializable {
    public String hoTen;
    public String pin;
    public ThongTin(String _hoTen) {
        hoTen = _hoTen;
    }
    public ThongTin(String _hoTen, String _pin) {
        hoTen = _hoTen;
        pin = _pin;
    }
}
