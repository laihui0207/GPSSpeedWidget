package com.huivip.gpsspeedwidget.lyrics.utils;


import com.huivip.gpsspeedwidget.lyrics.formats.LyricsFileReader;
import com.huivip.gpsspeedwidget.lyrics.formats.LyricsFileWriter;
import com.huivip.gpsspeedwidget.lyrics.formats.hrc.HrcLyricsFileReader;
import com.huivip.gpsspeedwidget.lyrics.formats.hrc.HrcLyricsFileWriter;
import com.huivip.gpsspeedwidget.lyrics.formats.krc.KrcLyricsFileReader;
import com.huivip.gpsspeedwidget.lyrics.formats.krc.KrcLyricsFileWriter;
import com.huivip.gpsspeedwidget.lyrics.formats.ksc.KscLyricsFileReader;
import com.huivip.gpsspeedwidget.lyrics.formats.ksc.KscLyricsFileWriter;
import com.huivip.gpsspeedwidget.lyrics.formats.lrc.LrcLyricsFileReader;
import com.huivip.gpsspeedwidget.lyrics.formats.lrc.LrcLyricsFileWriter;
import com.huivip.gpsspeedwidget.lyrics.formats.lrcwy.WYLyricsFileReader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.huivip.gpsspeedwidget.lyrics.utils.FileUtils.getFileExt;

/**
 * 歌词io操作
 * @author zhangliangming
 * 
 */
public class LyricsIOUtils {
	private static ArrayList<LyricsFileReader> readers;
	private static ArrayList<LyricsFileWriter> writers;

	static {
		readers = new ArrayList<LyricsFileReader>();
		readers.add(new HrcLyricsFileReader());
		readers.add(new KscLyricsFileReader());
		readers.add(new KrcLyricsFileReader());
		readers.add(new LrcLyricsFileReader());
		readers.add(new WYLyricsFileReader());
		//
		writers = new ArrayList<LyricsFileWriter>();
		writers.add(new HrcLyricsFileWriter());
		writers.add(new KscLyricsFileWriter());
		writers.add(new KrcLyricsFileWriter());
		writers.add(new LrcLyricsFileWriter());
		readers.add(new WYLyricsFileReader());
	}

	/**
	 * 获取支持的歌词文件格式
	 * 
	 * @return
	 */
	public static List<String> getSupportLyricsExts() {
		List<String> lrcExts = new ArrayList<String>();
		for (LyricsFileReader lyricsFileReader : readers) {
			lrcExts.add(lyricsFileReader.getSupportFileExt());
		}
		return lrcExts;
	}

	/**
	 * 获取歌词文件读取器
	 * 
	 * @param file
	 * @return
	 */
	public static LyricsFileReader getLyricsFileReader(File file) {
		return getLyricsFileReader(file.getName());
	}

	/**
	 * 获取歌词文件读取器
	 * 
	 * @param fileName
	 * @return
	 */
	public static LyricsFileReader getLyricsFileReader(String fileName) {
		String ext = getFileExt(fileName);
		for (LyricsFileReader lyricsFileReader : readers) {
			if (lyricsFileReader.isFileSupported(ext)) {
				return lyricsFileReader;
			}
		}
		return null;
	}

	/**
	 * 获取歌词保存器
	 *
	 * @param file
	 * @return
	 */
	public static LyricsFileWriter getLyricsFileWriter(File file) {
		return getLyricsFileWriter(file.getName());
	}

	/**
	 * 获取歌词保存器
	 *
	 * @param fileName
	 * @return
	 */
	public static LyricsFileWriter getLyricsFileWriter(String fileName) {
		String ext = getFileExt(fileName);
		for (LyricsFileWriter lyricsFileWriter : writers) {
			if (lyricsFileWriter.isFileSupported(ext)) {
				return lyricsFileWriter;
			}
		}
		return null;
	}
}
