package com.guo.android_extend.tools;

import android.annotation.TargetApi;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.os.Build;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gqj3375 on 2017/1/10.
 */

public class CameraHelper {

	public static int clamp(int x, int min, int max) {
		if (x > max) {
			return max;
		}
		if (x < min) {
			return min;
		}
		return x;
	}

	public static Rect cameraTapArea(int centerX, int centerY, float coefficient) {
		float focusAreaSize = 200;
		int halfAreaSize = Float.valueOf(focusAreaSize * coefficient).intValue() / 2;
		int left = clamp(centerX - halfAreaSize, -1000, 1000);
		int top = clamp(centerY - halfAreaSize, -1000, 1000);
		int right = clamp(centerX + halfAreaSize, -1000, 1000);
		int bottom = clamp(centerY + halfAreaSize, -1000, 1000);
		RectF rectF = new RectF(left, top, right, bottom);
		return new Rect(Math.round(rectF.left), Math.round(rectF.top), Math.round(rectF.right), Math.round(rectF.bottom));
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public static void touchFocus(Camera camera1, Camera camera2, MotionEvent event, View view, Camera.AutoFocusCallback callback) {
		Camera.Parameters parameters1 = camera1.getParameters();
		Camera.Parameters parameters2 = camera2.getParameters();
		int centerX = (int)(event.getX() / (float)view.getWidth() * 2000.0) - 1000;
		int centerY = (int)(event.getY() / (float)view.getHeight() * 2000.0) - 1000;
		Rect focusRect = cameraTapArea(centerX, centerY, 1f);
		Rect meteringRect = cameraTapArea(centerX, centerY, 1.5f);
		if (parameters1.getMaxNumFocusAreas() > 0) {
			List<Camera.Area> focusAreas = new ArrayList<Camera.Area>();
			focusAreas.add(new Camera.Area(focusRect, 600));
			parameters1.setFocusAreas(focusAreas);
		}
		if (parameters1.getMaxNumMeteringAreas() > 0) {
			List<Camera.Area> meteringAreas = new ArrayList<Camera.Area>();
			meteringAreas.add(new Camera.Area(meteringRect, 600));
			parameters1.setMeteringAreas(meteringAreas);
		}
		if (parameters2.getMaxNumFocusAreas() > 0) {
			List<Camera.Area> focusAreas = new ArrayList<Camera.Area>();
			focusAreas.add(new Camera.Area(focusRect, 600));
			parameters2.setFocusAreas(focusAreas);
		}
		if (parameters2.getMaxNumMeteringAreas() > 0) {
			List<Camera.Area> meteringAreas = new ArrayList<Camera.Area>();
			meteringAreas.add(new Camera.Area(meteringRect, 600));
			parameters2.setMeteringAreas(meteringAreas);
		}
		parameters1.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
		camera1.cancelAutoFocus();
		parameters2.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
		camera2.cancelAutoFocus();
		try {
			camera1.setParameters(parameters1);
			camera2.setParameters(parameters2);
		} catch (Exception e) {
			e.printStackTrace();
		}
		camera1.autoFocus(callback);
		camera2.autoFocus(callback);
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public static void touchFocus(Camera camera, MotionEvent event, View view, Camera.AutoFocusCallback callback) {
		Camera.Parameters parameters = camera.getParameters();
		Camera.Size size = parameters.getPreviewSize();
		int centerX = (int)(event.getX() / (float)view.getWidth() * 2000.0) - 1000;
		int centerY = (int)(event.getY() / (float)view.getHeight() * 2000.0) - 1000;
		Rect focusRect = cameraTapArea(centerX, centerY, 1f);
		Rect meteringRect = cameraTapArea(centerX, centerY, 1.5f);
		if (parameters.getMaxNumFocusAreas() > 0) {
			List<Camera.Area> focusAreas = new ArrayList<Camera.Area>();
			focusAreas.add(new Camera.Area(focusRect, 600));
			parameters.setFocusAreas(focusAreas);
		}
		if (parameters.getMaxNumMeteringAreas() > 0) {
			List<Camera.Area> meteringAreas = new ArrayList<Camera.Area>();
			meteringAreas.add(new Camera.Area(meteringRect, 600));
			parameters.setMeteringAreas(meteringAreas);
		}
		parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
		camera.cancelAutoFocus();
		try {
			camera.setParameters(parameters);
		} catch (Exception e) {
			e.printStackTrace();
		}
		camera.autoFocus(callback);
	}

	public static void lockfocus(Camera camera) {
		Camera.Parameters parameters = camera.getParameters();
		parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_FIXED);
		try {
			camera.setParameters(parameters);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void unlockfocus(Camera camera) {
		Camera.Parameters parameters = camera.getParameters();
		parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
		try {
			camera.setParameters(parameters);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public static void lockAeAwb(Camera camera) {
		Camera.Parameters parameters = camera.getParameters();
		if (parameters.isAutoExposureLockSupported()) {
			parameters.setAutoExposureLock(true);
		}
		if (parameters.isAutoWhiteBalanceLockSupported()) {
			parameters.setAutoWhiteBalanceLock(true);
		}
		try {
			camera.setParameters(parameters);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public static void unlockAeAwb(Camera camera) {
		Camera.Parameters parameters = camera.getParameters();
		if (parameters.isAutoExposureLockSupported()) {
			parameters.setAutoExposureLock(false);
		}
		if (parameters.isAutoWhiteBalanceLockSupported()) {
			parameters.setAutoWhiteBalanceLock(false);
		}
		try {
			camera.setParameters(parameters);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
