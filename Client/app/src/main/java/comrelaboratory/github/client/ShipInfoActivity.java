package comrelaboratory.github.client;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URISyntaxException;
import java.net.URL;

import comrelaboratory.github.client.Socket.SocketApplication;

public class ShipInfoActivity extends AppCompatActivity {
    Socket socket;
    ProgressDialog progressDialog;
    JsonObject jsonObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shipinfo);
        progressDialog = new ProgressDialog(this);

        Thread requestThread = new Thread(() -> {
            while (true) {
                try {
                    URL url = new URL("http://192.168.1.100:8080/info");
                    HttpURLConnection huc = (HttpURLConnection) url.openConnection();
                    huc.setRequestMethod("GET");
                    huc.setDoInput(true);
                    InputStream is = huc.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));

                    String line;
                    StringBuilder result = new StringBuilder();

                    while ((line = reader.readLine()) != null) {
                        result.append(line).append("\n");
                    }
                    jsonObject = new JsonParser().parse(result.toString()).getAsJsonObject();
                    runOnUiThread(() -> {
                        ((TextView) findViewById(R.id.departureTv_info)).setText(jsonObject.get("departure").getAsString());
                        ((TextView) findViewById(R.id.destination_info)).setText(jsonObject.get("destination").getAsString());
                        ((TextView) findViewById(R.id.directionTv_info)).setText(jsonObject.get("direction").getAsString());
                        ((TextView) findViewById(R.id.remainingTime_info)).setText(jsonObject.get("remainingTime").getAsInt() / 60 + "시간 " + jsonObject.get("remainingTime").getAsInt() % 60 + "분");
                        ((TextView) findViewById(R.id.speedTv_info)).setText(jsonObject.get("speedKnot").getAsString() + "knot/" + jsonObject.get("speedKph").getAsString() + "Kph");
                        ((TextView) findViewById(R.id.degreeTv_info)).setText(jsonObject.get("degree").getAsDouble() + "°");
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        requestThread.start();

        findViewById(R.id.mapBtn_info).setOnClickListener(v -> {
            requestThread.interrupt();
            Intent intent = new Intent(this, MapActivity.class);
            startActivity(intent);
        });
        try {
            SocketApplication.mSocket = IO.socket("http://10.156.145.143:8080");
        } catch (URISyntaxException ue) {
            ue.printStackTrace();
        }
        socket = SocketApplication.mSocket;
        socket.on(Socket.EVENT_CONNECT, connectListener);
        socket.on("emergency", emergencyListener);
        socket.connect();
    }

    private Emitter.Listener connectListener = args -> {
        Log.d(Constants.TAG, "Connected");
    };

    private Emitter.Listener emergencyListener = args -> {
        //TODO: 지도 띄워주기 등
        Log.d(Constants.TAG, "Emergency!");
    };
}
