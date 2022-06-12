package com.naapp.naapp;

import java.util.ArrayList;
import java.util.List;

public class QuanLyThe {

    private final List<The> khoThe = new ArrayList<>();

    public QuanLyThe() {
    }

    public void themThe(The the) {
        khoThe.add(the);
    }

    public The layThe(String _id) {
        int index = 0;
        for (The item : khoThe) {
            if (item.id.equals(_id)) {
                return item;
            }
            index++;
        }
        return null;
    }
    
    public The layTheDauTien() {
        if(!khoThe.isEmpty()) {
            return khoThe.get(0);
        }
        return null;
    }

    public void xoaThe(String _id) {
        int index = 0;
        for (The item : khoThe) {
            if (item.id.equals(_id)) {
                khoThe.remove(index);
                break;
            }
            index++;
        }
    }
}
