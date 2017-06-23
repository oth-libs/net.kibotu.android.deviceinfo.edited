package net.kibotu.android.deviceinfo.library;

import android.annotation.TargetApi;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.hardware.Sensor;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.SparseArray;
import android.view.Display;
import android.view.Surface;

import net.kibotu.android.deviceinfo.library.misc.ReflectionHelper;

import org.json.JSONArray;
import org.json.JSONException;

import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.hardware.Sensor.STRING_TYPE_ACCELEROMETER;
import static android.hardware.Sensor.STRING_TYPE_AMBIENT_TEMPERATURE;
import static android.hardware.Sensor.STRING_TYPE_GAME_ROTATION_VECTOR;
import static android.hardware.Sensor.STRING_TYPE_GEOMAGNETIC_ROTATION_VECTOR;
import static android.hardware.Sensor.STRING_TYPE_GRAVITY;
import static android.hardware.Sensor.STRING_TYPE_GYROSCOPE;
import static android.hardware.Sensor.STRING_TYPE_GYROSCOPE_UNCALIBRATED;
import static android.hardware.Sensor.STRING_TYPE_HEART_RATE;
import static android.hardware.Sensor.STRING_TYPE_LIGHT;
import static android.hardware.Sensor.STRING_TYPE_LINEAR_ACCELERATION;
import static android.hardware.Sensor.STRING_TYPE_MAGNETIC_FIELD;
import static android.hardware.Sensor.STRING_TYPE_MAGNETIC_FIELD_UNCALIBRATED;
import static android.hardware.Sensor.STRING_TYPE_ORIENTATION;
import static android.hardware.Sensor.STRING_TYPE_PRESSURE;
import static android.hardware.Sensor.STRING_TYPE_PROXIMITY;
import static android.hardware.Sensor.STRING_TYPE_RELATIVE_HUMIDITY;
import static android.hardware.Sensor.STRING_TYPE_ROTATION_VECTOR;
import static android.hardware.Sensor.STRING_TYPE_SIGNIFICANT_MOTION;
import static android.hardware.Sensor.STRING_TYPE_STEP_COUNTER;
import static android.hardware.Sensor.STRING_TYPE_STEP_DETECTOR;
import static android.hardware.Sensor.STRING_TYPE_TEMPERATURE;
import static android.hardware.Sensor.TYPE_ACCELEROMETER;
import static android.hardware.Sensor.TYPE_AMBIENT_TEMPERATURE;
import static android.hardware.Sensor.TYPE_GAME_ROTATION_VECTOR;
import static android.hardware.Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR;
import static android.hardware.Sensor.TYPE_GRAVITY;
import static android.hardware.Sensor.TYPE_GYROSCOPE;
import static android.hardware.Sensor.TYPE_GYROSCOPE_UNCALIBRATED;
import static android.hardware.Sensor.TYPE_HEART_RATE;
import static android.hardware.Sensor.TYPE_LIGHT;
import static android.hardware.Sensor.TYPE_LINEAR_ACCELERATION;
import static android.hardware.Sensor.TYPE_MAGNETIC_FIELD;
import static android.hardware.Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED;
import static android.hardware.Sensor.TYPE_ORIENTATION;
import static android.hardware.Sensor.TYPE_PRESSURE;
import static android.hardware.Sensor.TYPE_PROXIMITY;
import static android.hardware.Sensor.TYPE_RELATIVE_HUMIDITY;
import static android.hardware.Sensor.TYPE_ROTATION_VECTOR;
import static android.hardware.Sensor.TYPE_SIGNIFICANT_MOTION;
import static android.hardware.Sensor.TYPE_STEP_COUNTER;
import static android.hardware.Sensor.TYPE_STEP_DETECTOR;
import static android.hardware.Sensor.TYPE_TEMPERATURE;
import static android.hardware.SensorManager.SENSOR_DELAY_FASTEST;
import static android.hardware.SensorManager.SENSOR_DELAY_GAME;
import static android.hardware.SensorManager.SENSOR_DELAY_NORMAL;
import static android.hardware.SensorManager.SENSOR_DELAY_UI;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.M;
import static java.text.MessageFormat.format;
import static net.kibotu.android.deviceinfo.library.version.Version.isAtLeastVersion;

/**
 * Created by Nyaruhodo on 22.02.2016.
 */
final public class ViewHelper {

