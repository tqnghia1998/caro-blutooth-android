package cnpm31.nhom10.caroplay.Bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.res.Resources;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

import cnpm31.nhom10.caroplay.MainActivity;
import cnpm31.nhom10.caroplay.R;

import static cnpm31.nhom10.caroplay.MainActivity.bluetoothAdapter;
import static cnpm31.nhom10.caroplay.MainActivity.connectedBluetooth;
import static cnpm31.nhom10.caroplay.MainActivity.connectionHandler;
import static cnpm31.nhom10.caroplay.MainActivity.defaultUUID;
import static cnpm31.nhom10.caroplay.MainActivity.mainContext;

/* Kết nối bluetooth với tư cách là một client */
public class ClientBluetooth extends Thread {

    /* Socket dùng để giao tiếp với server */
    private final BluetoothSocket clientSocket;

    /* Dùng BluetoothDevice của server để tạo socket */
    public ClientBluetooth(BluetoothDevice serverDevice) {

        /* Tạo một Socket tạm */
        BluetoothSocket tmpSocket = null;

        /* Tạo Socket để kết nối với server */
        try {
            tmpSocket = serverDevice.createRfcommSocketToServiceRecord(defaultUUID);
        } catch (IOException e) {
            Log.e(MainActivity.TAG, "Tạo Socket kết nối đến server thất bại.", e);
        }

        /* Gán sang Socket chính */
        clientSocket = tmpSocket;
    }

    public void run() {

        /* Tắt chế độ discovery để khỏi làm chậm kết nối */
        bluetoothAdapter.cancelDiscovery();

        /* Bắt đầu kết nối đến server */
        try {
            clientSocket.connect();
        } catch (IOException connectException) {

            /* Nếu kết nối không thành công thì đóng Socket */
            try {
                clientSocket.close();

                /* Thông báo cho người dùng biết */
                connectionHandler.obtainMessage(-1
                        , 28 // 28 bytes của chuỗi bên dưới
                        , -1
                        , "C@O@N@N@E@C@T@ @F@A@I@L@E@D@".getBytes()).sendToTarget();

            } catch (IOException e) {
                Log.e(MainActivity.TAG, "Đóng Socket kết nối đến server thất bại.", e);
            }
            return;
        }

        /* Kết nối thành công, tuy nhiên làm việc với kết nối ở thread khác */
        Log.e(MainActivity.TAG, "Kết nối đến server thành công.");

        MainActivity.connectedBluetooth = new ConnectedBluetooth(clientSocket);
        MainActivity.connectedBluetooth.start();

        /* -------------------BẮT ĐẦU LIÊN LẠC Ở ĐÂY ----------------- */
        // Báo cho server biết là mình đã vào phòng
        connectedBluetooth.sendData(mainContext.getString(R.string.CLIENTCONFIRM).getBytes());
        // ...

        /* ----------------------------------------------------------- */
    }

    /* Đóng Socket và dừng tiểu trình */
    public void cancel() {
        try {
            clientSocket.close();
        } catch (IOException e) {
            Log.e(MainActivity.TAG, "Đóng Socket kết nối đến server thất bại.", e);
        }
    }
}