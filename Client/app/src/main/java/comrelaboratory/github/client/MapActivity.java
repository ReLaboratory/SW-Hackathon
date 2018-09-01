package comrelaboratory.github.client;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.estimote.coresdk.observation.region.beacon.BeaconRegion;
import com.estimote.coresdk.recognition.packets.Beacon;
import com.estimote.coresdk.service.BeaconManager;
import com.github.nkzawa.socketio.client.Socket;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

import comrelaboratory.github.client.Socket.SocketApplication;

public class MapActivity extends AppCompatActivity {

    Socket socket;
    BeaconManager beaconManager;
    JsonObject mapObject;
    Beacon currentBeacon;
    CircleView circleView;
    JsonArray beaconArr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        ProgressDialog progressDialog = new ProgressDialog(this);
        //progressDialog.show();

        LinearLayout linearLayout = findViewById(R.id.mapLayout_map);
        ArrayList<BeaconRegion> beaconList = new ArrayList<>();
        socket = SocketApplication.getSocket();

        new Thread(() -> {
            try {
                URL url = new URL("http://192.168.1.100:8080/map");
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

                mapObject = new JsonParser().parse(result.toString()).getAsJsonObject();
            } catch (IOException | ClassCastException e) {
                e.printStackTrace();
            }

            MapView mapView = new MapView(this, mapObject);
            runOnUiThread(() -> linearLayout.addView(mapView));

            beaconArr = Objects.requireNonNull(mapObject).getAsJsonArray("beacon");
            for (int i = 0; i < beaconArr.size(); i++)
                if (!beaconArr.get(i).getAsJsonObject().get("uuid").getAsString().isEmpty())
                    beaconList.add(new BeaconRegion("" + i, UUID.fromString(beaconArr.get(i).getAsJsonObject().get("uuid").getAsString()),
                            beaconArr.get(i).getAsJsonObject().get("major").getAsInt(),
                            beaconArr.get(i).getAsJsonObject().get("minor").getAsInt()));
            //runOnUiThread(progressDialog::dismiss);
        }).start();


        findViewById(R.id.reportBtn_map).setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("신고")
                .setMessage("현재 위치를 신고하시겠습니까")
                .setPositiveButton("네", ((dialog, which) -> {
                    Toast.makeText(this, "신고가 접수되었습니다.", Toast.LENGTH_SHORT).show();
                }))
                .setNegativeButton("아니요", (((dialog, which) -> {

                })))
                .create()
                .show());

        findViewById(R.id.helpBtn_map).setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("도움 요청")
                .setMessage("승무원을 부르시겠습니까?")
                .setPositiveButton("네", ((dialog, which) -> {
                    Toast.makeText(this, "승무원이 오고 있습니다.", Toast.LENGTH_SHORT).show();
                }))
                .setNegativeButton("아니요", (((dialog, which) -> {

                })))
                .create()
                .show());
/*        new Thread(() -> {
            int sec = 0;
            while(true) {
                if(sec > 600)
                    break;
                try {
                    socket.emit("location", currentBeacon);
                    Thread.sleep(1000);
                    sec += 1;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();*/
        beaconManager = new BeaconManager(getApplicationContext());
        beaconManager.setRangingListener((BeaconManager.BeaconRangingListener) (beaconRegion, beacons) -> {
            Log.d(Constants.TAG, beacons.toString());
            if (beacons.get(0).getProximityUUID() == currentBeacon.getProximityUUID())
                currentBeacon = beacons.get(0);
            else if (beacons.get(0).getRssi() < currentBeacon.getRssi())
                currentBeacon = beacons.get(0);

            for (int i = 0; i < beaconArr.size(); i++)
                if (beaconArr.get(i).getAsJsonObject().get("uuid").getAsString().equals(currentBeacon.getProximityUUID().toString())) {
                    linearLayout.removeView(circleView);
                    circleView = new CircleView(MapActivity.this,
                            beaconArr.get(i).getAsJsonObject().get("x").getAsFloat(),
                            beaconArr.get(i).getAsJsonObject().get("y").getAsFloat());
                    linearLayout.addView(circleView);
                    break;
                }
        });
        beaconManager.setForegroundScanPeriod(1000, 1000);
        beaconManager.connect(() -> {
            for (BeaconRegion beacon : beaconList)
                beaconManager.startRanging(beacon);
        });


