package cnpm31.nhom10.caroplay;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;
import cnpm31.nhom10.caroplay.GameBoard.GameBoard;

public class MainActivity extends Activity {

    // Các biến toàn cục dùng trong các fragment
    public static String userName = "anonymous";

    GameBoard gameBoard;
    public static ImageView imgUser1;
    public static ImageView imgUser2;
    ImageView imgExit;
    TextView txtUser1;
    TextView txtUser2;
    LoginFragment loginFragment;
    public static BluetoothSPP bluetoothSPP;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        char[] statusList = new char[gameBoard.boardGame.size()];
        for (int i = 0; i < gameBoard.boardGame.size(); i++) {
            statusList[i] = gameBoard.boardGame.get(i).getStatus();
        }
        outState.putCharArray("status", statusList);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Tạo một fragment đăng nhập
        loginFragment = new LoginFragment();

        // Tạo một game board
        gameBoard = new GameBoard(this, false);

        // Lấy dữ liệu đã lưu nếu có
        if (savedInstanceState != null) {
            char[] statusList = savedInstanceState.getCharArray("status");
            for (int i = 0; i < statusList.length; i++) {
                gameBoard.boardGame.get(i).setStatus(statusList[i]);
            }
        }

        bluetoothSPP = new BluetoothSPP(this);

        // Xử lý sự kiện nhận dữ liệu từ các thiết bị đã kết nối
        bluetoothSPP.setOnDataReceivedListener((data, message) -> {

            // Nếu nhận tên của đối thủ
            if (message.charAt(0) == '@') {
                txtUser1.setText(userName);
                txtUser2.setText(message.substring(1));
            }
            // Nếu là nhận nước đi
            if (message.charAt(0) == '~') {
                int position = Integer.parseInt(message.substring(1));
                gameBoard.rivalMove(position);
            }
        });

        // Xử lý sự kiện khi kết nối thành công
        bluetoothSPP.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() {
            public void onDeviceConnected(String name, String address) {
                // Thoát giao diện đăng nhập
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.remove(loginFragment);
                fragmentTransaction.commit();

                // Gửi tên qua
                bluetoothSPP.send("@" + userName, true);
            }

            public void onDeviceDisconnected() {
                // Toast.makeText(getApplicationContext()
                //        , "Kết nối đã bị mất", Toast.LENGTH_SHORT).show();
                showLogin();
            }

            public void onDeviceConnectionFailed() {
                // Toast.makeText(getApplicationContext()
                //         , "Không thể tạo kết nối", Toast.LENGTH_SHORT).show();
                showLogin();
            }
        });

        // Hiển thị giao diện đăng nhập
        showLogin();

        imgExit = findViewById(R.id.exit);
        imgExit.setOnClickListener(v -> {

            AlertDialog.Builder b = new AlertDialog.Builder(this);
            b.setTitle("Xác nhận");
            b.setMessage("Hủy kết nối hiện tại?");

            // Nếu cancel
            b.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });

            // Nếu OK - đồng ý đăng ký
            b.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    showLogin();
                }
            });

            b.show();
        });

        imgUser1 = findViewById(R.id.imgUser1);
        imgUser2 = findViewById(R.id.imgUser2);
        txtUser1 = findViewById(R.id.txtUser1);
        txtUser2 = findViewById(R.id.txtUser2);
    }

    // Khi ứng dụng bắt đầu, kiểm tra trạng thái của bluetooth
    public void onStart() {
        super.onStart();

        // Kiểm tra điện thoại có bật blutooth hay chưa
        if (!bluetoothSPP.isBluetoothEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
        } else {
            // Kiểm tra dịch vụ BluetoothSPP có được bật hay chưa
            if(!bluetoothSPP.isServiceAvailable()) {
                bluetoothSPP.setupService();
                bluetoothSPP.startService(BluetoothState.DEVICE_ANDROID);
            }
        }
    }

    // Dừng dịch vụ BluetoothSPP khi ứng dụng kết thúc
    public void onDestroy() {
        super.onDestroy();
        bluetoothSPP.stopService();
    }

    // Phương thức hiển thị màn hình chờ
    public void showLogin() {
        gameBoard.reSet();
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frameForFragment, loginFragment);
        fragmentTransaction.addToBackStack("login");
        fragmentTransaction.commit();
        bluetoothSPP.disconnect();
    }

    // Phương thức hiển thị kết nối với các thiết bị
    public static void findRival(Activity activity) {
        Intent intent = new Intent(activity, DeviceList.class);
        intent.putExtra("layout_list", R.layout.bluetooth_layout);
        intent.putExtra("no_devices_found", "Không có thiết bị nào");
        intent.putExtra("scan_for_devices", "Tìm kiếm");
        activity.startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
    }

    // Xử lý sự kiện sau khi đã chọn thiết bị để kết nối
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        // Nếu yêu cầu muốn kết nối tới thiết bị đã chọn
        if (requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            // Và bluetooth trong máy đã được bật
            if (resultCode == Activity.RESULT_OK)
                // Thì tiến hành tạo kết nối
                bluetoothSPP.connect(data);
        }
        // Nếu yêu cầu muốn bật BluetoothSPP
        else {
            if (requestCode == BluetoothState.REQUEST_ENABLE_BT) {
                // Và bluetooth trong máy đã được bật
                if (resultCode == Activity.RESULT_OK) {
                    // Thì bật dịch vụ BluetoothSPP
                    bluetoothSPP.setupService();
                    bluetoothSPP.startService(BluetoothState.DEVICE_ANDROID);
                }
                else {
                    Toast.makeText(this
                            , "Bluetooth chưa được bật"
                            , Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    // region Full Screen
    // Source: https://developer.android.com/training/system-ui/immersive
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    // Shows the system bars by removing all the flags
    // except for the ones that make the content appear under the system bars.
    private void showSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }
    // endregion
}
