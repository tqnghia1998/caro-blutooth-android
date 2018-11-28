package cnpm31.nhom10.caroplay.GameBoard;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import cnpm31.nhom10.caroplay.Gestures.MoveGestureDetector;
import cnpm31.nhom10.caroplay.Gestures.MoveListener;
import cnpm31.nhom10.caroplay.Gestures.ScaleListener;
import cnpm31.nhom10.caroplay.MainActivity;
import cnpm31.nhom10.caroplay.R;

import static cnpm31.nhom10.caroplay.MainActivity.mainContext;
import static com.facebook.AccessTokenManager.SHARED_PREFERENCES_NAME;

public class GameBoard {

    // Định nghĩa các trạng thái của một ô
    public static char STATUS_EMPTY = 0;
    public static char STATUS_USER1 = 1;
    public static char STATUS_USER2 = 2;
    public static char STATUS_TEMPORARY = 3;

    // Các đối tượng để hiển thị Grid view
    public GridView gvGame;
    public GameGridViewAdapter gridViewAdapter;
    public List<ItemStatus> boardGame;
    public boolean isWaiting;

    // Biến lưu vị trí ô tạm (ô vừa click một lần)
    private int currTempPos = -1;

    // Các Gesture Detector xử lý zoom và move
    private ScaleGestureDetector mScaleDetector;
    private MoveGestureDetector mMoveDetector;

    Context _context;

    // Array dùng để lưu bàn cờ
    public static String MACUser2 = new String("");
    public static StringBuilder listMoves = new StringBuilder();

    // Constructor
    @SuppressLint("ClickableViewAccessibility")
    public GameBoard(Context context, boolean _isWaiting) {

        _context = context;
        isWaiting = _isWaiting;

        // Ánh xạ control
        gvGame = ((Activity)_context).findViewById(R.id.gvGame);
        boardGame = new ArrayList<>();
        for (int i = 0; i < 400; i++) boardGame.add(new ItemStatus(STATUS_EMPTY, R.drawable.status_empty));

        // Thiết lập adapter
        gridViewAdapter = new GameGridViewAdapter(_context, R.layout.grid_view_item, boardGame);
        gvGame.setAdapter(gridViewAdapter);

        // Thiết lập Detectors Gesture
        mScaleDetector = new ScaleGestureDetector(_context, new ScaleListener());
        mMoveDetector = new MoveGestureDetector(_context, new MoveListener());

        // Thiết lập sự kiện zoom và di chuyển
        gvGame.setOnTouchListener((v, event) -> {

            // Gọi hai phương thức sau để lấy scale và tọa độ
            mScaleDetector.onTouchEvent(event);
            mMoveDetector.onTouchEvent(event);

            // Cập nhật scale
            gvGame.setScaleX(ScaleListener.mScaleFactor);
            gvGame.setScaleY(ScaleListener.mScaleFactor);

            // Cập nhậ tọa độ
            gvGame.setTranslationX(MoveListener.mFocusX);
            gvGame.setTranslationY(MoveListener.mFocusY);
            return false;
        });

        // Sự kiện chọn ô
        gvGame.setOnItemClickListener((parent, view, position, id) -> {

            // Nếu đang chờ đối thủ đánh thì thôi
            if (isWaiting) return;

            // Nếu ô đó còn trống, thì gán cho nó thành ô tạm
            if (boardGame.get(position).getStatus() == STATUS_EMPTY) {

                // Xóa ô tạm cũ đi
                if (currTempPos != -1) boardGame.get(currTempPos).setStatus(STATUS_EMPTY);

                // Gán ô tạm mới
                boardGame.get(position).setStatus(STATUS_TEMPORARY);
                currTempPos = position;

                gridViewAdapter.notifyDataSetChanged();
            }
            else
            // Nếu ô đó vừa click rồi, giờ click lần nữa thì chọn ô đó
            if (boardGame.get(position).getStatus() == STATUS_TEMPORARY) {

                // Đặt nước đi của mình lên bàn cờ
                boardGame.get(position).setStatus(STATUS_USER1);
                currTempPos = -1;
                gridViewAdapter.notifyDataSetChanged();

                // Lưu vào bộ nhớ
                // saveBoard(position, true);

                // Đợi đối thủ
                isWaiting = true;
                MainActivity.avatarUser1.setBackgroundResource(0);
                MainActivity.avatarUser2.setBackgroundResource(R.drawable.effect);

                /* --------CHUYỂN DỮ LIỆU Ở ĐÂY -------- */
                // Khi nhận dữ liệu, kiểm tra xem ký tự đầu và cuối có phải là "[" và "]"
                MainActivity.connectedBluetooth.sendData(("[" + position + "]").getBytes());

                // Kiểm tra mình thắng hay không
                if (isWin(position, STATUS_USER1)) {
                    AlertDialog.Builder b = new AlertDialog.Builder(view.getContext());
                    b.setTitle("Chúc mừng");
                    b.setMessage("Bạn đã chiến thắng!");
                    b.show();
                }
            }
            // Nếu ô đó là ô đã chọn (kể cả mình hay đối thủ), thì chỉ cần xóa ô tạm cũ
            else {
                boardGame.get(currTempPos == -1 ? 0 : currTempPos).setStatus(STATUS_EMPTY);
                gridViewAdapter.notifyDataSetChanged();
            }
        });
    }

