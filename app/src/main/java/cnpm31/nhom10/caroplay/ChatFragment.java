package cnpm31.nhom10.caroplay;

import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.audiofx.PresetReverb;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cnpm31.nhom10.caroplay.GameBoard.SingletonSharePrefs;

import static cnpm31.nhom10.caroplay.MainActivity.connectedBluetooth;
import static cnpm31.nhom10.caroplay.MainActivity.fragmentManager;
import static cnpm31.nhom10.caroplay.MainActivity.isPlaying;
import static cnpm31.nhom10.caroplay.MainActivity.mainContext;
import static cnpm31.nhom10.caroplay.MainActivity.pos;
import static cnpm31.nhom10.caroplay.MainActivity.queueData;

public class ChatFragment extends android.app.Fragment {

    // region CÁC KHAI BÁO LIÊN QUAN ĐẾN GIAO DIỆN
    public EditText editChat;
    public Button btnCloseChat;
    public Button btnResetText;
    public ImageButton btnRecorder;
    public List<ImageButton> listImgButton;
    // endregion

    public boolean IsRecording = false;
    public MediaRecorder myRecorder;

    public ChatFragment() {}

    @SuppressLint({"ClickableViewAccessibility", "ResourceType"})
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        /* Ánh xạ giao diện */
        editChat = view.findViewById(R.id.editChat);
        btnCloseChat = view.findViewById(R.id.btnCloseChat);
        btnResetText = view.findViewById(R.id.btnResetText);
        btnRecorder = view.findViewById(R.id.btnRecorder);

        /* Sự kiện ghi âm */
        btnRecorder.setOnClickListener(v -> {

            // Nếu đang ghi thì dừng
            if (IsRecording){
                IsRecording = false;
                myRecorder.stop();
                myRecorder.release();
                myRecorder = null;
                try {
                    // Mở và cắt file nhắn thoại thành từng phân mảnh
                    MainActivity.connectedBluetooth.sendFile(SingletonSharePrefs
                            .getInstance().get("caroPlayPath", String.class) + "/voice.3gp"
                            ,"V@O@I@C@E@");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // Gửi phân mảnh đầu tiên
                finally {
                    connectedBluetooth.sendData(queueData.get(pos));
                    pos++;
                }
                // Báo hiệu trên button
                btnRecorder.setBackgroundResource(R.drawable.button_style);
                btnRecorder.setImageResource(R.drawable.micro);
            }
            else {
                // Tạo một kiểu ghi âm mới
                myRecorder = new MediaRecorder();
                myRecorder.reset();
                try {
                    myRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    myRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                    myRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
                    myRecorder.setOutputFile(SingletonSharePrefs
                            .getInstance().get("caroPlayPath", String.class) + "/voice.3gp");
                }
                catch (Exception ignored){}
                try{
                    IsRecording = true;
                    myRecorder.prepare();
                    myRecorder.start();
                } catch (Exception ignored){}

                // Báo hiệu trên button
                btnRecorder.setBackgroundResource(R.drawable.button_style_red);
                btnRecorder.setImageResource(R.drawable.micro_listening);
            }
        });

        /* Sự kiện thoát */
        btnCloseChat.setOnClickListener(v -> {
            fragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.zoom_out_animation, R.anim.zoom_out_animation)
                    .remove(ChatFragment.this).commit();
        });

        /* Sự kiện xóa edit text */
        btnResetText.setOnClickListener(v -> editChat.setText(""));

        /* Sự kiện gửi tin nhắn */
        editChat.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                if (editChat.getText().toString().length() > 0) {
                    if (isPlaying) {
                        connectedBluetooth.sendData(("T@E@X@T@" + editChat.getText().toString()).getBytes());
                        fragmentManager.beginTransaction()
                                .setCustomAnimations(R.anim.zoom_out_animation, R.anim.zoom_out_animation)
                                .remove(ChatFragment.this).commit();
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
            fragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.zoom_out_animation, R.anim.zoom_out_animation)
                    .remove(ChatFragment.this).commit();
        });
        listImgButton.add(view.findViewById(R.id.beatBrickSticker));
        listImgButton.get(1).setOnClickListener(v -> {
            if (isPlaying) {
                connectedBluetooth.sendData("S@T@I@C@K@E@R@B@E@A@T@B@R@I@C@K@".getBytes());
            }
            fragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.zoom_out_animation, R.anim.zoom_out_animation)
                    .remove(ChatFragment.this).commit();
        });
        listImgButton.add(view.findViewById(R.id.bigSmileSticker));
        listImgButton.get(2).setOnClickListener(v -> {
            if (isPlaying) {
                connectedBluetooth.sendData("S@T@I@C@K@E@R@B@I@G@S@M@I@L@E@".getBytes());
            }
            fragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.zoom_out_animation, R.anim.zoom_out_animation)
                    .remove(ChatFragment.this).commit();
        });
        listImgButton.add(view.findViewById(R.id.bossSticker));
        listImgButton.get(3).setOnClickListener(v -> {
            if (isPlaying) {
                connectedBluetooth.sendData("S@T@I@C@K@E@R@B@O@S@S@".getBytes());
            }
            fragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.zoom_out_animation, R.anim.zoom_out_animation)
                    .remove(ChatFragment.this).commit();
        });
        listImgButton.add(view.findViewById(R.id.dribbleSticker));
        listImgButton.get(4).setOnClickListener(v -> {
            if (isPlaying) {
                connectedBluetooth.sendData("S@T@I@C@K@E@R@D@R@I@B@B@L@E@".getBytes());
            }
            fragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.zoom_out_animation, R.anim.zoom_out_animation)
                    .remove(ChatFragment.this).commit();
        });
        listImgButton.add(view.findViewById(R.id.hellBoySticker));
        listImgButton.get(5).setOnClickListener(v -> {
            if (isPlaying) {
                connectedBluetooth.sendData("S@T@I@C@K@E@R@H@E@L@L@B@O@Y@".getBytes());
            }
            fragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.zoom_out_animation, R.anim.zoom_out_animation)
                    .remove(ChatFragment.this).commit();
        });
        return view;
    }
}
