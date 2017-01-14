package com.sean.android.pedometer.base;

import android.database.Cursor;
import android.os.Environment;
import android.util.Log;

import com.sean.android.pedometer.BuildConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by Seonil on 2016-12-09.
 */

public class Logger {

    private static final String TAG = Logger.class.getSimpleName();

    public static void d(String logData) {
        d(TAG, logData);
    }

    public static void d(String tag, String logData) {
        print(tag, LOG_LEVEL_DEBUG, new Object[]{logData});
    }

    public static void e(String logData) {
        e(TAG, logData);
    }

    public static void e(String tag, String logData) {
        print(tag, LOG_LEVEL_ERROR, new Object[]{logData});
    }

    public static void i(String logData) {
        i(TAG, logData);
    }

    public static void i(String tag, String logData) {
        print(tag, LOG_LEVEL_INFO, new Object[]{logData});
    }

    public static void v(String logData) {
        v(TAG, logData);
    }

    public static void v(String tag, String logData) {
        print(tag, LOG_LEVEL_VERBOSE, new Object[]{logData});
    }

    public static void w(String logData) {
        w(TAG, logData);
    }

    public static void w(String tag, String logData) {
        print(tag, LOG_LEVEL_WARNING, new Object[]{logData});
    }

    public static void loggingMethodName(Class<?> clazz) {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        int position = 0;
        for (int i = 0; i < stackTraceElements.length; i++) {
            StackTraceElement stackTraceElement = stackTraceElements[i];
            if (stackTraceElement.getClassName().equals(Logger.class.getName())) {
                position = i + 1;
                break;
            }
        }

        if (position < stackTraceElements.length) {
            StackTraceElement stackTraceElement = stackTraceElements[position];
            debug(stackTraceElement.getMethodName());
        }
    }

    /**
     * 로그 레벨
     */
    public final static int LOG_LEVEL_DEBUG = 0;
    public final static int LOG_LEVEL_ERROR = 3;
    public final static int LOG_LEVEL_FAILURE = 6;
    public final static int LOG_LEVEL_INFO = 1;
    public final static int LOG_LEVEL_TIME = 4;
    public final static int LOG_LEVEL_VERBOSE = 5;
    public final static int LOG_LEVEL_WARNING = 2;

    /**
     * Thread deeps
     */
    public final static int ONE_MORE_DEEP = 5;

    /**
     * Release 모드 여부
     */
    private static boolean m_isReleaseMode = !BuildConfig.DEBUG; // (!Features.IS_TEST_MODE);

    /**
     * Tag 출력 여부
     */
    private static boolean m_isShowTag = true;

    /**
     * Thread 이름 출력 여부
     */
    private static boolean m_isShowThreadName = true;

    /**
     * Time 로그 출력 여부
     */
    private static boolean m_isShowTimeLog = true;

    /**
     * 파일 관련 오브젝트
     */
    private static File m_objFile = null;

    private static FileOutputStream m_objFileOutStream = null;
    /**
     * Synchronize 관련 오브젝트
     */
    final private static Object m_objLogLock = new Object();

    /**
     * 태그 이름을 저장하고 있는 문자열
     */
    private static String m_strTag = TAG;

    /**
     * 디버깅 로그를 출력한다.
     *
     * @param args 포맷 스트링
     */
    public static void debug(Object... args) {
        if (m_isReleaseMode) {
            return;
        }

        if (args.length > 1){
            try {
                Boolean isDebug = (Boolean) args[0];
                if(!isDebug.booleanValue()) {
                    return;
                }
            }catch (ClassCastException e){
                e.printStackTrace();
                return;
            }
        }

        synchronized (m_objLogLock) {
            print(LOG_LEVEL_DEBUG, args);
        }
    }

