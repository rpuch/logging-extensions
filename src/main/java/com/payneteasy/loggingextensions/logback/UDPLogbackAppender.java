package com.payneteasy.loggingextensions.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEventVO;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import com.payneteasy.loggingextensions.utils.SerializationUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

/**
 * Appender for logback which sends logging data using UDP protocol.
 * For each log message, corresponding ILoggingEvent is serialized
 * using standard Java serialization mechanism, and then it is
 * transmitted in a single datagram.
 * <p>
 * This class supports the following parameters:
 * <ul>
 *     <li><b>remoteHost</b> - name of the host to which datagrams
 *     will be sent</li>
 *     <li><b>port</b> - port number of the remote host to which datagrams
 *     will be sent</li>
 * </ul>
 * <p>
 * Example configuration follows:
 * <pre>{@code
 * <appender name="udp" class="com.payneteasy.loggingextensions.logback.UDPLogbackAppender">
 *     <remoteHost>localhost</remoteHost>
 *     <port>3333</port>
 * </appender>
 * }</pre>
 *
 * @author rpuch
 */
public class UDPLogbackAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {
    private String remoteHost;
    private int port = -1;

    private DatagramChannel channel;

    public void setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public void start() {
        if (remoteHost == null) {
            throw new IllegalStateException("remoteHost not configured");
        }
        if (port <= 0) {
            throw new IllegalStateException("port not configured");
        }

        try {
            channel = DatagramChannel.open();
            channel.connect(new InetSocketAddress(remoteHost, port));
        } catch (IOException e) {
            throw new IllegalStateException("Cannot open DatagramChannel", e);
        }

        started = true;
    }

    @Override
    public void stop() {
        started = false;

        try {
            channel.close();
        } catch (IOException e) {
            addWarn("Cannot close channel", e);
        }
        channel = null;
    }

    @Override
    protected void append(ILoggingEvent eventObject) {
        try {
            byte[] bytes = serializeObjectToBytes(LoggingEventVO.build(eventObject));
            channel.write(ByteBuffer.wrap(bytes));
        } catch (IOException e) {
            addError("Cannot send a message", e);
        }
    }

    private byte[] serializeObjectToBytes(ILoggingEvent eventObject) throws IOException {
        return SerializationUtils.serializeObjectToBytes(eventObject);
    }

}
