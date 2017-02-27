package com.guo.android_extend.tools;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * Created by gqj3375 on 2017/2/23.
 */

public class AssetsHelper {

	public static boolean copyToDisk(Context context, String fileName, String path_in_sdcard) {
		try {
			String PATH_LOGCAT;
			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				PATH_LOGCAT = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + path_in_sdcard;
			} else {
				PATH_LOGCAT = context.getFilesDir().getAbsolutePath() + File.separator + path_in_sdcard;
			}
			File file = new File(PATH_LOGCAT);
			if (!file.exists()) {
				file.mkdirs();
			}

			file = new File(PATH_LOGCAT + File.separator + fileName);
			if (!file.exists()) {
				if (!file.createNewFile()) {
					Log.e("AssetsHelper", "createNewFile FAIL!");
				}
			}
			FileOutputStream fout = new FileOutputStream(file);
			InputStream fin = context.getResources().getAssets().open(fileName);
			int length = fin.available();
			byte[] buffer = new byte[length];
			fin.read(buffer);
			fout.write(buffer);
			fin.close();
			fout.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}
