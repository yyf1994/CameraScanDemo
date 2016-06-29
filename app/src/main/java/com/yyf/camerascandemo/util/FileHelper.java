/**
 * Copyright (C) 2016, all rights reserved.
 * Company	SHENZHEN YUNZHONGFEI TECHNOLOGY CORP., LTD. 
 * Author	dingji
 * Since	2016-3-9
 */
package com.yyf.camerascandemo.util;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;

/**
 * 
 * @Author dingji
 * @Since 2016-3-9
 */

public class FileHelper {
	private Context context;
	/** SD卡是否存在 **/
	private boolean hasSD = false;
	/** SD卡的路径 **/
	private String SDPATH;
	/** 当前程序包的路径 **/
	private String FILESPATH;

	public FileHelper(Context context) {
		this.context = context;
		hasSD = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
		SDPATH = Environment.getExternalStorageDirectory().getPath();
		FILESPATH = this.context.getFilesDir().getPath();
	}

	/** * 在SD卡上创建文件 * * @throws IOException */
	public File createSDFile(String parentPath, String fileName) throws IOException {
		File file = new File(parentPath, fileName);
		Log.e("lxl", file.getPath());
		if (!file.exists()) {
			file.createNewFile();
		}
		return file;
	}

	/** * 在SD卡上创建文件夹 * * @throws IOException */
	public File createSDFileDir(String dirName) throws IOException {
		File file = new File(SDPATH + "//" + dirName);
		if (!file.exists()) {
			file.mkdirs();
		}
		return file;
	}

	/** * 删除SD卡上的文件 * * @param fileName */
	public boolean deleteSDFile(String fileName) {
		File file = new File(SDPATH + "//" + fileName);
		if (file == null || !file.exists() || file.isDirectory())
			return false;
		return file.delete();
	}

	/** * 写入内容到SD卡中的txt文本中 * str为内容 */
	public void writeSDFile(String str, String fileName) {
		try {
			Log.e("lxl", fileName);
			FileWriter fw = new FileWriter(fileName, true);
			// File f = new File(fileName);
			// fw.write(str);
			// FileOutputStream os = new FileOutputStream(f);
			// DataOutputStream out = new DataOutputStream(os);
			// out.writeShort(2);
			// out.writeUTF("");
			// System.out.println(out);
			// fw.flush();
			// fw.close();
			// System.out.println(fw);
			PrintWriter pw = new PrintWriter(fw);
			pw.println(str + "\n");
			pw.flush();
			fw.flush();
			pw.close();
			fw.close();
		} catch (Exception e) {
		}
	}

	/** *删除文本中的一行内容 */
	public void delete(String str, String fileName) {
		try {
			FileWriter fw = new FileWriter(fileName, true);
			// File f = new File(fileName);
			// fw.write(str);
			// FileOutputStream os = new FileOutputStream(f);
			// DataOutputStream out = new DataOutputStream(os);
			// out.writeShort(2);
			// out.writeUTF("");
			// System.out.println(out);
			// fw.flush();
			// fw.close();
			// System.out.println(fw);
			PrintWriter pw = new PrintWriter(fw);
			pw.println(str + "\n");
			pw.flush();
			fw.flush();
			pw.close();
			fw.close();
		} catch (Exception e) {
		}
	}

	/** * 读取SD卡中文本文件 * * @param fileName * @return */
	public String readSDFile(String fileName) {
		StringBuffer sb = new StringBuffer();
		File file = new File(SDPATH + "//" + fileName);
		try {
			FileInputStream fis = new FileInputStream(file);
			int c;
			while ((c = fis.read()) != -1) {
				sb.append((char) c);
			}
			fis.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}

	/** * 读取SD卡中某目录下的文本文件 * * @param fileName * @return */
	public String readSDDirFile(String dirName, String fileName) {
		StringBuffer sb = new StringBuffer();
		File file = new File(SDPATH + "/" + dirName + "//" + fileName);
		try {
			FileInputStream fis = new FileInputStream(file);
			int c;
			while ((c = fis.read()) != -1) {
				sb.append((char) c);
			}
			fis.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}

	public String getFILESPATH() {
		return FILESPATH;
	}

	public String getSDPATH() {
		return SDPATH;
	}

	public boolean hasSD() {
		return hasSD;
	}

	/**
	 * 得到文件夹的路径
	 * */
	public String getDir(String dirName, String fileName) {
		return SDPATH + "/" + dirName + "//" + fileName;
	}

	public void fileAppender(String fileName, String content,String content1) throws IOException {
		FileReader fileReader = new FileReader(fileName);
		BufferedReader reader = new BufferedReader(fileReader);
		String temp = null;
		// 一行一行的读
		StringBuilder sb = new StringBuilder();
		sb.append(content).append("\r\n");
		while ((temp = reader.readLine()) != null) {
			if(temp.equals(content1)){
				sb.append("").append("\r\n");
			} else{
				sb.append(temp).append("\r\n");
			}
			
		}
		reader.close();

		// 写回去
		RandomAccessFile mm = new RandomAccessFile(fileName, "rw");
		mm.writeBytes(sb.toString());
		mm.close();	
		
	}
	
/*	public int getlinenum(String fileName) throws IOException {
		FileReader fileReader = new FileReader(fileName);
		LineNumberReader linenum = new LineNumberReader(fileReader);
		int lineDel=linenum.getLineNumber();
		linenum.close();
		fileReader.close();
		return lineDel;
	}

	public void zhuijia(String fileName, String content) throws IOException {
		FileWriter fw = new FileWriter(fileName, true);
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(content+"\r\n");
		bw.newLine();
		bw.flush();
		bw.close();
		fw.close();
	}*/

}
