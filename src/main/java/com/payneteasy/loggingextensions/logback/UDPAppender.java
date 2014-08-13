package com.payneteasy.loggingextensions.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEventVO;
import ch.qos.logback.core.UnsynchronizedAppenderBase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

/**
 * @author rpuch
 */
public class UDPAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {
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
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(eventObject);
        oos.close();
        return baos.toByteArray();
    }

}
