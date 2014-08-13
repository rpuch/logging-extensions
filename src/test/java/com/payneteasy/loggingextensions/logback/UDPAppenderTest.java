package com.payneteasy.loggingextensions.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.StatusListener;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author rpuch
 */
public class UDPAppenderTest {
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(UDPAppenderTest.class);
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
        UDPAppender appender = new UDPAppender();
        appender.setPort(4000);
        try {
            appender.start();
            Assert.fail("start() should fail as remoteHost is not configured");
        } catch (IllegalStateException e) {
            Assert.assertEquals("remoteHost not configured", e.getMessage());
        }
    }

    @Test
    public void testNoPortSpecified() {
        UDPAppender appender = new UDPAppender();
        appender.setRemoteHost("localhost");
        try {
            appender.start();
            Assert.fail("start() should fail as remotePort is not configured");
        } catch (IllegalStateException e) {
            Assert.assertEquals("port not configured", e.getMessage());
        }
    }

    @Test
    public void testStartSetsIsStartedFlag() {
        UDPAppender appender = new UDPAppender();
        appender.setRemoteHost("localhost");
        appender.setPort(3000);
        appender.start();
        Assert.assertTrue("After start() is called, isStarted() must return true", appender.isStarted());
    }

    @Test
    public void testStopClearsIsStartedFlag() {
        UDPAppender appender = new UDPAppender();
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

        UDPAppender appender = new UDPAppender();
        appender.setContext(LOGGER.getLoggerContext());
        appender.setRemoteHost("localhost");
        appender.setPort(3333);
        appender.start();

        try {
            appender.doAppend(new LoggingEvent("FQCN", LOGGER, Level.DEBUG,
                    "Test message", generateThrowable(50), null));

            ILoggingEvent acceptedEvent = server.getResultFuture().get(5, TimeUnit.SECONDS);
            Assert.assertNotNull("Did not accept any datagram", acceptedEvent);
            Assert.assertEquals("Test message", acceptedEvent.getMessage());
            Assert.assertEquals(LOGGER.getName(), acceptedEvent.getLoggerName());
            Assert.assertEquals(Level.DEBUG, acceptedEvent.getLevel());
            Assert.assertNotNull(acceptedEvent.getThrowableProxy());
            Assert.assertEquals("Some exception", acceptedEvent.getThrowableProxy().getMessage());
        } finally {
            appender.stop();
            server.stop();
        }
    }

    private Throwable generateThrowable(int level) {
        if (level <= 0) {
            return new RuntimeException("Inner exception");
        } else if (level % 5 == 0) {
            return new RuntimeException("Some exception", generateThrowable(level - 1));
        } else {
            return generateThrowable(level - 1);
        }
    }
}