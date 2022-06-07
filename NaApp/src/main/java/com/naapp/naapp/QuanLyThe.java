package com.naapp.naapp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class QuanLyThe {
    private List<The> khoThe = new ArrayList<The>();
    public QuanLyThe() {
    }
    
    public void themThe(The the) {
        khoThe.add(the);
    }
    
    public The layThe(byte[] AID) {
        int index = 0;
        for (The item : khoThe) {
            if(Arrays.equals(item.AID, AID)){
                return item;
            }
            index++;
        }
        return null;
    }
    
    public void xoaThe(byte[] AID) {
        int index = 0;
        for (The item : khoThe) {
            if(Arrays.equals(item.AID, AID)) {
                khoThe.remove(index);
                break;
            }
            index++;
        }
    }
}
