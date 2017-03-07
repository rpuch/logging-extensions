package com.payneteasy.loggingextensions.logback;

import ch.qos.logback.classic.net.SocketAppender;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.spi.PreSerializationTransformer;

/**
 * <p>SocketAppender customization.</p>
 * <p>Currently the only customization is that if <b>sendLoggerNameInMessage</b>
 * is true (default is false), logger name is prepended to message
 * while loggerName is cleared (replaced with null).</p>
 *
 * @author rpuch
 */
public class CustomizedSocketAppender extends SocketAppender {
    private boolean sendLoggerNameInMessage = false;

    private static final PreSerializationTransformer<ILoggingEvent> pst =
        new LoggerNameInMessageTransformer();

    public void setSendLoggerNameInMessage(boolean sendLoggerNameInMessage) {
        this.sendLoggerNameInMessage = sendLoggerNameInMessage;
    }

    @Override
    public PreSerializationTransformer<ILoggingEvent> getPST() {
        if (sendLoggerNameInMessage) {
            return pst;
        } else {
            return super.getPST();
        }
    }
}
