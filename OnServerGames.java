package game;

import game.Process.UserLoop;
import game.Server.GameData;
import game.Server.User;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * This class gets the current games on the
 * server that are available and show them to the
 * client to choose a game or create on.
 */
public class OnServerGames extends JFrame {
    // Private fields
    private JButton send;
    private JButton create;
    private Socket connectionSocket;
    private ArrayList<GameData> data;
    private GameData finalChose;
    private JPanel c;

    /**
     * The constructor of the on server games.
     */
    public OnServerGames() {
        //
        setSize(new Dimension(500,500));
        setLocation(250,100);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setResizable(false);
        //
        try {
            c = new MainPanel(ImageIO.read(new File("src/game/IconsInGame/Farshid/background.png")));
            add(c);
        } catch (IOException e) {
            e.printStackTrace();
        }
        c.setLayout(null);
        //
        init();
        frameCreate();
        initButtons();
        //
        setVisible(true);
    }

    private void init() {
        send = new JButton("Send");
        create = new JButton("Create");
        try {
            connectionSocket = new Socket("127.0.0.1", 1724);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            data = (ArrayList<GameData>) new ObjectInputStream(connectionSocket.getInputStream()).readObject(); // Receiving the data
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void frameCreate() {
        //
        int counter = 1;
        for (GameData d : data) {
            JButton jButton = new JButton();
            jButton.setSize(new Dimension(200, 20));
            jButton.setText(d.toString());
            jButton.setLocation(150, counter * 20);
            jButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    finalChose = d;
                }
            });
            c.add(jButton);
            counter++;
        }
        //
        create.setSize(new Dimension(100, 20));
        send.setSize(new Dimension(100,20));
        create.setLocation(150, counter * 20);
        send.setLocation(250, counter * 20);
        c.add(send);
        c.add(create);
    }

    private void initButtons() {
        send.addActionListener(e -> new SwingWorker<>(){

            @Override
            protected Object doInBackground() throws Exception {
                connectionSocket.getOutputStream().write(1);
                if (finalChose == null)
                    return -1;
                else
                    return 0;
            }

            @Override
            protected void done() {
                setVisible(false);
                try {
                    User user = (User) new ObjectInputStream(connectionSocket.getInputStream()).readObject();
                    int result = (int) get();
                    if (result == -1)
                        System.exit(-1);
                    else {
                        user.gameData = finalChose;
                        UserLoop userLoop = new UserLoop(user);
                        userLoop.initialize();
                        userLoop.start();
                    }
                } catch (InterruptedException | ExecutionException | IOException | ClassNotFoundException ex) {
                    System.exit(-1);
                }
            }
        }.execute());
        create.addActionListener(e -> new SwingWorker<>(){

            @Override
            protected Object doInBackground() throws Exception {
                connectionSocket.getOutputStream().write(0);
                return null;
            }

            @Override
            protected void done() {
                setVisible(false);
                Setting.run();
            }
        }.execute());
    }

    private static class MainPanel extends JPanel {
        private Image bg;

        MainPanel(Image bg) {
            this.bg = bg;
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(bg, 0, 0, getWidth(), getHeight(), this);
        }
    }
}