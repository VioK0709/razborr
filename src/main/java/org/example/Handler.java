package org.example;

import java.io.BufferedOutputStream;

@FunctionalInterface  
public interface Handler {
    void handler(Request request, BufferedOutputStream outputStream);
}
