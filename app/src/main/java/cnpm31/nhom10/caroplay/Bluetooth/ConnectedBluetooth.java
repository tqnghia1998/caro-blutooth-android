package cnpm31.nhom10.caroplay.Bluetooth;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.ContextWrapper;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Hashtable;

import cnpm31.nhom10.caroplay.GameBoard.SingletonSharePrefs;
import cnpm31.nhom10.caroplay.MainActivity;

import static cnpm31.nhom10.caroplay.MainActivity.mainContext;
import static cnpm31.nhom10.caroplay.MainActivity.queueData;

public class ConnectedBluetooth extends Thread {

    /* Socket đang kết nối */
    private final BluetoothSocket mmSocket;

    /* Hai stream để đọc dữ liệu truyền nhận và buffer */
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;

    /* Hashmap lưu các phân mảnh */
    private Hashtable<Integer, byte[]> DataTable;

    /* Các kiểu phân mảnh file */
    private final byte[] voice_complete = "V@O@I@C@E@DONE".getBytes();
    private final byte[] voice = "V@O@I@C@E@".getBytes();
    private final byte[] image_complete = "A@V@A@T@A@R@DONE".getBytes();
    private final byte[] image = "A@V@A@T@A@R@".getBytes();

    public ConnectedBluetooth(BluetoothSocket socket) {

        /* Khởi tạo giá trị */
        mmSocket = socket;
        DataTable = new Hashtable<>();
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

        /* Đảm bảo đường dẫn caroPlayPath đã tồn tại */
        if (SingletonSharePrefs.getInstance().get("caroPlayPath", String.class).equals("")) {
            ContextWrapper cw = new ContextWrapper(mainContext);
            File directory = cw.getDir("caroPlayPath", Context.MODE_PRIVATE);
            SingletonSharePrefs.getInstance().put("caroPlayPath", directory.getAbsolutePath());
        }
    }

    public void run() {

        /* Buffer và số byte đọc được */
        byte[] mmBuffer = new byte[512];
        int numBytes;

        /* Liên tục đọc từ input stream cho đến khi exception (mất kết nối) */
        while (true) {
            try {
                int i, j;

                // Đọc dữ liệu từ InputStream
                numBytes = mmInStream.read(mmBuffer);

                // Kiểm tra có phải đang nhận phân mảnh file nhắn thoại
                for (i = 0; i < voice_complete.length; ++i){
                    if (voice_complete[i] != mmBuffer[i]) break;
                }

                // Kiểm tra có phải đang nhận phân mảnh file avatar
                for (j = 0; j < image_complete.length; ++j){
                    if (image_complete[j] != mmBuffer[j]) break;
                }

                // Nếu nhận được phân mảnh báo kết thúc file
                if (i == voice_complete.length || j == image_complete.length) {

                    // Loại file
                    byte[] type = i == voice_complete.length ? voice_complete : image_complete;

                    // Lấy số thứ tự của phân mảnh này
                    byte[] finalCount = Arrays.copyOfRange(mmBuffer, type.length,type.length + 4);
                    int countFragment = ByteBuffer.wrap(finalCount).getInt();

                    // Ghép các phân mảnh đã nhận thành file hoàn chỉnh
                    FileOutputStream output = null;
                    try{
                        // Mở file (nhắn thoại hoặc avatar)
                        output = new FileOutputStream(SingletonSharePrefs
                                .getInstance().get("caroPlayPath", String.class)
                                + (i == type.length ? "/voiceUser2.3gp" : "/avatarUser2.jpg"));

                        // Ghi vào file
                        for (int e = 0; e <countFragment ; ++e){
                            output.write(DataTable.get(e));
                        }
                        output.close();
                    } catch (Exception ignored){}
                    finally {
                        DataTable.clear();
                    }

                    // Gửi file về UI
                    String strType = type == voice_complete ? "R@E@C@E@I@V@E@D@V@O@I@C@E@" : "R@E@C@E@I@V@E@D@A@V@A@T@A@R@";
                    MainActivity.connectionHandler
                            .obtainMessage(1, strType.length(),-1
                                    , strType.getBytes()).sendToTarget();
                }

                // Nếu nhận được phân mảnh file nhắn thoại
                else if (i == voice.length || j == image.length) {

                    // Loại file
                    byte[] type = i == voice.length ? voice : image;

                    // Lấy lượng dữ liệu cần lưu
                    byte[] dataToSave = new byte[numBytes - 4 - type.length];
                    System.arraycopy(mmBuffer,type.length + 4, dataToSave,0,numBytes - type.length - 4);

                    // Lấy số thứ tự của phân mảnh
                    byte[] numberBuffer = Arrays.copyOfRange(mmBuffer, type.length,type.length + 4);
                    int number = ByteBuffer.wrap(numberBuffer).getInt();

                    // Thêm vào HashTable
                    DataTable.put(number, dataToSave);

                    // Báo hiệu đã nhận phân mảnh
                    sendData("R@E@C@E@I@V@E@D@".getBytes());
                    Log.e("TQN", "recv: " + number + "-" + dataToSave.length);
                }

                // Nếu dữ liệu nhận được không phải phân mảnh của file nào cả
                else {
                    MainActivity.connectionHandler
                            .obtainMessage(1, numBytes, -1, mmBuffer).sendToTarget();
                }
            } catch (IOException e) {
                Log.d(MainActivity.TAG, "Input stream bị mất kết nối.", e);
                break;
            }
        }
        /* Nếu bị mất kết nối */
        MainActivity.connectionHandler
                .obtainMessage(1, 18, -1, "S@U@R@R@E@N@D@E@R@".getBytes()).sendToTarget();
    }