    public static final long BYTES_TO_KB = 1024;
    public static final long BYTES_TO_MB = BYTES_TO_KB * 1024;
    public static final long BYTES_TO_GB = BYTES_TO_MB * 1024;
    public static final long BYTES_TO_TB = BYTES_TO_GB * 1024;
    public static final String BR = "<br>\n";

    private ViewHelper() throws IllegalAccessException {
        throw new IllegalAccessException();
    }

    public static String jsonArrayToString(final JSONArray array) {
        final StringBuilder buffer = new StringBuilder();

        for (int i = 0; i < array.length(); ++i) {
            try {
                buffer.append(array.getString(i)).append("\n");
            } catch (final JSONException e) {
                e.printStackTrace();
            }
        }

        return buffer.toString();
    }

    /**
     * alternative to Formatter.formatFileSize which doesn't show bytes and rounds to int
     */
    public static String formatBytes(final long bytes) {
        if (bytes <= 0)
            return "0 bytes";

        return bytes / BYTES_TO_TB > 0 ? String.format("%.2f TB", bytes / (float) BYTES_TO_TB) :
                bytes / BYTES_TO_GB > 0 ? String.format("%.2f GB", bytes / (float) BYTES_TO_GB) :
                        bytes / BYTES_TO_MB > 0 ? String.format("%.2f MB", bytes / (float) BYTES_TO_MB) :
                                bytes / BYTES_TO_KB > 0 ? String.format("%.2f KB", bytes / (float) BYTES_TO_KB) : bytes + " bytes";
    }

    public static String formatFrequency(final int clockHz) {
        return clockHz == 0 ? "Not available" : clockHz < 1000 * 1000 ? (clockHz / 1000) + " MHz" : (clockHz / 1000 / 1000) + "." + (clockHz / 1000 / 100) % 10 + " GHz";
    }

