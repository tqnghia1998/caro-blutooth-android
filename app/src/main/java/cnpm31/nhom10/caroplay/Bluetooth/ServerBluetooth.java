package cnpm31.nhom10.caroplay.Bluetooth;

import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.res.Resources;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import cnpm31.nhom10.caroplay.GameBoard.GameBoard;
import cnpm31.nhom10.caroplay.MainActivity;
import cnpm31.nhom10.caroplay.R;

import static cnpm31.nhom10.caroplay.MainActivity.bluetoothAdapter;
import static cnpm31.nhom10.caroplay.MainActivity.connectedBluetooth;
import static cnpm31.nhom10.caroplay.MainActivity.defaultUUID;
import static cnpm31.nhom10.caroplay.MainActivity.mainContext;

public class ServerBluetooth extends Thread {

    /* Socket dùng để lắng nghe client */
    private final BluetoothServerSocket serverListenSocket;

    /* Dùng BluetoothAdapter của tạo một BluetoothServerSocket */
    public ServerBluetooth() {

        /* Tạo một Socket tạm */
        BluetoothServerSocket tmpSocket = null;

        /* Tạo Socket để lắng nghe client */
        try {
            tmpSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord("CaroPlay", defaultUUID);
        } catch (IOException e) {
            Log.e(MainActivity.TAG, "Tạo Socket lắng nghe client thất bại.", e);
        }

        /* Gán sang Socket chính */
        serverListenSocket = tmpSocket;
    }

    public void run() {

        /* serverSocket chỉ dùng để lắng nghe, muốn kết nối cũng phải dùng một BluetoothSocket */
        BluetoothSocket serverSocket = null;

        /* Liên tục lắng nghe */
        while (true) {

            try {
                serverSocket = serverListenSocket.accept();
            } catch (IOException e) {
                Log.e(MainActivity.TAG, "Không lắng nghe được client nào.", e);
                break;
            }

            /* Trường hợp lắng nghe được một client */
            if (serverSocket != null) {

                Log.e(MainActivity.TAG, "Kết nối đến client thành công.");

                //Lấy MAC client
                GameBoard.MACUser2 = serverSocket.getRemoteDevice().getAddress();

                /* Quản lý kết nối */
                MainActivity.connectedBluetooth = new ConnectedBluetooth(serverSocket);
                MainActivity.connectedBluetooth.start();

                /* -------------------BẮT ĐẦU LIÊN LẠC Ở ĐÂY ----------------- */

                // Báo cho client biết là đã kết nối thành công
                connectedBluetooth.sendData(mainContext.getString(R.string.SERVERCONFIRM).getBytes());
                // ...

                /* ----------------------------------------------------------- */

                /* Đóng Socket lắng nghe (không còn tác dụng nữa) */
                try {
                    serverListenSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    /* Đóng Socket lắng nghe và dừng tiểu trình */
    public void cancel() {
        try {
            serverListenSocket.close();
        } catch (IOException e) {
            Log.e(MainActivity.TAG, "Đóng Socket lắng nghe client thất bại.", e);
        }
    }
}