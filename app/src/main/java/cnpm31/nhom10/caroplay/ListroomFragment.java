package cnpm31.nhom10.caroplay;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import cnpm31.nhom10.caroplay.Bluetooth.ClientBluetooth;

import static cnpm31.nhom10.caroplay.MainActivity.bluetoothAdapter;


public class ListroomFragment extends android.app.Fragment {

    // region CÁC KHAI BÁO LIÊN QUAN ĐẾN GIAO DIỆN
    public ToggleButton btnScan;
    public ListView listViewRoom;
    public ArrayList<String> listRoom;
    public ArrayAdapter<String> listRoomAdapter;
    // endregion

    // region CÁC KHAI BIẾN LIÊN QUAN ĐẾN BLUETOOTH
    HashMap<String, BluetoothDevice> listDevices;
    public ClientBluetooth clientBluetooth;
    BroadcastReceiver bReciever;

    public ListroomFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_listroom, container, false);

        /* Ánh xạ giao diện */
        btnScan = view.findViewById(R.id.btnScan);
        listViewRoom = view.findViewById(R.id.listViewRoom);

        /* Khởi tạo danh sách BluetoothDevice */
        listDevices = new HashMap<>();

        /* Khởi tạo danh sách và adapter */
        listRoom = new ArrayList<>();

        /* Mặc định danh sách sẽ có các thiết bị đã paired */
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                listRoom.add(device.getName());
                listDevices.put(device.getName(), device);
            }
        }
        listRoomAdapter = new ArrayAdapter<>(view.getContext(), android.R.layout.simple_list_item_1, listRoom);
        listViewRoom.setAdapter(listRoomAdapter);

        /* Tạo BroadcastReceiver để nhận mỗi khi tìm thấy một thiết bị Bluetooth */
        bReciever = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                /* Nếu là Intent Bluetooth */
                if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                    /* Thêm vào danh sách */
                    String deviceName = device.getName();
                    if (deviceName == null || listRoom.contains(deviceName)) return;

                    listRoom.add(deviceName);
                    listDevices.put(deviceName, device);
                    listRoomAdapter.notifyDataSetChanged();
                }
            }
        };

        /* Sự kiện button Tìm kiếm (nếu thiết bị đang cần kết nối chưa paired mới dùng) */
        btnScan.setOnCheckedChangeListener((buttonView, isChecked) -> {
            /* Tạo bộ lọc Intent chỉ lấy những intent Bluetooth */
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);

            /* Nếu button đang ấn thì xóa và cập nhật lại */
            if (isChecked) {
                listRoom.clear();
                getActivity().registerReceiver(bReciever, filter);
                bluetoothAdapter.startDiscovery();
            }
            else {
                getActivity().unregisterReceiver(bReciever);
                bluetoothAdapter.cancelDiscovery();
            }
        });

        /* Sự kiện chọn phòng */
        listViewRoom.setOnItemClickListener((parent, view1, position, id) -> {

            /* Lấy BluetoothDevice tương ứng ra*/
            BluetoothDevice device = listDevices.get(listRoom.get(position));

            /* Kết nối đến server */
            clientBluetooth = new ClientBluetooth(device);
            clientBluetooth.start();
        });

        return view;
    }

    @Override
    public void onStop() {
        super.onStop();
        btnScan.setChecked(false);
    }
}
