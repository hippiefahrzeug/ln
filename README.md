ln
==

A very simple but powerful logging class for Android projects.

I pass this class around my own projects and keep refining it. It's copy-paste and adjust and merge back. The worst kind of reuse. But the only kind that works. :)

Example:

    Ln.d("Hello world");

... will create the following output:

    D/com.sb.ln(32648): com.sb.SampleActivity:30 Hello world

You can edit the Ln class to change its behavior to your liking. Change the following constants:

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

