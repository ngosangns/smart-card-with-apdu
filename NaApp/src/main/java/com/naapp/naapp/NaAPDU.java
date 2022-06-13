package com.naapp.naapp;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.Signature;
import java.security.spec.RSAPublicKeySpec;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import javax.crypto.Cipher;
import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import javax.smartcardio.TerminalFactory;
import org.apache.commons.lang.SerializationUtils;

public class NaAPDU {

    public static Card card;
    public static CardChannel channel;
    public static The theDangKetNoi;
    
    // Su dung de xac thuc cac hanh dong cua admin
    private static final byte[] adminCode = {(byte)0x09, (byte)0x08, (byte)0x02, (byte)0x00};

    // Bảng map INS
    private static final byte INS_ADMIN_MO_KHOA_THE = (byte)0x11;
    private static final byte INS_LAY_ID = (byte) 0x12;
    private static final byte INS_CHECK_INFO_EXIST = (byte) 0x13;
    private static final byte INS_TAO_DU_LIEU = (byte) 0x14;
    private static final byte INS_DANG_NHAP = (byte) 0x15;
    private static final byte INS_XOA_DU_LIEU = (byte) 0x16;
    private static final byte INS_CAP_NHAT_DU_LIEU = (byte) 0x17;
    private static final byte INS_GEN_RSA_KEYPAIR = (byte) 0x30;
    private static final byte INS_GET_RSA_PUBKEY = (byte) 0x31;
    private static final byte INS_RSA_SIGN = (byte) 0x35;

    // Trạng thái trả về
    public static final byte[] TRANG_THAI_THANH_CONG = {(byte) 0x90, (byte) 0x00};
    public static final byte[] TRANG_THAI_KHOA_THE = {(byte) 0x68, (byte) 0x81};

    public static final int boiSoAES = 16;

    public static void ketNoiThe(byte[] AID) throws Exception {
        kiemTraDangKetNoi();

        // Hiển thị danh sách các thiết bị đầu cuối có sẵn
        TerminalFactory factory = TerminalFactory.getDefault();
        List<CardTerminal> terminals = factory.terminals().list();

        // Kết nối đến thẻ đầu tiên
        CardTerminal terminal = terminals.get(0);
        card = terminal.connect("*");
        channel = card.getBasicChannel();

        // Gửi request chọn applet dựa theo AID
        byte[] header = {(byte) 0x00, (byte) 0xA4, (byte) 0x04, (byte) 0x00};
        APDUTraVe ketQua = guiAPDULenh(header, AID, 0);
        if (!(ketQua != null && Arrays.equals(TRANG_THAI_THANH_CONG, ketQua.status))) {
            dongKetNoi();
            throw new Exception("Có lỗi xảy ra khi kết nối");
        }

        // Gửi request lấy ID của thẻ
        header = new byte[]{(byte) 0x80, INS_LAY_ID, (byte) 0x00, (byte) 0x00};
        ketQua = guiAPDULenh(header, null, 1);
        if (!(ketQua != null && Arrays.equals(TRANG_THAI_THANH_CONG, ketQua.status))) {
            dongKetNoi();
            throw new Exception("Có lỗi xảy ra khi kết nối");
        }
        byte[] _id = ketQua.data;

        // Kiểm tra thẻ đã tồn tại trong kho chưa
        // Nếu đã tồn tại thì sử dụng thông tin thẻ đã lưu trong kho
        // Nếu chưa thì tạo thẻ mới
        The _theDaTonTai = QuanLyThe.layThe(_id);
        if (_theDaTonTai != null) {
            theDangKetNoi = _theDaTonTai;
        } else {
            QuanLyThe.themThe(new The(_id));
            theDangKetNoi = QuanLyThe.layThe(_id);
        }
    }

