package org.example;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.PrintStream;


public class Main {
    public static void main(String[] args){
        Server server = new Server();
        initializeHandlers(server);
    }

    private static void initializeHandlers(Server server) {
        server.addHandler("GET", "/messages",(request, out) -> {
           sendResponse(out,"Отправка GET /message");
     });

        server.addHandler("POST", "/messages", (request, outputStream) -> {
                String resp = getMessages();
                PrintStream ps = new PrintStream(outputStream);
                ps.printf("HTTP/1.1 %s %s%n", 200, "OK")
                        .printf("Content-Type: %s%n", "index.html")
                        .printf("Content-Length:%s%n%n", resp.getBytes().length)
                        .flush();
        });
        server.start(9999);
    }

    private static void sendResponse(BufferedOutputStream out, String response) throws IOException {
        out.write((
                "HTTP/1.1 200 OK\r\n" +
                        "Content-Length: " + response.length() + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.write(response.getBytes());
        out.flush();
    }
    public static String getMessages() {
        return "[]";
    }
}