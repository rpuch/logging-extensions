package com.payneteasy.loggingextensions.logback;

import ch.qos.logback.classic.net.LoggingEventPreSerializationTransformer;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.spi.LoggingEventVO;
import ch.qos.logback.core.spi.PreSerializationTransformer;

import java.io.Serializable;

/**
 * @author rpuch
 */
public class LoggerNameInMessageTransformer implements PreSerializationTransformer<ILoggingEvent> {
    private final PreSerializationTransformer<ILoggingEvent> delegate
            = new LoggingEventPreSerializationTransformer();

    public Serializable transform(ILoggingEvent event) {
        if (event == null) {
            return null;
        }

        if (event instanceof LoggingEvent) {
            return LoggingEventVO.build(new LoggingEventWithLoggerNameInMessage(event));
        } else if (event instanceof LoggingEventVO) {
            return (LoggingEventVO) event;
        } else {
            throw new IllegalArgumentException("Unsupported type " + event.getClass().getName());
        }
    }
}
