package client;

import java.net.*;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.awt.image.*;
import java.awt.event.*;
import java.util.ArrayList;

public class Client {
    private final static int WIDTH_PROGRAM = 900;
    private final static int HEIGHT_PROGRAM = 600;
    private final static int WIDTH_PAINT = 800;
    private final static int HEIGHT_PAINT = 600;
    private final static int WIDTH_NOTIFICATION = 400;
    private final static int HEIGHT_NOTIFICATION = 50;

    private boolean isConnected = false;
    private Socket clientSocket;
    private BufferedReader readSocket;
    private BufferedWriter writeSocket;

    private JFrame frame;
    private JPanel menu;
    private BoardPanel boardPanel;
    private BufferedImage board = null;
    private Graphics2D graphics;
    private Color mainColor;
    private final ArrayList<String> nameOfBoards = new ArrayList<>();
    private int size = 10;

    private class BoardPanel extends JPanel{
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(board, 0, 0, this);
        }
    }

    private class ServerMessageHandler extends Thread {
        private String message;
        private String[] splitMessage;

        public ServerMessageHandler() {
            this.start();
        }

        @Override
        public void run() {
            try {
                try {
                    while (true) {
                        message = readSocket.readLine();
                        if (message.contains("NAMES:")) {
                            nameOfBoards.clear();
                            message = message.replaceFirst("NAMES:", "");
                            splitMessage = message.split(";");
                            for (int i = 0; i < splitMessage.length; ++i) {
                                nameOfBoards.add(splitMessage[i]);
                            }
                            continue;
                        }
                        splitMessage = message.split(" ", 2);
                        if (splitMessage[0].equals("CREATE")) {
                            if (splitMessage[1].equals("OK")) {
                                board = new BufferedImage(WIDTH_PAINT, HEIGHT_PAINT, BufferedImage.TYPE_INT_RGB);
                                graphics = board.createGraphics();
                                graphics.setColor(Color.white);
                                graphics.fillRect(0, 0, WIDTH_PAINT, HEIGHT_PAINT);
                                isConnected = true;
                                frame.remove(menu);
                                frame.add(boardPanel);
                                frame.repaint();
                            } else if (splitMessage[1].equals("EXISTS")) {
                                createNotification("Error! Board with this name was already created!");
                            }
                        } else if (splitMessage[0].equals("CONNECT")) {
                            if (splitMessage[1].equals("OK")) {
                                int[] rgbArray = new int[HEIGHT_PAINT * WIDTH_PAINT];
                                for (int i = 0; i < rgbArray.length; i++) {
                                    message = readSocket.readLine();
                                    rgbArray[i] = Integer.parseInt(message);
                                }
                                board = new BufferedImage(WIDTH_PAINT, HEIGHT_PAINT, BufferedImage.TYPE_INT_RGB);
                                board.setRGB(0, 0, WIDTH_PAINT, HEIGHT_PAINT, rgbArray, 0, WIDTH_PAINT);
                                graphics = board.createGraphics();
                                isConnected = true;
                                frame.remove(menu);
                                frame.add(boardPanel);
                                frame.repaint();
                            } else if (splitMessage[1].equals("NOT FOUND")) {
                                createNotification("Error! Board with this name is not found");
                                frame.repaint();
                            }
                        } else {
                            splitMessage = message.split(" ", 4);
                            int color = Integer.parseInt(splitMessage[0]);
                            int coordX = Integer.parseInt(splitMessage[1]);
                            int coordY = Integer.parseInt(splitMessage[2]);
                            int size = Integer.parseInt(splitMessage[3]);

                            graphics.setColor(new Color(color));
                            graphics.fillOval(coordX, coordY, size, size);
                            boardPanel.repaint();
                        }
                    }
                } catch (IOException exception) {
                    System.out.println(exception.toString());
                    readSocket.close();
                    writeSocket.close();
                }
            } catch (IOException exception) {
                System.out.println(exception.toString());
            }
        }
    }

    public Client(String serverHost, int serverPort) {
        try {
            try {
                clientSocket = new Socket(serverHost, serverPort);
                readSocket = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                writeSocket = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
                new ServerMessageHandler();
            } catch (IOException exception) {
                System.out.println(exception.toString());
                readSocket.close();
                writeSocket.close();
            }
        } catch (IOException exception) {
            System.out.println(exception.toString());
        }

        init();
        initTools();
        initListeners();
    }

    public void initListeners() {
        boardPanel.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                try {
                    try {
                        String message = mainColor.getRGB() + " " + (e.getX() - size / 2) + " " + (e.getY() - size / 2)
                                + " " + size;
                        writeSocket.write(message + "\n");
                        writeSocket.flush();
                    } catch (IOException exception) {
                        System.out.println(exception.toString());
                        readSocket.close();
                        writeSocket.close();
                    }
                } catch (IOException exception) {
                    System.out.println(exception.toString());
                }

            }
        });

        boardPanel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                try {
                    try {
                        String message = mainColor.getRGB() + " " + (e.getX() - size / 2) + " " + (e.getY() - size / 2)
                                + " " + size;
                        writeSocket.write(message + "\n");
                        writeSocket.flush();
                    } catch (IOException exception) {
                        System.out.println(exception.toString());
                        readSocket.close();
                        writeSocket.close();
                    }
                } catch (IOException exception) {
                    System.out.println(exception.toString());
                }
            }
        });
    }

    public void init() {
        frame = new JFrame("Paint");
        frame.setSize(WIDTH_PROGRAM, HEIGHT_PROGRAM);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.setVisible(true);

        boardPanel = new BoardPanel();
        boardPanel.setBounds(WIDTH_PROGRAM - WIDTH_PAINT, 0, WIDTH_PAINT, HEIGHT_PAINT);
        boardPanel.setOpaque(true);
        mainColor = Color.white;

        menu = new JPanel();
        menu.setBounds(WIDTH_PROGRAM - WIDTH_PAINT, 0, WIDTH_PAINT, HEIGHT_PAINT);
        menu.setBackground(Color.WHITE);
        menu.setLayout(new FlowLayout());
        frame.add(menu);

        JLabel inviteLabel = new JLabel("Enter the name of board:");
        menu.add(inviteLabel);
        JTextField textField = new JTextField();
        textField.setText("default name board");
        menu.add(textField);
        JButton createBoard = new JButton("Create");
        createBoard.addActionListener(event -> {
            String nameBoard = textField.getText();
            if (nameBoard.equals("")) {
                frame.repaint();
                return;
            }
            try {
                try {
                    writeSocket.write("CREATE " + nameBoard + "\n");
                    writeSocket.flush();
                } catch (IOException exception) {
                    System.out.println(exception.toString());
                    readSocket.close();
                    writeSocket.close();
                }
            } catch (IOException exception) {
                System.out.println(exception.toString());
            }
        });
        menu.add(createBoard);

        JButton joinBoard = new JButton("Connect");
        joinBoard.addActionListener(event -> {
            String nameBoard = textField.getText();
            if (nameBoard.equals("")) {
                System.out.println();
                frame.repaint();
                return;
            }
            try {
                try {
                    writeSocket.write("CONNECT " + nameBoard + "\n");
                    writeSocket.flush();
                } catch (IOException exception) {
                    System.out.println(exception.toString());
                    readSocket.close();
                    writeSocket.close();
                }
            } catch (IOException exception) {
                System.out.println(exception.toString());
            }
        });
        menu.add(joinBoard);

        JButton getBoardlist = new JButton("Look boards");
        getBoardlist.addActionListener(event -> {
            try {
                try {
                    writeSocket.write("GIVE BOARDS\n");
                    writeSocket.flush();
                    JFrame boardsFrame = new JFrame("Boards:");
                    boardsFrame.setLayout(new FlowLayout());
                    boardsFrame.setSize(WIDTH_PAINT / 2, HEIGHT_PAINT);
                    boardsFrame.setResizable(false);
                    boardsFrame.setVisible(true);
                    for (int i = 0; i < nameOfBoards.size(); i++) {
                        if (!nameOfBoards.get(i).equals("")) {
                            JButton button = new JButton(nameOfBoards.get(i));
                            int finalI = i;
                            button.addActionListener(e -> {
                                try {
                                    try {
                                        writeSocket.write("CONNECT " + nameOfBoards.get(finalI) + "\n");
                                        writeSocket.flush();
                                    } catch (IOException exception) {
                                        System.out.println(exception.toString());
                                        readSocket.close();
                                        writeSocket.close();
                                    }
                                } catch (IOException exception) {
                                    System.out.println(exception.toString());
                                }
                            });
                            boardsFrame.add(button);
                        }
                    }
                } catch (IOException exception) {
                    System.out.println(exception.toString());
                    readSocket.close();
                    writeSocket.close();
                }
            } catch (IOException exception) {
                System.out.println(exception.toString());
            }
        });
        menu.add(getBoardlist);
    }

    public void initTools() {
        JToolBar toolbar = new JToolBar("Toolbar");
        toolbar.setLayout(new BorderLayout());
        toolbar.setFloatable(false);
        toolbar.setBorderPainted(false);
        toolbar.setBackground(Color.GRAY);
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        addTool(panel, "menu");
        addTool(panel, "size10");
        addTool(panel, "size20");
        addTool(panel, "size40");
        addTool(panel, "size80");
        addTool(panel, "white");
        addTool(panel, "black");
        addTool(panel, "red");
        addTool(panel, "orange");
        addTool(panel, "yellow");
        addTool(panel, "green");
        addTool(panel, "cyan");
        addTool(panel, "blue");
        addTool(panel, "magenta");
        toolbar.add(panel);
        frame.add(toolbar, BorderLayout.WEST);
    }

    public void createNotification(String message) {
        JFrame notification = new JFrame("Notification");
        notification.setSize(WIDTH_NOTIFICATION, HEIGHT_NOTIFICATION);
        notification.setResizable(false);
        notification.setVisible(true);
        JLabel text = new JLabel(message);
        notification.getContentPane().add(BorderLayout.PAGE_START, text);
    }

    public void addTool(JPanel panel, String tool) {
        JButton button = new JButton(new ImageIcon(this.getClass().getClassLoader().getResource(tool + ".png")));
        button.setBorderPainted(false);
        button.setBackground(Color.lightGray);
        button.setOpaque(false);
        button.addActionListener(event -> {
            switch (tool) {
                case "menu":
                    if (isConnected) {
                        if (frame.isAncestorOf(menu)) {
                            frame.remove(menu);
                            frame.add(boardPanel);
                        } else {
                            frame.remove(boardPanel);
                            frame.add(menu);
                        }
                        frame.repaint();
                    }
                    break;
                case "size10":
                    size = 10;
                    break;
                case "size20":
                    size = 20;
                    break;
                case "size40":
                    size = 40;
                    break;
                case "size80":
                    size = 80;
                    break;
                case "white":
                    mainColor = Color.white;
                    break;
                case "black":
                    mainColor = Color.black;
                    break;
                case "red":
                    mainColor = Color.red;
                    break;
                case "orange":
                    mainColor = Color.orange;
                    break;
                case "yellow":
                    mainColor = Color.yellow;
                    break;
                case "green":
                    mainColor = Color.green;
                    break;
                case "cyan":
                    mainColor = Color.cyan;
                    break;
                case "blue":
                    mainColor = Color.blue;
                case "magenta":
                    mainColor = Color.magenta;
                    break;
                default:
                    mainColor = Color.black;
                    break;
            }
        });
        panel.add(button);
    }
}