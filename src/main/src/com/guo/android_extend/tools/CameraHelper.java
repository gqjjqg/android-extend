package com.guo.android_extend.tools;

import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.util.Log;
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
		camera.setParameters(parameters);
		camera.autoFocus(callback);
	}

	public static void lockfocus(Camera camera) {
		Camera.Parameters parameters = camera.getParameters();
		parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_FIXED);
		camera.setParameters(parameters);
	}

	public static void unlockfocus(Camera camera) {
		Camera.Parameters parameters = camera.getParameters();
		parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
		camera.setParameters(parameters);
	}
	public static void lockAeAwb(Camera camera) {

		Camera.Parameters parameters = camera.getParameters();
		if (parameters.isAutoExposureLockSupported()) {
			parameters.setAutoExposureLock(true);
		}
		if (parameters.isAutoWhiteBalanceLockSupported()) {
			parameters.setAutoWhiteBalanceLock(true);
		}
		camera.setParameters(parameters);
	}

	public static void unlockAeAwb(Camera camera) {
		Camera.Parameters parameters = camera.getParameters();
		if (parameters.isAutoExposureLockSupported()) {
			parameters.setAutoExposureLock(false);
		}
		if (parameters.isAutoWhiteBalanceLockSupported()) {
			parameters.setAutoWhiteBalanceLock(false);
		}
		camera.setParameters(parameters);
	}
}
