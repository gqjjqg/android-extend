package com.guo.android_extend.tools;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import static android.content.Context.SENSOR_SERVICE;

/**
 * Created by gqj3375 on 2017/1/10.
 */

public class SensorHelper {
	private SensorManager sensorManager;

	public SensorHelper(SensorManager sensorManager) {
		this.sensorManager = sensorManager;
	}

	public SensorHelper(Context context) {
		sensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
	}

	public void register(int type, SensorEventListener listener) {
		Sensor sensor = sensorManager.getDefaultSensor(type);
		sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_GAME);
	}

	public void unregister(SensorEventListener listener) {
		sensorManager.unregisterListener(listener);
	}

}