    public static void debugForLogImpl(Object... args) {
        if (m_isReleaseMode) {
            return;
        }

        if (args.length > 1 ){
            try {
                Boolean isDebug = (Boolean) args[0];
                if(!isDebug.booleanValue()) {
                    return;
                }
            }catch (ClassCastException e){
                e.printStackTrace();
                return;
            }
        }

        synchronized (m_objLogLock) {
            print(ONE_MORE_DEEP, LOG_LEVEL_DEBUG, args);
        }
    }

    /**
     * 현재 콜 스택을 덤프한다.
     */
    public static void dump() {
        if (m_isReleaseMode) {
            return;
        }

        synchronized (m_objLogLock) {
            Exception e = new Exception();
            printException(LOG_LEVEL_DEBUG, e);
        }
    }

    /**
     * 파일 로그 출력을 종료한다.
     * <p/>
     * //@see startFileLog
     */
    public static void endFileLog() {
        if (m_isReleaseMode) {
            return;
        }

        if (m_objFile == null || m_objFileOutStream == null) {
            close();
            return;
        }

        synchronized (m_objLogLock) {
            Date objToday = new Date();
            SimpleDateFormat objDate = new SimpleDateFormat("yyyy.MM.dd", Locale.KOREA);
            SimpleDateFormat objTime = new SimpleDateFormat("hh:mm:ss", Locale.KOREA);
            String strDate = "==============================================================================" + "\n" + "Finish File Logger" + "\n" + "Time        : " + objDate.format(objToday) + " " + objTime.format(objToday) + "\n" + "==============================================================================" + "\n";
            write(strDate);
            close();
        }
    }

    /**
     * 에러 로그를 출력한다.
     *
     * @param e Exception Object
     */
    public static void error(Exception e) {
        if (m_isReleaseMode) {
            return;
        }

        synchronized (m_objLogLock) {
            printException(LOG_LEVEL_ERROR, e);
        }
    }

    /**
     * 에러 로그를 출력한다.
     *
     * @param args 포맷 스트링
     */
    public static void error(Object... args) {
        if (m_isReleaseMode) {
            return;
        }

        synchronized (m_objLogLock) {
            print(LOG_LEVEL_ERROR, args);
        }
    }

    public static void errorForLogImpl(Object... args) {
        if (m_isReleaseMode) {
            return;
        }

        synchronized (m_objLogLock) {
            print(ONE_MORE_DEEP, LOG_LEVEL_ERROR, args);
        }
    }

    /**
     * 일반 정보 로그를 출력한다.
     *
     * @param args 포맷 스트링
     */
    public static void info(Object... args) {
        if (m_isReleaseMode) {
            return;
        }

        synchronized (m_objLogLock) {
            print(LOG_LEVEL_INFO, args);
        }
    }

    public static void debug(final Cursor c) {
        if (m_isReleaseMode) {
            return;
        }

        c.moveToFirst();
        String title = "";
        for (int i = 0; i < c.getColumnCount(); i++)
            title += c.getColumnName(i) + "\t| ";
        d(title);
        while (!c.isAfterLast()) {
            title = "";
            for (int i = 0; i < c.getColumnCount(); i++)
                title += c.getString(i) + "\t| ";
            d(title);
            c.moveToNext();
        }
    }

    /**
     * Logcat에 출력할 로그에 tag 이름을 설정한다.
     *
     * @param strTag 출력할 tag 이름
     */
    public static void setLogTag(String strTag) {
        m_strTag = strTag;
    }

    /**
     * Release 모들를 설정한다.
     * Release 모드로 설정될 경우 ERROR를 제외한 모든 로그가 출력되지 않는다.
     *
     * @param isRelease 릴리즈 모드 여부
     */
    public static void setReleaseMode(boolean isRelease) {
        m_isReleaseMode = isRelease;
    }

    /**
     * 로그 정보에 Tag 출력 여부를 결정한다.
     * 로그에 Tag를 출력할 경우 Logcat에서 Application의 로그만 쉽게 필터링이 가능하다.
     *
     * @param isShow Tag 출력 여부
     */
    public static void showTagName(boolean isShow) {
        m_isShowTag = isShow;
    }

