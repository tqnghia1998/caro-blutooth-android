package cnpm31.nhom10.caroplay.Bluetooth;

import android.bluetooth.BluetoothSocket;
import android.os.Environment;
import android.os.Message;
import android.provider.ContactsContract;
import android.renderscript.ScriptGroup;
import android.speech.tts.Voice;
import android.util.Log;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;

import cnpm31.nhom10.caroplay.MainActivity;

public class ConnectedBluetooth extends Thread {

    /* Socket đang kết nối */
    private final BluetoothSocket mmSocket;

    /* Hai stream để đọc dữ liệu truyền nhận và buffer */
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private Hashtable<Integer, byte[]> DataTable;
    private byte[] mmBuffer;
    private  final byte[] voice_complete = "voicedone".getBytes();
    private  final byte[] voice = "voice".getBytes();
    private  final String mOutput = Environment.getExternalStorageDirectory().getAbsolutePath()+"/receive.3gp";
    public  static boolean HaveNewChatVoice = false;

    public ConnectedBluetooth(BluetoothSocket socket) {

        /* Khởi tạo giá trị */
        mmSocket = socket;
        DataTable = new Hashtable<Integer, byte[]>();
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
        //mmBuffer = new byte[128];

        /* Số lượng byte đọc được */
        int numBytes;
        ArrayList<Byte> imageByte = new ArrayList<>();
        mmBuffer = new byte[128];
        boolean isImg = false;

        /* Liên tục đọc từ input stream cho đến khi exception (mất kết nối) */
        while (true) {
            try {

                /* Đọc vào buffer với số byte đọc được là numBytes */
                numBytes = mmInStream.read(mmBuffer);

                int i;

                for (i = 0; i < voice_complete.length;++i){
                    if (voice_complete[i] != mmBuffer[i]){
                        break;
                    }
                }

                // Nếu data nhận được là tín hiệu kết thúc chat voice
                if (i == 9) {
                    byte[] finalCount = Arrays.copyOfRange(mmBuffer,voice_complete.length,voice_complete.length+4);

                    // Ghép các mảnh data nhận dc thành đoạn chat voice hoàn chỉnh
                    FileOutputStream output = null;
                    int lastNumber = ByteBuffer.wrap(finalCount).getInt();
                    try{
                        output = new FileOutputStream(mOutput);

                        for (int j = 0; j <lastNumber ; ++j){
                            output.write(DataTable.get(j));
                        }

                        output.close();
                    } catch (Exception ex){
                        Toast.makeText(null,"Lỗi không xác định",Toast.LENGTH_LONG).show();
                        return;
                    }
                    finally {
                        DataTable.clear();
                    }

                    Message readMsg = MainActivity.connectionHandler
                            .obtainMessage(1,"Have new chat voice".length(),-1,"Have new chat voice".getBytes());
                    readMsg.sendToTarget();
                }
                // Nếu data nhận được là các phân mảnh chat voice
                else if (i == 5) {
                    byte[] dataToSave = new byte[numBytes - 4 - voice.length];
                    byte[] numnerBuffer = Arrays.copyOfRange(mmBuffer, voice.length,voice.length+4); //Lấy chuỗi byte là số thứ tự
                    System.arraycopy(mmBuffer,voice.length + 4,dataToSave,0,128 - voice.length - 4); //lấy data của chat voice
                    int number = ByteBuffer.wrap(numnerBuffer).getInt();
                    sleep(5);
                    SaveToHashtable(dataToSave, number); // Lưu data vào bảng data
                }
                else {
                    /* Gửi dữ liệu đọc được sang MainActivity */
                    Message readMsg = MainActivity.connectionHandler
                            .obtainMessage(1, numBytes, -1, mmBuffer);

                    /* Gửi đi đến MessageQueue */
                    readMsg.sendToTarget();
                }
            } catch (IOException e) {
                Log.d(MainActivity.TAG, "Input stream bị mất kết nối.", e);
                break;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        /* Nếu bị mất kết nối */
        MainActivity.connectionHandler
                .obtainMessage(1, 18, -1, "S@U@R@R@E@N@D@E@R@".getBytes()).sendToTarget();
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

    // Gửi file và loại file
    public void sendFile(String filePath, String type) throws IOException {

        FileInputStream in = null;
        try {
            in = new FileInputStream(filePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }

        int count = 0;
        byte[] header = type.getBytes();

        byte[] dataToSend = new byte[128];

        System.arraycopy(header,0,dataToSend,0,header.length);
        int sizeHeader = header.length;
        byte[] CountArray = new byte[4];

        while (in.read(dataToSend,4 + sizeHeader,128 - 4 - sizeHeader) != -1){
            CountArray = ByteBuffer.allocate(4).putInt(count).array();
            System.arraycopy(CountArray,0,dataToSend,sizeHeader,4);
            sendData(dataToSend);
            ++count;
        }

        // region Gửi tín hiệu hết file

        byte[] dataDone  = new byte[header.length + 8];
        byte[] headerDone = (type+"done").getBytes();
        CountArray = ByteBuffer.allocate(4).putInt(count).array();
        System.arraycopy(headerDone,0,dataDone,0,headerDone.length);
        System.arraycopy(CountArray,0,dataDone,headerDone.length,4);
        sendData(dataDone);

        // endregion
    }

    /* Đóng Socket và dừng tiểu trình (ngừng kết nối) */
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            Log.e(MainActivity.TAG, "Đóng kết nối thất bại.", e);
        }
    }

    public void SaveToHashtable(byte[] data, int key){
        DataTable.put(key, data);

    }
}
