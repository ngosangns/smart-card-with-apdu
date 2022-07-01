package com.naapp.naapp;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.util.Locale;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import org.apache.commons.io.FileUtils;

public class NaAppScreen extends JFrame {

    private byte[] tempAvatar;
    private The the;

    public NaAppScreen() {
        super();

        // Cài đặt cho Jrame
        setSize(800, 600); // Kích thước
        setLayout(null); // Bố trí
        setTitle("NaApp"); // Tiêu đề
        setResizable(false); // Chặn người dùng thay đổi kích thước
        setLocationRelativeTo(null); // Xuất hiện ở giữa

        // Khi tắt frame sẽ huỷ kết nối thẻ (nếu có) và kết thúc chương trình
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                NaAPDU.dongKetNoi();
                System.exit(0);
            }
        });

        hienThi();
    }

    // Màn hình ----------------------------------------------------------------
    public void manHinhKetNoiDenThe(String _thongBao) {
        xoaManHinh();
        
        JLabel tieuDe2 = new JLabel("CHỦ THẺ", JLabel.CENTER);
        tieuDe2.setFont(new Font("sans-serif", Font.PLAIN, 20));
        tieuDe2.setSize(300, 40);
        tieuDe2.setLocation(250, 80);
        add(tieuDe2);

        JButton b1 = new JButton("Xác thực thẻ vào thang máy/toà nhà");
        b1.setBounds(85, 150, 300, 40);
        b1.addActionListener((ActionEvent e) -> {
            try {
                NaAPDU.ketNoiThe(QuanLyThe.AID);
                the = NaAPDU.theDangKetNoi;
                if (NaAPDU.kiemTraTonTaiDuLieuTrongThe()) {
                    NaAPDU.xacThucThe();
                    throw new Exception("Đã xác thực! Có thể ra vào thang máy/toà nhà");
                } else {
                    throw new Exception("Thẻ không có dữ liệu");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, new JLabel(ex.getMessage(), JLabel.CENTER), "Thông báo", JOptionPane.PLAIN_MESSAGE);
            } finally {
                NaAPDU.dongKetNoi();
                the = null;
            }
        });
        add(b1);

        JButton b2 = new JButton("Thanh toán phí gửi xe tháng (200.000 đồng)");
        b2.setBounds(85, 200, 300, 40);
        b2.addActionListener((ActionEvent e) -> {
            long phiGuiXe = 200 * 1000;
            try {
                NaAPDU.ketNoiThe(QuanLyThe.AID);
                the = NaAPDU.theDangKetNoi;
                if (NaAPDU.kiemTraTonTaiDuLieuTrongThe()) {
                    NaAPDU.xacThucThe();
                    long soTienConLai = the.thongTin.soTien - phiGuiXe;
                    if(soTienConLai >= 0) {
                        ThongTin _tt = new ThongTin(the.thongTin);
                        _tt.soTien = soTienConLai;
                        NaAPDU.capNhatDuLieu(_tt, the.pin);
                        throw new Exception("Đã thanh toán!");
                    }
                    throw new Exception("Thẻ không đủ tiền để thanh toán");
                } else {
                    throw new Exception("Thẻ không có dữ liệu");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, new JLabel(ex.getMessage(), JLabel.CENTER), "Thông báo", JOptionPane.PLAIN_MESSAGE);
            } finally {
                NaAPDU.dongKetNoi();
                the = null;
            }
        });
        add(b2);
        
        JButton b3 = new JButton("Xác thực vào hầm gửi xe");
        b3.setBounds(415, 150, 300, 40);
        b3.addActionListener((ActionEvent e) -> {
            try {
                NaAPDU.ketNoiThe(QuanLyThe.AID);
                the = NaAPDU.theDangKetNoi;
                if (NaAPDU.kiemTraTonTaiDuLieuTrongThe()) {
                    manHinhNhapMaPin(null, null);
                } else {
                    throw new Exception("Xác thực thất bại: Thẻ không có dữ liệu");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, new JLabel(ex.getMessage(), JLabel.CENTER), "Lỗi", JOptionPane.PLAIN_MESSAGE);
            } finally {
                NaAPDU.dongKetNoi();
                the = null;
            }
        });
        add(b3);
        
        JButton b4 = new JButton("Nạp tiền");
        b4.setBounds(415, 200, 300, 40);
        b4.addActionListener((ActionEvent e) -> {
            try {
                NaAPDU.ketNoiThe(QuanLyThe.AID);
                the = NaAPDU.theDangKetNoi;
                if (NaAPDU.kiemTraTonTaiDuLieuTrongThe()) {
                    manHinhNhapMaPin(null, "nap_tien");
                } else {
                    throw new Exception("Thẻ chưa có dữ liệu. Vui lòng liên hệ quản lý toà nhà để tạo thông tin cho thẻ");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, new JLabel(ex.getMessage(), JLabel.CENTER), "Lỗi", JOptionPane.PLAIN_MESSAGE);
                NaAPDU.dongKetNoi();
                the = null;
            }
        });
        add(b4);
        
        JButton b5 = new JButton("Thay đổi mã PIN");
        b5.setBounds(415, 250, 300, 40);
        b5.addActionListener((ActionEvent e) -> {
            try {
                NaAPDU.ketNoiThe(QuanLyThe.AID);
                the = NaAPDU.theDangKetNoi;
                if (NaAPDU.kiemTraTonTaiDuLieuTrongThe()) {
                    manHinhNhapMaPin(null, "cap_nhat_ma_pin");
                } else {
                    throw new Exception("Thẻ chưa có dữ liệu. Vui lòng liên hệ quản lý toà nhà để tạo thông tin cho thẻ");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, new JLabel(ex.getMessage(), JLabel.CENTER), "Lỗi", JOptionPane.PLAIN_MESSAGE);
                NaAPDU.dongKetNoi();
                the = null;
            }
        });
        add(b5);
        
        JLabel tieuDe = new JLabel("ADMIN", JLabel.CENTER);
        tieuDe.setFont(new Font("sans-serif", Font.PLAIN, 20));
        tieuDe.setSize(300, 40);
        tieuDe.setLocation(250, 330);
        add(tieuDe);
        
        JButton b = new JButton("Tạo/Cập nhật thông tin thẻ");
        b.setBounds(85, 400, 300, 40);
        b.addActionListener((ActionEvent e) -> {
            try {
                NaAPDU.ketNoiThe(QuanLyThe.AID);
                the = NaAPDU.theDangKetNoi;
                if (NaAPDU.kiemTraTonTaiDuLieuTrongThe()) {
                    manHinhNhapMaPin(null, null);
                } else {
                    manHinhYeuCauTaoDuLieuChoThe();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, new JLabel(ex.getMessage(), JLabel.CENTER), "Lỗi", JOptionPane.PLAIN_MESSAGE);
                NaAPDU.dongKetNoi();
                the = null;
            }
        });
        add(b);
        
        JButton adminMoKhoaTheButton = new JButton("Mở khoá thẻ");
        adminMoKhoaTheButton.setBounds(415, 400, 300, 40);
        adminMoKhoaTheButton.addActionListener((ActionEvent e) -> {
            try {
                NaAPDU.ketNoiThe(QuanLyThe.AID);
                NaAPDU.adminMoKhoaThe();
                JOptionPane.showMessageDialog(null, new JLabel("Đã mở khoá thẻ", JLabel.CENTER), "Thông báo", JOptionPane.PLAIN_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, new JLabel(ex.getMessage(), JLabel.CENTER), "Lỗi", JOptionPane.PLAIN_MESSAGE);
            } finally {
                NaAPDU.dongKetNoi();
                the = null;
            }
        });
        add(adminMoKhoaTheButton);
        
        if(_thongBao != null && _thongBao.length() > 0)
            JOptionPane.showMessageDialog(null, new JLabel(_thongBao, JLabel.CENTER), "Thông báo", JOptionPane.PLAIN_MESSAGE);

        veLai();
    }

    public void manHinhNhapMaPin(String _thongBao, String manHinhTiepTheo) {
        xoaManHinh();

        JLabel tieuDe = new JLabel("Nhập mã pin");
        tieuDe.setSize(200, 40);
        tieuDe.setLocation(300, 280);
        add(tieuDe);

        JTextField pinField = new JPasswordField();
        pinField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                // limit to `boiSoAES` length
                if (pinField.getText().length() >= ThongTin.doDaiPin) {
                    e.consume();
                }
            }
        });
        pinField.setSize(200, 30);
        pinField.setLocation(300, 320);
        add(pinField);

        JLabel thongBao = new JLabel("");
        thongBao.setSize(200, 40);
        thongBao.setLocation(300, 350);
        add(thongBao);

        JButton dangNhapButton = new JButton("Đăng nhập");
        dangNhapButton.setBounds(505, 319, 100, 32);
        dangNhapButton.addActionListener((ActionEvent e) -> {
            String pin = pinField.getText();
            try {
                NaAPDU.dangNhap(pin);
                if(manHinhTiepTheo != null) {
                    switch(manHinhTiepTheo) {
                        case "nap_tien":
                            manHinhNapTien();
                            break;
                        case "cap_nhat_ma_pin":
                            manHinhCapNhatMaPIN(null);
                            break;
                        default:
                            manHinhXemDuLieu(null);
                    }
                } else {
                    manHinhXemDuLieu(null);
                }
            } catch (Exception ex) {
                thongBao.setForeground(Color.red);
                thongBao.setText(ex.getMessage());
            }
        });
        add(dangNhapButton);

        themNutHuyKetNoiThe();

        if (_thongBao != null) {
            thongBao.setForeground(Color.blue);
            thongBao.setText(_thongBao);
        }

        veLai();
    }

    public void manHinhYeuCauTaoDuLieuChoThe() {
        xoaManHinh();

        JLabel tieuDe = new JLabel("Thẻ chưa có dữ liệu", JLabel.CENTER);
        tieuDe.setSize(200, 40);
        tieuDe.setLocation(300, 280);
        add(tieuDe);

        JButton taoDuLieuButton = new JButton("Tạo dữ liệu");
        taoDuLieuButton.setSize(200, 30);
        taoDuLieuButton.setLocation(300, 320);
        taoDuLieuButton.addActionListener((ActionEvent e) -> {
            manHinhTaoDuLieu(null);
        });
        add(taoDuLieuButton);

        themNutHuyKetNoiThe();

        veLai();
    }

    public void manHinhTaoDuLieu(ThongTin tt) {
        xoaManHinh();

        // Tiêu đề
        JLabel tieuDe = new JLabel(tt == null ? "Tạo dữ liệu cho thẻ" : "Cập nhật dữ liệu", JLabel.CENTER);
        tieuDe.setSize(400, 40);
        tieuDe.setLocation(200, 30);
        tieuDe.setFont(tieuDe.getFont().deriveFont(Font.BOLD));
        add(tieuDe);

        // Họ và tên
        JLabel hoVaTenLabel = new JLabel("Họ và tên (*)");
        hoVaTenLabel.setSize(200, 40);
        hoVaTenLabel.setLocation(300, 60);
        add(hoVaTenLabel);
        JTextField hoTenField = new JTextField();
        hoTenField.setSize(200, 30);
        hoTenField.setLocation(300, 90);
        add(hoTenField);

        // PIN
        JLabel pinLabel = new JLabel("Nhập mã PIN (*)");
        pinLabel.setSize(200, 40);
        pinLabel.setLocation(300, 120);
        add(pinLabel);
        JTextField pinField = new JTextField();
        pinField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (pinField.getText().length() >= ThongTin.doDaiPin) // limit to `boiSoAES` characters
                {
                    e.consume();
                }
            }
        });
        pinField.setSize(200, 30);
        pinField.setLocation(300, 150);
        add(pinField);

        // Số tiền
        JLabel soTienLabel = new JLabel("Số tiền (*)");
        soTienLabel.setSize(200, 40);
        soTienLabel.setLocation(300, 180);
        add(soTienLabel);
        JFormattedTextField soTienField = new JFormattedTextField(NumberFormat.getNumberInstance());
        soTienField.setSize(200, 30);
        soTienField.setLocation(300, 210);
        soTienField.setValue(0);
        add(soTienField);
        
        // Năm sinh
        JLabel namSinhLabel = new JLabel("Năm sinh");
        namSinhLabel.setSize(200, 40);
        namSinhLabel.setLocation(300, 240);
        add(namSinhLabel);
        JTextField namSinhField = new JTextField();
        namSinhField.setSize(200, 30);
        namSinhField.setLocation(300, 270);
        add(namSinhField);
        
        // Số phòng
        JLabel maSoPhongLabel = new JLabel("Mã số phòng");
        maSoPhongLabel.setSize(200, 40);
        maSoPhongLabel.setLocation(300, 300);
        add(maSoPhongLabel);
        JTextField maSoPhongField = new JTextField();
        maSoPhongField.setSize(200, 30);
        maSoPhongField.setLocation(300, 330);
        add(maSoPhongField);
        
        // Số điện thoại
        JLabel soDienThoaiLabel = new JLabel("Số điện thoại");
        soDienThoaiLabel.setSize(200, 40);
        soDienThoaiLabel.setLocation(300, 360);
        add(soDienThoaiLabel);
        JTextField soDienThoaiField = new JTextField();
        soDienThoaiField.setSize(200, 30);
        soDienThoaiField.setLocation(300, 390);
        add(soDienThoaiField);

        // Hình đại diện
        JLabel avatarLabel = new JLabel("Hình đại diện (*)");
        avatarLabel.setSize(200, 40);
        avatarLabel.setLocation(530, 60);
        add(avatarLabel);
        JButton avatarButton = new JButton("Chọn hình đại diện");
        avatarButton.setSize(200, 30);
        avatarButton.setLocation(530, 90);

        // Thêm panel cho hình đại diện
        JPanel avatarPreviewImagePanel = new JPanel();
        avatarPreviewImagePanel.setSize(200, 400);
        avatarPreviewImagePanel.setLocation(530, 120);
        add(avatarPreviewImagePanel);

        // Cài đặt hành động cho nút chọn hình đại diện
        avatarButton.addActionListener((ActionEvent e) -> {
            try {
                // Khung chọn file
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.addChoosableFileFilter(new ImageFilter());
                fileChooser.setAcceptAllFileFilterUsed(false);

                // Hành động người dùng chọn file
                int option = fileChooser.showOpenDialog(null);

                if (option == JFileChooser.APPROVE_OPTION) {
                    // Lấy thông tin hình ảnh và lưu vào biến tạm
                    File avatarFile = fileChooser.getSelectedFile();
                    tempAvatar = FileUtils.readFileToByteArray(avatarFile);

                    // Thêm hình ảnh vào panel
                    tempAvatar = themHinhAnhVaoPanel(tempAvatar, avatarPreviewImagePanel);
                    avatarButton.setText("Hình: " + avatarFile.getName());
                } else {
                    // Nếu là tạo dữ liệu thì sẽ xoá hình cũ đi nếu không
                    // chọn hình mới
                    if (tt == null) {
                        tempAvatar = null;
                        avatarButton.setText("Chọn hình đại diện");
                        avatarPreviewImagePanel.removeAll();
                        avatarPreviewImagePanel.revalidate();
                        avatarPreviewImagePanel.repaint();
                    }
                }
            } catch (IOException ex) {
                avatarButton.setText("Có lỗi xảy ra khi chọn hình");
            }
        });
        add(avatarButton);

        // Thông báo
        JLabel thongBao = new JLabel("", JLabel.CENTER);
        thongBao.setSize(200, 40);
        thongBao.setLocation(300, 420);
        add(thongBao);

        // Nút tạo dữ liệu
        JButton taoDuLieuButton = new JButton(tt == null ? "Tạo dữ liệu" : "Cập nhật");
        taoDuLieuButton.setSize(150, 30);
        taoDuLieuButton.setLocation(325, 460);
        taoDuLieuButton.addActionListener((ActionEvent e) -> {
            String _hoTen = hoTenField.getText();
            String _pin = pinField.getText();
            long _soTien = ((Number) soTienField.getValue()).longValue();
            String _soDienThoai = soDienThoaiField.getText();
            String _maSoPhong = maSoPhongField.getText();
            String _namSinh = namSinhField.getText();

            // Kiểm tra dữ liệu
            if (_hoTen.equals("")) {
                thongBao.setForeground(Color.red);
                thongBao.setText("Tên không được để trống");
                return;
            }
            if (_pin.equals("")) {
                thongBao.setForeground(Color.red);
                thongBao.setText("Mã PIN không được để trống");
                return;
            }
            if (_pin.length() != ThongTin.doDaiPin) {
                thongBao.setForeground(Color.red);
                thongBao.setText("Mã PIN phải có 6 ký tự");
                return;
            }
            if (_soTien < 0) {
                thongBao.setForeground(Color.red);
                thongBao.setText("Số tiền không được bỏ trống hoặc nhỏ hơn 0");
            }
            if (tempAvatar == null || tempAvatar.length == 0) {
                thongBao.setForeground(Color.red);
                thongBao.setText("Hình đại diện không được để trống");
                return;
            }

            try {
                if (tt == null) {
                    ThongTin _tt = new ThongTin(_hoTen);
                    _tt.avatar = tempAvatar;
                    _tt.soTien = _soTien;
                    _tt.namSinh = _namSinh;
                    _tt.maSoPhong = _maSoPhong;
                    _tt.soDienThoai = _soDienThoai;

                    NaAPDU.taoDuLieu(_tt, _pin);
                    manHinhNhapMaPin("Tạo dữ liệu thành công", null);
                } else {
                    ThongTin _tt = new ThongTin(_hoTen, tt.id);
                    _tt.avatar = tempAvatar;
                    _tt.soTien = _soTien;
                    _tt.namSinh = _namSinh;
                    _tt.maSoPhong = _maSoPhong;
                    _tt.soDienThoai = _soDienThoai;
                    NaAPDU.capNhatDuLieu(_tt, _pin);
                    manHinhXemDuLieu("Cập nhật dữ liệu thành công");
                }
            } catch (Exception ex) {
                thongBao.setForeground(Color.red);
                thongBao.setText(ex.getMessage());
            }
        });
        add(taoDuLieuButton);

        // Nút quay lại
        JButton quayLaiButton = new JButton("Quay lại");
        quayLaiButton.setSize(150, 30);
        quayLaiButton.setLocation(325, 490);
        quayLaiButton.addActionListener((ActionEvent e) -> {
            if (tt == null) {
                manHinhYeuCauTaoDuLieuChoThe();
            } else {
                manHinhXemDuLieu(null);
            }
        });
        add(quayLaiButton);

        // Đưa dữ liệu vào field nếu tồn tại
        tempAvatar = null;
        if (tt != null) {
            hoTenField.setText(tt.hoTen);
            soTienField.setValue(tt.soTien);
            maSoPhongField.setText(tt.maSoPhong);
            namSinhField.setText(tt.namSinh);
            soDienThoaiField.setText(tt.soDienThoai);
            // Lấy dữ liệu hình ảnh (nếu có)
            if (tt.avatar != null) {
                try {
                    tempAvatar = tt.avatar;
                    tempAvatar = themHinhAnhVaoPanel(tempAvatar, avatarPreviewImagePanel);
                } catch (IOException ex) {
                    thongBao.setForeground(Color.red);
                    thongBao.setText("Có lỗi xảy ra khi lấy dữ liệu hình ảnh");
                }
            }
        } else {
            hoTenField.setText("");
            soTienField.setValue(0);
            maSoPhongField.setText("");
            namSinhField.setText("");
            soDienThoaiField.setText("");
        }
        
        // Kiếm tra mã PIN, đưa dữ liệu vào nếu tồn tại
        if (the.pin != null) {
            pinField.setText(the.pin);
        } else {
            pinField.setText("");
        }

        veLai();
    }
    
    public void manHinhCapNhatMaPIN(ThongTin tt) {
        xoaManHinh();

        // Tiêu đề
        JLabel tieuDe = new JLabel("Nhập mã PIN mới cho thẻ", JLabel.CENTER);
        tieuDe.setSize(400, 40);
        tieuDe.setLocation(200, 30);
        tieuDe.setFont(tieuDe.getFont().deriveFont(Font.BOLD));
        add(tieuDe);

        // Mã PIN mới
        JLabel pinLabel = new JLabel("Nhập mã PIN mới");
        pinLabel.setSize(200, 40);
        pinLabel.setLocation(300, 60);
        add(pinLabel);
        JTextField pinField = new JPasswordField();
        pinField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (pinField.getText().length() >= ThongTin.doDaiPin) // limit to `boiSoAES` characters
                {
                    e.consume();
                }
            }
        });
        pinField.setSize(200, 30);
        pinField.setLocation(300, 90);
        add(pinField);
        
        // Xác nhận mã PIN mới
        JLabel xacNhanPinLabel = new JLabel("Xác nhận mã PIN mới");
        xacNhanPinLabel.setSize(200, 40);
        xacNhanPinLabel.setLocation(300, 120);
        add(xacNhanPinLabel);
        JTextField xacNhanPinField = new JPasswordField();
        xacNhanPinField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (xacNhanPinField.getText().length() >= ThongTin.doDaiPin) // limit to `boiSoAES` characters
                {
                    e.consume();
                }
            }
        });
        xacNhanPinField.setSize(200, 30);
        xacNhanPinField.setLocation(300, 150);
        add(xacNhanPinField);
        
        // Thông báo
        JLabel thongBao = new JLabel("", JLabel.CENTER);
        thongBao.setSize(200, 40);
        thongBao.setLocation(300, 180);
        add(thongBao);

        // Nút cập nhật PIN
        JButton capNhatPINButton = new JButton("Cập nhật mã PIN");
        capNhatPINButton.setSize(150, 30);
        capNhatPINButton.setLocation(325, 220);
        capNhatPINButton.addActionListener((ActionEvent e) -> {
            String _pin = pinField.getText();
            String _xacNhanPin = xacNhanPinField.getText();
            
            // Kiểm tra dữ liệu
            if (_pin.equals("")) {
                thongBao.setForeground(Color.red);
                thongBao.setText("PIN mới không được để trống");
                return;
            }
            if (_xacNhanPin.equals("")) {
                thongBao.setForeground(Color.red);
                thongBao.setText("PIN xác nhận không được để trống");
                return;
            }
            if (_pin.length() != ThongTin.doDaiPin) {
                thongBao.setForeground(Color.red);
                thongBao.setText("PIN mới phải có 6 ký tự");
                return;
            }
            if (_xacNhanPin.length() != ThongTin.doDaiPin) {
                thongBao.setForeground(Color.red);
                thongBao.setText("PIN xác nhận phải có 6 ký tự");
                return;
            }
            if(!_pin.equals(_xacNhanPin)) {
                thongBao.setForeground(Color.red);
                thongBao.setText("PIN xác nhận không đúng");
                return;
            }
            
            try {
                ThongTin _tt = new ThongTin(the.thongTin);
                NaAPDU.capNhatDuLieu(_tt, _pin);
                NaAPDU.dongKetNoi();
                the = null;
                manHinhKetNoiDenThe("Cập nhật mã PIN thành công");
            } catch (Exception ex) {
                thongBao.setForeground(Color.red);
                thongBao.setText(ex.getMessage());
            }
        });
        add(capNhatPINButton);
        
        themNutHuyKetNoiThe();

        veLai();
    }

    public void manHinhXemDuLieu(String _thongBao) {
        xoaManHinh();

        // Tiêu đề
        JLabel tieuDe = new JLabel("Thông tin chủ thẻ", JLabel.CENTER);
        tieuDe.setSize(400, 40);
        tieuDe.setLocation(200, 30);
        tieuDe.setFont(tieuDe.getFont().deriveFont(Font.BOLD));
        add(tieuDe);

        // ID
        JLabel idLabel = new JLabel("Mã chủ thẻ");
        idLabel.setSize(200, 40);
        idLabel.setLocation(300, 60);
        add(idLabel);
        JLabel idField = new JLabel(the.thongTin.id);
        idField.setSize(200, 30);
        idField.setLocation(300, 85);
        add(idField);

        // Họ và tên
        JLabel hoVaTenLabel = new JLabel("Họ và tên");
        hoVaTenLabel.setSize(200, 40);
        hoVaTenLabel.setLocation(300, 120);
        add(hoVaTenLabel);
        JLabel hoTenField = new JLabel(the.thongTin.hoTen);
        hoTenField.setSize(200, 30);
        hoTenField.setLocation(300, 145);
        add(hoTenField);

        // Số tiền
        JLabel soTienLabel = new JLabel("Số tiền còn lại trong thẻ");
        soTienLabel.setSize(200, 40);
        soTienLabel.setLocation(300, 180);
        add(soTienLabel);
        JLabel soTienField = new JLabel(NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(the.thongTin.soTien));
        soTienField.setSize(200, 30);
        soTienField.setLocation(300, 205);
        add(soTienField);
        
        // Năm sinh
        JLabel namSinhLabel = new JLabel("Năm sinh");
        namSinhLabel.setSize(200, 40);
        namSinhLabel.setLocation(300, 240);
        add(namSinhLabel);
        JLabel namSinhField = new JLabel(the.thongTin.namSinh);
        namSinhField.setSize(200, 30);
        namSinhField.setLocation(300, 265);
        add(namSinhField);
        
        // Mã số phòng
        JLabel soPhongLabel = new JLabel("Mã số phòng");
        soPhongLabel.setSize(200, 40);
        soPhongLabel.setLocation(300, 300);
        add(soPhongLabel);
        JLabel soPhongField = new JLabel(the.thongTin.maSoPhong);
        soPhongField.setSize(200, 30);
        soPhongField.setLocation(300, 325);
        add(soPhongField);
        
        // Số điện thoại
        JLabel soDienThoaiLabel = new JLabel("Số điện thoại");
        soDienThoaiLabel.setSize(200, 40);
        soDienThoaiLabel.setLocation(300, 360);
        add(soDienThoaiLabel);
        JLabel soDienThoaiField = new JLabel(the.thongTin.soDienThoai);
        soDienThoaiField.setSize(200, 30);
        soDienThoaiField.setLocation(300, 385);
        add(soDienThoaiField);

        // Thông báo
        JLabel thongBao = new JLabel(_thongBao != null ? _thongBao : "", JLabel.CENTER);
        thongBao.setForeground(Color.blue);
        thongBao.setSize(200, 40);
        thongBao.setLocation(300, 420);
        add(thongBao);
        
        // Hình đại diện
        JLabel avatarLabel = new JLabel("Hình đại diện");
        avatarLabel.setSize(200, 40);
        avatarLabel.setLocation(530, 60);
        add(avatarLabel);

        // Dữ liệu cho hình đại diện
        if (the.thongTin.avatar != null) {
            JPanel avatarPreviewImagePanel = new JPanel();
            avatarPreviewImagePanel.setSize(200, 400);
            avatarPreviewImagePanel.setLocation(530, 90);
            add(avatarPreviewImagePanel);
            try {
                // Lấy thông tin hình ảnh
                tempAvatar = the.thongTin.avatar;
                InputStream is = new ByteArrayInputStream(tempAvatar);
                BufferedImage tempAvatarBufferedImage = ImageIO.read(is);

                // Scale & giảm dung lượng hình ảnh
                double aspectRatio = tempAvatarBufferedImage.getWidth() / tempAvatarBufferedImage.getHeight();
                tempAvatarBufferedImage = scale(tempAvatarBufferedImage, 70, (int) (70 / aspectRatio));

                // Lưu lại hình ảnh đã tối ưu dung lượng
                ByteArrayOutputStream _baos = new ByteArrayOutputStream();
                ImageIO.write(tempAvatarBufferedImage, "jpg", _baos);
                tempAvatar = _baos.toByteArray();

                // Scale hình trên panel
                ImageIcon avatarImageIcon = new ImageIcon(tempAvatarBufferedImage);
                Image avatarImage = avatarImageIcon.getImage().getScaledInstance(200, (int) (200 / aspectRatio), Image.SCALE_DEFAULT);
                avatarImageIcon = new ImageIcon(avatarImage);
                JLabel avatarPreviewImage = new JLabel(avatarImageIcon);

                // Cập nhật hình ảnh mới vào panel
                avatarPreviewImagePanel.removeAll();
                avatarPreviewImagePanel.add(avatarPreviewImage);
                avatarPreviewImagePanel.revalidate();
                avatarPreviewImagePanel.repaint();
            } catch (IOException ex) {
                thongBao.setForeground(Color.red);
                thongBao.setText("Có lỗi xảy ra khi lấy dữ liệu ảnh");
                avatarPreviewImagePanel.removeAll();
                avatarPreviewImagePanel.revalidate();
                avatarPreviewImagePanel.repaint();
            }
        }

        // Nút cập nhật dữ liệu
        JButton capNhatDuLieuButton = new JButton("Cập nhật dữ liệu");
        capNhatDuLieuButton.setSize(150, 30);
        capNhatDuLieuButton.setLocation(325, 460);
        capNhatDuLieuButton.addActionListener((ActionEvent e) -> {
            manHinhTaoDuLieu(the.thongTin);
        });
        add(capNhatDuLieuButton);

        themNutHuyKetNoiThe();

        JButton xoaDuLieuButton = new JButton("Xoá dữ liệu");
        xoaDuLieuButton.setSize(150, 30);
        xoaDuLieuButton.setLocation(325, 490);
        xoaDuLieuButton.addActionListener((ActionEvent e) -> {
            int input = JOptionPane.showConfirmDialog(null,
                    "Xoá toàn bộ dữ liệu trên thẻ?", "Xác nhận",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
            if (input == 0) {
                try {
                    NaAPDU.xoaDuLieu();
                    manHinhYeuCauTaoDuLieuChoThe();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, new JLabel("Có lỗi xảy ra khi xoá dữ liệu", JLabel.CENTER), "Lỗi", JOptionPane.PLAIN_MESSAGE);
                }
            }
        });
        add(xoaDuLieuButton);

        veLai();
    }
    
    public void manHinhNapTien() {
        // Tiêu đề
        JLabel tieuDe = new JLabel("Nạp tiền vào thẻ", JLabel.CENTER);
        tieuDe.setSize(400, 40);
        tieuDe.setLocation(200, 30);
        tieuDe.setFont(tieuDe.getFont().deriveFont(Font.BOLD));
        add(tieuDe);
        
        // Số tiền
        JLabel soTienLabel = new JLabel("Số tiền còn lại trong thẻ");
        soTienLabel.setSize(200, 40);
        soTienLabel.setLocation(300, 180);
        add(soTienLabel);
        JLabel soTienField = new JLabel(NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(the.thongTin.soTien));
        soTienField.setSize(200, 30);
        soTienField.setLocation(300, 205);
        add(soTienField);
    }

    // Hàm hỗ trợ --------------------------------------------------------------
    private void xoaManHinh() {
        getContentPane().removeAll();
    }

    private void veLai() {
        revalidate();
        repaint();
    }

    private void hienThi() {
        setVisible(true);
    }

    private byte[] themHinhAnhVaoPanel(byte[] _image, JPanel _panel) throws IOException {
        byte[] _reducedImage;
        InputStream _is = new ByteArrayInputStream(_image);
        BufferedImage tempAvatarBufferedImage = ImageIO.read(_is);

        // Scale & giảm dung lượng hình ảnh
        double aspectRatio = tempAvatarBufferedImage.getWidth() / tempAvatarBufferedImage.getHeight();
        tempAvatarBufferedImage = scale(tempAvatarBufferedImage, 70, (int) (70 / aspectRatio));

        // Lưu lại hình ảnh đã tối ưu dung lượng
        ByteArrayOutputStream _baos = new ByteArrayOutputStream();
        ImageIO.write(tempAvatarBufferedImage, "jpg", _baos);
        _reducedImage = _baos.toByteArray();

        // Scale hình trên panel
        ImageIcon avatarImageIcon = new ImageIcon(tempAvatarBufferedImage);
        Image avatarImage = avatarImageIcon.getImage().getScaledInstance(200, (int) (200 / aspectRatio), Image.SCALE_DEFAULT);
        avatarImageIcon = new ImageIcon(avatarImage);
        JLabel avatarPreviewImage = new JLabel(avatarImageIcon);

        // Cập nhật hình ảnh mới vào panel
        _panel.removeAll();
        _panel.add(avatarPreviewImage);
        _panel.revalidate();
        _panel.repaint();

        return _reducedImage;
    }

    private void themNutHuyKetNoiThe() {
        JButton huyKetNoiButton = new JButton("Huỷ kết nối thẻ");
        huyKetNoiButton.setBounds(10, 525, 120, 32);
        huyKetNoiButton.addActionListener((ActionEvent e) -> {
            NaAPDU.dongKetNoi();
            the = null;
            manHinhKetNoiDenThe(null);
        });
        add(huyKetNoiButton);
    }

    private BufferedImage scale(BufferedImage img, int targetWidth, int targetHeight) {
        int type = (img.getTransparency() == Transparency.OPAQUE) ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
        BufferedImage ret = img;
        BufferedImage scratchImage = null;
        Graphics2D g2 = null;
        int w = img.getWidth();
        int h = img.getHeight();
        int prevW = w;
        int prevH = h;
        do {
            if (w > targetWidth) {
                w /= 2;
                w = (w < targetWidth) ? targetWidth : w;
            }
            if (h > targetHeight) {
                h /= 2;
                h = (h < targetHeight) ? targetHeight : h;
            }
            if (scratchImage == null) {
                scratchImage = new BufferedImage(w, h, type);
                g2 = scratchImage.createGraphics();
            }
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.drawImage(ret, 0, 0, w, h, 0, 0, prevW, prevH, null);
            prevW = w;
            prevH = h;
            ret = scratchImage;
        } while (w != targetWidth || h != targetHeight);
        if (g2 != null) {
            g2.dispose();
        }
        if (targetWidth != ret.getWidth() || targetHeight != ret.getHeight()) {
            scratchImage = new BufferedImage(targetWidth, targetHeight, type);
            g2 = scratchImage.createGraphics();
            g2.drawImage(ret, 0, 0, null);
            g2.dispose();
            ret = scratchImage;
        }
        return ret;

    }
}