    public static boolean kiemTraTonTaiDuLieuTrongThe() throws Exception {
        kiemTraKhongKetNoi();

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

    public static void taoDuLieu(ThongTin tt, String _pin) throws Exception {
        kiemTraKhongKetNoi();

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
        theDangKetNoi.pubKeyRSA = factory.generatePublic(spec);

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

        theDangKetNoi.pin = _pin;
        theDangKetNoi.thongTin = tt;
        QuanLyThe.luuKhoThe();
    }

    public static void capNhatDuLieu(ThongTin tt, String _pin) throws Exception {
        xacThucThe();

        // Gửi thông tin qua applet
        byte[] ttBytes = pad(SerializationUtils.serialize(tt), boiSoAES);
        byte[] _oldPinBytes = pad(theDangKetNoi.pin.getBytes(), boiSoAES);
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

        theDangKetNoi.pin = _pin;
        theDangKetNoi.thongTin = tt;
        QuanLyThe.luuKhoThe();
    }

    public static void dangNhap(String _pin) throws Exception {
        xacThucThe();

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
                    byte[] _paddedPIN = pad(theDangKetNoi.pin.getBytes(), boiSoAES);
                    int _ttLen = ketQua.data.length - _paddedPIN.length;
                    byte[] _tt = new byte[_ttLen];
                    System.arraycopy(ketQua.data, _paddedPIN.length, _tt, 0, _ttLen);
                    theDangKetNoi.thongTin = (ThongTin) SerializationUtils.deserialize(_tt);
                    theDangKetNoi.pin = _pin;
                } catch (Exception e) {
                    throw new Exception("Mã PIN không đúng");
                }
            }
        } else {
            throw new Exception("Lỗi trạng thái hoặc mã PIN không đúng");
        }
    }

    public static void xoaDuLieu() throws Exception {
        kiemTraKhongKetNoi();

        // Gửi request
        byte[] header = {(byte) 0x80, INS_XOA_DU_LIEU, (byte) 0x00, (byte) 0x00};
        byte[] data = pad(theDangKetNoi.pin.getBytes(), boiSoAES);
        APDUTraVe ketQua = guiAPDULenh(header, data, 1);

        // Kiểm tra kết quả
        if (!Arrays.equals(TRANG_THAI_THANH_CONG, ketQua.status)) {
            throw new Exception("Có lỗi xảy ra khi xoá thông tin trên thẻ");
        }

        theDangKetNoi.thongTin = null;
        theDangKetNoi.pin = null;
        QuanLyThe.luuKhoThe();
    }

    // Tạo một chuỗi ngẫu nhiên và gửi nó cho applet ký bằng private key và trả về signature
    // Sau đó verify signature đó. Nếu đúng thì thẻ an toàn để kết nối với hệ thống
    public static void xacThucThe() throws Exception {
        kiemTraKhongKetNoi();

        if (theDangKetNoi.pubKeyRSA == null) {
            throw new Exception("Không có dữ liệu để xác thực thẻ");
        }

        // Tạo một instance của RSA sử dụng public key của thẻ đã tạo
        Cipher encryptCipher = Cipher.getInstance("RSA");
        encryptCipher.init(Cipher.ENCRYPT_MODE, theDangKetNoi.pubKeyRSA);

        // Tạo chuỗi ngẫu nhiên gửi đến thẻ để ký sau đó thẻ gửi lại chuỗi đã ký
        // Dùng public key RSA đã lưu trước đó để xác thực chuỗi đó có đúng là được ký bởi private key tương ứng không
        // Chuỗi ngẫu nhiên nên có độ dài là bội số của 16 (tốt nhất nên là 16)
        byte[] randomMessage = UUID.randomUUID().toString().substring(0, boiSoAES).getBytes();

        // Gửi chuỗi ngẫu nhiên cho applet để ký với private key
        byte[] header = new byte[]{(byte) 0x80, INS_RSA_SIGN, 0, 0};
        APDUTraVe ketQua = guiAPDULenh(header, randomMessage, 1);
        if (!Arrays.equals(TRANG_THAI_THANH_CONG, ketQua.status)) {
            throw new Exception("Có lỗi xảy ra khi xác thực RSA");
        }

        // Kiểm tra chữ ký của thẻ
        Signature sig = Signature.getInstance("SHA1WithRSA");
        sig.initVerify(theDangKetNoi.pubKeyRSA);
        sig.update(randomMessage);
        if (!sig.verify(ketQua.data)) {
            throw new Exception("Thẻ không được xác thực");
        }
    }
    
    public static void adminMoKhoaThe() throws Exception {
        kiemTraKhongKetNoi();
        
        // Gửi request
        byte[] header = {(byte) 0x80, INS_ADMIN_MO_KHOA_THE, (byte) 0x00, (byte) 0x00};
        byte[] data = adminCode;
        APDUTraVe ketQua = guiAPDULenh(header, data, 1);

        // Kiểm tra kết quả
        if (!Arrays.equals(TRANG_THAI_THANH_CONG, ketQua.status)) {
            throw new Exception("Có lỗi xảy ra khi mở khoá thẻ");
        }
    }

    public static APDUTraVe guiAPDULenh(byte[] header, byte[] data, int length) throws Exception {
        kiemTraKhongKetNoi();

        ResponseAPDU ketQua = channel.transmit(new CommandAPDU(header[0], header[1], header[2], header[3], data, length));

        // Parse kết quả trả về
        byte[] trangThai = {(byte) ketQua.getSW1(), (byte) ketQua.getSW2()};

        if (Arrays.equals(TRANG_THAI_KHOA_THE, trangThai)) {
            throw new Exception("Thẻ đã bị khoá");
        }

        return new APDUTraVe(trangThai, ketQua.getData());
    }

    public static void dongKetNoi() {
        try {
            kiemTraKhongKetNoi();
            card.disconnect(false);
            card = null;
            channel = null;
            theDangKetNoi = null;
        } catch (Exception e) {
        }
    }

    public static void kiemTraKhongKetNoi() throws Exception {
        if (card == null || channel == null) {
            throw new Exception("Không có kết nối đến thẻ");
        }
    }

    public static void kiemTraDangKetNoi() throws Exception {
        if (card != null || channel != null) {
            throw new Exception("Đang có kết nối khác đến thẻ");
        }
    }

    // Hàm hỗ trợ padding mảng byte theo độ dài đưa vào
    public static byte[] pad(byte[] _arr, int _doDai) {
        int _paddedSize = (_arr.length % _doDai > 0) ? (_arr.length + _doDai) - (_arr.length % _doDai) : _arr.length;
        if (_paddedSize != _arr.length) {
            byte[] result = new byte[_paddedSize];
            System.arraycopy(_arr, 0, result, 0, _arr.length);
            return result;
        }
        return _arr;
    }
}