    // Phương thức chọn ô của đối thủ
    public void rivalMove(int position) {

        // Cập nhật bàn cờ
        boardGame.get(position).setStatus(STATUS_USER2);
        gridViewAdapter.notifyDataSetChanged();

        // Lưu vào bộ nhớ
        // saveBoard(position, false);

        // Không đợi nữa, tới lượt mình
        isWaiting = false;
        MainActivity.avatarUser2.setBackgroundResource(0);
        MainActivity.avatarUser1.setBackgroundResource(R.drawable.effect);

        // Kiểm tra đối thủ thắng hay không
        if (isWin(position, STATUS_USER2)) {

            // Hiển thị thông báo
            AlertDialog.Builder b = new AlertDialog.Builder(_context);
            b.setTitle("Chia buồn");
            b.setMessage("Bạn đã thua cuộc!");
            b.show();

            // Không cho chơi nữa
            isWaiting = true;
        }
    }

    // Phương thức kiểm tra mình thắng hay thua
    public boolean isWin(int position, char user) {

        // Kiểm tra cột
        int toadoX = position / 20;
        int toadoY = position % 20;

        int countCol = 1;
        int countRow = 1;
        int countMainDiagonal = 1;
        int countSkewDiagonal = 1;

        while(--toadoX >= 0 && boardGame.get(toadoX * 20 + toadoY).getStatus() == user) countCol++;
        toadoX = position / 20;
        while(++toadoX <= 19 && boardGame.get(toadoX * 20 + toadoY).getStatus() == user) countCol++;
        toadoX = position / 20;
        if (countCol >= 5) return true;

        // Kiểm tra dòng
        while(--toadoY >= 0 && boardGame.get(toadoX * 20 + toadoY).getStatus() == user) countRow++;
        toadoY = position % 20;
        while(++toadoY <= 19 && boardGame.get(toadoX * 20 + toadoY).getStatus() == user) countRow++;
        toadoY = position % 20;
        if (countRow >= 5) return true;

        // Kiểm tra chéo chính
        while(--toadoX >= 0 && --toadoY >= 0 && boardGame.get(toadoX * 20 + toadoY).getStatus() == user) countMainDiagonal++;
        toadoX = position / 20;
        toadoY = position % 20;
        while(++toadoX <= 19 && ++toadoY <= 19 && boardGame.get(toadoX * 20 + toadoY).getStatus() == user) countMainDiagonal++;
        toadoX = position / 20;
        toadoY = position % 20;
        if (countMainDiagonal >= 5) return true;

        // Kiểm tra chéo phụ
        while(--toadoX >= 0 && ++toadoY >= 0 && boardGame.get(toadoX * 20 + toadoY).getStatus() == user) countSkewDiagonal++;
        toadoX = position / 20;
        toadoY = position % 20;
        while(++toadoX <= 19 && --toadoY <= 19 && boardGame.get(toadoX * 20 + toadoY).getStatus() == user) countSkewDiagonal++;
        if (countSkewDiagonal >= 5) return true;

        return false;
    }

    // Phương thức reset bàn cờ
    public void reSet() {
        for (int i = 0; i < 400; i++) boardGame.get(i).setStatus(STATUS_EMPTY);
        gridViewAdapter.notifyDataSetChanged();
        isWaiting = true;
    }

    /*// Phương thức lưu bàn cờ
    public void saveBoard(int position, boolean isOurMove) {
        if (MACUser2.equals("")) return;
        if (!isOurMove) position = -position;
        listMoves.append(position).append(",");
        SingletonSharePrefs.getInstance().put(MACUser2, listMoves.toString());
        SingletonSharePrefs.getInstance().put("isWaiting", isWaiting);
    }*/

}
