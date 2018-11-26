package cnpm31.nhom10.caroplay;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.UUID;

import cnpm31.nhom10.caroplay.Bluetooth.ConnectedBluetooth;
import cnpm31.nhom10.caroplay.Bluetooth.ServerBluetooth;
import cnpm31.nhom10.caroplay.GameBoard.GameBoard;
import cnpm31.nhom10.caroplay.GameBoard.SingletonSharePrefs;

import static cnpm31.nhom10.caroplay.EditProfileFragment.getCircledBitmap;

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

            // region Thông điệp gửi ảnh avatar
            /*if (msg.arg1 > 1024) {
                Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, msg.arg1);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    avatarUser2.setImageBitmap(getCircledBitmap(bmp));
                    Toast.makeText(mainContext, "" + msg.arg1, Toast.LENGTH_SHORT).show();
                }
                return;
            }*/
            // endregion

            String message = new String(data);

            if (message.equals("Have new chat voice")){
                btnPlay.setBackgroundResource(R.drawable.effect);
                CanPlay = true;
            }

            // region Thông điệp gửi tên và giới tính
            if (message.length() > 8 && message.substring(0, 8).equals("N@A@M@E@")) {
                nameUser2.setText(message.substring(8));
                /*// Gửi avatar cho đối phương
                Bitmap bitmap = ((BitmapDrawable)avatarUser1.getDrawable()).getBitmap();
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG,100, stream);
                byte [] dataBitmap = stream.toByteArray();
                new CountDownTimer(250, 250) {
                    public void onTick(long millisUntilFinished) {}
                    public void onFinish() {
                        connectedBluetooth.sendData(dataBitmap);
                    }
                }.start();*/
            }
            /*if (message.equals("A@V@A@T@A@R@")) {
                // Gửi avatar cho đối phương
                Bitmap bitmap = ((BitmapDrawable)avatarUser1.getDrawable()).getBitmap();
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG,100, stream);
                byte [] dataBitmap = stream.toByteArray();
                connectedBluetooth.sendData(dataBitmap);
            }*/
            // endregion

            // region Thông điệp liên quan đến kết nối thành công/thất bại
            /* Nếu mình đang là server, và nhận được confirm từ client */
            if (message.equals(mainContext.getString(R.string.CLIENTCONFIRM))) {

                // Thông báo bắt đầu chơi
                AlertDialog.Builder b = new AlertDialog.Builder(imgExit.getContext());
                b.setTitle("Đối thủ đã vào phòng!");
                b.setMessage("Bạn là người chơi trước.");
                b.setNegativeButton("Ok", (dialog, which) -> {
                    dialog.cancel();
                });
                b.show();

                // Mình có thể đi nước đầu tiên
                isPlaying = true;
                gameBoard.isWaiting = false;
                avatarUser2.setBackgroundResource(0);
                // Gửi tên cho đối phương
                connectedBluetooth.sendData(("N@A@M@E@" + nameUser1.getText().toString()).getBytes());
            }
            /* Nếu mình đang là client, và nhận được confirm từ server */
            else if (message.equals(mainContext.getString(R.string.SERVERCONFIRM))) {

                // Thông báo bắt đầu chơi
                AlertDialog.Builder b = new AlertDialog.Builder(imgExit.getContext());
                b.setTitle("Bạn đã vào phòng!");
                b.setMessage("Chủ phòng là người chơi trước.");
                b.setNegativeButton("Ok", (dialog, which) -> {
                    dialog.cancel();
                });
                b.show();

                // Tắt giao diện Welcome
                isPlaying = true;
                gameBoard.isWaiting = true;
                fragmentManager.beginTransaction().remove(welcomeFragment).commit();
                avatarUser1.setBackgroundResource(0);
                // Gửi tên cho đối phương
                connectedBluetooth.sendData(("N@A@M@E@" + nameUser1.getText().toString()).getBytes());
            }
            // Nếu nhận được tin CONNECT FAILED, thì đó chính là
            // mình tự gửi cho mình khi kết nối thất bại/mất kết nối
            else if (message.equals("C@O@N@N@E@C@T@ @F@A@I@L@E@D@")) {
                AlertDialog.Builder b = new AlertDialog.Builder(imgExit.getContext());
                b.setTitle("Không thể kết nối!");
                b.setMessage("Vui lòng thử lại.");
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
                if (!isPlaying) return;
                AlertDialog.Builder b = new AlertDialog.Builder(imgExit.getContext());
                b.setTitle("Đối thủ đã thoát!");
                b.setMessage("Nhấn OK để chờ đối thủ mới.\nNhấn Cancel để xem lại trận đánh.");
                b.setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.cancel();
                });
                b.setPositiveButton("Ok", (dialog, which) -> {
                    gameBoard.reSet();
                    serverBluetooth = new ServerBluetooth();
                    serverBluetooth.start();
                });
                b.show();
                /* Tắt luôn kết nối */
                MainActivity.connectedBluetooth.cancel();
                isPlaying = false;
                gameBoard.isWaiting = true;
                avatarUser2.setImageResource(R.drawable.avatar_user1);
                avatarUser2.setBackgroundResource(0);
                avatarUser1.setBackgroundResource(R.drawable.effect);
                nameUser2.setText("");
            }
            // endregion

            // region Thông điệp chat/tin nhắn thoại
            if (message.length() > 8 && message.substring(0, 8).equals("T@E@X@T@")) {
                ReceiveFragment receiveFragment = new ReceiveFragment();
                Bundle bundle = new Bundle();
                bundle.putString("text", message.substring(8));
                receiveFragment.setArguments(bundle);

                // Tự động ẩn trong 3s
                new CountDownTimer(3000, 1000) {
                    public void onTick(long millisUntilFinished) {
                        fragmentManager.beginTransaction().replace(R.id.tooltipReceive, receiveFragment).commit();
                    }
                    public void onFinish() {
                        fragmentManager.beginTransaction().remove(receiveFragment).commit();
                    }
                }.start();
            }
            if (message.length() > 14 && message.substring(0, 14).equals("S@T@I@C@K@E@R@")) {
                String sticker = message.substring(14);

                // Tự động ẩn trong 3s
                new CountDownTimer(3000, 1000) {
                    public void onTick(long millisUntilFinished) {
                        if (sticker.equals("A@F@T@E@R@B@O@O@M@")) imgSticker.setBackgroundResource(R.drawable.after_boom_sticker);
                        if (sticker.equals("B@E@A@T@B@R@I@C@K@")) imgSticker.setBackgroundResource(R.drawable.beat_brick_sticker);
                        if (sticker.equals("B@I@G@S@M@I@L@E@")) imgSticker.setBackgroundResource(R.drawable.big_smile_sticker);
                        if (sticker.equals("B@O@S@S@")) imgSticker.setBackgroundResource(R.drawable.boss_sticker);
                        if (sticker.equals("D@R@I@B@B@L@E@")) imgSticker.setBackgroundResource(R.drawable.dribble_sticker);
                        if (sticker.equals("H@E@L@L@B@O@Y@")) imgSticker.setBackgroundResource(R.drawable.hell_boy_sticker);
                    }
                    public void onFinish() {
                        imgSticker.setBackgroundResource(0);
                    }
                }.start();
            }
            // endregion
        }
    };
    // endregion

    // region CÁC KHAI BÁO LIÊN QUAN ĐẾN GIAO DIỆN CHƠI CỜ
    public static GameBoard gameBoard;
    public static ImageView imgExit;
    public static boolean isPlaying;
    public static TextView nameUser1;
    public static TextView nameUser2;
    public static ImageView avatarUser1;
    public static ImageView imgSticker;
    public static ImageView avatarUser2;
    public static ImageButton btnPlay;

    // endregion

    // Đường dẫn lưu file chat voice nhận được
    public static final String mOutputFile = Environment.getExternalStorageDirectory().getAbsolutePath()+"/receive.3gp";
    public static boolean CanPlay = false;

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

        CheckSomePermission();

        /* Khởi tạo GameBoard */
        gameBoard = new GameBoard(this, true);
        isPlaying = false;

        /* Khởi tạo giao diện Welcome */
        welcomeFragment = new WelcomeFragment();
        fragmentManager = getFragmentManager();

        /* Hiển thị giao diện Welcome */
        fragmentManager.beginTransaction()
                .replace(R.id.frameWelcome, welcomeFragment)
                .commit();

        /* Ánh xạ giao diện */
        avatarUser1 = findViewById(R.id.avatarUser1);
        avatarUser2 = findViewById(R.id.avatarUser2);
        imgSticker = findViewById(R.id.imgSticker);
        nameUser1 = findViewById(R.id.nameUser1);
        nameUser2 = findViewById(R.id.nameUser2);
        imgExit = findViewById(R.id.exit);
        btnPlay = findViewById(R.id.btnPlay);

        /* Sự kiện reload avatar đối phương */
        /*avatarUser2.setOnClickListener(v -> {
            connectedBluetooth.sendData("A@V@A@T@A@R@".getBytes());
        });*/

        /* Cập nhật ảnh đại diện và tên */
        nameUser1.setText(SingletonSharePrefs.getInstance().get("name", String.class));
        String savedDirectory = SingletonSharePrefs.getInstance().get("caroPlayPath", String.class);
        if (!savedDirectory.equals("")){
            File savedAvatar =  new File(savedDirectory, "avatar.jpg");
            try {
                Bitmap savedBitmap = BitmapFactory.decodeStream(new FileInputStream(savedAvatar));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    savedBitmap = getCircledBitmap(savedBitmap);
                }
                avatarUser1.setImageBitmap(savedBitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
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
                    isPlaying = false;
                }
            });
            b.show();
        });

        /* Sự kiện nhắn tin */
        ChatFragment chatFragment = new ChatFragment();
        avatarUser1.setOnClickListener(v -> {
            if (getFragmentManager().beginTransaction().isEmpty()){
                fragmentManager.beginTransaction()
                        .replace(R.id.tooltipChat, chatFragment)
                        .commit();
            }
        });

        // Sự kiện phát tin nhắn thoại nhận được
        btnPlay.setOnClickListener(v -> {

            if(!CanPlay){
                return;
            }

            MediaPlayer mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setDataSource(mOutputFile);
                mediaPlayer.prepare();
                mediaPlayer.start();
                Toast.makeText(getApplicationContext(), "Playing Audio", Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                // make something
            }
            finally {
                btnPlay.setBackgroundResource(0);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
    // endregion

    // region Helper Methods

    // Kiểm tra các quyền của ứng dụng
    public void CheckSomePermission(){
        // Check for permissions
        int permission = ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permission2 = ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO);

        // If we don't have permissions, ask user for permissions
        if (permission != PackageManager.PERMISSION_GRANTED) {
            String[] PERMISSIONS_STORAGE = {
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            };
            int REQUEST_EXTERNAL_STORAGE = 1;

            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }

        if (permission2!=PackageManager.PERMISSION_GRANTED){
            String[] RECORDER_AUDIO = {Manifest.permission.RECORD_AUDIO};
            int REQUEST = 1;
            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    RECORDER_AUDIO,
                    REQUEST
            );
        }
    }

    // Xử lí khi nhận được tin nhắn là chat voice

    public static void HandleChatVoiceMessage(String type, byte[] data){
        CanPlay = false;

        if (type.equals("V@O@I@C@E@")){
            try {
                FileOutputStream out = new FileOutputStream(mOutputFile,false);
                out.write(data);

            } catch (Exception ex) {

            }
        }
        else
        {
            try {
                FileOutputStream out = new FileOutputStream(mOutputFile,true);
                out.write(data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (data.length < 1024){
            CanPlay = true;
            btnPlay.setBackgroundResource(R.drawable.effect);
        }
    }

    // endregion
}
