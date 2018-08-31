package comrelaboratory.github.client;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.estimote.coresdk.observation.region.beacon.BeaconRegion;
import com.estimote.coresdk.recognition.packets.Beacon;
import com.estimote.coresdk.service.BeaconManager;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.UUID;

public class MapActivity extends AppCompatActivity {

    BeaconManager beaconManager;
    Beacon currentBeacon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        JsonObject mapObject;

        try {
            URL url = new URL("localhost:8080");
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

        //TODO: 맵 렌더링하기 with Canvas

        ArrayList<BeaconRegion> beaconList = new ArrayList<>();
        beaconList.add(new BeaconRegion("beacon_seokjin", UUID.fromString("b9407F30-f5f8-466e-aff9-25556b57fe6d"), 0, 0));
        beaconList.add(new BeaconRegion("beacon_me", UUID.fromString("f9f8173b-be05-4b48-89d9-d1e700b1fff9"), 0, 0));

        beaconManager = new BeaconManager(getApplicationContext());
        beaconManager.setRangingListener((BeaconManager.BeaconRangingListener) (beaconRegion, beacons) -> {
            Log.d(Constants.TAG, beacons.toString());
            if(beacons.get(0).getProximityUUID() == currentBeacon.getProximityUUID())
                currentBeacon = beacons.get(0);
            else if(beacons.get(0).getRssi() < currentBeacon.getRssi())
                currentBeacon = beacons.get(0);
            //TODO: currentBeacon 의 위치 찍어주기
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
        //TODO: 블루투스 확인하기
    }*/
}
