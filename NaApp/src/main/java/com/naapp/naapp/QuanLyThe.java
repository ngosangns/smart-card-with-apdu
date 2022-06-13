package com.naapp.naapp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class QuanLyThe implements Serializable {

    public static byte[] AID = {(byte) 0x11, (byte) 0x22, (byte) 0x33, (byte) 0x44, (byte) 0x55, (byte) 0x66}; // AID của loại thẻ này
    private static final String duongDanLuuKhoThe = System.getenv("USERPROFILE") + "\\Documents\\kho_the_data";
    private static List<The> khoThe = new ArrayList<>(); // Kho thẻ

    public static void themThe(The the) throws Exception {
        khoThe.add(the);
        luuKhoThe();
    }

    public static The layThe(byte[] _id) {
        int index = 0;
        for (The item : khoThe) {
            if (Arrays.equals(item.id, _id)) {
                return item;
            }
            index++;
        }
        return null;
    }

    public static The layTheDauTien() {
        if (!khoThe.isEmpty()) {
            return khoThe.get(0);
        }
        return null;
    }

    public static void xoaThe(byte[] _id) throws Exception {
        int index = 0;
        for (The item : khoThe) {
            if (Arrays.equals(item.id, _id)) {
                khoThe.remove(index);
                break;
            }
            index++;
        }
        luuKhoThe();
    }

    public static void luuKhoThe() throws Exception {
        ByteArrayOutputStream _bos = new ByteArrayOutputStream();
        ObjectOutputStream _oos = new ObjectOutputStream(_bos);
        _oos.writeObject(khoThe);
        byte[] _bytes = _bos.toByteArray();

        try ( FileOutputStream stream = new FileOutputStream(duongDanLuuKhoThe)) {
            stream.write(_bytes);
        }
    }

    public static void loadKhoThe() throws Exception {
        try {
            File _f = new File(duongDanLuuKhoThe);
            // Kiểm tra file lưu dữ liệu có tồn tại chưa
            // Nếu có thì load dữ liệu kho thẻ từ file này
            if (_f.exists() && !_f.isDirectory()) {
                byte[] _bytes = Files.readAllBytes(_f.toPath());

                try ( ObjectInputStream _ois = new ObjectInputStream(new ByteArrayInputStream(_bytes))) {
                    List<The> _khoThe = (ArrayList<The>) _ois.readObject();
                    QuanLyThe.khoThe = _khoThe;
                }
            }
        } catch (Exception ex) {
            try {
                File f = new File(duongDanLuuKhoThe);
                f.delete();
            } catch (Exception e) {
            }
            throw new Exception("Có lỗi xảy ra khi load dữ liệu. Vui lòng khởi động lại.");
        }
    }
}
