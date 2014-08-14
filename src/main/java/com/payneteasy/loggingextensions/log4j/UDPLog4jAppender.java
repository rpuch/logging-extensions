package com.payneteasy.loggingextensions.log4j;

import com.payneteasy.loggingextensions.utils.SerializationUtils;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.ErrorCode;
import org.apache.log4j.spi.LoggingEvent;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

/**
 * Appender for log4j which sends logging data using UDP protocol.
 * For each log message, corresponding LoggingEvent is serialized
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
 * <appender name="udp" class="com.payneteasy.loggingextensions.log4j.UDPLog4jAppender">
 *     <param name="remoteHost" value="localhost"/>
 *     <param name="port" value="3333"/>
 * </appender>
 * }</pre>
 *
 * @author rpuch
 */
public class UDPLog4jAppender extends AppenderSkeleton {
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
    public void activateOptions() {
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
    }

    public void close() {
        try {
            channel.close();
        } catch (IOException e) {
            errorHandler.error("Cannot close channel", e, ErrorCode.CLOSE_FAILURE);
        }
        channel = null;
    }

    public boolean requiresLayout() {
        return false;
    }

    @Override
    protected void append(LoggingEvent eventObject) {
        prepareEventObject(eventObject);
        try {
            byte[] bytes = serializeObjectToBytes(eventObject);
            channel.write(ByteBuffer.wrap(bytes));
        } catch (IOException e) {
            errorHandler.error("Cannot send a message", e, ErrorCode.WRITE_FAILURE);
        }
    }

    private void prepareEventObject(LoggingEvent event) {
        event.getNDC();
        event.getThreadName();
        event.getMDCCopy();
        event.getRenderedMessage();
        event.getThrowableStrRep();
    }

    private byte[] serializeObjectToBytes(LoggingEvent eventObject) throws IOException {
        return SerializationUtils.serializeObjectToBytes(eventObject);
    }

}
