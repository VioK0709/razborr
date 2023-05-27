package org.example;

import java.io.BufferedOutputStream;

@FunctionalInterface    //функциональный интерфейс только с 1 методеом обработки запроса и выддачи? выходного потока
public interface Handler {
    void handler(Request request, BufferedOutputStream outputStream);
}