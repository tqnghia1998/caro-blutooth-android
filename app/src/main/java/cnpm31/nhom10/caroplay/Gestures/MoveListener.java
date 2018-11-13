package cnpm31.nhom10.caroplay.Gestures;

import android.graphics.PointF;
import android.widget.Toast;

public class MoveListener extends MoveGestureDetector.SimpleOnMoveGestureListener {

    public static float mFocusX;
    public static float mFocusY;

    @Override
    public boolean onMove(MoveGestureDetector detector) {
        PointF d = detector.getFocusDelta();
        mFocusX += (d.x * ScaleListener.mScaleFactor) / 3;
        mFocusY += (d.y * ScaleListener.mScaleFactor) / 3;
        if (mFocusX > (ScaleListener.mScaleFactor - 1.0F) * 480) {
            mFocusX = (ScaleListener.mScaleFactor - 1.0F) * 480;
        }
        if (mFocusX < -(ScaleListener.mScaleFactor - 1.0F) * 480) {
            mFocusX = -(ScaleListener.mScaleFactor - 1.0F) * 480;
        }
        if (mFocusY > (ScaleListener.mScaleFactor - 1.0F) * 480) {
            mFocusY = (ScaleListener.mScaleFactor - 1.0F) * 480;
        }
        if (mFocusY < -(ScaleListener.mScaleFactor - 1.0F) * 480) {
            mFocusY = -(ScaleListener.mScaleFactor - 1.0F) * 480;
        }
        return true;
    }
}
