package org.example;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.BufferedReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Request {

    private final BufferedReader in;
    private Map<String, String> queryParameters;
    private String method;
    private String path;
    private Map<String, String> headers;
    private String body;

    private List<NameValuePair> queryParams;

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Request(BufferedReader in) {
        this.in = in;
        queryParameters = new HashMap<>();
        headers = new HashMap<>();
    }

    public String getQueryParam(String name) {
        if (queryParams.isEmpty()) {
            return null;
        }
        for (NameValuePair param : queryParams) {
            if (name.equals(param.getName())) {
                System.out.println("Параметры запроса для " + name + ": " + param.getValue());
            }
        }
        System.out.println("Нет такого параметра запроса");
        return null;
    }


    public List<NameValuePair> getQueryParams() {
        for (NameValuePair param : queryParams) {
            System.out.println("Название запроса: " + param.getName() + ", параметры запроса: " + param.getValue());
        }
        return queryParams;
    }

    public void setQueryParams(List<NameValuePair> queryParams) {
        this.queryParams = queryParams;
    }


    public List<NameValuePair> getBodyParams() {
        return URLEncodedUtils.parse(body, Charset.forName("UTF-8"));
    }
}