package com.naapp.naapp;

import java.io.Serializable;
import java.util.UUID;

public class ThongTin implements Serializable {
    public String hoTen;
    public String id;
    public byte[] avatar;
    public long soTien = 0;
    public ThongTin(String _hoTen) {
        hoTen = _hoTen;
        id = UUID.randomUUID().toString();
    }
    
    public ThongTin(String _hoTen, String _id) {
        hoTen = _hoTen;
        id = _id;
    }
    
    public ThongTin(ThongTin tt) {
        hoTen = tt.hoTen;
        id = tt.id;
        avatar = tt.avatar;
        soTien = tt.soTien;
    }
}
