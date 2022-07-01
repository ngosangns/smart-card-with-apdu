package com.naapp.naapp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public class ThongTin implements Serializable {
    public String hoTen;
    public String id;
    public byte[] avatar;
    public long soTien = 0;
    public String maSoPhong;
    public String namSinh;
    public String soDienThoai;
    public LocalDateTime hanGuiXe;
    public String goiGuiXe; // "thang"/"quy"/null
    
    public static int doDaiPin = 6;
    public ThongTin(String _hoTen) {
        hoTen = _hoTen;
        id = UUID.randomUUID().toString();
    }
    
    public ThongTin(String _hoTen, String _id) {
        hoTen = _hoTen;
        id = _id;
    }
    
    public ThongTin(ThongTin tt) {
        id = tt.id;
        hoTen = tt.hoTen;
        avatar = tt.avatar;
        soTien = tt.soTien;
        namSinh = tt.namSinh;
        maSoPhong = tt.maSoPhong;
        soDienThoai = tt.soDienThoai;
        hanGuiXe = tt.hanGuiXe;
        goiGuiXe = tt.goiGuiXe;
    }
}
