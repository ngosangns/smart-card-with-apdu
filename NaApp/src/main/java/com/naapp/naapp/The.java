package com.naapp.naapp;

import java.util.Arrays;
import java.util.List;
import javax.smartcardio.*;
import org.apache.commons.lang.SerializationUtils;

public class The {

    public byte[] AID;
    public String host;
    public int port;

    Card card;
    CardChannel channel;

    public static final byte[] TRANG_THAI_THANH_CONG = {(byte) 0x90, (byte) 0x00};
    
    // Bảng map INS
    private static final byte INS_SET_AES_KEY              = (byte)0x10;
    private static final byte INS_SET_AES_ICV              = (byte)0x11;
    private static final byte INS_DO_AES_CIPHER            = (byte)0x12;
    private static final byte CHECK_INFO_EXIST             = (byte)0x13;
    private static final byte TAO_DU_LIEU                  = (byte)0x14;

    public The(byte[] _AID) {
        AID = _AID;
    }

    public boolean ketNoi() {
        try {
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
        } catch (CardException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean kiemTraTonTaiDuLieuTrongThe() throws Exception {
        // Gửi request
        byte[] testHeader = {(byte)0x80, CHECK_INFO_EXIST, (byte)0x00, (byte)0x00};
        byte[] data = {(byte)0x00};
        APDUTraVe ketQua = guiAPDULenh(testHeader, data, 1);

        // Kiểm tra kết quả
        if(ketQua == null) throw new Exception("Có lỗi xảy ra");
        if (Arrays.equals(TRANG_THAI_THANH_CONG, ketQua.status))
            return ketQua.data[0] != (byte)0x00;
        return false;
    }
    
    public boolean taoDuLieu(ThongTin tt) throws Exception {
        // Gửi request
        byte[] testHeader = {(byte)0x80, TAO_DU_LIEU, (byte)0x00, (byte)0x00};
        byte[] data = SerializationUtils.serialize(tt);
        APDUTraVe ketQua = guiAPDULenh(testHeader, data, 1);

        // Kiểm tra kết quả
        if(ketQua == null) throw new Exception("Có lỗi xảy ra");
        if (Arrays.equals(TRANG_THAI_THANH_CONG, ketQua.status))
            return ketQua.data[0] != (byte)0x00;
        return false;
    }

    public APDUTraVe guiAPDULenh(byte[] header, byte[] data, int length) {
        try {
            ResponseAPDU ketQua = channel.transmit(new CommandAPDU(header[0], header[1], header[2], header[3], data, length));
            // Kiểm tra trạng thái
            byte[] trangThai = {(byte) ketQua.getSW1(), (byte) ketQua.getSW2()};
            return new APDUTraVe(trangThai, ketQua.getData());
        } catch (CardException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean dongKetNoi() {
        try {
            card.disconnect(false);
            return true;
        } catch (CardException e) {
            e.printStackTrace();
            return false;
        }
    }
}
