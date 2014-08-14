package com.payneteasy.loggingextensions.utils;

import org.apache.log4j.spi.LoggingEvent;

/**
 * @author rpuch
 */
public class UDPLog4jAcceptor extends AbstractUDPLogAcceptor<LoggingEvent> {
    public UDPLog4jAcceptor(int serverPort) {
        super(serverPort);
    }
}
