package com.payneteasy.loggingextensions.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.LoggerContextVO;
import org.slf4j.Marker;

import java.util.Map;

/**
 * <p>ILoggingEvent implementation which delegates to another event.</p>
 * <p>It changes the following behavior:
 * <ul>
 *     <li>getLoggerName() returns null</li>
 *     <li>getMessage() prepends the message with getLoggerName()
 *     from the delegate.</li>
 *     <li>getFormattedMessage() prepends the message with getLoggerName()
 *     from the delegate.</li>
 * </ul>
 * </p>
 *
 * @author rpuch
 */
class LoggingEventWithLoggerNameInMessage implements ILoggingEvent {
    private final ILoggingEvent event;

    LoggingEventWithLoggerNameInMessage(ILoggingEvent event) {
        this.event = event;
    }

    public String getThreadName() {
        return event.getThreadName();
    }

    public Level getLevel() {
        return event.getLevel();
    }

    public String getMessage() {
        return prependWithLoggerName(event.getMessage());
    }

    private String prependWithLoggerName(String message) {
        return event.getLoggerName() + ":\n" + message;
    }

    public Object[] getArgumentArray() {
        return event.getArgumentArray();
    }

    public String getFormattedMessage() {
        return prependWithLoggerName(event.getFormattedMessage());
    }

    public String getLoggerName() {
        return null;
    }

    public LoggerContextVO getLoggerContextVO() {
        return event.getLoggerContextVO();
    }

    public IThrowableProxy getThrowableProxy() {
        return event.getThrowableProxy();
    }

    public StackTraceElement[] getCallerData() {
        return event.getCallerData();
    }

    public boolean hasCallerData() {
        return event.hasCallerData();
    }

    public Marker getMarker() {
        return event.getMarker();
    }

    public Map<String, String> getMDCPropertyMap() {
        return event.getMDCPropertyMap();
    }

    public Map<String, String> getMdc() {
        return event.getMdc();
    }

    public long getTimeStamp() {
        return event.getTimeStamp();
    }

    public void prepareForDeferredProcessing() {
        event.prepareForDeferredProcessing();
    }
}
