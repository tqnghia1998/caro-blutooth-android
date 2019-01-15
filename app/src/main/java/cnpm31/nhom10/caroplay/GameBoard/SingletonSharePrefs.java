package cnpm31.nhom10.caroplay.GameBoard;

import android.content.Context;
import android.content.SharedPreferences;

import cnpm31.nhom10.caroplay.MainActivity;

public class SingletonSharePrefs {

    private static final String PREFS_NAME = "CaroPlay_ShareRefs";
    private static SingletonSharePrefs mInstance;
    private SharedPreferences mSharedPreferences;

    private SingletonSharePrefs() {
        mSharedPreferences = MainActivity.mainContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static SingletonSharePrefs getInstance() {
        if (mInstance == null) {
            mInstance = new SingletonSharePrefs();
        }
        return mInstance;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> anonymousClass) {
        if (anonymousClass == String.class) {
            return (T) mSharedPreferences.getString(key, "");
        } else if (anonymousClass == Boolean.class) {
            return (T) Boolean.valueOf(mSharedPreferences.getBoolean(key, false));
        } else if (anonymousClass == Float.class) {
            return (T) Float.valueOf(mSharedPreferences.getFloat(key, 0));
        } else if (anonymousClass == Integer.class) {
            return (T) Integer.valueOf(mSharedPreferences.getInt(key, 0));
        } else if (anonymousClass == Long.class) {
            return (T) Long.valueOf(mSharedPreferences.getLong(key, 0));
        }
        return null;
    }

    public <T> void put(String key, T data) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        if (data instanceof String) {
            editor.putString(key, (String) data);
        } else if (data instanceof Boolean) {
            editor.putBoolean(key, (Boolean) data);
        } else if (data instanceof Float) {
            editor.putFloat(key, (Float) data);
        } else if (data instanceof Integer) {
            editor.putInt(key, (Integer) data);
        } else if (data instanceof Long) {
            editor.putLong(key, (Long) data);
        }
        editor.apply();
    }

    public void clear() {

       SharedPreferences.Editor editor = mSharedPreferences.edit();
       editor.remove(GameBoard.MACUser2);
       editor.remove("isWaiting");
       editor.commit();
    }
}
