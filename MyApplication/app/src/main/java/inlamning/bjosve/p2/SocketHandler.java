package inlamning.bjosve.p2;

import android.app.AlertDialog;
import android.location.Location;
import android.util.JsonWriter;
import android.widget.ScrollView;
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
import java.util.Timer;

import static android.os.Build.VERSION_CODES.N;


/**
 * Created by bjorsven on 2017-04-30.
 */

public class SocketHandler {
    private Socket socket;
    private MapsActivity activity;
    private DataInputStream dis;
    private DataOutputStream dos;
    private String message;
    protected boolean runningListener;
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

    public void registerToGroup(String groupName) {
        new Sender("register", groupName).start();
    }

    public void unregister() {
        new Sender("unregister").start();
    }

    public void viewMembers(String groupName) {
        new Sender("members", groupName).start();
    }
    public void sendLocation(Location location){
        new Sender("location", location).start();
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
            new LocationHandler().start();
        }
    }

    private class LocationHandler extends Thread {

        public void run() {
            while (runningListener) {

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        activity.sendNewLocation();
                    }
                });

                try {
                    Thread.sleep(30000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
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
                                    } catch (JSONException e) {
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
                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            activity.sendNewLocation();
                                        }
                                    });

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
                        case "members":
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        JSONArray jsonArray = json.getJSONArray("members");
                                        TextView group = new TextView(activity);
                                        group.setText("Group " + json.getString("group") + ":");
                                        activity.llTexts.addView(group);
                                        for (int i = 0; i < jsonArray.length(); i++) {
                                            JSONObject jsonMember = jsonArray.getJSONObject(i);
                                            final TextView memberText = new TextView(activity);
                                            memberText.setText("Member #" + i + ": " +
                                                    jsonMember.getString("member"));

                                            activity.llTexts.addView(memberText);
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
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
                                            JSONObject jsonGroup = jsonArray.getJSONObject(i);
                                            groupText.setText("Group #" + i + ": " +
                                                    jsonGroup.getString("group"));

                                            activity.llTexts.addView(groupText);
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            break;
                        case "location":
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        activity.pinMap(json.getString("id"), Double.parseDouble(json.getString("longitude")), Double.parseDouble(json.getString("latitude")));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                }
                            });

                            break;
                        case "locations":
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        activity.clearMap();
                                        JSONArray jsonArray = json.getJSONArray("location");
                                        TextView group = new TextView(activity);
                                        group.setText("Group " + json.getString("group") + ":");
                                        activity.llTexts.addView(group);
                                        for (int i = 0; i < jsonArray.length(); i++) {
                                            JSONObject jsonMember = jsonArray.getJSONObject(i);
                                            final TextView memberText = new TextView(activity);
                                            memberText.setText(jsonMember.getString("member") + " " +
                                                    jsonMember.getString("longitude") + " / " +
                                                    jsonMember.getString("latitude"));
                                            activity.llTexts.addView(memberText);
                                            double longitude = Double.parseDouble(jsonMember.getString("longitude"));
                                            double latitude =  Double.parseDouble(jsonMember.getString("latitude"));
                                            activity.pinMap(jsonMember.getString("member"),longitude,latitude);
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            break;


                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        activity.svText.fullScroll(ScrollView.FOCUS_DOWN);
                    }
                });
            }
        }
    }

    private class Sender extends Thread {

        private String sendTo;
        private Location location;
        private String groupName;

        public Sender(String sendTo) {

            this.sendTo = sendTo;
        }

        public Sender(String sendTo, Location location) {
            this.sendTo = sendTo;
            this.location = location;
        }

        public Sender(String sendTo, String groupName) {
            this.sendTo = sendTo;
            this.groupName = groupName;
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
                                .name("group").value(groupName)
                                .name("member").value("Bjaern")
                                .endObject();


                        json = new JSONObject(stringWriter.toString());
                        dos.writeUTF(json.toString());
                        dos.flush();
                        break;
                    case "unregister":
                        if (id != null) {
                            writer.beginObject()
                                    .name("type").value("unregister")
                                    .name("id").value(id)
                                    .endObject();
                            json = new JSONObject(stringWriter.toString());
                            dos.writeUTF(json.toString());
                            dos.flush();
                        } else {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(activity, "Not registered in any groups", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        break;
                    case "members":
                        writer.beginObject()
                                .name("type").value("members")
                                .name("group").value(groupName)
                                .endObject();
                        json = new JSONObject(stringWriter.toString());
                        dos.writeUTF(json.toString());
                        dos.flush();
                        break;
                    case "groups":
                        writer.beginObject()
                                .name("type").value("groups")
                                .endObject();
                        json = new JSONObject(stringWriter.toString());
                        dos.writeUTF(json.toString());
                        dos.flush();
                        break;
                    case "location":
                        if (id != null) {
                            writer.beginObject()
                                    .name("type").value("location")
                                    .name("id").value(id)
                                    .name("longitude").value(String.valueOf(location.getLongitude()))
                                    .name("latitude").value(String.valueOf(location.getLatitude()))
                                    .endObject();
                            json = new JSONObject(stringWriter.toString());
                            dos.writeUTF(json.toString());
                            dos.flush();
                        } else {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(activity, "Not registered in any groups", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
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