    /**
     * Tag에 Thread 이름의 출력 여부를 설정한다.
     * Thread 이름을 출력할 경우 "tag.threadname" 형식으로 출력된다.
     *
     * @param isShow Thread 이름 출력 여부
     */
    public static void showThreadName(boolean isShow) {
        m_isShowThreadName = isShow;
    }

    public static void showTimeLog(boolean isShow) {
        m_isShowTimeLog = isShow;
    }

    public static void printLongString(String str) {
        int start = 0, end = 0, endModify = 0;
        while (str.length() > end + 1 && end != -1) {
            end = str.indexOf('\n', start);
            if (end - start > 120) {
                end = start + 120;
                endModify = 1;
            }
            if (end == -1) end = str.length();
            Logger.debugForLogImpl(str.substring(start, end + endModify));
            start = end + 1;
            endModify = 0;
        }
    }

    /**
     * 파일 로그 출력을 시작한다.
     * <p/>
     * 출력된 파일 로그는 SD카드(/sdcard/[SERVICENAME]/[SERVICENAME]_[DATE].log)에 저장되며
     * AndroidManifest에 "android.permission.WRITE_EXTERNAL_STORAGE"를 추가해야 한다.
     *
     * @param strServiceName 서비스 이름
     *                       //@see endFileLog
     */
    public static void startFileLog(String strServiceName) {
        if (m_isReleaseMode || m_objFile != null) {
            return;
        }

        synchronized (m_objLogLock) {
            String strMountState = Environment.getExternalStorageState();
            if (!strMountState.equals(Environment.MEDIA_MOUNTED)) {
                return;
            }

            Calendar objCal = Calendar.getInstance();
            String strDir = Environment.getExternalStorageDirectory() + "/" + strServiceName;
            File objDir = new File(strDir);
            if (!objDir.isDirectory()) {
                if (!objDir.mkdir()) {
                    return;
                }
            }

            String strFilePath = strDir + "/" + strServiceName + "_" + String.format("%02d", objCal.get(Calendar.MONTH) + 1) + String.format("%02d", objCal.get(Calendar.HOUR_OF_DAY)) + String.format("%02d", objCal.get(Calendar.MINUTE)) + String.format("%02d", objCal.get(Calendar.SECOND)) + ".log";
            m_objFile = new File(strFilePath);
            boolean bExist = m_objFile.exists();

            // remove
            if (bExist) {
                if (m_objFile.delete()) {
                    bExist = false;
                }
            }

            // create
            //            if(!bExist) {
            //                try {
            //                    m_objFile.createNewFile();
            //                } catch(IOException e) {
            //                    close();
            //                    return;
            //                }
            //            }

            // log
            Date objToday = new Date();
            SimpleDateFormat objDate = new SimpleDateFormat("yyyy.MM.dd", Locale.KOREA);
            SimpleDateFormat objTime = new SimpleDateFormat("hh:mm:ss", Locale.KOREA);
            String strDate = "==============================================================================" + "\n" + "Start File Logger" + "\n" + "Sevice Name : " + strServiceName + "\n" + "File Path   : " + strFilePath + "\n" + "Time        : " + objDate.format(objToday) + " " + objTime.format(objToday) + "\n" + "==============================================================================" + "\n";

            // write
            try {
                if (!m_objFile.canWrite()) {
                    return;
                }

                m_objFileOutStream = new FileOutputStream(m_objFile);
                write(strDate);
            } catch (IOException e) {
                close();
            }
        }
    }

    /**
     * Timer 함수들 (wskim)
     */

    static private Date beginDate = null;
    static private Date checkedDate = null;
    static private Date currentDate = null;
    static private HashMap<Integer, TimeCheck> dateMap = new HashMap<>();

    static class TimeCheck {
        public Date beginDate;
        public Date checkedDate;

        TimeCheck(Date beginDate, Date checkedDate) {
            this.beginDate = beginDate;
            this.checkedDate = checkedDate;
        }
    }

