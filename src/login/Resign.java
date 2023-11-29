package login;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import util.Util;

public class Resign extends JFrame {

    private JPanel contentPane;
    private JTextField textField;
    private JPasswordField passwordField;
    private JPasswordField passwordField_1;
    private JLabel lblNewLabel;

    public Resign() {
        setTitle("ע��\n");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(350, 250, 450, 300);
        contentPane = new JPanel() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(new ImageIcon("images\\22.jpg").getImage(), 0,0, getWidth(), getHeight(), null);
            }
        };
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        textField = new JTextField();
        textField.setBounds(180, 42, 104, 20);//�û���
        textField.setOpaque(false);
        contentPane.add(textField);
        textField.setColumns(10);

        //��ʾ��Ϣ���û���
        JLabel YHM = new JLabel("�û�����");
        YHM.setBounds(130, 42, 185, 20);
        YHM.setForeground(Color.blue);
        contentPane.add(YHM);

        passwordField = new JPasswordField();
        passwordField.setEchoChar('*');
        passwordField.setOpaque(false);
        passwordField.setBounds(180, 98, 104, 20);//����
        contentPane.add(passwordField);

        //��ʾ��Ϣ������
        JLabel MM = new JLabel("���룺");
        MM.setBounds(130, 98, 185, 20);
        MM.setForeground(Color.blue);
        contentPane.add(MM);

        passwordField_1 = new JPasswordField();
        passwordField_1.setBounds(180, 152, 104, 21);//ȷ������
        passwordField_1.setOpaque(false);
        contentPane.add(passwordField_1);

        //��ʾ��Ϣ������
        JLabel QRMM = new JLabel("ȷ�����룺");
        QRMM.setBounds(130, 152, 185, 20);
        QRMM.setForeground(Color.blue);
        contentPane.add(QRMM);

        //ע�ᰴť
        final JButton btnNewButton_1 = new JButton();
        btnNewButton_1.setText("ע��");
        btnNewButton_1.setBounds(120, 198, 80, 40);
        getRootPane().setDefaultButton(btnNewButton_1);
        contentPane.add(btnNewButton_1);

        //���ذ�ť
        final JButton btnNewButton_2 = new JButton("");
        btnNewButton_2.setText("����");
        btnNewButton_2.setBounds(250, 198, 80, 40);
        contentPane.add(btnNewButton_2);

        //��ʾ��Ϣ
        lblNewLabel = new JLabel();
        lblNewLabel.setBounds(55, 218, 185, 20);
        lblNewLabel.setForeground(Color.red);
        contentPane.add(lblNewLabel);

        //���ذ�ť����
        btnNewButton_2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                btnNewButton_2.setEnabled(false);
                //���ص�½����
                Login frame = new Login();
                frame.setVisible(true);
                setVisible(false);
            }
        });

        //ע�ᰴť����
        btnNewButton_1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Properties userPro = new Properties();
                File file = new File("Users.properties");
                Util.loadPro(userPro, file);

                String u_name = textField.getText();
                String u_pwd = new String(passwordField.getPassword());
                String u_pwd_ag = new String(passwordField_1.getPassword());

                // �ж��û����Ƿ�����ͨ�û����Ѵ���
                if (u_name.length() != 0) {

                    if (userPro.containsKey(u_name)) {
                        lblNewLabel.setText("�û����Ѵ���!");
                    } else {
                        isPassword(userPro, file, u_name, u_pwd, u_pwd_ag);
                    }
                } else {
                    lblNewLabel.setText("�û�������Ϊ�գ�");
                }
            }

            private void isPassword(Properties userPro,
                                    File file, String u_name, String u_pwd, String u_pwd_ag) {
                if (u_pwd.equals(u_pwd_ag)) {
                    if (u_pwd.length() != 0) {
                        userPro.setProperty(u_name, u_pwd_ag);
                        try {
                            userPro.store(new FileOutputStream(file),
                                    "Copyright (c) Boxcode Studio");
                        } catch (FileNotFoundException e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        } catch (IOException e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        }
                        btnNewButton_1.setEnabled(false);
                        //���ص�½����
                        Login frame = new Login();
                        frame.setVisible(true);
                        setVisible(false);
                    } else {
                        lblNewLabel.setText("����Ϊ�գ�");
                    }
                } else {
                    lblNewLabel.setText("���벻һ�£�");
                }
            }
        });
    }
}

