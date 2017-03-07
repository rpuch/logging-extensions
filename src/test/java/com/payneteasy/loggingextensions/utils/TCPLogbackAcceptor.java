package com.payneteasy.loggingextensions.utils;

import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * @author rpuch
 */
public class TCPLogbackAcceptor extends AbstractTCPLogAcceptor<ILoggingEvent> {
    public TCPLogbackAcceptor(int serverPort) {
        super(serverPort);
    }
}