    public static void beginTime(Integer timeIndex, Object... args) {
        if (!m_isShowTimeLog) {
            return;
        }

        beginDate = checkedDate = currentDate = new Date();
        TimeCheck beginTime = new TimeCheck(beginDate, checkedDate);
        dateMap.put(timeIndex, beginTime);

        synchronized (m_objLogLock) {
            print(LOG_LEVEL_TIME, args);
        }
    }

    public static void time(Integer timeIndex, Object... args) {
        if (!m_isShowTimeLog) {
            return;
        }

        currentDate = new Date();
        TimeCheck checkedTime = dateMap.get(timeIndex);
        beginDate = checkedTime.beginDate;
        checkedDate = checkedTime.checkedDate;

        checkedTime.checkedDate = currentDate;
        dateMap.put(timeIndex, checkedTime);

        synchronized (m_objLogLock) {
            print(LOG_LEVEL_TIME, args);
        }
    }

    public static void endTime(Integer timeIndex, Object... args) {
        if (!m_isShowTimeLog) {
            return;
        }

        currentDate = new Date();
        TimeCheck checkedTime = dateMap.get(timeIndex);

        if (checkedTime != null) {
            beginDate = checkedTime.beginDate;
            checkedDate = checkedTime.checkedDate;

            checkedTime.checkedDate = currentDate;
            dateMap.put(timeIndex, checkedTime);
        } else {
            beginDate = null;
        }

        synchronized (m_objLogLock) {
            print(LOG_LEVEL_TIME, args);
        }

        dateMap.remove(timeIndex);
    }

    /**
     * 설명 로그를 출력한다.
     *
     * @param args 포맷 스트링
     */
    public static void verbose(Object... args) {
        if (m_isReleaseMode) {
            return;
        }

        try {
            Boolean isDebug = (Boolean) args[0];
            if(!isDebug.booleanValue()) {
                return;
            }
        }catch (ClassCastException e){
            e.printStackTrace();
            return;
        }

        synchronized (m_objLogLock) {
            print(LOG_LEVEL_VERBOSE, args);
        }
    }

    /**
     * 경고 로그를 출력한다.
     *
     * @param args 포맷 스트링
     */
    public static void warning(Object... args) {
        if (m_isReleaseMode) {
            return;
        }

        synchronized (m_objLogLock) {
            print(LOG_LEVEL_WARNING, args);
        }
    }

    public static void warningForLogImpl(Object... args) {
        if (m_isReleaseMode) {
            return;
        }

        synchronized (m_objLogLock) {
            print(ONE_MORE_DEEP, LOG_LEVEL_WARNING, args);
        }
    }

    /**
     * Output stream을 닫는다.
     */
    private synchronized static void close() {
        try {
            if (m_objFileOutStream != null) {
                m_objFileOutStream.close();
            }
        } catch (IOException e1) {
            // do nothing
        }
        m_objFileOutStream = null;
        m_objFile = null;
    }

    private static void print(int nLevel, Object[] args) {
        print(m_strTag, ONE_MORE_DEEP, nLevel, args);
    }

    private static void print(String tag, int nLevel, Object[] args) {
        print(tag, ONE_MORE_DEEP, nLevel, args);
    }

    private static void print(int traceDeep, int nLevel, Object[] args) {
        print(m_strTag, traceDeep, nLevel, args);
    }

