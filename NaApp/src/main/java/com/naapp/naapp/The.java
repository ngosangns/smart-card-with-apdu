package com.naapp.naapp;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import javax.smartcardio.*;
import org.apache.commons.lang.SerializationUtils;
import java.security.*;
import java.security.spec.RSAPublicKeySpec;
import java.util.UUID;
import javax.crypto.Cipher;

public class The {

    public String id;
    Card card;
    CardChannel channel;
    ThongTin thongTin;
    String pin;
    public int boiSoAES = 16;
    PublicKey pubKeyRSA;
    boolean daXacThuc;

    public static final byte[] TRANG_THAI_THANH_CONG = {(byte) 0x90, (byte) 0x00};
    public static final byte[] TRANG_THAI_KHOA_THE = {(byte) 0x68, (byte) 0x81};

    // Bảng map INS
    private static final byte INS_CHECK_INFO_EXIST = (byte) 0x13;
    private static final byte INS_TAO_DU_LIEU = (byte) 0x14;
    private static final byte INS_DANG_NHAP = (byte) 0x15;
    private static final byte INS_XOA_DU_LIEU = (byte) 0x16;
    private static final byte INS_CAP_NHAT_DU_LIEU = (byte) 0x17;

    // Thu vien RSA
    private static final byte INS_GEN_RSA_KEYPAIR = (byte) 0x30;
    private static final byte INS_GET_RSA_PUBKEY = (byte) 0x31;
    private static final byte INS_RSA_SIGN = (byte) 0x35;

    public The() {
        id = UUID.randomUUID().toString();
    }

    public void ketNoi() throws Exception {
        // Hiển thị danh sách các thiết bị đầu cuối có sẵn
        TerminalFactory factory = TerminalFactory.getDefault();
        List<CardTerminal> terminals = factory.terminals().list();

        // Kết nối đến thẻ đầu tiên
        CardTerminal terminal = terminals.get(0);
        card = terminal.connect("*");
        channel = card.getBasicChannel();

        // Gửi request chọn applet dựa theo AID
        byte[] testHeader = {(byte) 0x00, (byte) 0xA4, (byte) 0x04, (byte) 0x00};
        APDUTraVe ketQua = guiAPDULenh(testHeader, null, 0);

        // Kiểm tra kết quả
        if (!(ketQua != null && Arrays.equals(TRANG_THAI_THANH_CONG, ketQua.status))) {
            throw new Exception("Có lỗi xảy ra khi kết nối");
        }
    }

    public boolean kiemTraTonTaiDuLieuTrongThe() throws Exception {
        // Gửi request
        byte[] testHeader = {(byte) 0x80, INS_CHECK_INFO_EXIST, (byte) 0x00, (byte) 0x00};
        byte[] data = {(byte) 0x00};
        APDUTraVe ketQua = guiAPDULenh(testHeader, data, 1);

        // Kiểm tra kết quả
        if (ketQua == null) {
            throw new Exception("Có lỗi xảy ra");
        }
        if (Arrays.equals(TRANG_THAI_THANH_CONG, ketQua.status)) {
            return ketQua.data[0] != (byte) 0x00;
        }
        return false;
    }

