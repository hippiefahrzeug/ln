package com.sb.ln;

import android.os.Environment;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.util.Locale;
import java.util.logging.*;

public class Ln {
    private static final String TAG = Ln.class.getName();

    // write to android's logging facility
    private static final boolean ANDROID_LOG = true;
    // write into a logfile, requires 'android.permission.WRITE_EXTERNAL_STORAGE' permission
    private static final boolean FILE_LOG = false;
    // log level. change this for production to Level.SEVERE.
    private static final Level LOG_LEVEL = Level.FINE;
    // align the log messages so they appear left justified
    private static final boolean LEFT_JUSTIFIED = true;
    // max size of the log files
    private static final int MAX_LOG_SIZE = 1000000; // 1 Mb
    // number of log files kept (max amount of space occupied is NUM_LOG_FILES * MAX_LOG_SIZE)
    private static final int NUM_LOG_FILES = 3;
    // the package name of the log
    public static final String LOG_PKG = "com.sb.ln";

    private static final String LOG_PATH = Environment.getExternalStorageDirectory().getPath() + "/ln.%g.log";

    private static Logger logger;
    private static int maxTagLength = 0; // set to max known length for left justified log messages

    static {
        logger = Logger.getLogger("");
        // need to remove default handler which is buggy (i.e. debug
        // logs are thrown away)
        for (Handler handler : logger.getHandlers()) {
            logger.removeHandler(handler);
        }

        try {
            if (FILE_LOG) {
                FileHandler fileHandler = new FileHandler(LOG_PATH, MAX_LOG_SIZE, NUM_LOG_FILES);
                fileHandler.setFormatter(new LnFileLogFormatter());
                logger.addHandler(fileHandler);
            }
        }
        catch (IOException e) {
            Log.e(TAG, "IOException caught: ", e);
        }

        if (ANDROID_LOG) {
            Handler androidLogHandler = new FixedAndroidHandler();
            androidLogHandler.setFormatter(new LnAndroidLogFormatter());
            logger.addHandler(androidLogHandler);
        }

        logger.setLevel(LOG_LEVEL);
    }

    /**
     * prints out something like this:
     *
     *  8/21/12 2:16 PM (  3ms) com.sb.activity.SampleActivity e log message
     *                      ^- time elapsed since last log
     *
     * this is meant to be used for the filelogger only.
     */
    private static class LnFileLogFormatter extends Formatter {
        private static long previousTimestamp = System.currentTimeMillis();
        @Override
        public String format(LogRecord logRecord) {
            String elapsed = ms2duration(logRecord.getMillis() - previousTimestamp);
            String t = time2DateTime(logRecord.getMillis());
            previousTimestamp = logRecord.getMillis();
            if (logRecord.getSourceClassName().equals(TAG)) {
                String res = String.format(t + " %s %6s %s",
                        elapsed, logRecord.getLevel(), logRecord.getMessage() + "\n");
                return res;
            }
            // in case java logging is used directly...
            String src = createTagString(logRecord.getSourceClassName() + "    ");
            String res = String.format(t + " %s %6s %s %s",
                    elapsed, logRecord.getLevel(), src, logRecord.getMessage() + "\n");
            return res;
        }
    }

    private static class LnAndroidLogFormatter extends Formatter {
        @Override
        public String format(LogRecord logRecord) {
            if (logRecord.getSourceClassName().equals(TAG)) {
                return logRecord.getMessage();
            }
            // in case java logging is used directly...
            String src = createTagString(logRecord.getSourceClassName() + "    ");
            String res = String.format("%s %s", src, logRecord.getMessage() + "\n");
            return res;
        }
    }

    public static void d(String msg) {
        if (ANDROID_LOG || FILE_LOG) { // check for performance reasons only
            String tag = createTagString(getTag());
            log(tag, msg, Log.DEBUG);
        }
    }

