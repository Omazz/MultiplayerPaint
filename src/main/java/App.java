import javax.swing.*;

import server.*;
import client.*;

public class App {
    public static void main(String[] args) {
        if (args.length != 0) {
            if (args[0].equals("server")) {
                System.out.println("SERVER");
                new Server();
            } else if (args[0].equals("client") && args.length == 3) {
                System.out.println("CLIENT");
                SwingUtilities.invokeLater(() -> {
                    new Client(args[1], Integer.parseInt(args[2]));
                });
            } else {
                for (String arg : args) {
                    System.out.println(arg);
                }
            }
        } else {
            System.out.println("args = 0");
        }
    }
}