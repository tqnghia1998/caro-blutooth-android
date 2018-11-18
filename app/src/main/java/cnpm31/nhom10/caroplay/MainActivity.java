package cnpm31.nhom10.caroplay;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.UUID;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;
import cnpm31.nhom10.caroplay.Bluetooth.ClientBluetooth;
import cnpm31.nhom10.caroplay.Bluetooth.ConnectedBluetooth;
import cnpm31.nhom10.caroplay.Bluetooth.ServerBluetooth;
import cnpm31.nhom10.caroplay.GameBoard.GameBoard;
import cnpm31.nhom10.caroplay.GameBoard.UserProfile;

public class MainActivity extends Activity {

    // region CÁC KHAI BÁO LIÊN QUAN ĐẾN BLUETOOTH
    public static String TAG = "CaroPlay_DEBUG";            /* Hỗ trợ Debug */
    public static Context mainContext;                      /* Context của UI */
    public static String oldBluetoothName;                  /* Tên cũ của Bluetooth, dùng để khôi phục */
    public static UUID defaultUUID;                         /* Mã UUID mặc định của Bluetooth */
    public static BluetoothAdapter bluetoothAdapter;        /* BluetoothAdapter mặc định */
    public static ServerBluetooth serverBluetooth;          /* Tiểu trình đóng vai trò làm server */
    public static ConnectedBluetooth connectedBluetooth;    /* Tiểu trình quản lý kết nối */
    @SuppressLint("HandlerLeak")
    public static android.os.Handler connectionHandler      /** QUẢN LÝ THÔNG ĐIỆP GỬI NHẬN TẠI ĐÂY **/
            = new android.os.Handler() {
        @Override
        public void handleMessage(Message msg) {

            // Trích chuỗi thông điệp (nhị phân) nhận được
            byte[] data = new byte[msg.arg1];
            for (int i = 0; i < msg.arg1; i++) data[i] = ((byte[]) msg.obj)[i];
            String message = new String(data);

            // region Thông điệp liên quan đến kết nối thành công/thất bại
            /* Nếu mình đang là server, và nhận được confirm từ client */
            if (message.equals(mainContext.getString(R.string.CLIENTCONFIRM))) {

                // Thông báo bắt đầu chơi
                AlertDialog.Builder b = new AlertDialog.Builder(imgExit.getContext());
                b.setTitle("Thông báo");
                b.setMessage("Đối thủ đã vào phòng\nBạn là người đi trước");
                b.setNegativeButton("Ok", (dialog, which) -> {
                    dialog.cancel();
                });
                b.show();
                // Mình có thể đi nước đầu tiên
                isPlaying = true;
                gameBoard.isWaiting = false;
                avatarUser2.setBackgroundResource(0);
            }
            /* Nếu mình đang là client, và nhận được confirm từ server */
            else if (message.equals(mainContext.getString(R.string.SERVERCONFIRM))) {

                // Thông báo bắt đầu chơi
                AlertDialog.Builder b = new AlertDialog.Builder(imgExit.getContext());
                b.setTitle("Thông báo");
                b.setMessage("Bạn đã vào phòng\nChủ phòng là người đi trước");
                b.setNegativeButton("Ok", (dialog, which) -> {
                    dialog.cancel();
                });
                b.show();

                // Tắt giao diện Welcome
                isPlaying = true;
                fragmentManager.beginTransaction()
                        .remove(welcomeFragment)
                        .commit();
                avatarUser1.setBackgroundResource(0);
            }
            // Nếu nhận được tin CONNECT FAILED, thì đó chính là
            // mình tự gửi cho mình khi kết nối thất bại/mất kết nối
            else if (message.equals("C@O@N@N@E@C@T@ @F@A@I@L@E@D@")) {
                AlertDialog.Builder b = new AlertDialog.Builder(imgExit.getContext());
                b.setTitle("Thông báo");
                b.setMessage("Không thể kết nối");
                b.show();
            }
            // endregion

            // region Thông điệp tọa độ nước đi của đối thủ
            /* Nếu ký tự đầu là "[" và ký tự cuối là "]" */
            else if (message.charAt(0) =='[' && message.charAt(message.length() - 1) ==']') {

                // Lấy tọa độ
                int position = Integer.parseInt(message.substring(1, message.length() - 1));

                // Đặt nước đi lên bàn cờ
                gameBoard.rivalMove(position);
            }
            // endregion

            // region Thông điệp thoát khỏi phòng
            /* Trường hợp đối thủ thoát, nhận được chuỗi E@X@I@T@E@D@ */
            else if (message.equals("S@U@R@R@E@N@D@E@R@")) {
                AlertDialog.Builder b = new AlertDialog.Builder(imgExit.getContext());
                b.setTitle("Chúc mừng");
                b.setMessage("Đối thủ đã thoát!");
                b.show();
                isPlaying = false;
                /* Tắt luôn kết nối */
                MainActivity.connectedBluetooth.cancel();
            }
            // endregion
        }
    };
    // endregion

