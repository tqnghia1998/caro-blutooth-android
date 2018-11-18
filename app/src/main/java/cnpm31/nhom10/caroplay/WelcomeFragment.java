package cnpm31.nhom10.caroplay;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ToggleButton;

import cnpm31.nhom10.caroplay.Bluetooth.ServerBluetooth;

import static cnpm31.nhom10.caroplay.MainActivity.fragmentManager;

public class WelcomeFragment extends android.app.Fragment {

    // region CÁC KHAI BÁO LIÊN QUAN ĐẾN GIAO DIỆN
    public EditText usernameInput;
    public Button btnCreateRoom;
    public ToggleButton btnFindRoom;
    public ListroomFragment listroomFragment;
    // endregion

    public WelcomeFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_welcome, container, false);

       /* Ánh xạ giao diện */
        usernameInput = view.findViewById(R.id.usernameInput);
        btnCreateRoom = view.findViewById(R.id.btnCreateRoom);
        btnFindRoom = view.findViewById(R.id.btnFindRoom);

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
            Intent getVisible = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            startActivityForResult(getVisible, 0);

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
        return view;
    }
}
