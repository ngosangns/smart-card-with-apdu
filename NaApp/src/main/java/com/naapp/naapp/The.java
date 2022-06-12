package com.naapp.naapp;

import java.io.Serializable;
import java.security.PublicKey;

public class The implements Serializable {
    public byte[] id; // ID riêng của mỗi thẻ
    public ThongTin thongTin; // Thông tin trên thẻ
    public String pin; // PIN của thẻ
    public PublicKey pubKeyRSA; // Public key của RSA dùng để xác thực thẻ

    public The(byte[] _id) {
        id = _id;
    }
}