    // region CÁC KHAI BÁO LIÊN QUAN ĐẾN GIAO DIỆN CHƠI CỜ
    public static GameBoard gameBoard;
    public static ImageView imgExit;
    public static boolean isPlaying;

    public static UserProfile user1;
    public static TextView nameUser1;
    public static ImageView avatarUser1;

    public static UserProfile user2;
    public static TextView nameUser2;
    public static ImageView avatarUser2;
    // endregion

    // region CÁC KHAI BÁO LIÊN QUAN ĐẾN GIAO DIỆN KHÁC
    public static WelcomeFragment welcomeFragment;
    public static FragmentManager fragmentManager;
    // endregion

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainContext = getApplicationContext();

        /* Xử lý các biến Bluetooth */
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        defaultUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
        oldBluetoothName = BluetoothAdapter.getDefaultAdapter().getName();

        /* Nếu điện thoại không hỗ trợ Bluetooth thì thông báo */
        if (bluetoothAdapter == null) {
            new AlertDialog.Builder(this)
                    .setTitle("Thông báo")
                    .setMessage("Điện thoại của bạn không hỗ trợ Bluetooth")
                    .setPositiveButton("Exit", (dialog, which) -> System.exit(0))
                    .show();
        }

        /* Nếu chưa bật Bluetooth trong điện thoại */
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBT, 1);
        }

        /* Khởi tạo giao diện Welcome */
        welcomeFragment = new WelcomeFragment();
        fragmentManager = getFragmentManager();

        /* Hiển thị giao diện Welcome */
        fragmentManager.beginTransaction()
                .replace(R.id.frameWelcome, welcomeFragment)
                .commit();

        /* Khởi tạo GameBoard */
        gameBoard = new GameBoard(this, true);
        isPlaying = false;

        /* Ánh xạ giao diện */
        avatarUser1 = findViewById(R.id.avatarUser1);
        avatarUser2 = findViewById(R.id.avatarUser2);
        nameUser1 = findViewById(R.id.nameUser1);
        nameUser2 = findViewById(R.id.nameUser2);
        imgExit = findViewById(R.id.exit);

        /* Sự kiện thoát khỏi phòng */
        imgExit.setOnClickListener(v -> {

            AlertDialog.Builder b = new AlertDialog.Builder(v.getContext());
            b.setTitle("Xác nhận");
            b.setMessage("Bạn có muốn thoát?");

            // Nếu cancel
            b.setNegativeButton("Cancel", (dialog, id) -> dialog.cancel());

            // Nếu OK - đồng ý đăng ký
            b.setPositiveButton("Ok", (dialog, id) -> {

                /* Hiển thị lại giao diện Welcome */
                fragmentManager.beginTransaction()
                        .replace(R.id.frameWelcome, welcomeFragment)
                        .commit();

                /* Reset bàn cờ và cập nhật số trận đánh */
                gameBoard.reSet();

                /* Nếu đang chơi, thì gửi thông điệp đầu hàng */
                if (isPlaying) {
                    MainActivity.connectedBluetooth.sendData("S@U@R@R@E@N@D@E@R@".getBytes());
                    /* Tắt luôn kết nối */
                    MainActivity.connectedBluetooth.cancel();
                }
            });
            b.show();
        });
    }

    // region FULL SCREEN
    // Source: https://developer.android.com/training/system-ui/immersive
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
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
