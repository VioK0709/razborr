package org.example;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class Main {
    public static void main(String[] args) throws IOException {
        Server server = new Server();
        initializeHandlers(server);
    }

    private static void initializeHandlers(Server server) {
        server.addHandler("GET", "/messages", new Handler() {
            @Override
            public void publish(LogRecord record) {
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() throws SecurityException {
            }

            public void handle(Request request, BufferedOutputStream outputStream) {
                String resp = getMessages();
                PrintStream ps = new PrintStream(outputStream);
                ps.printf("HTTP/1.1 %s %s%n", 200, "OK")
                        .printf("Content-Type: %s%n", "index.html")
                        .printf("Content-Length:%s%n%n", resp.getBytes().length)
                        .flush();
            }
        });

        server.addHandler("POST", "/messages", new Handler() {
            @Override
            public void publish(LogRecord record) {
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() throws SecurityException {
            }

            public void handler(Request request, BufferedOutputStream outputStream) {
                String resp = getMessages();
                PrintStream ps = new PrintStream(outputStream);
                ps.printf("HTTP/1.1 %s %s%n", 200, "OK")
                        .printf("Content-Type: %s%n", "messages")
                        .printf("Content-Length:%s%n%n", resp.getBytes().length)
                        .flush();
            }
        });

        server.start(9999);
    }

    public static String getMessages() {
        return "[]";
    }
}