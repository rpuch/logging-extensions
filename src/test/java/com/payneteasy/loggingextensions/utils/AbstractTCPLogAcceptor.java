package com.payneteasy.loggingextensions.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author rpuch
 */
abstract class AbstractTCPLogAcceptor<E> {
    private final int serverPort;
    private final ExecutorService acceptorExecutor = Executors.newSingleThreadExecutor();
    private ServerSocket serverSocket;

    private volatile boolean started = false;
    private volatile boolean stopped = false;

    private final CountDownLatch acceptedLatch = new CountDownLatch(1);
    private volatile E acceptedEvent;
    private final Future<E> resultFuture = new ResultFuture();

    protected AbstractTCPLogAcceptor(int serverPort) {
        this.serverPort = serverPort;
    }

    public void start() {
        if (started) {
            throw new IllegalStateException("Already started");
        }
        started = true;
        serverSocket = openAndBindServerSocket();
        acceptorExecutor.execute(new Runnable() {
            public void run() {
                try {
                    mainLoop();
                } catch (IOException e) {
                    if (!stopped) {
                        e.printStackTrace();
                        stop();
                    }
                }
            }
        });
    }

    private void mainLoop() throws IOException {
        while (!stopped) {
            Socket socket = serverSocket.accept();
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            try {
                @SuppressWarnings("unchecked") E event = (E) ois.readObject();
                acceptedEvent = event;
                acceptedLatch.countDown();
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    private ServerSocket openAndBindServerSocket() {
        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(serverPort);
            return serverSocket;
        } catch (IOException e) {
            throw new IllegalStateException("Cannot open server socket", e);
        }
    }

    public synchronized void stop() {
        if (stopped) {
            return;
        }
        stopped = true;
        try {
            serverSocket.close();
        } catch (IOException e) {
            // ignoring
        }
        acceptorExecutor.shutdown();
        try {
            acceptorExecutor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            // ignoring
        }
    }

    public Future<E> getResultFuture() {
        return resultFuture;
    }

    private class ResultFuture implements Future<E> {
        public boolean cancel(boolean mayInterruptIfRunning) {
            throw new UnsupportedOperationException();
        }

        public boolean isCancelled() {
            return false;
        }

        public boolean isDone() {
            return acceptedLatch.getCount() == 0;
        }

        public E get() throws InterruptedException, ExecutionException {
            acceptedLatch.await();
            return acceptedEvent;
        }

        public E get(long timeout,
                TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            acceptedLatch.await(timeout, unit);
            return acceptedEvent;
        }
    }
}
