package com.payneteasy.loggingextensions.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import com.payneteasy.loggingextensions.Throwables;
import com.payneteasy.loggingextensions.utils.TCPLogbackAcceptor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * @author rpuch
 */
public class CustomizedSocketAppenderTest {
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(CustomizedSocketAppenderTest.class);

    private TCPLogbackAcceptor server;

    @Before
    public void setUp() {
        server = new TCPLogbackAcceptor(4444);
        server.start();
    }

    @After
    public void cleanup() {
        server.stop();
    }

    @Test
    public void testSend() throws Exception {
        CustomizedSocketAppender appender = new CustomizedSocketAppender();
        appender.setContext(LOGGER.getLoggerContext());
        appender.setRemoteHost("localhost");
        appender.setPort(4444);
        appender.start();

        try {
            appender.doAppend(new LoggingEvent("FQCN", LOGGER, Level.DEBUG,
                    "Test message", Throwables.generateThrowable(50), null));

            ILoggingEvent acceptedEvent = server.getResultFuture().get(5, TimeUnit.SECONDS);
            assertNotNull("Did not accept any message", acceptedEvent);
            assertEquals("Test message", acceptedEvent.getMessage());
            assertEquals("Test message", acceptedEvent.getFormattedMessage());
            assertEquals(LOGGER.getName(), acceptedEvent.getLoggerName());
            assertEquals(Level.DEBUG, acceptedEvent.getLevel());
            assertNotNull(acceptedEvent.getThrowableProxy());
            assertEquals("Some exception", acceptedEvent.getThrowableProxy().getMessage());
        } finally {
            appender.stop();
            server.stop();
        }
    }

    @Test
    public void testSendWithLoggerNameInMessage() throws Exception {
        CustomizedSocketAppender appender = new CustomizedSocketAppender();
        appender.setContext(LOGGER.getLoggerContext());
        appender.setRemoteHost("localhost");
        appender.setPort(4444);
        appender.setSendLoggerNameInMessage(true);
        appender.start();

        try {
            appender.doAppend(new LoggingEvent("FQCN", LOGGER, Level.DEBUG,
                    "Test message", Throwables.generateThrowable(50), null));

            ILoggingEvent acceptedEvent = server.getResultFuture().get(5, TimeUnit.SECONDS);
            assertNotNull("Did not accept any message", acceptedEvent);
            assertEquals("com.payneteasy.loggingextensions.logback.CustomizedSocketAppenderTest:\nTest message", acceptedEvent.getMessage());
            assertEquals("com.payneteasy.loggingextensions.logback.CustomizedSocketAppenderTest:\nTest message", acceptedEvent.getFormattedMessage());
            assertNull(null, acceptedEvent.getLoggerName());
            assertEquals(Level.DEBUG, acceptedEvent.getLevel());
            assertNotNull(acceptedEvent.getThrowableProxy());
            assertEquals("Some exception", acceptedEvent.getThrowableProxy().getMessage());
        } finally {
            appender.stop();
            server.stop();
        }
    }
}