package cnpm31.nhom10.caroplay.Bluetooth;

import android.bluetooth.BluetoothSocket;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import cnpm31.nhom10.caroplay.MainActivity;

import static cnpm31.nhom10.caroplay.MainActivity.connectionHandler;

public class ConnectedBluetooth extends Thread {

    /* Socket đang kết nối */
    private final BluetoothSocket mmSocket;

    /* Hai stream để đọc dữ liệu truyền nhận và buffer */
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private byte[] mmBuffer;

    public ConnectedBluetooth(BluetoothSocket socket) {

        /* Khởi tạo giá trị */
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        /* Lấy input và output stream */
        try {
            tmpIn = socket.getInputStream();
        } catch (IOException e) {
            Log.e(MainActivity.TAG, "Tạo input stream thất bại.", e);
        }
        try {
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            Log.e(MainActivity.TAG, "Tạo output stream thất bại.", e);
        }

        /* Gán vào đối tượng chính */
        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    public void run() {

        /* Cấp phát cho buffer */
        mmBuffer = new byte[512];

        /* Số lượng byte đọc được */
        int numBytes;

        /* Liên tục đọc từ input stream cho đến khi exception (mất kết nối) */
        while (true) {
            try {
                /* Đọc vào buffer với số byte đọc được là numBytes */
                numBytes = mmInStream.read(mmBuffer);

                /* Gửi dữ liệu đọc được sang MainActivity */
                Message readMsg = MainActivity.connectionHandler
                        .obtainMessage(1, numBytes, -1, mmBuffer);

                /* Gửi đi đến MessageQueue */
                readMsg.sendToTarget();

            } catch (IOException e) {
                Log.d(MainActivity.TAG, "Input stream bị mất kết nối.", e);
                break;
            }
        }
    }

    /* Gửi dữ liệu sang đối phương kết nối */
    public void sendData(byte[] bytes) {
        try {
            /* Đưa dữ liệu lên output stream */
            mmOutStream.write(bytes);

            /* Share the sent message with the UI activity? */

        } catch (IOException e) {
            Log.e(MainActivity.TAG, "Output stream bị mất kết nối.", e);

            /* Send the failure message back to the UI activity? */
        }
    }

    /* Đóng Socket và dừng tiểu trình (ngừng kết nối) */
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            Log.e(MainActivity.TAG, "Đóng kết nối thất bại.", e);
        }
    }
}
