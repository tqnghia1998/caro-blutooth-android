- Nhóm 11

- Môn học: Phát triển ứng dụng trên thiết bị di động

- Khóa 2016, năm học 2018 - 1019

- Giảng viên: Trương Toàn Thịnh, Ngô Ngọc Đăng Khoa

- Thành viên:

  + 1612422 - Trịnh Quang Nghĩa (Leader)
  + 1612425 - Tạ Đăng Hiếu Nghĩa (Developer)
  + 1612421 - Nguyễn Ngọc Nghĩa (Developer)
  + 1612424 - Đặng Ngọc Nghĩa (Developer)

-> Cập nhật ngày 13/11: Hoàn thành tính năng chơi cờ caro giữa hai thiết bị thông qua bluetooth.
-> Cập nhật ngày 18/11:
+ Bỏ dùng thư viện có sẵn, tự viết phần bluetooth
https://developer.android.com/guide/topics/connectivity/bluetooth
+ Biến dùng để quản lý kết nối: MainActivity.connectedBluetooth
    . Gửi tới đối phương: Phương thức sendData()
    . Gửi tới chính mình (vì đang chạy trong thread nên không thể thao tác trên UI): Dùng Handler
    -> MainActivity.connectionHandler.obtainMessage(Loại tin, Số byte, <Không cần thiết>, buffer dạng byte[]).sendToTarget();
    . Hàm để nhận kết nối: Xem trong phần CÁC KHAI BÁO LIÊN QUAN ĐẾN BLUETOOTH, chỗ @SuppressLint("HandlerLeak")