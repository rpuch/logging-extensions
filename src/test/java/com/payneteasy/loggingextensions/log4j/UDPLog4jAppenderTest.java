package com.payneteasy.loggingextensions.log4j;

import com.payneteasy.loggingextensions.Throwables;
import com.payneteasy.loggingextensions.utils.UDPLog4jAcceptor;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author rpuch
 */
public class UDPLog4jAppenderTest {
    private static final Logger LOGGER = (Logger) Logger.getLogger(UDPLog4jAppenderTest.class);

    @Test
    public void testNoHostSpecified() {
        UDPLog4jAppender appender = new UDPLog4jAppender();
        appender.setPort(4000);
        try {
            appender.activateOptions();
            Assert.fail("activateOptions() should fail as remoteHost is not configured");
        } catch (IllegalStateException e) {
            Assert.assertEquals("remoteHost not configured", e.getMessage());
        }
    }

    @Test
    public void testNoPortSpecified() {
        UDPLog4jAppender appender = new UDPLog4jAppender();
        appender.setRemoteHost("localhost");
        try {
            appender.activateOptions();
            Assert.fail("activateOptions() should fail as remotePort is not configured");
        } catch (IllegalStateException e) {
            Assert.assertEquals("port not configured", e.getMessage());
        }
    }

    @Test
    public void testSend() throws InterruptedException, TimeoutException, ExecutionException {
        UDPLog4jAcceptor server = new UDPLog4jAcceptor(3333);
        server.start();

        UDPLog4jAppender appender = new UDPLog4jAppender();
        appender.setRemoteHost("localhost");
        appender.setPort(3333);
        appender.activateOptions();

        try {
            appender.doAppend(new LoggingEvent("FQCN", LOGGER, Level.DEBUG,
                    "Test message", Throwables.generateThrowable(50)));

            LoggingEvent acceptedEvent = server.getResultFuture().get(5, TimeUnit.SECONDS);
            Assert.assertNotNull("Did not accept any datagram", acceptedEvent);
            Assert.assertEquals("Test message", acceptedEvent.getMessage());
            Assert.assertEquals(LOGGER.getName(), acceptedEvent.getLoggerName());
            Assert.assertEquals(Level.DEBUG, acceptedEvent.getLevel());
            Assert.assertNotNull(acceptedEvent.getThrowableInformation());
            Assert.assertNotNull(acceptedEvent.getThrowableInformation().getThrowableStrRep());
            Assert.assertTrue(acceptedEvent.getThrowableInformation().getThrowableStrRep().length > 0);
            Assert.assertTrue(
                    acceptedEvent.getThrowableInformation().getThrowableStrRep()[0].contains("Some exception"));
        } finally {
            appender.close();
            server.stop();
        }
    }
}