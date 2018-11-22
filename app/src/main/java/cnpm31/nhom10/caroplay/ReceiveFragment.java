package cnpm31.nhom10.caroplay;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static cnpm31.nhom10.caroplay.MainActivity.connectedBluetooth;
import static cnpm31.nhom10.caroplay.MainActivity.fragmentManager;
import static cnpm31.nhom10.caroplay.MainActivity.isPlaying;


public class ReceiveFragment extends android.app.Fragment {

    // region CÁC KHAI BÁO LIÊN QUAN ĐẾN GIAO DIỆN
    public TextView txtReceive;
    // endregion

    public ReceiveFragment() {}

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_receive, container, false);

        /* Ánh xạ giao diện */
        txtReceive = view.findViewById(R.id.txtReceive);

        txtReceive.setText(getArguments().getString("text"));

        return view;
    }
}