    /**
     * 로그를 출력한다.
     *
     * @param nLevel 로그 레벨
     * @param args   포맷 스트링
     */
    private static void print(String tag, int traceDeep, int nLevel, Object[] args) {
        if (m_isReleaseMode) {
            return;
        }

        Thread objThread = Thread.currentThread();

        // get thread name
        String strThreadName = "";
        if (m_isShowThreadName) {
            strThreadName = objThread.getName();
        }

        // get file name and line number
        String strFileName = objThread.getStackTrace()[traceDeep].getFileName();
        int nLineNumber = objThread.getStackTrace()[traceDeep].getLineNumber();

        // limit filename length
        if (strFileName.length() > 20) {
            strFileName = strFileName.substring(0, 20);
        }

        // format
        int index = 0;
        String strFormat;
        if (args[0] == null) {
            strFormat = "null";
        } else if (args.length > 1 && args[0] instanceof Boolean) {
            index = 1;
            strFormat = args[1].toString();
        } else {
            strFormat = args[0].toString();
        }
        strFormat = strFormat.replaceAll("%d", "%s");
        strFormat = strFormat.replaceAll("%f", "%s");
        strFormat = strFormat.replaceAll("%c", "%s");
        strFormat = strFormat.replaceAll("%b", "%s");
        strFormat = strFormat.replaceAll("%x", "%s");
        strFormat = strFormat.replaceAll("%l", "%s");

        // argument
        String strArgument = "";
        switch (args.length - index - 1) {
            case 0:
                strArgument = strFormat;
                break;
            case 1:
                strArgument = String.format(strFormat, args[1 + index]);
                break;
            case 2:
                strArgument = String.format(strFormat, args[1 + index], args[2 + index]);
                break;
            case 3:
                strArgument = String.format(strFormat, args[1 + index], args[2 + index], args[3 + index]);
                break;
            case 4:
                strArgument = String.format(strFormat, args[1 + index], args[2 + index], args[3 + index], args[4 + index]);
                break;
            case 5:
                strArgument = String.format(strFormat, args[1 + index], args[2 + index], args[3 + index], args[4 + index], args[5 + index]);
                break;
            case 6:
                strArgument = String.format(strFormat, args[1 + index], args[2 + index], args[3 + index], args[4 + index], args[5 + index], args[6 + index]);
                break;
            case 7:
                strArgument = String.format(strFormat, args[1 + index], args[2 + index], args[3 + index], args[4 + index], args[5 + index], args[6 + index], args[7 + index]);
                break;
            case 8:
                strArgument = String.format(strFormat, args[1 + index], args[2 + index], args[3 + index], args[4 + index], args[5 + index], args[6 + index], args[7 + index], args[8 + index]);
                break;
            case 9:
                strArgument = String.format(strFormat, args[1 + index], args[2 + index], args[3 + index], args[4 + index], args[5 + index], args[6 + index], args[7 + index], args[8 + index], args[9 + index]);
                break;
            case 10:
                strArgument = String.format(strFormat, args[1 + index], args[2 + index], args[3 + index], args[4 + index], args[5 + index], args[6 + index], args[7 + index], args[8 + index], args[9 + index], args[10 + index]);
                break;
            default:
                break;
        }

        // log
        String strLog;
        if (m_isShowTimeLog && nLevel == LOG_LEVEL_TIME) {
            if (currentDate == null || beginDate == null) {
                strLog = String.format("[%-15s:%-20s:%5d] %s (No data)\n", strThreadName, strFileName, nLineNumber, strArgument);
            } else {
                long diff1 = currentDate.getTime() - beginDate.getTime();
                long diff2 = currentDate.getTime() - checkedDate.getTime();
                strLog = String.format("[%-15s:%-20s:%5d] %s [ %,d.%03d (%,d.%03d) ] \n", strThreadName, strFileName, nLineNumber, strArgument, diff1 / 1000, diff1 % 1000, diff2 / 1000, diff2 % 1000);
            }
        } else if (m_isShowTag) {
            strLog = String.format("[%-15s:%-20s:%5d] %s\n", strThreadName, strFileName, nLineNumber, strArgument);
        } else {
            strLog = String.format("[%-20s:%5d] %s\n", strFileName, nLineNumber, strArgument);
        }

        // tag
        String strTag = tag;

        // Level
        switch (nLevel) {
            case LOG_LEVEL_ERROR:
            case LOG_LEVEL_FAILURE: {
                // 라인넘버를 찍기 위하여 exception 을 강제로 발생시킴.
                Exception e = new Exception();
                StackTraceElement element = e.getStackTrace()[2];
                Log.e(strTag, "[Line = " + element.getLineNumber() + "] -- " + strLog);
            }
            break;
            case LOG_LEVEL_WARNING:
                Log.w(strTag, strLog);
                break;
            case LOG_LEVEL_INFO:
            case LOG_LEVEL_TIME:
                Log.i(strTag, strLog);
                break;
            case LOG_LEVEL_VERBOSE:
                Log.v(strTag, strLog);
                break;
            case LOG_LEVEL_DEBUG:
            default:
                Log.d(strTag, strLog);
                break;
        }

        if (m_objFileOutStream != null) {
            write(strLog);
        }
    }

