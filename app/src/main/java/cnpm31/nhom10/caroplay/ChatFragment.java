package cnpm31.nhom10.caroplay;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static android.support.v4.content.ContextCompat.getSystemService;
import static android.support.v4.content.ContextCompat.getSystemServiceName;
import static cnpm31.nhom10.caroplay.MainActivity.connectedBluetooth;
import static cnpm31.nhom10.caroplay.MainActivity.fragmentManager;
import static cnpm31.nhom10.caroplay.MainActivity.isPlaying;
import static cnpm31.nhom10.caroplay.MainActivity.mainContext;


public class ChatFragment extends android.app.Fragment {

    // region CÁC KHAI BÁO LIÊN QUAN ĐẾN GIAO DIỆN
    public EditText editChat;
    public Button btnCloseChat;
    public Button btnResetText;
    public List<ImageButton> listImgButton;
    // endregion

    public ChatFragment() {}

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        /* Ánh xạ giao diện */
        editChat = view.findViewById(R.id.editChat);
        btnCloseChat = view.findViewById(R.id.btnCloseChat);
        btnResetText = view.findViewById(R.id.btnResetText);

        /* Sự kiện thoát */
        btnCloseChat.setOnClickListener(v -> {
            fragmentManager.beginTransaction().remove(ChatFragment.this).commit();
        });

        /* Sự kiện xóa edit text */
        btnResetText.setOnClickListener(v -> editChat.setText(""));

        /* Sự kiện gửi tin nhắn */
        editChat.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                if (editChat.getText().toString().length() > 0) {
                    if (isPlaying) {
                        connectedBluetooth.sendData(("T@E@X@T@" + editChat.getText().toString()).getBytes());
                        fragmentManager.beginTransaction().remove(ChatFragment.this).commit();
                    }
                }
            }
            return false;
        });

        /* Các sự kiện gửi sticker */
        listImgButton = new ArrayList<>();
        listImgButton.add(view.findViewById(R.id.afterBoomSticker));
        listImgButton.get(0).setOnClickListener(v -> {
            if (isPlaying) {
                connectedBluetooth.sendData("S@T@I@C@K@E@R@A@F@T@E@R@B@O@O@M@".getBytes());
            }
            fragmentManager.beginTransaction().remove(ChatFragment.this).commit();
        });
        listImgButton.add(view.findViewById(R.id.beatBrickSticker));
        listImgButton.get(1).setOnClickListener(v -> {
            if (isPlaying) {
                connectedBluetooth.sendData("S@T@I@C@K@E@R@B@E@A@T@B@R@I@C@K@".getBytes());
            }
            fragmentManager.beginTransaction().remove(ChatFragment.this).commit();
        });
        listImgButton.add(view.findViewById(R.id.bigSmileSticker));
        listImgButton.get(2).setOnClickListener(v -> {
            if (isPlaying) {
                connectedBluetooth.sendData("S@T@I@C@K@E@R@B@I@G@S@M@I@L@E@".getBytes());
            }
            fragmentManager.beginTransaction().remove(ChatFragment.this).commit();
        });
        listImgButton.add(view.findViewById(R.id.bossSticker));
        listImgButton.get(3).setOnClickListener(v -> {
            if (isPlaying) {
                connectedBluetooth.sendData("S@T@I@C@K@E@R@B@O@S@S@".getBytes());
            }
            fragmentManager.beginTransaction().remove(ChatFragment.this).commit();
        });
        listImgButton.add(view.findViewById(R.id.dribbleSticker));
        listImgButton.get(4).setOnClickListener(v -> {
            if (isPlaying) {
                connectedBluetooth.sendData("S@T@I@C@K@E@R@D@R@I@B@B@L@E@".getBytes());
            }
            fragmentManager.beginTransaction().remove(ChatFragment.this).commit();
        });
        listImgButton.add(view.findViewById(R.id.hellBoySticker));
        listImgButton.get(5).setOnClickListener(v -> {
            if (isPlaying) {
                connectedBluetooth.sendData("S@T@I@C@K@E@R@H@E@L@L@B@O@Y@".getBytes());
            }
            fragmentManager.beginTransaction().remove(ChatFragment.this).commit();
        });
        return view;
    }
}
