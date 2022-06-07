package com.naapp.naapp;

public class APDUTraVe {
    byte status1;
    byte status2;
    byte[] status;
    byte[] data;
    
    public APDUTraVe(byte[] _status, byte[] _data) {
        status1 = _status[0];
        status2 = _status[1];
        status = _status;
        data = _data;
    }
}