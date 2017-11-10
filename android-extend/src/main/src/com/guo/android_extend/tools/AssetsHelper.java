package com.guo.android_extend.tools;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * Created by gqj3375 on 2017/2/23.
 */

public class AssetsHelper {

	public static boolean zipDecodeToDisk(Context context, String assetsFile, String path_in_sdcard) {
		try {
			if (copyToDisk(context, assetsFile, path_in_sdcard)) {
				String PATH_LOGCAT;
				if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
					PATH_LOGCAT = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + path_in_sdcard;
				} else {
					PATH_LOGCAT = context.getFilesDir().getAbsolutePath() + File.separator + path_in_sdcard;
				}
				File file = new File(PATH_LOGCAT + File.separator + assetsFile);
				ZipFile zipFile = new ZipFile(file);
				ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(file));
				ZipEntry zipEntry = null;
				while ((zipEntry = zipInputStream.getNextEntry()) != null) {
					String fileName = zipEntry.getName();
					File temp = new File(PATH_LOGCAT  + File.separator + fileName);
					if (! temp.getParentFile().exists()) {
						temp.getParentFile().mkdirs();
					}
					OutputStream os = new FileOutputStream(temp);
					BufferedOutputStream Bout = new BufferedOutputStream(os);
					InputStream is = zipFile.getInputStream(zipEntry);
					BufferedInputStream Bin = new BufferedInputStream(is);
					int len = 0;
					while ((len = Bin.read()) != -1) {
						Bout.write(len);
					}
					Bout.close();
					os.close();
					Bin.close();
					is.close();
				}
				zipInputStream.close();
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

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
			fout.flush();
			fout.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}