    public static void killCpu() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                int x = 0;
                for (int i = 0; i < Integer.MAX_VALUE; ++i) {
                    ++x;
                }
            }
        }).start();
    }


    public static String getSensorName(final Sensor sensor) {

        if (isAtLeastVersion(Build.VERSION_CODES.KITKAT_WATCH))
            return sensor.getStringType();

        String name;

        switch (sensor.getType()) {
            case TYPE_ACCELEROMETER:
                name = STRING_TYPE_ACCELEROMETER;
            case TYPE_AMBIENT_TEMPERATURE:
                name = STRING_TYPE_AMBIENT_TEMPERATURE;
            case TYPE_GAME_ROTATION_VECTOR:
                name = STRING_TYPE_GAME_ROTATION_VECTOR;
            case TYPE_GEOMAGNETIC_ROTATION_VECTOR:
                name = STRING_TYPE_GEOMAGNETIC_ROTATION_VECTOR;
            case 24:
                name = "android.sensor.glance_gesture";
            case TYPE_GRAVITY:
                name = STRING_TYPE_GRAVITY;
            case TYPE_GYROSCOPE:
                name = STRING_TYPE_GYROSCOPE;
            case TYPE_GYROSCOPE_UNCALIBRATED:
                name = STRING_TYPE_GYROSCOPE_UNCALIBRATED;
            case TYPE_HEART_RATE:
                name = STRING_TYPE_HEART_RATE;
            case TYPE_LIGHT:
                name = STRING_TYPE_LIGHT;
            case TYPE_LINEAR_ACCELERATION:
                name = STRING_TYPE_LINEAR_ACCELERATION;
            case TYPE_MAGNETIC_FIELD:
                name = STRING_TYPE_MAGNETIC_FIELD;
            case TYPE_MAGNETIC_FIELD_UNCALIBRATED:
                name = STRING_TYPE_MAGNETIC_FIELD_UNCALIBRATED;
            case 25:
                name = "android.sensor.pick_up_gesture";
            case TYPE_PRESSURE:
                name = STRING_TYPE_PRESSURE;
            case TYPE_PROXIMITY:
                name = STRING_TYPE_PROXIMITY;
            case TYPE_RELATIVE_HUMIDITY:
                name = STRING_TYPE_RELATIVE_HUMIDITY;
            case TYPE_ROTATION_VECTOR:
                name = STRING_TYPE_ROTATION_VECTOR;
            case TYPE_SIGNIFICANT_MOTION:
                name = STRING_TYPE_SIGNIFICANT_MOTION;
            case TYPE_STEP_COUNTER:
                name = STRING_TYPE_STEP_COUNTER;
            case TYPE_STEP_DETECTOR:
                name = STRING_TYPE_STEP_DETECTOR;
            case 22:
                name = "android.sensor.tilt_detector";
            case 23:
                name = "android.sensor.wake_gesture";
            case TYPE_ORIENTATION:
                name = STRING_TYPE_ORIENTATION;
            case TYPE_TEMPERATURE:
                name = STRING_TYPE_TEMPERATURE;
            default:
                name = Build.UNKNOWN;
        }
        return name;
    }

    /**
     * Based PixelFormat and native http://stackoverflow.com/a/12068719
     */
    public synchronized static String nameForPixelFormat(final int pixelFormat) {
        String res;
        switch (pixelFormat) {
            case PixelFormat.TRANSLUCENT:
                res = "Translucent";
                break;
            case PixelFormat.TRANSPARENT:
                res = "Transparent";
                break;
            case PixelFormat.OPAQUE:
                res = "Opaque";
                break;
            case PixelFormat.RGBA_8888:
                res = "RGBA_8888";
                break;
            case PixelFormat.RGBX_8888:
                res = "RGBX_8888";
                break;
            case PixelFormat.RGB_888:
                res = "RGB_888";
                break;
            case PixelFormat.RGB_565:
                res = "RGB_565";
                break;
            case PixelFormat.RGBA_5551:
                res = "RGBA_5551";
                break;
            case PixelFormat.RGBA_4444:
                res = "RGBA_4444";
                break;
            case PixelFormat.A_8:
                res = "A_8";
                break;
            case PixelFormat.L_8:
                res = "L_8";
                break;
            case PixelFormat.LA_88:
                res = "LA_88";
                break;
            case PixelFormat.RGB_332:
                res = "RGB_332";
                break;
            case 0x13:
                res = "YCbCr_420_P";
                break;
            case 0x21:
                res = "YCbCr_420_SP";
                break;
            case 0x20:
            case 0x22:
                res = "YCrCb_420_SP_TILED";
                break;
            case ImageFormat.NV16:
                //case PixelFormat.YCbCr_422_SP: deprecated
            case 0x23:
            case 0x24:
            case 0x12:
                res = "YCbCr_422_P";
                break;
            case ImageFormat.NV21:
                //case PixelFormat.YCbCr_420_SP: deprecated
                res = "YCbCr_420_SP";
                break;
            case ImageFormat.YUY2:
                //case PixelFormat.YCbCr_422_I: deprecated
            case 0x16:
                res = "CbYCrY_422_I";
                break;
            case ImageFormat.JPEG:
                //case PixelFormat.JPEG: deprecated
                res = "Jpeg";
                break;
            case 0x15:
            case 0x17:
                res = "reserved";
                break;
            case 0x18:
                res = "range unavailable";
                break;
            case PixelFormat.UNKNOWN:
            default:
                res = "Unknown";
        }
        return res;
    }

    public synchronized static <T extends Comparable<? super T>> List<T> asSortedList(final Collection<T> c) {
        final List<T> list = new ArrayList<T>(c);
        Collections.sort(list);
        return list;
    }

    public static String t(final int amountTabs) {
        if (amountTabs < 1) return "";
        String ret = "\t";
        for (int i = 1; i < amountTabs; ++i)
            ret += "\t";
        return ret;
    }

    public synchronized static String appendGLInfoArray(final int[] a) {
        return a[0] + a[1] + a[2] == 0
                ? "Not Available."
                : "[-2^" + a[0] + ", 2^" + a[1] + "]" + (a[2] > 0 ? ", 2^" + a[2] : "");
    }

    public synchronized static String firstLetterToUpperCase(final String word) {
        return Character.toString(word.charAt(0)).toUpperCase() + word.substring(1);
    }

    public static String nameForRotation(final int rotation) {
        String ret;
        switch (rotation) {
            case Surface.ROTATION_0:
                ret = "0°";
                break;
            case Surface.ROTATION_90:
                ret = "90°";
                break;
            case Surface.ROTATION_180:
                ret = "180°";
                break;
            case Surface.ROTATION_270:
                ret = "270°";
                break;
            default:
                ret = "" + rotation;
        }
        return ret;
    }

    public static String formatBool(final boolean isTrue) {
        return isTrue ? "Yes" : "No";
    }

    public static String formatInches(final double screenInches) {
        return String.format("%.2f inches", screenInches);
    }

    public static String inchToCm(final double screenInches) {
        return String.format("%.2f cm", screenInches * 2.54f);
    }

    public static String formatPixel(final double screenDiagonalPixel) {
        return String.format("%.2f px", screenDiagonalPixel);
    }

    public static Map<String, String> parseRamSmall(final String procMem) {
        final LinkedHashMap<String, String> ramMap = new LinkedHashMap<>();

        final String lines[] = procMem.trim().split("\n");

        int i = 0;

        for (String line : lines) {
            if (i >= 2) break;
            final String[] token = line.split(" ");
            // proc mem output looks like this each line: "info byte kB" with tons of non-utf8-spaces in between,
            // so split(" ") doesn't work properly, but we can just take the first and length-2 token index to get what we want
            ramMap.put(token[0], formatBytes(Integer.valueOf(token[token.length - 2])));
            ++i;
        }

        return ramMap;
    }

    public static Map<String, String> parseRam(final String procMem) {
        final LinkedHashMap<String, String> ramMap = new LinkedHashMap<String, String>();

        final String lines[] = procMem.trim().split("\n");

        for (String line : lines) {
            final String[] token = line.split(" ");
            // proc mem output looks like this each line: "info byte kB" with tons of non-utf8-spaces in between,
            // so split(" ") doesn't work properly, but we can just take the first and length-2 token index to get what we want
            ramMap.put(token[0], formatBytes(Integer.valueOf(token[token.length - 2])));
        }

        return ramMap;
    }

    public static JSONArray sort(final JSONArray jsonArray) {
        final List<String> jsonValues = new ArrayList<String>();
        for (int i = 0; i < jsonArray.length(); i++)
            try {
                jsonValues.add(jsonArray.getString(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        Collections.sort(jsonValues);
        return new JSONArray(jsonValues);
    }

    public static String formatKeyBoardHidden(final int hardKeyboardHidden) {
        switch (hardKeyboardHidden) {
            case Configuration.HARDKEYBOARDHIDDEN_YES:
                return formatBool(true);
            case Configuration.HARDKEYBOARDHIDDEN_NO:
                return formatBool(false);
            default:
                return "Undefined";
        }
    }

    public static String formatKeyboard(final int keyboard) {
        switch (keyboard) {
            case Configuration.KEYBOARD_NOKEYS:
                return "NOKEYS";
            case Configuration.KEYBOARD_QWERTY:
                return "QWERTY";
            case Configuration.KEYBOARD_12KEY:
                return "12KEY";
            default:
                return "Undefined";
        }
    }

    public static String collectFlags(@NonNull Display display) {
        SparseArray<String> mFlagsNames = new SparseArray<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            int flags = display.getFlags();
            for (Field field : display.getClass().getFields()) {
                if (field.getName().startsWith("FLAG_")) {
                    try {
                        mFlagsNames.put(field.getInt(null), field.getName());
                    } catch (IllegalAccessException ignored) {
                    }
                }
            }
            return display.getDisplayId() + ".flags=" + activeFlags(mFlagsNames, flags) + '\n';
        }
        return "";
    }

    private static String activeFlags(@NonNull SparseArray<String> valueNames, int bitfield) {
        final StringBuilder result = new StringBuilder();

        // Look for masks, apply it an retrieve the masked value
        for (int i = 0; i < valueNames.size(); i++) {
            final int maskValue = valueNames.keyAt(i);
            final int value = bitfield & maskValue;
            if (value > 0) {
                if (result.length() > 0) {
                    result.append('+');
                }
                result.append(valueNames.get(value));
            }
        }
        return result.toString();
    }


    public static String formatScreenLayout(final int screenLayout) {

        String result;

        switch (screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) {
            case Configuration.SCREENLAYOUT_SIZE_SMALL:
                result = "SIZE_UNDEFINED";
                break;
            case Configuration.SCREENLAYOUT_SIZE_NORMAL:
                result = "SIZE_SMALL";
                break;
            case Configuration.SCREENLAYOUT_SIZE_LARGE:
                result = "SIZE_NORMAL";
                break;
            case Configuration.SCREENLAYOUT_SIZE_XLARGE:
                result = "SIZE_LARGE";
                break;
            case Configuration.SCREENLAYOUT_SIZE_UNDEFINED:
            default:
                result = "SIZE_UNDEFINED";
        }

        result += "\n";

        switch (screenLayout & Configuration.SCREENLAYOUT_LONG_MASK) {
            case Configuration.SCREENLAYOUT_LONG_MASK:
                result += "SIZE_XLARGE";
                break;
            case Configuration.SCREENLAYOUT_LONG_NO:
                result += "LONG_MASK";
                break;
            case Configuration.SCREENLAYOUT_LONG_YES:
                result += "LONG_NO";
                break;
            case Configuration.SCREENLAYOUT_LONG_UNDEFINED:
            default:
                result = "SIZE_UNDEFINED";
        }

        if (!isAtLeastVersion(JELLY_BEAN_MR1)) {
            result += "\n(LAYOUTDIR_MASK Added in Api 17)";
        } else {
            result += "\n";

            final int SCREENLAYOUT_LAYOUTDIR_MASK = ReflectionHelper.getPublicStaticField(Configuration.class, "SCREENLAYOUT_LAYOUTDIR_MASK");
            final int SCREENLAYOUT_LAYOUTDIR_LTR = ReflectionHelper.getPublicStaticField(Configuration.class, "SCREENLAYOUT_LAYOUTDIR_LTR");
            final int SCREENLAYOUT_LAYOUTDIR_RTL = ReflectionHelper.getPublicStaticField(Configuration.class, "SCREENLAYOUT_LAYOUTDIR_RTL");

            if ((screenLayout & SCREENLAYOUT_LAYOUTDIR_MASK) == SCREENLAYOUT_LAYOUTDIR_LTR)
                result += "LAYOUTDIR_LTR";

            else if ((screenLayout & SCREENLAYOUT_LAYOUTDIR_MASK) == SCREENLAYOUT_LAYOUTDIR_RTL)
                result += "LAYOUTDIR_RTL";
            else
                result += "LAYOUTDIR_UNDEFINED";
        }

        return result;
    }

    public static String formatNavigationHidden(final int navigation) {

        String result;

        switch (navigation) {
            case Configuration.NAVIGATIONHIDDEN_NO:
                result = formatBool(false);
                break;
            case Configuration.NAVIGATIONHIDDEN_YES:
                result = formatBool(true);
                break;
            case Configuration.NAVIGATIONHIDDEN_UNDEFINED:
            default:
                result = "Undefined";
        }

        return result;
    }

    public static String formatNavigation(final int navigation) {

        String result;

        switch (navigation) {
            case Configuration.NAVIGATION_TRACKBALL:
                result = "Trackball";
                break;
            case Configuration.NAVIGATION_WHEEL:
                result = "Wheel";
                break;
            case Configuration.NAVIGATION_DPAD:
                result = "DPad";
                break;
            case Configuration.NAVIGATION_NONAV:
                result = "NONAV";
                break;
            case Configuration.NAVIGATION_UNDEFINED:
            default:
                result = "Undefined";
        }

        return result;
    }

    public static String formatOrientation(Configuration cfg) {
        String result;

        switch (cfg.orientation) {
            case Configuration.ORIENTATION_PORTRAIT:
                result = "Portrait";
                break;
            case Configuration.ORIENTATION_LANDSCAPE:
                result = "Landscape";
                break;
            case Configuration.ORIENTATION_SQUARE:
                result = "Square";
                break;
            case Configuration.ORIENTATION_UNDEFINED:
            default:
                result = "Undefined";
        }

        return result;
    }


    public static String formatOrientation(Display cfg) {
        String result;

        switch (cfg.getRotation()) {
            case Surface.ROTATION_90:
                result = "90";
                break;
            case Surface.ROTATION_180:
                result = "180";
                break;
            case Surface.ROTATION_270:
                result = "270";
                break;
            case Surface.ROTATION_0:
            default:
                result = "0";
        }

        return result;
    }

    public static String formatTouchscreen(final int touchscreen) {

        String result;

        switch (touchscreen) {
            case Configuration.TOUCHSCREEN_NOTOUCH:
                result = "No touch";
                break;
            case Configuration.TOUCHSCREEN_STYLUS:
                result = "Stylus";
                break;
            case Configuration.TOUCHSCREEN_FINGER:
                result = "Finger";
                break;
            default:
                result = "Undefined";
        }

        return result;
    }

    public static String formatUIMode(final int uiMode) {

        String result;

        switch (uiMode & Configuration.UI_MODE_TYPE_MASK) {
            case Configuration.UI_MODE_TYPE_NORMAL:
                result = "NORMAL";
                break;
            case Configuration.UI_MODE_TYPE_DESK:
                result = "DESK";
                break;
            case Configuration.UI_MODE_TYPE_CAR:
                result = "CAR";
                break;
            case Configuration.UI_MODE_TYPE_UNDEFINED:
            default:
                result = "Type Undefined";
        }

        result += "\n";

        switch (uiMode & Configuration.UI_MODE_NIGHT_MASK) {
            case Configuration.UI_MODE_NIGHT_NO:
                result += "NIGHT_NO";
                break;
            case Configuration.UI_MODE_NIGHT_YES:
                result = "NIGHT_YES";
                break;
            case Configuration.UI_MODE_NIGHT_UNDEFINED:
            default:
                result = "NIGHT_UNDEFINED";
        }

        return result;
    }

    public static String formatKeyboardHidden(final int keyboardHidden) {

        String result;

        switch (keyboardHidden) {
            case Configuration.HARDKEYBOARDHIDDEN_NO:
                result = formatBool(false);
                break;
            case Configuration.HARDKEYBOARDHIDDEN_YES:
                result = formatBool(true);
                break;
            case Configuration.HARDKEYBOARDHIDDEN_UNDEFINED:
            default:
                result = "Undefined";
        }

        return result;
    }

    public static String formatLineSeparator(final String lineSeparator) {
        return lineSeparator.equals("\n") ? "\\n" : lineSeparator.equals("\n\r") ? "\\n\\r" : lineSeparator;

    }

    public static String formatPercent(final float usage) {
        final NumberFormat formatter = DecimalFormat.getPercentInstance();
        formatter.setMaximumFractionDigits(2);
        return formatter.format(usage);
    }

    public static Bitmap invert(final Bitmap src) {
        final Bitmap output = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());
        int A, R, G, B;
        int pixelColor;
        int height = src.getHeight();
        int width = src.getWidth();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pixelColor = src.getPixel(x, y);
                A = Color.alpha(pixelColor);

                R = 255 - Color.red(pixelColor);
                G = 255 - Color.green(pixelColor);
                B = 255 - Color.blue(pixelColor);

                output.setPixel(x, y, Color.argb(A, R, G, B));
            }
        }

        return output;
    }

    @Nullable
    public static String formatSdkString(String fieldName) {
        fieldName = fieldName.replaceAll("_", " ");
        String firstLetter = fieldName.substring(0, 1);
        fieldName = firstLetter.toUpperCase() + fieldName.substring(1).toLowerCase();

        Pattern p = Pattern.compile(" [a-z]");
        Matcher m = p.matcher(fieldName);
        while (m.find()) {
            int index = m.start();
            fieldName = fieldName.substring(0, index) + fieldName.substring(index, index + 2).toUpperCase() + fieldName.substring(index + 2);
        }

        Pattern mrPattern = Pattern.compile(" (Mr\\d)");
        Matcher mrMatcher = mrPattern.matcher(fieldName);

        String[] result;
        if (mrMatcher.find()) {
            fieldName = fieldName.replaceAll(" Mr\\d", "");
            result = new String[]{fieldName, mrMatcher.group(1).toUpperCase()};
        } else
            result = new String[]{fieldName, null};

        return TextUtils.join(" ", result);
    }

    public static CharSequence getFormattedTimeDifference(long createdAt) {
        final Calendar c = Calendar.getInstance();
        c.setTimeInMillis(createdAt);
        return DateUtils.getRelativeTimeSpanString(c.getTimeInMillis(), Calendar.getInstance().getTimeInMillis(), DateUtils.MINUTE_IN_MILLIS);
    }

    @TargetApi(M)
    public static String formatSupportedModes(@NonNull final Display.Mode[] supportedModes) {
        if (!isAtLeastVersion(M))
            return "";

        String refreshRates = "";
        for (final Display.Mode mode : supportedModes) {
            refreshRates += format("[{0}x{1}] {2}", mode.getPhysicalWidth(), mode.getPhysicalWidth(), mode.getRefreshRate());
        }
        return refreshRates;
    }

    public static String getAccuracyName(final int accuracy) {
        switch (accuracy) {
            case SENSOR_DELAY_FASTEST:
                return "SENSOR_DELAY_FASTEST";
            case SENSOR_DELAY_GAME:
                return "SENSOR_DELAY_GAME";
            case SENSOR_DELAY_UI:
                return "SENSOR_DELAY_UI";
            case SENSOR_DELAY_NORMAL:
            default:
                return "SENSOR_DELAY_NORMAL";
        }
    }
}
