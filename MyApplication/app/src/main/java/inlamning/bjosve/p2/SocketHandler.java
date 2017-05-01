package inlamning.bjosve.p2;

import android.app.AlertDialog;
import android.util.JsonWriter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.Socket;

import static android.os.Build.VERSION_CODES.N;


/**
 * Created by bjorsven on 2017-04-30.
 */

public class SocketHandler extends Thread {
    private Socket socket;
    private MapsActivity activity;
    private DataInputStream dis;
    private DataOutputStream dos;
    private String message;
    private boolean runningListener;
    private String id = null;


    public SocketHandler(MapsActivity activity) {

        this.activity = activity;
        new Initializer().start();
    }


    public void shutDownSocket() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void viewGroups() {
        new Sender("groups").start();
    }

    public void sendMessage() {

    }

    public void registerToGroup() {
        new Sender("register").start();
    }

    public void unregister() {
        new Sender("unregister").start();
    }


    private class Initializer extends Thread {
        public void run() {
            try {
                socket = new Socket(InetAddress.getByName("195.178.227.53"), 7117);
                dis = new DataInputStream(socket.getInputStream());
                dos = new DataOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
            runningListener = true;
            new Listener().start();
        }
    }

    private class Listener extends Thread {

        public void run() {

            while (runningListener) {
                try {
                    message = dis.readUTF();
                    final JSONObject json = new JSONObject(message);


                    switch (json.getString("type")) {
                        case "exception":
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        TextView rowTextView = new TextView(activity);
                                        rowTextView.setText(json.getString("message"));
                                        activity.llTexts.addView(rowTextView);
                                    }
                                    catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            break;
                        case "register":
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    TextView register = new TextView(activity);
                                    try {
                                        id = json.getString("id");
                                        register.setText("Registered " + json.getString("id") +
                                                " to group " + json.getString("group"));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }


                                    activity.llTexts.addView(register);

                                }
                            });

                            break;
                        case "unregister":
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    TextView unregister = new TextView(activity);
                                        unregister.setText("Unregistered " + id);


                                    activity.llTexts.addView(unregister);
                                }
                            });
                            break;
                        case "groups":
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        JSONArray jsonArray = json.getJSONArray("groups");
                                        for (int i = 0; i < jsonArray.length(); i++) {
                                            final TextView groupText = new TextView(activity);
                                            groupText.setText("Group #" + i + ": " +
                                                    jsonArray.getString(i));

                                            activity.llTexts.addView(groupText);
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            break;
                    }
            } catch(JSONException e){
                e.printStackTrace();
            } catch(IOException e){
                e.printStackTrace();
            }
        }
    }
}

private class Sender extends Thread {

    private String sendTo;

    public Sender(String sendTo) {

        this.sendTo = sendTo;
    }

    public void run() {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        final JSONObject json;
        try {

            switch (sendTo) {
                case "register":
                    writer.beginObject()
                            .name("type").value("register")
                            .name("group").value("test")
                            .name("member").value("bjaaern")
                            .endObject();


                    json = new JSONObject(stringWriter.toString());
                    dos.writeUTF(json.toString());
                    dos.flush();
                    break;
                case "unregister":
                    if(id!=null) {
                        writer.beginObject()
                                .name("type").value("unregister")
                                .name("id").value(id)
                                .endObject();
                        json = new JSONObject(stringWriter.toString());
                        dos.writeUTF(json.toString());
                        dos.flush();
                    }
                    else{
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(activity,"Not registered in any groups",Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                    break;
                case "groups":
                    writer.beginObject()
                            .name("type").value("groups")
                            .endObject();
                    json = new JSONObject(stringWriter.toString());
                    dos.writeUTF(json.toString());
                    dos.flush();
                    break;

            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
}
