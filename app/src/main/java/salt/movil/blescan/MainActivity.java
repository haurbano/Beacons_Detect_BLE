package salt.movil.blescan;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    BluetoothAdapter mBluetoothAdapter;
    private Handler mHandler;

    private static final long SCAN_PERIOD = 10000;
    private boolean isScaning;
    List<String> listDevicesStr;

    //Views
    ListView listDevicesBT;
    FloatingActionButton fabfindDevices, fabClearDevices;
    TextView txtDetectando;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listDevicesBT = (ListView) findViewById(R.id.list_dispositivos_bt);
        fabfindDevices = (FloatingActionButton) findViewById(R.id.btn_fab_buscar_dispositivos);
        fabClearDevices = (FloatingActionButton) findViewById(R.id.btn_clear_list_devices);
        txtDetectando = (TextView) findViewById(R.id.txt_detectando);

        fabfindDevices.setOnClickListener(this);
        fabClearDevices.setOnClickListener(this);
        mHandler = new Handler();
        isScaning = false;
        listDevicesStr = new ArrayList<>();

        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)){
            activarBluetoth();
        }else {
            makeToas("No soporta BLE");
        }
    }

    private void activarBluetoth(){

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        if (mBluetoothAdapter != null || !mBluetoothAdapter.isEnabled()){
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent,1);
        }else{
            makeToas("No se pudo habilitar el bluetooth");
        }
    }

    private void makeToas(String msj){
        Toast.makeText(this,msj,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_fab_buscar_dispositivos:
                if (mBluetoothAdapter != null || mBluetoothAdapter.isEnabled()) {
                    buscarDevices();
                    txtDetectando.setVisibility(View.VISIBLE);
                }
                else
                    makeToas("Bluetooth no soportado");
                break;
            case R.id.btn_clear_list_devices:
                listDevicesStr.clear();
                llenarLista(listDevicesStr);
                break;
        }
    }

    private void buscarDevices() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                isScaning = false;
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
                txtDetectando.setVisibility(View.INVISIBLE);
            }
        },SCAN_PERIOD);

        isScaning = true;
        mBluetoothAdapter.startLeScan(mLeScanCallback);

    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            Log.i("haur","Dispositivo: "+device.getName() +", "+device.getUuids()+", "+device.getType()+", "+device.getAddress());
            String adress =  device.getAddress();
            listDevicesStr.add(adress);
            llenarLista(listDevicesStr);
        }
    };

    private void llenarLista(List<String> devices){
        ArrayAdapter adapter = new ArrayAdapter(getApplicationContext(),android.R.layout.simple_list_item_1,devices);
        listDevicesBT.setAdapter(adapter);
    }

}
