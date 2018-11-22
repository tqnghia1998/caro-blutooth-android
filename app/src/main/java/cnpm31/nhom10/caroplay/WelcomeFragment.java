package cnpm31.nhom10.caroplay;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.v17.leanback.widget.Util;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import cnpm31.nhom10.caroplay.Bluetooth.ServerBluetooth;
import cnpm31.nhom10.caroplay.GameBoard.GameBoard;
import cnpm31.nhom10.caroplay.GameBoard.SingletonSharePrefs;

import static cnpm31.nhom10.caroplay.EditProfileFragment.getCircledBitmap;
import static cnpm31.nhom10.caroplay.MainActivity.avatarUser1;
import static cnpm31.nhom10.caroplay.MainActivity.avatarUser2;
import static cnpm31.nhom10.caroplay.MainActivity.bluetoothAdapter;
import static cnpm31.nhom10.caroplay.MainActivity.fragmentManager;
import static cnpm31.nhom10.caroplay.MainActivity.mainContext;
import static cnpm31.nhom10.caroplay.MainActivity.nameUser2;

public class WelcomeFragment extends android.app.Fragment {

    // region CÁC KHAI BÁO LIÊN QUAN ĐẾN GIAO DIỆN
    public Button btnCreateRoom;
    public ToggleButton btnFindRoom;
    public static EditText username;
    public Button btnEditProfile;
    public ListroomFragment listroomFragment;
    // endregion

    public WelcomeFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_welcome, container, false);

       /* Ánh xạ giao diện */
        btnCreateRoom = view.findViewById(R.id.btnCreateRoom);
        btnFindRoom = view.findViewById(R.id.btnFindRoom);
        username = view.findViewById(R.id.username);

        /* Thiết lập một số giao diện */
        username.setText(SingletonSharePrefs.getInstance().get("name", String.class));
        avatarUser2.setImageResource(R.drawable.avatar_user1);
        avatarUser2.setBackgroundResource(R.drawable.effect);
        nameUser2.setText("");

        /* Sự kiện button Tạo phòng */
        btnCreateRoom.setOnClickListener(v -> {

            /* Tắt giao diện tìm phòng*/
            btnFindRoom.setChecked(false);

            /* Tắt giao diện Welcome */
            getFragmentManager()
                    .beginTransaction()
                    .remove(this)
                    .commit();

            /* Bật chế độ hiển thị với các thiết bị khác */
            if(bluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE)
            {
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                startActivityForResult(intent, 0);
            }

            /* Lúc này đang đóng vai trò là một server */
            MainActivity.serverBluetooth = new ServerBluetooth();
            MainActivity.serverBluetooth.start();
        });

        /* Khởi tạo giao diện tìm phòng */
        listroomFragment = new ListroomFragment();

        /* Sự kiện button Tìm phòng */
        btnFindRoom.setOnCheckedChangeListener((buttonView, isChecked) -> {
            /* Nếu button đang ấn thì hiển thị giao diện tìm phòng */
            if (isChecked) {
                fragmentManager.beginTransaction()
                        .replace(R.id.frameListroom, listroomFragment)
                        .commit();
            }
            else {
                fragmentManager.beginTransaction()
                        .remove(listroomFragment)
                        .commit();
            }
        });

        /* Chỉnh sửa thông tin cá nhân */
        EditProfileFragment editProfileFragment = new EditProfileFragment();

        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        btnEditProfile.setOnClickListener(v -> fragmentManager.beginTransaction()
                .add(R.id.frameWelcome, editProfileFragment)
                .commit());
        return view;
    }
}
