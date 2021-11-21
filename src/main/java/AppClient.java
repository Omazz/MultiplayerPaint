import client.Client;

import javax.swing.*;

public class AppClient {
    public static void main(String[] args) {
        if (args.length != 0) {
            if (args.length == 2) {
                System.out.println("CLIENT");
                SwingUtilities.invokeLater(() -> {
                    new Client(args[0], Integer.parseInt(args[1]));
                });
            } else for (String arg : args) {
                System.out.println(arg);
            }
        } else {
            System.out.println("args = 0");
        }
    }
}