    public static void i(String msg) {
        if (ANDROID_LOG || FILE_LOG) { // check for performance reasons only
            String tag = createTagString(getTag());
            log(tag, msg, Log.INFO);
        }
    }

    public static void w(String msg) {
        String tag = createTagString(getTag());
        log(tag, msg, Log.WARN);
    }

    public static void e(String msg) {
        String tag = createTagString(getTag());
        log(tag, msg, Log.ERROR);
    }

    public static void e(Throwable e) {
        String tag = createTagString(getTag());
        log(tag, getStackTrace(e), Log.ERROR);
    }

    /**
     * returns String of caller's class name
     *
     * example stack trace:
     *
     * 0. dalvik.system.VMStack.getThreadStackTrace(Native Method)
     * 1. java.lang.Thread.getStackTrace(Thread.java:737)
     * 2. com.sb.Ln.getTag(Ln.java:68)
     * 3. com.sb.Ln.d(Ln.java:17)
     * 4. com.sb.activity.SampleActivity.onCreate(SampleActivity.java:33)
     *
     * => therefore 4. stack trace element is used
     */
    private static String getTag() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        StackTraceElement element = stackTrace[4];
        return String.format("%s:%-3s", element.getClassName(), element.getLineNumber());
    }

    private static void log(String tag, String msg, int level) {
        switch (level) {
            case Log.DEBUG:
                logger.fine(tag + " " + msg);
                return;
            case Log.INFO:
                logger.info(tag + " " + msg);
                return;
            case Log.WARN:
                logger.warning(tag + " " + msg);
                return;
            case Log.ERROR:
            default:
                logger.severe(tag + " " + msg);
        }
    }

    private static String createTagString(String tag) {
        if (LEFT_JUSTIFIED) {
            int length = tag.length();
            if (maxTagLength < length) {
                maxTagLength = length;
            }
            String fmt = "%" + maxTagLength + "s";
            return String.format(fmt, tag);
        }
        return tag;
    }

    public static String getStackTrace(Throwable ex) {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        ex.printStackTrace(new PrintStream(buf));
        return buf.toString();
    }

    private static String time2DateTime(long d) {
        DateFormat df = DateFormat.getDateTimeInstance(
                DateFormat.MEDIUM,
                DateFormat.MEDIUM,
                Locale.US
        );
        return df.format(d);
    }

    public static String ms2duration(long d) {
        long s = d/1000;
        long ms = d - s * 1000;
        long m = s/60;
        s = s - m * 60;
        long h = m / 60;
        m = m - h * 60;
        long da = h / 24;
        h = h - da * 24;

        if (da > 0) {
            return String.format("( %3dd)", da);
        }
        if (h > 0) {
            return String.format("(%2dhrs)", h);
        }
        else if (m > 0) {
            return String.format("(%2dmin)", m);
        }
        else if (s > 0) {
            return String.format("(%2dsec)", s);
        }
        return String.format("(%3dms)", ms);
    }

    /**
     * The default implementation of Android's log-handler
     * throws away debug messages.
     */
    private static class FixedAndroidHandler extends Handler {
        @Override
        public void close() {
        }

        @Override
        public void flush() {
        }

        @Override
        public void publish(LogRecord record) {
            int level = getAndroidLevel(record.getLevel());
            try {
                String message = getFormatter().format(record);
                Log.println(level, LOG_PKG, message);
            }
            catch (RuntimeException e) {
                Log.e("AndroidHandler", "ServiceError logging message.", e);
            }
        }

        /**
         * Converts a {@link java.util.logging.Logger} logging level into an Android one.
         */
        int getAndroidLevel(Level level) {
            int value = level.intValue();
            if (value >= 1000) { // SEVERE
                return Log.ERROR;
            }
            else if (value >= 900) { // WARNING
                return Log.WARN;
            }
            else if (value >= 800) { // INFO
                return Log.INFO;
            }
            else {
                return Log.DEBUG;
            }
        }
    }
}
