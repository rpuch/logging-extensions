package com.payneteasy.loggingextensions;

/**
 * @author rpuch
 */
public class Throwables {
    public static Throwable generateThrowable(int level) {
        if (level <= 0) {
            return new RuntimeException("Inner exception");
        } else if (level % 5 == 0) {
            return new RuntimeException("Some exception", generateThrowable(level - 1));
        } else {
            return generateThrowable(level - 1);
        }
    }
}
