
// Created December 29, 2018 by Pawel Saniewski


import java.net.SocketException;
import java.net.UnknownHostException;

public class Main {

    private static final String USAGE = "\nUse the following format to use the program:" +
            "\n\tjava Main [argument1] [args...]" +
            "\n\nargument1\t(client) ip address of the server" +
            "\n\t\t\t(server) number of ports to listen on" +
            "\n\nargs...\t\tspace-separated ports sequence";


    public static void main(String[] args) {

        if (args.length >= 2) {

            boolean correctArgs = true;
            int[] ports = new int[args.length-1];

            for (int i = 1; i < args.length && correctArgs; i++) {

                if (!args[i].matches("\\d{1,5}")) {
                    correctArgs = false;

                } else {
                    ports[i-1] = Integer.parseInt(args[i]);
                }

            }

            if ((args[0].matches("\\d{1,3}.\\d{1,3}.\\d{1,3}.\\d{1,3}") || args[0].equals("localhost")) && correctArgs) {

                try {
                    new Client(args[0], ports).knock();
                } catch (SocketException | UnknownHostException ex) {
                    ex.printStackTrace();
                }

            } else if (args[0].matches("\\d+") && correctArgs) {

                try {
                    new Server(Integer.parseInt(args[0]), ports);
                } catch (SocketException ex) {
                    ex.printStackTrace();
                }

            } else System.err.println(Main.USAGE);

        } else System.err.println(Main.USAGE);



        /* SIMPLE TESTING: */


//        try {
//            //new Server(5, 12341, 12342, 12343);
//            new Client("127.0.0.1", 12341, 12342, 12343).knock();
//        } catch (SocketException ex) {
//            ex.printStackTrace();
//        } catch (UnknownHostException ex) {
//            ex.printStackTrace();
//        }


//        java.util.Scanner scan = new java.util.Scanner(System.in);
//        System.out.print("1 for server, 2 for client: ");
//        String response = scan.nextLine();
//
//        switch (response) {
//            case "1":
//                try {
//                    new Server(5, 12341, 12342, 12343, 12344, 12345, 12346, 12347, 12348, 12349, 12340);
//                } catch (SocketException ex) {
//                    ex.printStackTrace();
//                }
//                break;
//            case "2":
//                try {
//                    new Client("127.0.0.1", 12341, 12342, 12343, 12344, 12345, 12346, 12347, 12348, 12349, 12340).knock();
//                } catch (java.io.IOException ex) {
//                    ex.printStackTrace();
//                }
//                break;
//            default:
//                System.out.println("Goodbye!");
//                break;
//        }

    }

}