    /**
     * Exception 개체를 덤프한다.
     *
     * @param nLevel 로그 레벨
     * @param e      Exception
     */
    static private void printException(int nLevel, Exception e) {
        if (m_isReleaseMode) {
            return;
        }

        StackTraceElement[] aElement = e.getStackTrace();

        Thread objThread = Thread.currentThread();
        // get thread name
        String strThreadName = "";
        if (m_isShowThreadName) {
            strThreadName = objThread.getName();
        }
        // tag
        String strTag = m_strTag;
        if (m_isShowThreadName) {
            strTag = m_strTag + "." + strThreadName;
        }

        // get file name and line number
        String strFileName = objThread.getStackTrace()[4].getFileName();
        int nLineNumber = objThread.getStackTrace()[4].getLineNumber();

        // limit filename length
        if (strFileName.length() > 20) {
            strFileName = strFileName.substring(0, 20);
        }

        int nCount = aElement.length;
        String strLog;

        // print head line
        if (nLevel == LOG_LEVEL_ERROR) {
            strLog = String.format("[%-20s:%5d] %s: %s", strFileName, nLineNumber, e.getClass().getName(), e.getMessage());
        } else {
            strLog = String.format("[%-20s:%5d] %s", strFileName, nLineNumber, "== PRINT CALL STACK ==");
        }

        // Level
        switch (nLevel) {
            case LOG_LEVEL_ERROR:
                Log.e(strTag, strLog);
                break;
            default:
                Log.d(strTag, strLog);
                break;
        }

        if (m_objFileOutStream != null) {
            write(strLog);
        }

        // print stack trace
        for (int i = 0; i < nCount; i++) {
            if (i == 0 && nLevel != LOG_LEVEL_ERROR) {
                // do nothing
                continue;
            } else {
                strLog = String.format("[%-20s:%5d]    at %s %s (%s:%d)", strFileName, nLineNumber, aElement[i].getClassName(), aElement[i].getMethodName(), aElement[i].getFileName(), aElement[i].getLineNumber());
            }

            // Level
            switch (nLevel) {
                case LOG_LEVEL_ERROR:
                    Log.e(strTag, strLog);
                    break;
                default:
                    Log.d(strTag, strLog);
                    break;
            }

            if (m_objFileOutStream != null) {
                write(strLog);
            }
        }
    }

    /**
     * 파일에 로그를 출력한다.
     *
     * @param strLog 출력할 로그 문자열
     */
    private synchronized static void write(String strLog) {
        if (m_isReleaseMode) {
            return;
        }

        if (m_objFile == null || m_objFileOutStream == null) {
            close();
            return;
        }

        try {
            if (!m_objFile.canWrite()) {
                return;
            }

            Date objToday = new Date();
            SimpleDateFormat objTimeDate = new SimpleDateFormat("MM/dd", Locale.KOREA);
            SimpleDateFormat objTime = new SimpleDateFormat("hh:mm:ss", Locale.KOREA);
            String strPrefix = "[" + objTimeDate.format(objToday) + " " + objTime.format(objToday) + "] " + strLog;

            m_objFileOutStream.write(strPrefix.getBytes());
        } catch (IOException e) {
            close();
        }
    }
}
