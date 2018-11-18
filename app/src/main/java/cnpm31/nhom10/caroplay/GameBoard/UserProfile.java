package cnpm31.nhom10.caroplay.GameBoard;

public class UserProfile {

    public String nameUser;     /* Tên người chơi */
    public byte[] avatarImg;    /* Ảnh avatar (xử lý sau) */

    /* Không cho tùy ý tạo đối tượng */
    private UserProfile() {}

    public UserProfile(String _nameUser, byte[] _avatarImg) {
        nameUser = _nameUser;
        avatarImg = _avatarImg;
    }
}
