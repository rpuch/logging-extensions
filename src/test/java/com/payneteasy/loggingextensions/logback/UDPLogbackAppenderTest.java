package com.payneteasy.loggingextensions.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.StatusListener;
import com.payneteasy.loggingextensions.Throwables;
import com.payneteasy.loggingextensions.utils.UDPLogbackAcceptor;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * @author rpuch
 */
public class UDPLogbackAppenderTest {
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(UDPLogbackAppenderTest.class);
    private static final StatusListener STATUS_LISTENER = new StatusListener() {
        public void addStatusEvent(Status status) {
            throw new AssertionError(status.getMessage(), status.getThrowable());
        }
    };

    @BeforeClass
    public static void initClass() {
        LOGGER.getLoggerContext().getStatusManager().add(STATUS_LISTENER);
    }

    @AfterClass
    public static void cleanUpForClass() {
        LOGGER.getLoggerContext().getStatusManager().remove(STATUS_LISTENER);
    }

    @Test
    public void testNoHostSpecified() {
        UDPLogbackAppender appender = new UDPLogbackAppender();
        appender.setPort(4000);
        try {
            appender.start();
            Assert.fail("start() should fail as remoteHost is not configured");
        } catch (IllegalStateException e) {
            assertEquals("remoteHost not configured", e.getMessage());
        }
    }

    @Test
    public void testNoPortSpecified() {
        UDPLogbackAppender appender = new UDPLogbackAppender();
        appender.setRemoteHost("localhost");
        try {
            appender.start();
            Assert.fail("start() should fail as remotePort is not configured");
        } catch (IllegalStateException e) {
            assertEquals("port not configured", e.getMessage());
        }
    }

    @Test
    public void testStartSetsIsStartedFlag() {
        UDPLogbackAppender appender = new UDPLogbackAppender();
        appender.setRemoteHost("localhost");
        appender.setPort(3000);
        appender.start();
        Assert.assertTrue("After start() is called, isStarted() must return true", appender.isStarted());
    }

    @Test
    public void testStopClearsIsStartedFlag() {
        UDPLogbackAppender appender = new UDPLogbackAppender();
        appender.setRemoteHost("localhost");
        appender.setPort(3000);
        appender.start();
        appender.stop();
        Assert.assertFalse("After stop() is called, isStarted() must return false", appender.isStarted());
    }

    @Test
    public void testSend() throws InterruptedException, TimeoutException, ExecutionException {
        UDPLogbackAcceptor server = new UDPLogbackAcceptor(3333);
        server.start();

        UDPLogbackAppender appender = new UDPLogbackAppender();
        appender.setContext(LOGGER.getLoggerContext());
        appender.setRemoteHost("localhost");
        appender.setPort(3333);
        appender.start();

        try {
            appender.doAppend(new LoggingEvent("FQCN", LOGGER, Level.DEBUG,
                    "Test message", Throwables.generateThrowable(50), null));

            ILoggingEvent acceptedEvent = server.getResultFuture().get(5, TimeUnit.SECONDS);
            assertNotNull("Did not accept any datagram", acceptedEvent);
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
    public void testSendWithLoggerNameInMessage() throws InterruptedException, TimeoutException, ExecutionException {
        UDPLogbackAcceptor server = new UDPLogbackAcceptor(3333);
        server.start();

        UDPLogbackAppender appender = new UDPLogbackAppender();
        appender.setContext(LOGGER.getLoggerContext());
        appender.setRemoteHost("localhost");
        appender.setPort(3333);
        appender.setSendLoggerNameInMessage(true);
        appender.start();

        try {
            appender.doAppend(new LoggingEvent("FQCN", LOGGER, Level.DEBUG,
                    "Test message", Throwables.generateThrowable(50), null));

            ILoggingEvent acceptedEvent = server.getResultFuture().get(5, TimeUnit.SECONDS);
            assertNotNull("Did not accept any datagram", acceptedEvent);
            assertEquals("com.payneteasy.loggingextensions.logback.UDPLogbackAppenderTest:\nTest message", acceptedEvent.getMessage());
            assertEquals("com.payneteasy.loggingextensions.logback.UDPLogbackAppenderTest:\nTest message", acceptedEvent.getFormattedMessage());
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