package cnpm31.nhom10.caroplay;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;

public class LoginFragment extends android.app.Fragment {

    // Edit text nhập tên người chơi
    EditText userName;

    // Button tìm đối thủ
    Button btnConnect;
    Button btnListen;

    public LoginFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        // Ánh xạ các control
        userName = view.findViewById(R.id.userName);
        btnConnect = view.findViewById(R.id.btnConnect);
        btnListen = view.findViewById(R.id.btnListen);

        btnListen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent getVisible = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                startActivityForResult(getVisible, 0);
            }
        });

        // Lấy lại tên người chơi đã nhập trước đó
        userName.setText(MainActivity.userName);

        // Xử lý sự kiện button Tìm đối thủ
        btnConnect.setOnClickListener(v -> {

            // Nếu tên chưa nhập thì thông báo
            if (userName.getText().toString().isEmpty()) {
                Toast.makeText(getActivity(), "Vui lòng nhập tên của bạn", Toast.LENGTH_SHORT).show();
                return;
            }

            // Nếu tên đã nhập
            MainActivity.userName = userName.getText().toString();

            // Hiển thị danh sách các đối thủ
            MainActivity.findRival(getActivity());
        });

    return view;
    }
}
