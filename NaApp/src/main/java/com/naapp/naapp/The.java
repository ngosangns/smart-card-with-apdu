package com.naapp.naapp;

import java.util.Arrays;
import java.util.List;
import javax.smartcardio.*;
import org.apache.commons.lang.SerializationUtils;

public class The {

    public byte[] AID;
    Card card;
    CardChannel channel;
    ThongTin thongTin;

    public static final byte[] TRANG_THAI_THANH_CONG = {(byte) 0x90, (byte) 0x00};
    
    // Bảng map INS
    private static final byte INS_CHECK_INFO_EXIST         = (byte)0x13;
    private static final byte INS_TAO_DU_LIEU              = (byte)0x14;
    private static final byte INS_DANG_NHAP                = (byte)0x15;

    public The(byte[] _AID) {
        AID = _AID;
    }

    public boolean ketNoi() throws Exception {
        // Hiển thị danh sách các thiết bị đầu cuối có sẵn
        TerminalFactory factory = TerminalFactory.getDefault();
        List<CardTerminal> terminals = factory.terminals().list();

        // Kết nối đến thẻ đầu tiên
        CardTerminal terminal = terminals.get(0);
        card = terminal.connect("*");
        channel = card.getBasicChannel();

        // Gửi request chọn applet dựa theo AID
        byte[] testHeader = {(byte)0x00, (byte)0xA4, (byte)0x04, (byte)0x00};
        byte[] data = AID;
        APDUTraVe ketQua = guiAPDULenh(testHeader, data, 0);

        // Kiểm tra kết quả
        if(ketQua != null) {
            if (Arrays.equals(TRANG_THAI_THANH_CONG, ketQua.status)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean kiemTraTonTaiDuLieuTrongThe() throws Exception {
        // Gửi request
        byte[] testHeader = {(byte)0x80, INS_CHECK_INFO_EXIST, (byte)0x00, (byte)0x00};
        byte[] data = {(byte)0x00};
        APDUTraVe ketQua = guiAPDULenh(testHeader, data, 1);

        // Kiểm tra kết quả
        if(ketQua == null) throw new Exception("Có lỗi xảy ra");
        if (Arrays.equals(TRANG_THAI_THANH_CONG, ketQua.status))
            return ketQua.data[0] != (byte)0x00;
        return false;
    }
    
    public boolean taoDuLieu(ThongTin tt) throws Exception {
        // Tạo dữ liệu gửi qua applet
        byte[] ttBytes = SerializationUtils.serialize(tt);
        byte[] pinBytes = tt.pin.getBytes();
        byte[] data = new byte[tt.pin.getBytes().length + ttBytes.length + 1];
        data[0] = (byte)pinBytes.length;
        System.arraycopy(pinBytes, 0, data, 1, pinBytes.length);
        System.arraycopy(ttBytes, 0, data, pinBytes.length + 1, ttBytes.length);
        
        // Gửi request
        byte[] header = {(byte)0x80, INS_TAO_DU_LIEU, (byte)0x00, (byte)0x00};
        APDUTraVe ketQua = guiAPDULenh(header, data, 1);

        // Kiểm tra kết quả
        if(ketQua == null) throw new Exception("Có lỗi xảy ra");
        if (Arrays.equals(TRANG_THAI_THANH_CONG, ketQua.status))
            return ketQua.data[0] != (byte)0x00;
        return false;
    }
    
    public void dangNhap(String pin) throws Exception {
        thongTin = null;
        
        // Gửi request
        byte[] testHeader = {(byte)0x80, INS_DANG_NHAP, (byte)0x00, (byte)0x00};
        byte[] data = pin.getBytes();
        APDUTraVe ketQua = guiAPDULenh(testHeader, data, 1);

        // Kiểm tra kết quả
        if (Arrays.equals(TRANG_THAI_THANH_CONG, ketQua.status)) {
            if(ketQua.data.length == 1 && ketQua.data[0] == (byte)0x00) {
                throw new Exception("Mã PIN không đúng");
            } else if(ketQua.data.length == 1 && ketQua.data[0] == (byte)0x02) {
                throw new Exception("Thẻ đã bị khoá");
            } else if(ketQua.data.length > 1) {
                thongTin = (ThongTin)SerializationUtils.deserialize(ketQua.data);
            }
        } else {
            throw new Exception("Lỗi trạng thái");
        }
    }

    public APDUTraVe guiAPDULenh(byte[] header, byte[] data, int length) throws Exception {
        try {
            ResponseAPDU ketQua = channel.transmit(new CommandAPDU(header[0], header[1], header[2], header[3], data, length));
            // Kiểm tra trạng thái
            byte[] trangThai = {(byte) ketQua.getSW1(), (byte) ketQua.getSW2()};
            return new APDUTraVe(trangThai, ketQua.getData());
        } catch (CardException e) {
            throw new Exception("Lỗi gửi dữ liệu");
        }
    }

    public void dongKetNoi() throws CardException {
        card.disconnect(false);
    }
}
