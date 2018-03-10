package common.log;

import java.io.*;
import java.util.Calendar;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.helpers.CountingQuietWriter;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.helpers.OptionConverter;
import org.apache.log4j.spi.LoggingEvent;

/**
 * 特色平台日志处理.
 * 
 * 日志路径: /home/ap/ngplog/log/YYYYMMDD/XXXX/XXXX.log
 * 
 * YYYYMMDD - 机器时间.
 * 
 * XXXX - 业务模块.
 * 
 * @author Z.Q.S
 * @create 2008.03.17
 *
 */
public class NGPFileAppender extends FileAppender {

	// 日志创建日期.
	private String logCreateDate = null;
	
	public NGPFileAppender() {
		maxFileSize = 10485760L;
		maxBackupIndex = 1;
	}

	public NGPFileAppender(Layout layout, String filename, boolean append)
			throws IOException {
		super(layout, filename, append);
		maxFileSize = 10485760L;
		maxBackupIndex = 1;
	}

	public NGPFileAppender(Layout layout, String filename) throws IOException {
		super(layout, filename);
		maxFileSize = 10485760L;
		maxBackupIndex = 1;
	}

	public int getMaxBackupIndex() {
		return maxBackupIndex;
	}

	public long getMaximumFileSize() {
		return maxFileSize;
	}

	public void rollOver() {
		LogLog.debug("rolling over count="
				+ ((CountingQuietWriter) super.qw).getCount());
		LogLog.debug("maxBackupIndex=" + maxBackupIndex);
		if (maxBackupIndex > 0) {
			File file = new File(super.fileName+ '.' + logCreateDate + '.' + maxBackupIndex);
			if (file.exists())
				file.delete();
			File target;
			for (int i = maxBackupIndex - 1; i >= 1; i--) {
				file = new File(super.fileName+ '.' + logCreateDate + "." + i);
				if (file.exists()) {
					target = new File(super.fileName+ '.' + logCreateDate + '.' + (i + 1));
					LogLog.debug("Renaming file " + file + " to " + target);
					file.renameTo(target);
				}
			}

			target = new File(super.fileName+ '.' + logCreateDate + "." + 1);
			closeFile();
			file = new File(super.fileName);
			LogLog.debug("Renaming file " + file + " to " + target);
			file.renameTo(target);
		}
		try {
			setFile(super.fileName, false, super.bufferedIO, super.bufferSize);
		} catch (IOException e) {
			LogLog.error("setFile(" + super.fileName + ", false) call failed.",
					e);
		}
	}
	
	/**
	 * 自定义日志文件名称.
	 */
    public synchronized void setFile(String file) {
    	logCreateDate = nowDate();
		String logFile = file.replaceAll("YYYYMMDD", logCreateDate);
		int idx = logFile.lastIndexOf("/");
		if (-1 != idx) {
			File filePath = new File(logFile.substring(0, idx));
			if (!filePath.exists()) filePath.mkdirs();
		}
		super.setFile(logFile);
    }

	public synchronized void setFile(String fileName, boolean append,
			boolean bufferedIO, int bufferSize) throws IOException {
		super.setFile(fileName, append, super.bufferedIO, super.bufferSize);
		if (append) {
			File f = new File(fileName);
			((CountingQuietWriter) super.qw).setCount(f.length());
		}
	}

	public void setMaxBackupIndex(int maxBackups) {
		maxBackupIndex = maxBackups;
	}

	public void setMaximumFileSize(long maxFileSize) {
		this.maxFileSize = maxFileSize;
	}

	public void setMaxFileSize(String value) {
		maxFileSize = OptionConverter.toFileSize(value, maxFileSize + 1L);
	}

	protected void setQWForFiles(Writer writer) {
		super.qw = new CountingQuietWriter(writer, super.errorHandler);
	}

	protected void subAppend(LoggingEvent event) {
		
		// 判断日期是否一致
		// 如果不一致, 则创建新日志文件.
		String date = nowDate();
		if (logCreateDate.equals(date)) {
			// 不做处理.
		} else {
			// 切换文件.
			switchFileName(date);
		}
		
		super.subAppend(event);
		if (super.fileName != null
				&& ((CountingQuietWriter) super.qw).getCount() >= maxFileSize)
			rollOver();
	}
	
	/**
	 * 切换日志文件.
	 * 
	 * @param date
	 */
	private synchronized void switchFileName(String date) {
		closeFile();
		String logFile = super.fileName.replaceAll(logCreateDate, date);
		int idx = logFile.lastIndexOf("/");
		if (-1 != idx) {
			File filePath = new File(logFile.substring(0, idx));
			if (!filePath.exists()) filePath.mkdirs();
		}
		logCreateDate = date;

		try {
			setFile(logFile, false, super.bufferedIO, super.bufferSize);
		} catch (IOException e) {
			LogLog.error("setFile(" + logFile + ", false) call failed.",
					e);
		}
	}
	
	private String nowDate() {
		Calendar now = Calendar.getInstance();

		String year, month, day;
		year = String.valueOf(now.get(Calendar.YEAR));
		if (now.get(Calendar.MONTH) < 9)
			month = "0" + String.valueOf(now.get(Calendar.MONTH) + 1);
		else
			month = String.valueOf(now.get(Calendar.MONTH) + 1);
		if (now.get(Calendar.DAY_OF_MONTH) < 10)
			day = "0" + String.valueOf(now.get(Calendar.DAY_OF_MONTH));
		else
			day = String.valueOf(now.get(Calendar.DAY_OF_MONTH));

		return (year + month + day);
	}


	protected long maxFileSize;

	protected int maxBackupIndex;
}
