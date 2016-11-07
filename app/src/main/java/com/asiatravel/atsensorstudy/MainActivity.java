package com.asiatravel.atsensorstudy;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    public static final String TAG = "TAG";
    private static final int SENSOR_SHAKE = 1;

    private SensorManager mSensorManager;
    private PowerManager.WakeLock mWakeLock;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int sensorValue = msg.what;
            if (sensorValue == SENSOR_SHAKE) {
                Toast.makeText(MainActivity.this, "摇动了" + index, Toast.LENGTH_SHORT).show();
                index = index < 2 ? ++index : 0;
                imageView.setImageResource(imageArray[index]);
            }
        }
    };

    private ImageView imageView;
    private int[] imageArray = new int[]{R.drawable.search, R.drawable.sort, R.drawable.dialog};
    private int index = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initSensor();
        imageView = (ImageView) findViewById(R.id.image);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void initSensor() {
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);

        Sensor distanceSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        mSensorManager.registerListener(this, distanceSensor, SensorManager.SENSOR_DELAY_NORMAL);

        PowerManager mPowerManager = (PowerManager) getSystemService(POWER_SERVICE);
        mWakeLock = mPowerManager.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, TAG);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int sensorType = event.sensor.getType();
        switch (sensorType) {
            case Sensor.TYPE_ACCELEROMETER:
                float[] values = event.values;
                float xValue = values[0];
                float yValue = values[1];
                float zValue = values[2];
                int compareValue = 20;
                if (Math.abs(xValue) > compareValue || Math.abs(yValue) > compareValue || Math.abs(zValue) > compareValue) {
                    Message message = mHandler.obtainMessage();
                    message.what = SENSOR_SHAKE;
                    mHandler.sendMessage(message);
                }
                break;
            case Sensor.TYPE_PROXIMITY:
                float[] proximityValues = event.values;
                if (proximityValues[0] == 0.0) {
                    Toast.makeText(this, "贴近手机", Toast.LENGTH_SHORT).show();
                    if (mWakeLock.isHeld()) {
                        return;
                    } else {
                        mWakeLock.acquire();
                    }
                } else {
                    if (mWakeLock.isHeld()) {
                        return;
                    } else {
                        mWakeLock.setReferenceCounted(false);
                        mWakeLock.release();
                    }
                    Toast.makeText(this, "远离手机", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

}
