
// Created December 29, 2018 by Pawel Saniewski


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.*;

public class Server {

    private int[] portSeq;
    private List<Map<InetAddress, Integer>> clients;

    public Server(int noOfPorts, int... portSeq) throws SocketException {
        this.clients = Collections.synchronizedList(new LinkedList<>());
        this.portSeq = portSeq;

        for (int i = 0; i < portSeq.length; i++) {
            this.clients.add(new HashMap<>());
            new ServerThread(portSeq[i], i).start();
        }

        for (int i = 0; i < noOfPorts; i++) {
            new ServerThread().start();
        }
    }

    private class ServerThread extends Thread {

        private boolean running;
        private int portID;
        private DatagramSocket socket;
        private byte[] buf = new byte[256];

        public ServerThread() throws SocketException {
            this.socket = new DatagramSocket();
            this.portID = -1;
        }

        public ServerThread(int port, int portID) throws SocketException {
            this.socket = new DatagramSocket(port);
            this.portID = portID;
        }

        public void run() {
            this.running = true;

            while(running) {

                try {

                    this.buf = new byte[256];
                    DatagramPacket packet = new DatagramPacket(this.buf, this.buf.length);

                    this.socket.receive(packet);

                    String packetReceived = new String(packet.getData(), 0, packet.getLength());

                    /* FOR DEBUGGING: *///System.out.println("UDP Socket " + this.portID + ": Packet received: " + packetReceived);


                    if (packetReceived.equals("cleanup")
                                || !packetReceived.equals(String.valueOf(this.portID))
                                || (this.portID == -1 && packet.getData() != null)) {

                        for (Map<InetAddress, Integer> knocks : clients) {
                            knocks.remove(packet.getAddress());
                        }

                        /* FOR DEBUGGING: *///System.out.println("UDP Socket " + this.portID + ": Cleanup succesful");

                    } else if (packetReceived.equals(String.valueOf(this.portID))) {
                        boolean add = true;

                        for (int i = 0; i < clients.size() && i != this.portID && add; i++) {
                            if (!clients.get(i).containsKey(packet.getAddress())) {
                                add = false;
                            }
                        }

                        if (add) {
                            clients.get(this.portID).putIfAbsent(packet.getAddress(), packet.getPort());
                        }

                        if (this.portID == clients.size()-1 && add) {

                            System.out.println("Client (IP: " + packet.getAddress() + " ; port: " + packet.getPort() + ") connected to a correct sequence of UDP ports.");


                            new Thread(() -> {

                                try (ServerSocket ss = new ServerSocket(0)) {

                                    this.buf = String.valueOf(ss.getLocalPort()).getBytes();

                                    System.out.println("Sending TCP port number (" + ss.getLocalPort() + ") to client (IP: " + packet.getAddress() + " ; port: " + packet.getPort() + ")...");

                                    this.socket.send(new DatagramPacket(this.buf, this.buf.length, packet.getAddress(), packet.getPort()));


                                    System.out.println("Server (" + InetAddress.getLocalHost().toString() + ") is listening on TCP port: " + ss.getLocalPort());


                                    Socket client = ss.accept();

                                    try (
                                            ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
                                            ObjectInputStream in = new ObjectInputStream(client.getInputStream())
                                    ) {

                                        String msg = in.readUTF();
                                        System.out.println("Message received:\n\t" + msg);

                                        if (msg.equals("Is connection established?")) {
                                            msg = "Yes, connection is established.";

                                            out.writeUTF(msg);
                                            out.flush();

                                            System.out.println("Message sent:\n\t" + msg);
                                        }
                                    } catch (IOException ex) {
                                        ex.printStackTrace();
                                    }
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                            }).start();
                        }
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

    }

}
