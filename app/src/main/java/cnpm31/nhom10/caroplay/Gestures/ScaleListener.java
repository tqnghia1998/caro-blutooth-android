package cnpm31.nhom10.caroplay.Gestures;

import android.view.ScaleGestureDetector;

public class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

    public static float mScaleFactor = 1.0F;

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        mScaleFactor *= detector.getScaleFactor();
        mScaleFactor = Math.max(1.0F, Math.min(mScaleFactor, 2.5F));
        return false;
    }
}
