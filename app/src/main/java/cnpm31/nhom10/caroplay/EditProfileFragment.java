package cnpm31.nhom10.caroplay;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import cnpm31.nhom10.caroplay.Bluetooth.ServerBluetooth;
import cnpm31.nhom10.caroplay.GameBoard.SingletonSharePrefs;

import static android.app.Activity.RESULT_OK;
import static cnpm31.nhom10.caroplay.MainActivity.avatarUser1;
import static cnpm31.nhom10.caroplay.MainActivity.bluetoothAdapter;
import static cnpm31.nhom10.caroplay.MainActivity.fragmentManager;
import static cnpm31.nhom10.caroplay.MainActivity.mainContext;
import static cnpm31.nhom10.caroplay.MainActivity.nameUser1;
import static cnpm31.nhom10.caroplay.MainActivity.welcomeFragment;
import static cnpm31.nhom10.caroplay.WelcomeFragment.username;

public class EditProfileFragment extends android.app.Fragment {

    // region CÁC KHAI BÁO LIÊN QUAN ĐẾN GIAO DIỆN
    ImageView imgAvatar;
    public Button btnBackToWelcome;
    public Button btnSave;
    public Button btnFromCamera;
    public Button btnFromLibrary;
    public EditText editUsername;
    public RadioGroup gender;
    // endregion

    public EditProfileFragment() {
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_editprofile, container, false);

        /* Ánh xạ giao diện */
        imgAvatar = view.findViewById(R.id.avatar);
        btnBackToWelcome = view.findViewById(R.id.btnBackToWelcome);
        btnSave = view.findViewById(R.id.btnSave);
        btnFromCamera = view.findViewById(R.id.btnFromCamera);
        btnFromLibrary = view.findViewById(R.id.btnFromLibrary);
        editUsername = view.findViewById(R.id.editUsername);
        gender = view.findViewById(R.id.gender);

        /* KHÔI PHỤC THÔNG TIN ĐỂ HIỂN THỊ LÊN GIAO DIỆN */
        editUsername.setText(SingletonSharePrefs.getInstance().get("name", String.class));
        if (SingletonSharePrefs.getInstance().get("gender", Boolean.class)) {
            gender.check(R.id.rdoMale);
        } else {
            gender.check(R.id.rdoFemale);
        }

        String savedDirectory = SingletonSharePrefs.getInstance().get("caroPlayPath", String.class);
        if (!savedDirectory.equals("")){
            File savedAvatar =  new File(savedDirectory, "avatar.jpg");
            try {
                Bitmap savedBitmap = BitmapFactory.decodeStream(new FileInputStream(savedAvatar));
                savedBitmap = getCircledBitmap(savedBitmap);
                imgAvatar.setImageBitmap(savedBitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        /* SỰ KIỆN LƯU THÔNG TIN */
        btnSave.setOnClickListener(v -> {
            // Lưu tên và giới tính trước
            SingletonSharePrefs.getInstance().put("name", editUsername.getText().toString());
            SingletonSharePrefs.getInstance().put("gender", gender.getCheckedRadioButtonId() == R.id.rdoMale);
            nameUser1.setText(SingletonSharePrefs.getInstance().get("name", String.class));
            username.setText(SingletonSharePrefs.getInstance().get("name", String.class));

            // Ghi ảnh vào bộ nhớ trong
            ContextWrapper cw = new ContextWrapper(getActivity());
            File directory = cw.getDir("caroPlayPath", Context.MODE_PRIVATE);
            File avatar = new File(directory,"avatar.jpg");
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(avatar);
                Bitmap bitmap = ((BitmapDrawable)imgAvatar.getDrawable()).getBitmap();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                avatarUser1.setImageBitmap(bitmap);
            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            try {
                assert out != null;
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Sau đó lưu đường dẫn vào
            SingletonSharePrefs.getInstance().put("caroPlayPath", directory.getAbsolutePath());

            // Thông báo người dùng
            AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
            b.setTitle("Thông báo!");
            b.setMessage("Thay đổi thông tin thành công.");
            b.setNegativeButton("Ok", (dialog, which) -> {
                dialog.cancel();
            });
            b.show();
        });

        /* Sự kiện quay về màn hình welcome */
        btnBackToWelcome.setOnClickListener(v -> fragmentManager
                .beginTransaction()
                .replace(R.id.frameWelcome, welcomeFragment).commit());

        /* Sự kiện thay avatar từ chụp ảnh */
        btnFromCamera.setOnClickListener(v -> {
            Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(takePicture, 0);
        });

        /* Sự kiện thay avatar từ thư viện */
        btnFromLibrary.setOnClickListener(v -> {
            Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(pickPhoto , 1);
        });

        return view;
    }

    /* Xử lý intent chụp ảnh và chọn ảnh từ thư viện */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        if (resultCode == RESULT_OK) {

            // Trường hợp chọn file ảnh từ thư viện
            if (imageReturnedIntent.getData() != null) {
                Uri selectedImage = imageReturnedIntent.getData();
                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                imgAvatar.setImageBitmap(getCircledBitmap(bitmap));
                return;
            }
            // Trường hợp chọn lấy trực tiếp từ ảnh mới chụp
            Bundle extras = imageReturnedIntent.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");

            imgAvatar.setImageBitmap(getCircledBitmap(imageBitmap));
        }
    }

    /* Phương thức cắt ảnh thành hình tròn */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static Bitmap getCircledBitmap(Bitmap bitmap) {

        /* Nếu ảnh ngang thì xoay ảnh đứng lại */
        if(bitmap.getWidth() > bitmap.getHeight()) {
            Matrix matrix = new Matrix();
            matrix.postRotate(-90);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        }

        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2, bitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }
}
