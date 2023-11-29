package server;


import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

import function.Bean;
import function.ClientBean;

import javax.swing.*;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class Server extends JFrame implements Runnable{
    ServerSocket serverSocket = null;
    HashMap<String, ChatThread> userMap = new HashMap<>();
    HashMap<String, ArrayList<String>> groupMap = new HashMap<>();
    JPanel jPanel = new JPanel();
    JPanel jButtonPanel = new JPanel();
    JTextArea jTextArea = new JTextArea();
    JButton broadButton = new JButton("���͹㲥");
    JButton LogoutButton = new JButton("ǿ������");
    JList<String> jList = new JList<>(new Vector<>(userMap.keySet()));
    JScrollPane jScrollPane = new JScrollPane(jList);
    String pastMsg = "";

    public Server() {
        this.setTitle("������");
        this.setSize(500, 300);
        this.setLocation(420, 180);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setVisible(true);
        this.add(jPanel);
        jPanel.setLayout(new BorderLayout());
        jPanel.add(jScrollPane, BorderLayout.CENTER);
        jPanel.add(jButtonPanel, BorderLayout.SOUTH);
        jPanel.setSize(500, 300);
        jButtonPanel.setLayout(new GridLayout(1, 2));
        jButtonPanel.add(broadButton);
        jButtonPanel.add(LogoutButton);
        jTextArea.setEditable(false);
        broadButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String msg = new JOptionPane().showInputDialog(jButtonPanel, "������Ҫ�㲥������");
                if (!msg.equals("")) {
                    for (ChatThread ct : userMap.values()) {
                        ct.pStream.println(Message.BroadcastMsg + "###" + msg);
                    }
                    pastMsg += Message.BroadcastMsg + "@@@" + msg + "###";
                }


            }
        });
        LogoutButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!jList.isSelectionEmpty()) {
                    String user = jList.getSelectedValue();
                    if (userMap.containsKey(user)) {
                        String msg = Message.LogoutMsg + "###" + user;
                        for (ChatThread ct : userMap.values()) {
                            ct.pStream.println(msg);
                        }
                        try {
                            userMap.get(user).socket.close();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                        userMap.remove(user);
                    }
                }
                UpdateList();
            }
        });
        new Thread(this).start();
    }

    public void UpdateList() {
        Vector<String> vector = new Vector<>(userMap.keySet());
        jList.setListData(vector);
    }


    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(9000);
            while (true) {
                Socket socket = serverSocket.accept();
                new ChatThread(this, socket);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }



    private static ServerSocket ss;
    public static HashMap<String, ClientBean> onlines;
    static {
        try {
            ss = new ServerSocket(8520);
            onlines = new HashMap<String, ClientBean>();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    class CatClientThread extends Thread {
        private Socket client;
        private Bean bean;
        private ObjectInputStream ois;
        private ObjectOutputStream oos;

        public CatClientThread(Socket client) {
            this.client = client;
        }

        @Override
        public void run() {
            try {
                // ��ͣ�Ĵӿͻ��˽�����Ϣ
                while (true) {
                    // ��ȡ�ӿͻ��˽��յ���catbean��Ϣ
                    ois = new ObjectInputStream(client.getInputStream());
                    bean = (Bean)ois.readObject();

                    // ����catbean�У�type������һ������
                    switch (bean.getType()) {
                        // �����߸���
                        case 0: { // ����
                            // ��¼���߿ͻ����û����Ͷ˿���clientbean��
                            ClientBean cbean = new ClientBean();
                            cbean.setName(bean.getName());
                            cbean.setSocket(client);
                            // ��������û�
                            onlines.put(bean.getName(), cbean);
                            // ������������catbean�������͸��ͻ���
                            Bean serverBean = new Bean();
                            serverBean.setType(0);
                            serverBean.setInfo(bean.getTimer() + "  "
                                    + bean.getName() + "������"+"\r\n");
                            // ֪ͨ���пͻ���������
                            HashSet<String> set = new HashSet<String>();
                            // �ͻ��ǳ�
                            set.addAll(onlines.keySet());
                            serverBean.setClients(set);
                            sendAll(serverBean);
                            break;
                        }
                        case -1: { // ����
                            // ������������catbean�������͸��ͻ���
                            Bean serverBean = new Bean();
                            serverBean.setType(-1);

                            try {
                                oos = new ObjectOutputStream(
                                        client.getOutputStream());
                                oos.writeObject(serverBean);
                                oos.flush();
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }

                            onlines.remove(bean.getName());

                            // ��ʣ�µ������û����������뿪��֪ͨ
                            Bean serverBean2 = new Bean();
                            serverBean2.setInfo(bean.getTimer() + "  "
                                    + bean.getName() + " " + " ������"+"\r\n");
                            serverBean2.setType(0);
                            HashSet<String> set = new HashSet<String>();
                            set.addAll(onlines.keySet());
                            serverBean2.setClients(set);

                            sendAll(serverBean2);
                            return;
                        }
                        case 1: { // ����

//						 ������������catbean�������͸��ͻ���
                            Bean serverBean = new Bean();

                            serverBean.setType(1);
                            serverBean.setClients(bean.getClients());
                            serverBean.setInfo(bean.getInfo());
                            serverBean.setName(bean.getName());
                            serverBean.setTimer(bean.getTimer());
                            // ��ѡ�еĿͻ���������
                            sendMessage(serverBean);
                            break;
                        }
                        case 5: { // ����

//						 ������������catbean�������͸��ͻ���
                            Bean serverBean = new Bean();

                            serverBean.setType(5);
                            serverBean.setClients(bean.getClients());
                            serverBean.setInfo(bean.getInfo());
                            serverBean.setName(bean.getName());
                            serverBean.setTimer(bean.getTimer());
                            // ��ѡ�еĿͻ���������
                            sendAll(serverBean);
                            //sendMessage(serverBean);
                            break;
                        }
                        case 2: { // ��������ļ�
                            // ������������catbean�������͸��ͻ���
                            Bean serverBean = new Bean();
                            String info = bean.getTimer() + "  " + bean.getName()
                                    + "���㴫���ļ�,�Ƿ���Ҫ����";

                            serverBean.setType(2);
                            serverBean.setClients(bean.getClients()); // ���Ƿ��͵�Ŀ�ĵ�
                            serverBean.setFileName(bean.getFileName()); // �ļ�����
                            serverBean.setSize(bean.getSize()); // �ļ���С
                            serverBean.setInfo(info);
                            serverBean.setName(bean.getName()); // ��Դ
                            serverBean.setTimer(bean.getTimer());
                            // ��ѡ�еĿͻ���������
                            sendMessage(serverBean);

                            break;
                        }
                        case 3: { // ȷ�������ļ�
                            Bean serverBean = new Bean();

                            serverBean.setType(3);
                            serverBean.setClients(bean.getClients()); // �ļ���Դ
                            serverBean.setTo(bean.getTo()); // �ļ�Ŀ�ĵ�
                            serverBean.setFileName(bean.getFileName()); // �ļ�����
                            serverBean.setIp(bean.getIp());
                            serverBean.setPort(bean.getPort());
                            serverBean.setName(bean.getName()); // ���յĿͻ�����
                            serverBean.setTimer(bean.getTimer());
                            // ֪ͨ�ļ���Դ�Ŀͻ����Է�ȷ�������ļ�
                            sendMessage(serverBean);
                            break;
                        }
                        case 4: {
                            Bean serverBean = new Bean();

                            serverBean.setType(4);
                            serverBean.setClients(bean.getClients()); // �ļ���Դ
                            serverBean.setTo(bean.getTo()); // �ļ�Ŀ�ĵ�
                            serverBean.setFileName(bean.getFileName());
                            serverBean.setInfo(bean.getInfo());
                            serverBean.setName(bean.getName());// ���յĿͻ�����
                            serverBean.setTimer(bean.getTimer());
                            sendMessage(serverBean);

                            break;
                        }
                        default: {
                            break;
                        }
                    }
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                close();
            }
        }

        // ��ѡ�е��û���������
        private void sendMessage(Bean serverBean) {
            // ����ȡ�����е�values
            Set<String> cbs = onlines.keySet();
            Iterator<String> it = cbs.iterator();
            // ѡ�пͻ�
            HashSet<String> clients = serverBean.getClients();
            while (it.hasNext()) {
                // ���߿ͻ�
                String client = it.next();
                // ѡ�еĿͻ����������ߵģ��ͷ���serverbean
                if (clients.contains(client)) {
                    Socket c = onlines.get(client).getSocket();
                    ObjectOutputStream oos;
                    try {
                        oos = new ObjectOutputStream(c.getOutputStream());
                        oos.writeObject(serverBean);
                        oos.flush();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                }
            }
        }

        // �����е��û���������
        public void sendAll(Bean serverBean) {
            Collection<ClientBean> clients = onlines.values();
            Iterator<ClientBean> it = clients.iterator();
            ObjectOutputStream oos;
            while (it.hasNext()) {
                Socket c = it.next().getSocket();
                try {
                    oos = new ObjectOutputStream(c.getOutputStream());
                    oos.writeObject(serverBean);
                    oos.flush();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

        private void close() {
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if (client != null) {
                try {
                    client.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    public void start() {
        try {
            while (true) {
                Socket client = ss.accept();
                new CatClientThread(client).start();
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Server server1=new Server();
        server1.start();
        // new Server().start();

    }

    class ChatThread implements Runnable {
        Server server = null;
        Socket socket = null;
        BufferedReader bReader = null;
        PrintStream pStream = null;

        public ChatThread(Server server, Socket socket) {
            try {
                this.server = server;
                this.socket = socket;
                bReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                pStream = new PrintStream(socket.getOutputStream());
            } catch (Exception e) {
                // TODO: handle exception
            }
            new Thread(this).start();
        }

        @Override
        public void run() {
            while (true) {
                try {
                    String msg = bReader.readLine();
                    server.jTextArea.append(msg + "\n");
                    String[] msgs = msg.split("###");
                    if (msgs[0].equals(Message.LoginMsg)) {
                        userLogin(msgs[1]);
                    } else if (msgs[0].equals(Message.LogoutMsg)) {
                        this.socket.close();
                        userLogout(msgs[1]);
                        break;
                    } else if (msgs[0].equals(Message.PrivateMsg)) {
                        if (server.userMap.containsKey(msgs[1])) {
                            server.userMap.get(msgs[1]).pStream.println(msg);
                        }
                    } else if (msgs[0].equals(Message.NewGrChatMsg)) {
                        setNewChatRoom(msgs[1], msgs[2], msgs[3]);
                    } else if (msgs[0].equals(Message.GroupMsg)) {
                        if (server.groupMap.containsKey(msgs[1])) {
                            for (String mem : server.groupMap.get(msgs[1])) {
                                server.userMap.get(mem).pStream.println(msg);
                            }
                        }
                    } else if (msgs[0].equals(Message.UpdateGrLMsg)) {
                        if (server.groupMap.containsKey(msgs[1])) {
                            String s = msgs[0] + "###" + msgs[1] + "###";
                            for (String name : server.groupMap.get(msgs[1])) {
                                s = s + name + "@@@";
                            }
                            server.userMap.get(msgs[2]).pStream.println(s);
                        }
                    } else if (msgs[0].equals(Message.ExitGroupChatMsg)) {
                        if (server.groupMap.containsKey(msgs[1])) {
                            server.groupMap.get(msgs[1]).remove(msgs[2]);
                            String s = Message.UpdateGrLMsg + "###" + msgs[1] + "###";
                            for (String name : server.groupMap.get(msgs[1])) {
                                s = s + name + "@@@";
                            }
                            for (String name : server.groupMap.get(msgs[1])) {
                                server.userMap.get(name).pStream.println(s);
                            }
                        }
                    } else if (msgs[0].equals(Message.ChatroomMsg)) {
                        if (server.userMap.containsKey(msgs[1])) {
                            for (ChatThread ct : server.userMap.values()) {
                                ct.pStream.println(msg);
                            }
                            server.pastMsg += msgs[1] + "@@@" + msgs[2] + "###";
                        }
                    } else if (msgs[0].equals(Message.PastMsg)) {
                        if (server.userMap.containsKey(msgs[1])) {
                            server.userMap.get(msgs[1]).pStream.println(Message.PastMsg + "###" + server.pastMsg);
                        }
                    }
                } catch (Exception e) {
                    // TODO: handle exception
                }

            }
        }

        public void userLogin(String msg) {
            if (server.userMap.containsKey(msg)) {
                pStream.println(Message.UserExsistMsg);
                return;
            } else {
                server.userMap.put(msg, this);
                String userList = Message.UserListMsg + "###";
                for (String name : server.userMap.keySet()) {
                    userList = userList + name + "@@@";
                }
                for (ChatThread ct : server.userMap.values()) {
                    ct.pStream.println(userList);
                }
            }
            server.UpdateList();
        }

        public void userLogout(String msg) {
            if (!server.userMap.containsKey(msg)) {
                return;
            } else {
                for (ChatThread ct : server.userMap.values()) {
                    ct.pStream.println(Message.LogoutMsg + "###" + msg);
                }
                server.userMap.remove(msg);
                for (String s : server.groupMap.keySet()) {
                    ArrayList<String> a = server.groupMap.get(s);
                    if (a.contains(msg)) {
                        a.remove(msg);
                        if (a.isEmpty()) {
                            server.groupMap.remove(s);
                        }
                    }
                }
            }
            server.UpdateList();
        }

        private void setNewChatRoom(String gn, String stern, String un) {
            if (un.length() == 0)
                return;
            if (server.groupMap.containsKey(gn)) {
                server.userMap.get(stern).pStream.println(Message.GroupExsistMsg + "###" + gn + "###" + un);
                return;
            }
            String[] names = un.split("@@@");

            ArrayList<String> arrayList = new ArrayList<>();
            arrayList.add(stern);
            for (String name : names) {
                arrayList.add(name);
            }
            server.groupMap.put(gn, arrayList);
        }
    }
}




