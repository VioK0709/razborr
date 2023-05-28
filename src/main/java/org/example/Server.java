package org.example;

import org.apache.http.NameValuePair;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Handler;


public class Server {
    public static final String GET = "GET";
    public static final String POST = "POST";
    private static final String FORM_URLENCODED = "application/x-www-form-urlencoded";
    static ExecutorService executeIt = Executors.newFixedThreadPool(64);

    final List<String> validPaths = List.of("/index.html", "/spring.png", "/spring.svg", "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");

    private final ConcurrentHashMap<String, ConcurrentHashMap<String, Handler>> handlers = new ConcurrentHashMap<>();

    private void connectionProcessing(Socket socket) throws URISyntaxException {
        final var allowedMethods = List.of(GET, POST);
        try (socket; final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             final var out = new BufferedOutputStream(socket.getOutputStream())) {
            final var limit = 4096;
            in.mark(limit);
            final var buffer = new byte[limit];
            final var read = in.read();

            var requestLineEnd = 0;
            final var requestLine = new String(Arrays.copyOf(buffer, requestLineEnd)).split(" ");
            final var requestLineDelimiter = new byte[]{'\r', '\n'};
            requestLineEnd = indexOf(buffer, requestLineDelimiter, 0, read);
            if (requestLine.length != 3) {
                badRequest(out);
                socket.close();
                return;
            }

            final String method = requestLine[0];
            final var path = requestLine[1];
            if (!path.startsWith("/")) {
                badRequest(out);
                socket.close();
                return;
            }
            Request request = new Request(in);
            request.setMethod(method);
            request.setPath(path);

            final var headersDelimiter = new byte[]{'\r', '\n', '\r', '\n'};
            final var headersStart = requestLineEnd + requestLineDelimiter.length;
            final var headersEnd = indexOf(buffer, headersDelimiter, headersStart, read);
            if (headersEnd == -1) {
                badRequest(out);
                return;
            }

            in.reset();
            in.skip(headersStart);

            final var headersBytes = in.read(CharBuffer.allocate(headersEnd - headersStart));
            final var headers = Arrays.asList(new String(String.valueOf(headersBytes)).split("\r\n"));
            System.out.println(headers);

            if (!method.equals(GET)) {
                in.skip(headersDelimiter.length);
                final var contentLength = extractHeader(headers, "Content-Length");
                if (contentLength.isPresent()) {
                    final var length = Integer.parseInt(contentLength.get());
                    final var bodyBytes = in.read(CharBuffer.allocate(length));
                    final var body = new String(String.valueOf(bodyBytes));
                    System.out.println(body);
                    Optional<String> contentType = extractHeader(headers, "Content-Length");
                    if (contentType.isPresent() && contentType.get().equals(FORM_URLENCODED)) {
                        System.out.println("Параметры из тела: ");
                        List<NameValuePair> bodyParams = request.getQueryParams();
                        for (NameValuePair pair : bodyParams) {
                            System.out.println(pair.getName() + ": " + pair.getValue());
                        }
                    }
                }
            }
            if (!validPaths.contains(path)) {
                out.write((
                        "HTTP/1.1 404 Not Found\r\n" +
                                "Content-Length: 0\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                out.flush();
                return;
            }

            final var filePath = Path.of(".", "public", path);
            final var mimeType = Files.probeContentType(filePath);

            if (path.equals("/classic.html")) {
                final var template = Files.readString(filePath);
                final var content = template.replace(
                        "{time}",
                        LocalDateTime.now().toString()
                ).getBytes();
                out.write((
                        "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: " + mimeType + "\r\n" +
                                "Content-Length: " + content.length + "\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                out.write(content);
                out.flush();
                return;
            }
            final var length = Files.size(filePath);
            out.write((
                    "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: " + mimeType + "\r\n" +
                            "Content-Length: " + length + "\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
            Files.copy(filePath, out);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String url = null;
        int i = url.indexOf("?");
        if (i == -1) {
            System.out.println(url);
        }
        String result = url.substring(0, i);
        System.out.println(result);
    }

    public void start(int port) {
        try (final var serverSocket = new ServerSocket(port)) {
            System.out.println("Server started on port " + port);
            while (true) {
                final var socket = serverSocket.accept();
                executeIt.submit(() -> {
                    try {
                        connectionProcessing(socket);
                    } catch (URISyntaxException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addHandler(String method, String path, Handler handler) {
        if (!handlers.contains(method)) {
            handlers.put(method, new ConcurrentHashMap<>());
        }
        handlers.get(method).put(path, handler);
    }


    private static int indexOf(byte[] array, byte[] target, int start, int max) {
        outer:
        for (int i = start; i < max - target.length + 1; i++) {
            for (int j = 0; j < target.length; j++) {
                if (array[i + j] != target[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }

    private static void badRequest(BufferedOutputStream out) throws IOException {
        out.write((
                "HTTP/1.1 400 Bad Request\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.flush();
    }

    private static Optional<String> extractHeader(List<String> headers, String header) {
        return headers.stream()
                .filter(o -> o.startsWith(header))
                .map(o -> o.substring(o.indexOf(" ")))
                .map(String::trim)
                .findFirst();
    }
}