
// Created December 29, 2018 by Pawel Saniewski


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.concurrent.TimeUnit;

public class Client {

    private int[] portSeq;
    private InetAddress hostIP;
    private DatagramSocket socket;
    private byte[] buf = new byte[256];

    public Client(String ipAddr, int... portSeq) throws SocketException, UnknownHostException {
        this.socket = new DatagramSocket();
        this.socket.setSoTimeout(10000);
        this.portSeq = portSeq;
        this.hostIP = InetAddress.getByName(ipAddr);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                this.socket = new DatagramSocket();

                for (int port : this.portSeq) {
                    this.buf = "cleanup".getBytes();
                    this.socket.send(new DatagramPacket(this.buf, this.buf.length, this.hostIP, port));
                }

                this.close();

            } catch (SocketException ex) {
                ex.printStackTrace();
            } catch (UnknownHostException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }));
    }

    public void knock() {

        try {

            for (int i = 0; i < this.portSeq.length; i++) {
                this.buf = String.valueOf(i).getBytes();
                this.socket.send(new DatagramPacket(this.buf, this.buf.length, this.hostIP, this.portSeq[i]));
                TimeUnit.MILLISECONDS.sleep(100);
            }

            this.buf = new byte[256];
            DatagramPacket packet = new DatagramPacket(this.buf, this.buf.length);

            socket.receive(packet);

            String tcpPort = new String(packet.getData(), 0, packet.getLength());

            System.out.println("Connecting to port: " + tcpPort);

            tcpConnect(Integer.parseInt(tcpPort));

        } catch (SocketTimeoutException ex) {
            System.err.println("timeout");
            return;
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    private void tcpConnect(int tcpPort) {

        try (
                Socket client = new Socket(this.hostIP.getHostAddress(), tcpPort);
                ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(client.getInputStream())
        ) {

            String msg = "Is connection established?";

            out.writeUTF(msg);
            out.flush();

            System.out.println("Message sent:\n\t" + msg);

            TimeUnit.MILLISECONDS.sleep(100);

            msg = in.readUTF();
            System.out.println("Message received:\n\t" + msg);

        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    public void close() {
        this.socket.close();
    }

}