/*        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter mBluetoothAdapter = Objects.requireNonNull(bluetoothManager).getAdapter();

        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, Constants.REQUEST_BT);
        }
        BluetoothLeScanner bluetoothLeScanner = Objects.requireNonNull(mBluetoothAdapter).getBluetoothLeScanner();
        ScanCallback scanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                BluetoothDevice bluetoothDevice = result.getDevice();
                Log.d(Constants.TAG, "Name: " + bluetoothDevice.getAddress() + " RSSI: " + result.getRssi() + " UUID: " + result.getScanRecord().getServiceUuids() + " Name : " + result.getScanRecord().getDeviceName().getBytes());

            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                super.onBatchScanResults(results);
                for(ScanResult result : results) {
                    BluetoothDevice bluetoothDevice = result.getDevice();
                        Log.d(Constants.TAG, "Name: " + bluetoothDevice.getAddress() + " RSSI: " + result.getRssi() + " UUID: " + result.toString());
                }
            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
                Log.d(Constants.TAG, "" + errorCode);
            }
        };

        List<ScanFilter> scanFilters = new ArrayList<>();
        scanFilters.add(new ScanFilter.Builder().setServiceUuid(ParcelUuid.fromString("05f62a3d-f60f-44bc-b36e-2b80fd6c9679")).build());
        scanFilters.add(new ScanFilter.Builder().setServiceUuid(ParcelUuid.fromString("7727ee98-71cd-465c-a093-a4780e64242c")).build());
        scanFilters.add(new ScanFilter.Builder().setServiceUuid(ParcelUuid.fromString("eed0fa1d-93cf-43c8-ab0a-7db871a11784")).build());

        findViewById(R.id.scanBtn).setOnClickListener(v -> {
            if(!isDetecting)
            {
                isDetecting = true;
                bluetoothLeScanner.startScan(*//*scanFilters, new ScanSettings
                        .Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                        .build(),*//* scanCallback);
            } else {
                isDetecting = false;
                bluetoothLeScanner.stopScan(scanCallback);
            }
        });*/
    }

/*    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }*/
}

class MapView extends View {
    JsonObject jsonObject;

    MapView(Context context) {
        super(context);
    }

    MapView(Context context, JsonObject jsonObject) {
        this(context);
        this.jsonObject = jsonObject;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Paint paint = new Paint();
        JsonArray jsonArray;
        JsonObject item;
        setBackgroundColor(Color.WHITE);

        jsonArray = (JsonArray) jsonObject.get("way");
        paint.setColor(Color.GRAY);
        for (int i = 0; i < jsonArray.size(); i++) {
            item = jsonArray.get(i).getAsJsonObject();
            canvas.drawRect(item.get("x").getAsFloat() * 40,
                    item.get("y").getAsFloat() * 40,
                    item.get("x").getAsFloat() * 40 + item.get("width").getAsFloat() * 40,
                    item.get("y").getAsFloat() * 40 + item.get("height").getAsFloat() * 40,
                    paint);
        }

        jsonArray = (JsonArray) jsonObject.get("seat");
        paint.setColor(Color.BLACK);
        for (int i = 0; i < jsonArray.size(); i++) {
            item = jsonArray.get(i).getAsJsonObject();
            canvas.drawRect(item.get("x").getAsFloat() * 40,
                    item.get("y").getAsFloat() * 40,
                    item.get("x").getAsFloat() * 40 + item.get("width").getAsFloat() * 40,
                    item.get("y").getAsFloat() * 40 + item.get("height").getAsFloat() * 40,
                    paint);
        }

        jsonArray = (JsonArray) jsonObject.get("exit");
        paint.setColor(Color.GREEN);
        for (int i = 0; i < jsonArray.size(); i++) {
            item = jsonArray.get(i).getAsJsonObject();
            canvas.drawRect(item.get("x").getAsFloat() * 40,
                    item.get("y").getAsFloat() * 40,
                    item.get("x").getAsFloat() * 40 + item.get("width").getAsFloat() * 40,
                    item.get("y").getAsFloat() * 40 + item.get("height").getAsFloat() * 40,
                    paint);
        }

        jsonArray = (JsonArray) jsonObject.get("beacon");
        paint.setColor(Color.YELLOW);
        for (int i = 0; i < jsonArray.size(); i++) {
            item = jsonArray.get(i).getAsJsonObject();
            canvas.drawCircle(item.get("x").getAsFloat() * 40,
                    item.get("y").getAsFloat() * 40,
                    item.get("width").getAsFloat() * 40,
                    paint);
        }
    }
}

class CircleView extends View {
    float x, y;

    CircleView(Context context) {
        super(context);
    }

    CircleView(Context context, float x, float y) {
        this(context);
        this.x = x;
        this.y = y;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint paint = new Paint();
        paint.setColor(Color.RED);

        canvas.drawCircle(x, y, 1, paint);
    }
}
