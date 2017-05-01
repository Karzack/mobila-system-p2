package inlamning.bjosve.p2;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by bjorsven on 2017-04-30.
 */

public class SocketHandler extends Thread{
    private Socket socket;
    private MapsActivity activity;
    private DataInputStream dis;
    private DataOutputStream dos;

    public SocketHandler(MapsActivity activity) {

        this.activity = activity;

    }

    @Override
    public void run() {
        try {
            socket = new Socket(InetAddress.getByName("195.178.227.53"), 7117);
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    public void closeSocket() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