    public void taoDuLieu(ThongTin tt, String _pin) throws Exception {
        // Tạo RSA key pair và mã hoá private key sử dụng mã PIN làm khoá (AES)
        byte[] header = {(byte) 0x80, INS_GEN_RSA_KEYPAIR, 0, 0};
        byte[] paddedPIN = pad(_pin.getBytes(), boiSoAES); // Do dai PIN sau khi pad = boiSoAES => _pin.length <= boiSoAES (bat buoc)
        APDUTraVe ketQua = guiAPDULenh(header, paddedPIN, 0);
        if (!Arrays.equals(TRANG_THAI_THANH_CONG, ketQua.status)) {
            throw new Exception("Có lỗi xảy ra khi tạo mã khoá RSA");
        }

        // Lấy ra modulus của public key của RSA
        header = new byte[]{(byte) 0x80, INS_GET_RSA_PUBKEY, 0, 0};
        ketQua = guiAPDULenh(header, null, 128);
        if (!Arrays.equals(TRANG_THAI_THANH_CONG, ketQua.status)) {
            throw new Exception("Có lỗi xảy ra khi lấy modulus public key của RSA");
        }
        byte[] modulus = ketQua.data;

        // Lấy ra exponent của public key của RSA
        header = new byte[]{(byte) 0x80, INS_GET_RSA_PUBKEY, 1, 0};
        ketQua = guiAPDULenh(header, null, 128);
        if (!Arrays.equals(TRANG_THAI_THANH_CONG, ketQua.status)) {
            throw new Exception("Có lỗi xảy ra khi lấy exponent public key của RSA");
        }
        byte[] exponent = ketQua.data;

        // Tạo và lưu public key từ modulus và exponent
        BigInteger modulusAsBigInt = new BigInteger(1, modulus);
        BigInteger exponentAsBigInt = new BigInteger(1, exponent);
        RSAPublicKeySpec spec = new RSAPublicKeySpec(modulusAsBigInt, exponentAsBigInt);
        KeyFactory factory = KeyFactory.getInstance("RSA");
        pubKeyRSA = factory.generatePublic(spec);

        // Gửi thông tin chủ thẻ qua applet
        byte[] ttBytes = pad(SerializationUtils.serialize(tt), boiSoAES);
        byte[] data = new byte[paddedPIN.length + ttBytes.length];
        System.arraycopy(paddedPIN, 0, data, 0, paddedPIN.length);
        System.arraycopy(ttBytes, 0, data, paddedPIN.length, ttBytes.length);

        header = new byte[]{(byte) 0x80, INS_TAO_DU_LIEU, (byte) 0x00, (byte) 0x00};
        ketQua = guiAPDULenh(header, data, 0);

        if (!(Arrays.equals(TRANG_THAI_THANH_CONG, ketQua.status))) {
            throw new Exception("Có lỗi xảy ra khi tạo dữ liệu");
        }

        pin = _pin;
        thongTin = tt;
    }

    public void capNhatDuLieu(ThongTin tt, String _pin) throws Exception {
        // Gửi thông tin qua applet
        byte[] ttBytes = pad(SerializationUtils.serialize(tt), boiSoAES);
        byte[] _oldPinBytes = pad(pin.getBytes(), boiSoAES);
        byte[] _pinBytes = pad(_pin.getBytes(), boiSoAES);
        byte[] data = new byte[_pinBytes.length + _oldPinBytes.length + ttBytes.length];
        System.arraycopy(_oldPinBytes, 0, data, 0, _oldPinBytes.length);
        System.arraycopy(_pinBytes, 0, data, _oldPinBytes.length, _pinBytes.length);
        System.arraycopy(ttBytes, 0, data, _oldPinBytes.length + _pinBytes.length, ttBytes.length);

        byte[] header = new byte[]{(byte) 0x80, INS_CAP_NHAT_DU_LIEU, (byte) 0x00, (byte) 0x00};
        APDUTraVe ketQua = guiAPDULenh(header, data, 1);

        if (!Arrays.equals(TRANG_THAI_THANH_CONG, ketQua.status)) {
            throw new Exception("Có lỗi xảy ra khi cập nhật dữ liệu");
        }

        pin = _pin;
        thongTin = tt;
    }

    public void dangNhap(String _pin) throws Exception {
        xacThucKetNoi();

        thongTin = null;

        // Gửi request
        byte[] header = {(byte) 0x80, INS_DANG_NHAP, (byte) 0x00, (byte) 0x00};
        byte[] data = pad(_pin.getBytes(), boiSoAES);
        APDUTraVe ketQua = guiAPDULenh(header, data, 1);

        // Kiểm tra kết quả
        if (Arrays.equals(TRANG_THAI_THANH_CONG, ketQua.status)) {
            if (ketQua.data.length == 1 && ketQua.data[0] == (byte) 0x00) {
                throw new Exception("Mã PIN không đúng");
            } else if (ketQua.data.length == 1 && ketQua.data[0] == (byte) 0x02) {
                throw new Exception("Thẻ đã bị khoá");
            } else if (ketQua.data.length > 1) {
                try {
                    // Du lieu tra ve la PIN + thongTin.
                    // Do do can phai tach PIN ra khoi du lieu tra ve => ta se co thong tin chu the
                    byte[] _paddedPIN = pad(pin.getBytes(), boiSoAES);
                    int _ttLen = ketQua.data.length - _paddedPIN.length;
                    byte[] _tt = new byte[_ttLen];
                    System.arraycopy(ketQua.data, _paddedPIN.length, _tt, 0, _ttLen);
                    thongTin = (ThongTin) SerializationUtils.deserialize(_tt);
                    pin = _pin;
                } catch (Exception e) {
                    throw new Exception("Mã PIN không đúng");
                }
            }
        } else {
            throw new Exception("Lỗi trạng thái hoặc mã PIN không đúng");
        }
    }

