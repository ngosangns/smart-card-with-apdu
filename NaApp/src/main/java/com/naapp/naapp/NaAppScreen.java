package com.naapp.naapp;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

public class NaAppScreen extends JFrame {

    private final QuanLyThe quanLyThe;
    private The the;

    public NaAppScreen(QuanLyThe _quanLyThe) {
        super();

        quanLyThe = _quanLyThe;
        byte[] AID = {(byte) 0X11, (byte) 0X22, (byte) 0X33, (byte) 0X44, (byte) 0X55, (byte) 0X66};
        the = quanLyThe.layThe(AID);

        setSize(800, 600);
        setLayout(null);
        setTitle("NaApp");
        setResizable(false);
        setLocationRelativeTo(null);

        // Khi tắt frame sẽ huỷ kết nối thẻ (nếu có) và kết thúc chương trình
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                try {
                    try {
                        the.dongKetNoi();
                    } catch (Exception exx) {
                    }
                    System.exit(0);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null,
                            new JLabel("Có lỗi xảy ra khi đóng ứng dụng", JLabel.CENTER),
                            "Lỗi", JOptionPane.PLAIN_MESSAGE);
                }
            }
        });

        hienThi();
    }

    // Màn hình ----------------------------------------------------------------
    public void manHinhKetNoiDenThe() {
        xoaManHinh();

        JLabel thongBao = new JLabel("");
        thongBao.setSize(200, 40);
        thongBao.setLocation(300, 320);
        add(thongBao);

        JButton b = new JButton("Kết nối đến thẻ");
        b.setBounds(300, 280, 200, 40);
        b.addActionListener((ActionEvent e) -> {
            try {
                if (the.ketNoi()) {
                    try {
                        if (the.kiemTraTonTaiDuLieuTrongThe()) {
                            manHinhNhapMaPin(null);
                        } else {
                            manHinhYeuCauTaoDuLieuChoThe();
                        }
                    } catch (Exception ex) {
                        thongBao.setForeground(Color.red);
                        thongBao.setText("Có lỗi xảy ra khi kiểm tra dữ liệu thẻ");
                    }
                } else {
                    throw new Exception("");
                }
            } catch (Exception ex) {
                thongBao.setForeground(Color.red);
                thongBao.setText("Có lỗi xảy ra khi kết nối đến thẻ");
            }
        });
        add(b);

        veLai();
    }

    public void manHinhNhapMaPin(String _thongBao) {
        xoaManHinh();

        JLabel tieuDe = new JLabel("Nhập mã pin");
        tieuDe.setSize(200, 40);
        tieuDe.setLocation(300, 280);
        add(tieuDe);

        JTextField pinField = new JPasswordField();
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
                the.dangNhap(pin);
                if (the.thongTin == null) {
                    thongBao.setForeground(Color.red);
                    thongBao.setText("Có lỗi xảy ra");
                    return;
                }
                manHinhXemDuLieu();
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

        veLai();
    }

    public void manHinhTaoDuLieu(ThongTin tt) {
        xoaManHinh();

        // Tiêu đề
        JLabel tieuDe = new JLabel(tt == null ? "Tạo dữ liệu cho thẻ" : "Cập nhật dữ liệu", JLabel.CENTER);
        tieuDe.setSize(400, 40);
        tieuDe.setLocation(200, 50);
        tieuDe.setFont(tieuDe.getFont().deriveFont(Font.BOLD));
        add(tieuDe);

        // Họ và tên
        JLabel hoVaTenLabel = new JLabel("Họ và tên");
        hoVaTenLabel.setSize(200, 40);
        hoVaTenLabel.setLocation(300, 100);
        add(hoVaTenLabel);
        JTextField hoTenField = new JTextField();
        hoTenField.setSize(200, 30);
        hoTenField.setLocation(300, 130);
        add(hoTenField);

        // PIN
        JLabel pinLabel = new JLabel("Nhập mã PIN");
        pinLabel.setSize(200, 40);
        pinLabel.setLocation(300, 160);
        add(pinLabel);
        JTextField pinField = new JTextField();
        pinField.setSize(200, 30);
        pinField.setLocation(300, 190);
        add(pinField);

        JLabel thongBao = new JLabel("", JLabel.CENTER);
        thongBao.setSize(200, 40);
        thongBao.setLocation(300, 220);
        add(thongBao);

        // Nút tạo dữ liệu
        JButton taoDuLieuButton = new JButton(tt == null ? "Tạo dữ liệu" : "Cập nhật");
        taoDuLieuButton.setSize(150, 30);
        taoDuLieuButton.setLocation(325, 260);
        taoDuLieuButton.addActionListener((ActionEvent e) -> {
            String hoTen = hoTenField.getText();
            String pin = pinField.getText();

            // Kiểm tra dữ liệu
            if (hoTen.equals("")) {
                thongBao.setForeground(Color.red);
                thongBao.setText("Tên không được để trống");
                return;
            }
            if (pin.equals("")) {
                thongBao.setForeground(Color.red);
                thongBao.setText("Mã PIN không được để trống");
                return;
            }

            try {
                if (the.taoDuLieu(new ThongTin(hoTen, pin))) {
                    manHinhNhapMaPin(tt == null ? "Tạo dữ liệu thành công" : "Cập nhật dữ liệu thành công");
                } else {
                    thongBao.setForeground(Color.red);
                    thongBao.setText(tt == null ? "Tạo dữ liệu thất bại" : "Cập nhật dữ liệu thất bại");
                }
            } catch (Exception ex) {
                Logger.getLogger(NaAppScreen.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        add(taoDuLieuButton);

        // Nút quay lại
        JButton quayLaiButton = new JButton("Quay lại");
        quayLaiButton.setSize(150, 30);
        quayLaiButton.setLocation(325, 290);
        quayLaiButton.addActionListener((ActionEvent e) -> {
            if (tt == null) {
                manHinhYeuCauTaoDuLieuChoThe();
            } else {
                manHinhXemDuLieu();
            }
        });
        add(quayLaiButton);

        if (tt != null) {
            hoTenField.setText(tt.hoTen);
            pinField.setText(tt.pin);
        }

        veLai();
    }

    public void manHinhXemDuLieu() {
        xoaManHinh();

        // Tiêu đề
        JLabel tieuDe = new JLabel("Thông tin chủ thẻ", JLabel.CENTER);
        tieuDe.setSize(400, 40);
        tieuDe.setLocation(200, 50);
        tieuDe.setFont(tieuDe.getFont().deriveFont(Font.BOLD));
        add(tieuDe);

        // Họ và tên
        JLabel hoVaTenLabel = new JLabel("Họ và tên");
        hoVaTenLabel.setSize(200, 40);
        hoVaTenLabel.setLocation(300, 100);
        add(hoVaTenLabel);
        JLabel hoTenField = new JLabel(the.thongTin.hoTen);
        hoTenField.setSize(200, 30);
        hoTenField.setLocation(300, 130);
        add(hoTenField);

        JLabel thongBao = new JLabel("", JLabel.CENTER);
        thongBao.setSize(200, 40);
        thongBao.setLocation(300, 220);
        add(thongBao);

        // Nút cập nhật dữ liệu
        JButton capNhatDuLieuButton = new JButton("Cập nhật dữ liệu");
        capNhatDuLieuButton.setSize(150, 30);
        capNhatDuLieuButton.setLocation(325, 260);
        capNhatDuLieuButton.addActionListener((ActionEvent e) -> {
            manHinhTaoDuLieu(the.thongTin);
        });
        add(capNhatDuLieuButton);

        themNutHuyKetNoiThe();
        
        JButton xoaDuLieuButton = new JButton("Xoá dữ liệu");
        xoaDuLieuButton.setSize(150, 30);
        xoaDuLieuButton.setLocation(325, 260);
        xoaDuLieuButton.addActionListener((ActionEvent e) -> {
            int input = JOptionPane.showConfirmDialog(null, 
                "Xoá toàn bộ dữ liệu trên thẻ?", "Xác nhận", 
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
            if(input == 0) {
                try {
                    the.xoaDuLieu(the.thongTin.pin);
                } catch(Exception ex) {
                    JOptionPane.showMessageDialog(null, new JLabel("Có lỗi xảy ra khi xoá dữ liệu", JLabel.CENTER), "Lỗi", JOptionPane.PLAIN_MESSAGE);
                }
            }
        });
        

        veLai();
    }

    // Hàm hỗ trợ --------------------------------------------------------------
    private void xoaManHinh() {
        getContentPane().removeAll();
    }

    private void veLai() {
        repaint();
    }

    private void hienThi() {
        setVisible(true);
    }

    private void themNutHuyKetNoiThe() {
        JButton huyKetNoiButton = new JButton("Huỷ kết nối thẻ");
        huyKetNoiButton.setBounds(10, 525, 120, 32);
        huyKetNoiButton.addActionListener((ActionEvent e) -> {
            try {
                the.dongKetNoi();
                the.thongTin = null;
                manHinhKetNoiDenThe();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, new JLabel("Có lỗi xảy ra khi huỷ kết nối đến thẻ", JLabel.CENTER), "Lỗi", JOptionPane.PLAIN_MESSAGE);
            }
        });
        add(huyKetNoiButton);
    }

    // Hành động ---------------------------------------------------------------
}
