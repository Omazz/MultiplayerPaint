package server;

import client.Client;

import java.io.*;
import java.awt.*;
import java.net.*;
import java.util.*;
import java.awt.image.*;

public class Server {
    private ServerSocket serverSocket = null;
    private final HashMap<String, BufferedImage> boards;
    private final ArrayList<ClientThread> clients;

    public class ClientThread extends Thread {
        private Socket clientSocket = null;
        private BufferedReader readSocket = null;
        private BufferedWriter writeSocket = null;
        private String boardName = null;

        private Graphics2D graphics = null;

        public ClientThread(Socket clientSocket) {
            this.clientSocket = clientSocket;
            try {
                readSocket = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                writeSocket = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            } catch (IOException exception) {
                System.out.println(exception.getMessage());
            }
        }

        @Override
        public void run() {
            sendReport("The client " + this.getName() + " is connected");
            synchronized (clients) {
                sendReport("Number of clients: " + clients.size() + "\n");
            }
            try {
                try {
                    while (true) {
                        String message = readSocket.readLine();
                        if (message.contains("GIVE BOARDS")) {
                            ArrayList<String> names = new ArrayList<>(boards.keySet());
                            StringBuilder boardNames = new StringBuilder("NAMES:");
                            for (String name : names) {
                                boardNames.append(name).append(";");
                            }
                            writeSocket.write(boardNames + "\n");
                            writeSocket.flush();
                            continue;
                        }
                        String[] splitMessage = message.split(" ", 2);
                        if (splitMessage[0].equals("CREATE")) {
                            boolean isContains;
                            synchronized (boards) {
                                isContains = boards.containsKey(splitMessage[1]);
                            }
                            if (isContains) {
                                synchronized (this) {
                                    writeSocket.write("CREATE EXISTS\n");
                                    writeSocket.flush();
                                }
                            } else {
                                synchronized (this) {
                                    writeSocket.write("CREATE OK\n");
                                    writeSocket.flush();
                                }
                                String boardNameOld = boardName;

                                boardName = splitMessage[1];
                                synchronized (boards) {
                                    boards.put(boardName, new BufferedImage(Client.WIDTH_PAINT, Client.HEIGHT_PAINT, BufferedImage.TYPE_INT_RGB));
                                    graphics = boards.get(boardName).createGraphics();
                                }
                                synchronized (boards.get(boardName)) {
                                    graphics.setColor(Color.white);
                                    graphics.fillRect(0, 0, Client.WIDTH_PAINT, Client.HEIGHT_PAINT);
                                }
                                sendReport("Board \"" + boardName + "\" is created by " + this.getName() + "\n");
                                synchronized (boards) {
                                    sendReport("Number of boards: " + boards.size() + "\n");
                                }
                                System.out.println("We create board with name " + splitMessage[1]);
                                checkBoards(boardNameOld);
                            }
                        } else if (splitMessage[0].equals("CONNECT")) {
                            boolean isContains;
                            synchronized (boards) {
                                isContains = boards.containsKey(splitMessage[1]);
                            }
                            if (isContains) {
                                synchronized (this) {
                                    writeSocket.write("CONNECT OK\n");
                                    writeSocket.flush();
                                }
                                String boardNameOld = boardName;

                                boardName = splitMessage[1];
                                synchronized (boards.get(boardName)) {
                                    graphics = boards.get(boardName).createGraphics();
                                }
                                int[] rgbArray = new int[Client.WIDTH_PAINT * Client.HEIGHT_PAINT];
                                synchronized (boards.get(boardName)) {
                                    boards.get(boardName).getRGB(0, 0, Client.WIDTH_PAINT, Client.HEIGHT_PAINT, rgbArray, 0, Client.WIDTH_PAINT);
                                }
                                synchronized (this) {
                                    for (int j : rgbArray) {
                                        writeSocket.write(j + "\n");
                                        writeSocket.flush();
                                    }
                                }
                                checkBoards(boardNameOld);
                            } else {
                                synchronized (this) {
                                    writeSocket.write("CONNECT NOT FOUND\n");
                                    writeSocket.flush();
                                }
                            }
                        } else if (boardName != null) {
                            splitMessage = message.split(" ", 4);
                            int color = Integer.parseInt(splitMessage[0]);
                            int coordX = Integer.parseInt(splitMessage[1]);
                            int coordY = Integer.parseInt(splitMessage[2]);
                            int size = Integer.parseInt(splitMessage[3]);
                            System.out.println(message);
                            synchronized (boards.get(boardName)) {
                                graphics.setColor(new Color(color));
                                graphics.fillOval(coordX, coordY, size, size);
                            }

                            synchronized (clients) {
                                for (ClientThread clientThread : clients) {
                                    if (clientThread.boardName != null && clientThread.boardName.equals(boardName)) {
                                        synchronized (clientThread) {
                                            clientThread.writeSocket.write(message + "\n");
                                            clientThread.writeSocket.flush();
                                        }
                                    }
                                }
                            }
                        }
                    }
                } finally {
                    clientSocket.close();
                    readSocket.close();
                    writeSocket.close();
                    synchronized (clients) {
                        clients.remove(this);
                        sendReport("The client " + this.getName() + " is unavailable.\nNumber of clients: \"" + clients.size());
                    }
                    checkBoards(boardName);
                }
            } catch (Exception exception) {
                System.out.println(exception.toString() + "\n");
            }
        }
    }

    public Server() {
        boards = new HashMap<>();
        clients = new ArrayList<>();
        try {
            serverSocket = new ServerSocket(0);
            System.out.println("PORT: " + serverSocket.getLocalPort());
            while (true) {
                ClientThread newClient = new ClientThread(serverSocket.accept());
                newClient.setName("Client" + clients.size());
                synchronized (clients) {
                    clients.add(newClient);
                    clients.get(clients.size() - 1).start();
                }
            }
        } catch (IOException exception) {
            System.out.println(exception.getMessage());
        }
    }

    public Server(boolean test) {
        boards = new HashMap<>();
        clients = new ArrayList<>();
        try {
            serverSocket = new ServerSocket(0);
            System.out.println("PORT: " + serverSocket.getLocalPort());
            if (test) {
                return;
            } else {
                while (true) {
                    ClientThread newClient = new ClientThread(serverSocket.accept());
                    newClient.setName("Client" + clients.size());
                    synchronized (clients) {
                        clients.add(newClient);
                        clients.get(clients.size() - 1).start();
                    }
                }
            }
        } catch (IOException exception) {
            System.out.println(exception.getMessage());
        }
    }

    public boolean isCreated() {
        return serverSocket.getLocalPort() > 0;
    }

    public boolean checkBoards(String boardName) {
        if (boardName == null) {
            return false;
        }
        boolean boardUsed = false;
        for (ClientThread clientThread : clients) {
            synchronized (clientThread) {
                if (clientThread.boardName != null && clientThread.boardName.equals(boardName)) {
                    boardUsed = true;
                    break;
                }
            }
        }
        if (!boardUsed) {
            synchronized (boards) {
                boards.remove(boardName);
                sendReport("Board \"" + boardName + "\" is not using and was deleted\nNumber of boards: " + boards.size() + "\n");
                return false;
            }
        }
        return true;
    }

    public boolean sendReport(String report) {
        System.out.println(report);
        return LogWriter.writeEvent(report);
    }

    public boolean isClientsEmpty() {
        return clients.size() == 0;
    }

    public boolean isBoardsEmpty() {
        return boards.size() == 0;
    }
}