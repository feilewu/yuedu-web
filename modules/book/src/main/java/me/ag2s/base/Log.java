package me.ag2s.base;

public final class Log {
    public static int d(String tag, String msg) {
        System.out.println("D/" + tag + ": " + msg);
        return 0;
    }

    public static int e(String tag, String msg) {
        System.err.println("E/" + tag + ": " + msg);
        return 0;
    }

    public static int e(String tag, String msg, Throwable tr) {
        System.err.println("E/" + tag + ": " + msg);
        tr.printStackTrace(System.err);
        return 0;
    }

    public static int v(String tag, String msg) {
        System.out.println("V/" + tag + ": " + msg);
        return 0;
    }
}
