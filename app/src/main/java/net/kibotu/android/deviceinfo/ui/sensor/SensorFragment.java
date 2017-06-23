package net.kibotu.android.deviceinfo.ui.sensor;

import android.hardware.Sensor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import net.kibotu.android.deviceinfo.R;
import net.kibotu.android.deviceinfo.library.Device;
import net.kibotu.android.deviceinfo.model.ListItem;
import net.kibotu.android.deviceinfo.ui.list.ListFragment;

import java.util.List;

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
import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static com.common.android.utils.extensions.FragmentExtensions.replaceToBackStackBySlidingHorizontally;
import static net.kibotu.android.deviceinfo.library.ViewHelper.getSensorName;
import static net.kibotu.android.deviceinfo.library.version.Version.isAtLeastVersion;

/**
 * Created by Nyaruhodo on 21.02.2016.
 */
public class SensorFragment extends ListFragment {

    private List<Sensor> sensorList;

    @Override
    public String getTitle() {
        return getString(R.string.menu_item_sensor);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sensorList = Device.getSensorList();
        for (final Sensor s : sensorList) {
            final ListItem listItem = new ListItem().setLabel(s.getName())
                    .addChild(new ListItem().setLabel("Type").setValue(getSensorName(s)))
                    .addChild(new ListItem().setLabel("Vendor").setValue(s.getVendor()))
                    .addChild(new ListItem().setLabel("Version").setValue(s.getVersion()))
                    .addChild(new ListItem().setLabel("Resolution").setValue(s.getResolution()))
                    .addChild(new ListItem().setLabel("Max Range").setValue(s.getMaximumRange()))
                    .addChild(new ListItem().setLabel("Min Delay").setValue(s.getMinDelay()));

            if (isAtLeastVersion(LOLLIPOP)) {
                listItem.addChild(new ListItem().setLabel("Max Delay").setValue(s.getMaxDelay()));
            }

            if (isAtLeastVersion(LOLLIPOP)) {
                listItem.addChild(new ListItem().setLabel("Wake Up Sensor").setValue(s.isWakeUpSensor()));
            }

            if (isAtLeastVersion(LOLLIPOP)) {
                listItem.addChild(new ListItem().setLabel("Reporting Mode").setValue(s.getReportingMode()));
            }

            if (isAtLeastVersion(KITKAT)) {
                listItem.addChild(new ListItem().setLabel("Fifo Reserved Event Count").setValue(s.getFifoReservedEventCount()));
            }

            if (isAtLeastVersion(KITKAT)) {
                listItem.addChild(new ListItem().setLabel("Fifo Max Event Count").setValue(s.getFifoMaxEventCount()));
            }

            addSubListItem(listItem);
        }

        adapter.setOnItemClickListener((listItem, itemView, position) -> showSensorData(sensorList.get(position)));
    }

    private static void showSensorData(final Sensor sensor) {

        switch (sensor.getType()) {
            case TYPE_ACCELEROMETER:
                replaceToBackStackBySlidingHorizontally(new AccelerationSensorFragment());
                break;
            case TYPE_AMBIENT_TEMPERATURE:
            case TYPE_GAME_ROTATION_VECTOR:
            case TYPE_GEOMAGNETIC_ROTATION_VECTOR:
            case 24:
                String name = "android.sensor.glance_gesture";
            case TYPE_GRAVITY:
            case TYPE_GYROSCOPE:
            case TYPE_GYROSCOPE_UNCALIBRATED:
            case TYPE_HEART_RATE:
            case TYPE_LIGHT:
            case TYPE_LINEAR_ACCELERATION:
            case TYPE_MAGNETIC_FIELD:
            case TYPE_MAGNETIC_FIELD_UNCALIBRATED:
            case 25:
                name = "android.sensor.pick_up_gesture";
            case TYPE_PRESSURE:
            case TYPE_PROXIMITY:
                replaceToBackStackBySlidingHorizontally(new ProximitySensorFragment());
                break;
            case TYPE_RELATIVE_HUMIDITY:
            case TYPE_ROTATION_VECTOR:
            case TYPE_SIGNIFICANT_MOTION:
            case TYPE_STEP_COUNTER:
            case TYPE_STEP_DETECTOR:
            case 22:
                name = "android.sensor.tilt_detector";
            case 23:
                name = "android.sensor.wake_gesture";
            case TYPE_ORIENTATION:
            case TYPE_TEMPERATURE:
            default:
                name = android.os.Build.UNKNOWN;
        }
    }

    @Override
    protected int getHomeIcon() {
        return R.drawable.sensors;
    }
}