    public void xoaDuLieu() throws Exception {
        xacThucKetNoi();

        // Gửi request
        byte[] header = {(byte) 0x80, INS_XOA_DU_LIEU, (byte) 0x00, (byte) 0x00};
        byte[] data = pad(pin.getBytes(), boiSoAES);
        APDUTraVe ketQua = guiAPDULenh(header, data, 1);

        // Kiểm tra kết quả
        if (!Arrays.equals(TRANG_THAI_THANH_CONG, ketQua.status)) {
            throw new Exception("Có lỗi xảy ra khi xoá thông tin trên thẻ");
        }

        thongTin = null;
        pin = null;
    }

    public APDUTraVe guiAPDULenh(byte[] header, byte[] data, int length) throws Exception {
        // Gửi lệnh APDU
        ResponseAPDU ketQua = channel.transmit(new CommandAPDU(header[0], header[1], header[2], header[3], data, length));

        // Parse kết quả trả về
        byte[] trangThai = {(byte) ketQua.getSW1(), (byte) ketQua.getSW2()};

        if (Arrays.equals(TRANG_THAI_KHOA_THE, trangThai)) {
            throw new Exception("Thẻ đã bị khoá");
        }

        return new APDUTraVe(trangThai, ketQua.getData());
    }

    // Tạo một chuỗi ngẫu nhiên và gửi nó cho applet ký bằng private key và trả về signature
    // Sau đó verify signature đó. Nếu đúng thì thẻ an toàn để kết nối với hệ thống
    private void xacThucKetNoi() throws Exception {
        // Tạo một instance của RSA sử dụng public key của thẻ đã tạo
        Cipher encryptCipher = Cipher.getInstance("RSA");
        encryptCipher.init(Cipher.ENCRYPT_MODE, pubKeyRSA);

        // Tạo chuỗi ngẫu nhiên (tạm sử dụng chính mã PIN)
        // chuỗi ngẫu nhiên nên có độ dài là bội số của 16 (tốt nhất nên là 16)
        // Gửi mã PIN qua applet vừa có chức năng giải mã private key vừa có
        // chức năng ký vào mã PIN để gửi lại cho hệ thống verify
        byte[] randomMessage = pad(pin.getBytes(), boiSoAES);

        // Gửi chuỗi ngẫu nhiên cho applet để ký với private key
        byte[] header = new byte[]{(byte) 0x80, INS_RSA_SIGN, 0, 0};
        APDUTraVe ketQua = guiAPDULenh(header, randomMessage, 1);
        if (!Arrays.equals(TRANG_THAI_THANH_CONG, ketQua.status)) {
            throw new Exception("Có lỗi xảy ra khi xác thực RSA");
        }

        // Kiểm tra chữ ký của thẻ
        Signature sig = Signature.getInstance("SHA1WithRSA");
        sig.initVerify(pubKeyRSA);
        sig.update(randomMessage);
        daXacThuc = sig.verify(ketQua.data);

        if (!daXacThuc) {
            throw new Exception("Xác thực RSA thất bại");
        }
    }

    public void dongKetNoi() throws CardException {
        card.disconnect(false);
    }

    public byte[] pad(byte[] _arr, int _doDai) {
        int _paddedSize = (_arr.length % _doDai > 0) ? (_arr.length + _doDai) - (_arr.length % _doDai) : _arr.length;
        if (_paddedSize != _arr.length) {
            byte[] result = new byte[_paddedSize];
            System.arraycopy(_arr, 0, result, 0, _arr.length);
            return result;
        }
        return _arr;
    }
}
