package com.naapp.naapp;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class NaAppScreen extends JFrame {
    private final QuanLyThe quanLyThe;
    private The the;
    public NaAppScreen(QuanLyThe _quanLyThe) {
        super();
        
        quanLyThe = _quanLyThe;
        byte[] AID = {(byte)0X11, (byte)0X22, (byte)0X33, (byte)0X44, (byte)0X55, (byte)0X66};
        the = quanLyThe.layThe(AID);
        
        setSize(800, 600);
        setLayout(null);
        setTitle("NaApp");
        setResizable(false);
        setLocationRelativeTo(null);
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
            if(this.the.ketNoi()) {
                try {
                    if(this.the.kiemTraTonTaiDuLieuTrongThe()) {
                        manHinhNhapMaPin();
                    } else {
                        manHinhYeuCauTaoDuLieuChoThe();
                    }
                } catch (Exception ex) {
                    thongBao.setForeground(Color.red);
                    thongBao.setText("Có lỗi xảy ra khi kiểm tra dữ liệu");
                }
                
            } else {
                thongBao.setForeground(Color.red);
                thongBao.setText("Có lỗi xảy ra khi kết nối đến thẻ");
            }
        });
        add(b);
        
        veLai();
    }
    
    public void manHinhNhapMaPin() {
        xoaManHinh();
        
        JLabel tieuDe = new JLabel("Nhập mã pin");
        tieuDe.setSize(200, 40);
        tieuDe.setLocation(300, 280);
        add(tieuDe);
        
        JTextField pinField = new JPasswordField();
        pinField.setSize(200, 30);
        pinField.setLocation(300, 320);
        add(pinField);
        
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
            manHinhTaoDuLieu();
        });
        add(taoDuLieuButton);
        
        veLai();
    }
    
    public void manHinhTaoDuLieu() {
        xoaManHinh();
        
        // Tiêu đề
        JLabel tieuDe = new JLabel("Tạo dữ liệu cho thẻ", JLabel.CENTER);
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
        
        JLabel thongBao = new JLabel("", JLabel.CENTER);
        thongBao.setSize(200, 40);
        thongBao.setLocation(300, 160);
        add(thongBao);
        
        // Nút tạo dữ liệu
        JButton taoDuLieuButton = new JButton("Tạo dữ liệu");
        taoDuLieuButton.setSize(150, 30);
        taoDuLieuButton.setLocation(325, 200);
        taoDuLieuButton.addActionListener((ActionEvent e) -> {
            String hoTen = hoTenField.getText();
            
            // Kiểm tra dữ liệu
            if(hoTen.equals("")) {
                thongBao.setForeground(Color.red);
                thongBao.setText("Tên không được để trống");
                return;
            }
            
            try {
                if(this.the.taoDuLieu(new ThongTin(hoTen))) {
                    thongBao.setForeground(Color.blue);
                    thongBao.setText("Tạo dữ liệu thành công");
                } else {
                    thongBao.setForeground(Color.red);
                    thongBao.setText("Tạo dữ liệu thất bại");
                }
            } catch (Exception ex) {
                Logger.getLogger(NaAppScreen.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        add(taoDuLieuButton);
        
        veLai();
    }
    
    // -------------------------------------------------------------------------
    
    private void xoaManHinh() {
        getContentPane().removeAll();
    }
    
    private void veLai() {
        repaint();
    }
    
    private void hienThi() {
        setVisible(true);
    }
    
    // Hành động ---------------------------------------------------------------
}
