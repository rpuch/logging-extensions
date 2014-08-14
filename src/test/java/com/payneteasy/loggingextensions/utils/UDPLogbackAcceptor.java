package com.payneteasy.loggingextensions.utils;

import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * @author rpuch
 */
public class UDPLogbackAcceptor extends AbstractUDPLogAcceptor<ILoggingEvent> {
    public UDPLogbackAcceptor(int serverPort) {
        super(serverPort);
    }
}