    /* Gửi dữ liệu dạng mảng byte[] */
    public void sendData(byte[] bytes) {
        try {
            mmOutStream.write(bytes);
        } catch (IOException e) {
            Log.e(MainActivity.TAG, "Output stream bị mất kết nối.", e);
        }
    }

    /* Gửi dữ liệu dạng file */
    public void sendFile(String filePath, String type) throws IOException {

        // Tạo InputStream để đọc file
        FileInputStream in = null;
        try {
            in = new FileInputStream(filePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }

        // Đếm số phân mảnh
        int count = 0;

        // Loại file truyền đi
        byte[] header = type.getBytes();
        int sizeHeader = header.length;

        // Lần lượt tạo từng phân mảnh với định dạng:
        while (true){
            byte[] dataToSend = new byte[512];

            // Đầu tiên là loại file (V@O@I@C@E@, I@M@A@G@E@)
            System.arraycopy(header,0,dataToSend,0,sizeHeader);

            // Tiếp theo là số thứ tự phân mảnh
            byte[] CountArray = ByteBuffer.allocate(4).putInt(count).array();
            System.arraycopy(CountArray,0, dataToSend, sizeHeader,4);

            // Tiếp theo là một lượng data của file sao cho tổng kích thước phân mảnh là 512
            int sizeRead = in.read(dataToSend,4 + sizeHeader,512 - 4 - sizeHeader);
            if (sizeRead == -1) break;

            // Trường hợp đây là phân mảnh cuối cùng
            if (sizeRead < 512 - 4 - sizeHeader) {

                // Tạo một phân mảnh mới vừa đủ kích thước (vì phân mảnh hiện tại là 512 bytes)
                byte[] newDataToSend = new byte[4 + sizeHeader + sizeRead];

                // Copy lượng data cần thiết và thêm phân mảnh vào queue
                System.arraycopy(dataToSend, 0, newDataToSend, 0, newDataToSend.length);
                queueData.add(newDataToSend);
            }
            // Trường hợp đây là các phân mảnh giữa (kích thước luôn là 512 bytes)
            else {
                queueData.add(dataToSend);
            }
            ++count;
        }

        // Phân mảnh báo hết file
        byte[] dataDone  = new byte[header.length + 8];
        byte[] headerDone = (type + "DONE").getBytes();
        byte[] CountArray = ByteBuffer.allocate(4).putInt(count).array();
        System.arraycopy(headerDone,0,dataDone,0,headerDone.length);
        System.arraycopy(CountArray,0,dataDone,headerDone.length,4);
        queueData.add(dataDone);
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
