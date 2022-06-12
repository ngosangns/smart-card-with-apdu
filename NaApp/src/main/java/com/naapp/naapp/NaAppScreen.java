package com.naapp.naapp;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
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
    public void manHinhKetNoiDenThe() {
        xoaManHinh();

        JButton b = new JButton("(Admin) Kết nối đến thẻ");
        b.setBounds(250, 280, 300, 40);
        b.addActionListener((ActionEvent e) -> {
            try {
                NaAPDU.ketNoiThe(QuanLyThe.AID);
                the = NaAPDU.theDangKetNoi;
                if (NaAPDU.kiemTraTonTaiDuLieuTrongThe()) {
                    manHinhNhapMaPin(null);
                } else {
                    manHinhYeuCauTaoDuLieuChoThe();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, new JLabel(ex.getMessage(), JLabel.CENTER), "Lỗi", JOptionPane.PLAIN_MESSAGE);
            }
        });
        add(b);

        JButton b1 = new JButton("Xác thực thẻ vào thang máy/toà nhà");
        b1.setBounds(250, 230, 300, 40);
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
        b2.setBounds(250, 330, 300, 40);
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

        veLai();
    }

    public void manHinhNhapMaPin(String _thongBao) {
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
                if (pinField.getText().length() >= NaAPDU.boiSoAES) {
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
                manHinhXemDuLieu(null);
            } catch (Exception ex) {
                thongBao.setForeground(Color.red);
                thongBao.setText(ex.getMessage());
            }
        });
        add(dangNhapButton);

        themNutHuyKetNoiThe();
        themNutAdminMoKhoaThe();

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
        pinField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (pinField.getText().length() >= NaAPDU.boiSoAES) // limit to `boiSoAES` characters
                {
                    e.consume();
                }
            }
        });
        pinField.setSize(200, 30);
        pinField.setLocation(300, 190);
        add(pinField);

        // Số tiền
        JLabel soTienLabel = new JLabel("Số tiền");
        soTienLabel.setSize(200, 40);
        soTienLabel.setLocation(300, 220);
        add(soTienLabel);
        JFormattedTextField soTienField = new JFormattedTextField(NumberFormat.getNumberInstance());
        soTienField.setSize(200, 30);
        soTienField.setLocation(300, 250);
        soTienField.setValue(0);
        add(soTienField);

        // Hình đại diện
        JLabel avatarLabel = new JLabel("Hình đại diện");
        avatarLabel.setSize(200, 40);
        avatarLabel.setLocation(530, 100);
        add(avatarLabel);
        JButton avatarButton = new JButton("Chọn hình đại diện");
        avatarButton.setSize(200, 30);
        avatarButton.setLocation(530, 130);

        // Thêm panel cho hình đại diện
        JPanel avatarPreviewImagePanel = new JPanel();
        avatarPreviewImagePanel.setSize(200, 400);
        avatarPreviewImagePanel.setLocation(530, 160);
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

        JLabel thongBao = new JLabel("", JLabel.CENTER);
        thongBao.setSize(200, 40);
        thongBao.setLocation(300, 360);
        add(thongBao);

        // Nút tạo dữ liệu
        JButton taoDuLieuButton = new JButton(tt == null ? "Tạo dữ liệu" : "Cập nhật");
        taoDuLieuButton.setSize(150, 30);
        taoDuLieuButton.setLocation(325, 400);
        taoDuLieuButton.addActionListener((ActionEvent e) -> {
            String _hoTen = hoTenField.getText();
            String _pin = pinField.getText();
            long _soTien = ((Number) soTienField.getValue()).longValue();

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
                    NaAPDU.taoDuLieu(_tt, _pin);
                    manHinhNhapMaPin("Tạo dữ liệu thành công");
                } else {
                    ThongTin _tt = new ThongTin(_hoTen, tt.id);
                    _tt.avatar = tempAvatar;
                    _tt.soTien = _soTien;
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
        quayLaiButton.setLocation(325, 430);
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
        }
        if (the.pin != null) {
            pinField.setText(the.pin);
        } else {
            pinField.setText("");
        }

        veLai();
    }

    public void manHinhXemDuLieu(String _thongBao) {
        xoaManHinh();

        // Tiêu đề
        JLabel tieuDe = new JLabel("Thông tin chủ thẻ", JLabel.CENTER);
        tieuDe.setSize(400, 40);
        tieuDe.setLocation(200, 50);
        tieuDe.setFont(tieuDe.getFont().deriveFont(Font.BOLD));
        add(tieuDe);

        // ID
        JLabel idLabel = new JLabel("Mã chủ thẻ");
        idLabel.setSize(200, 40);
        idLabel.setLocation(300, 100);
        add(idLabel);
        JLabel idField = new JLabel(the.thongTin.id);
        idField.setSize(200, 30);
        idField.setLocation(300, 130);
        add(idField);

        // Họ và tên
        JLabel hoVaTenLabel = new JLabel("Họ và tên");
        hoVaTenLabel.setSize(200, 40);
        hoVaTenLabel.setLocation(300, 160);
        add(hoVaTenLabel);
        JLabel hoTenField = new JLabel(the.thongTin.hoTen);
        hoTenField.setSize(200, 30);
        hoTenField.setLocation(300, 190);
        add(hoTenField);

        // Số tiền
        JLabel soTienLabel = new JLabel("Số tiền còn lại trong thẻ");
        soTienLabel.setSize(200, 40);
        soTienLabel.setLocation(300, 220);
        add(soTienLabel);
        JLabel soTienField = new JLabel(NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(the.thongTin.soTien));
        soTienField.setSize(200, 30);
        soTienField.setLocation(300, 250);
        add(soTienField);

        // Hình đại diện
        JLabel avatarLabel = new JLabel("Hình đại diện");
        avatarLabel.setSize(200, 40);
        avatarLabel.setLocation(530, 100);
        add(avatarLabel);

        // Thông báo
        JLabel thongBao = new JLabel(_thongBao != null ? _thongBao : "", JLabel.CENTER);
        thongBao.setForeground(Color.blue);
        thongBao.setSize(200, 40);
        thongBao.setLocation(300, 360);
        add(thongBao);

        // Dữ liệu cho hình đại diện
        if (the.thongTin.avatar != null) {
            JPanel avatarPreviewImagePanel = new JPanel();
            avatarPreviewImagePanel.setSize(200, 400);
            avatarPreviewImagePanel.setLocation(530, 130);
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
        capNhatDuLieuButton.setLocation(325, 400);
        capNhatDuLieuButton.addActionListener((ActionEvent e) -> {
            manHinhTaoDuLieu(the.thongTin);
        });
        add(capNhatDuLieuButton);

        JPanel avatarPreviewImagePanel = new JPanel();
        avatarPreviewImagePanel.setSize(200, 400);
        avatarPreviewImagePanel.setLocation(530, 130);
        try {
            ImageIcon avatarImageIcon = new ImageIcon(ImageIO.read(new ByteArrayInputStream(the.thongTin.avatar)));
            JLabel avatarPreviewImage = new JLabel(avatarImageIcon);
            avatarPreviewImagePanel.add(avatarPreviewImage);
        } catch (IOException ex) {
            thongBao.setForeground(Color.red);
            thongBao.setText("Có lỗi xảy ra khi lấy dữ liệu hình ảnh");
        }
        add(avatarPreviewImagePanel);

        themNutHuyKetNoiThe();

        JButton xoaDuLieuButton = new JButton("Xoá dữ liệu");
        xoaDuLieuButton.setSize(150, 30);
        xoaDuLieuButton.setLocation(325, 430);
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
            manHinhKetNoiDenThe();
        });
        add(huyKetNoiButton);
    }

    private void themNutAdminMoKhoaThe() {
        JLabel moTa = new JLabel("Sử dụng khi thẻ bị khoá", JLabel.CENTER);
        moTa.setSize(150, 32);
        moTa.setLocation(635, 495);
        add(moTa);

        JButton adminMoKhoaTheButton = new JButton("Admin mở khoá thẻ");
        adminMoKhoaTheButton.setBounds(635, 525, 150, 32);
        adminMoKhoaTheButton.addActionListener((ActionEvent e) -> {
            try {
                NaAPDU.adminMoKhoaThe();
                JOptionPane.showMessageDialog(null, new JLabel("Đã mở khoá thẻ", JLabel.CENTER), "Thông báo", JOptionPane.PLAIN_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, new JLabel(ex.getMessage(), JLabel.CENTER), "Lỗi", JOptionPane.PLAIN_MESSAGE);
            }
        });
        add(adminMoKhoaTheButton);
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
