package cnpm31.nhom10.caroplay.GameBoard;

import cnpm31.nhom10.caroplay.R;

public class ItemStatus {

    public ItemStatus(char status, int picture) {
        this.status = status;
        this.image = picture;
    }

    public char getStatus() {
        return status;
    }

    public void setStatus(char status) {
        this.status = status;
        switch (status) {
            case 0: image = R.drawable.status_empty; break;
            case 1: image = R.drawable.status_user1; break;
            case 2: image = R.drawable.status_user2; break;
            case 3: image = R.drawable.status_temporary; break;
        }
    }

    public int getImage() {
        return image;
    }

    public void setImage(int picture) {
        this.image = picture;
    }

    private char status;
    private int image;
}
