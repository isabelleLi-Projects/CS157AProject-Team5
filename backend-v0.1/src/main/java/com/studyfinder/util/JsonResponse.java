package com.studyfinder.util;

import java.io.IOException;

import com.studyfinder.model.SessionUser;

import javax.servlet.http.HttpServletResponse;

/**
 * Writes small JSON responses without needing a JSON library.
 * Every string passes through quote(), which escapes it safely.
 */
public final class JsonResponse {

    private JsonResponse() {
    }

    public static void send(HttpServletResponse response, int status, String json) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        // Account data should never be cached by the browser or a proxy.
        response.setHeader("Cache-Control", "no-store");
        response.getWriter().write(json);
    }

    /** {"ok":true,"user":{...}} */
    public static void sendUser(HttpServletResponse response, int status, SessionUser user) throws IOException {
        send(response, status, "{\"ok\":true,\"user\":" + user(user) + "}");
    }

    /** {"ok":false,"error":"..."} */
    public static void error(HttpServletResponse response, int status, String message) throws IOException {
        send(response, status, "{\"ok\":false,\"error\":" + quote(message) + "}");
    }

    /** {"ok":false,"error":"...","field":"email"}, so the page can show the message under the right input. */
    public static void fieldError(HttpServletResponse response, int status, String field, String message)
            throws IOException {
        send(response, status, "{\"ok\":false,\"error\":" + quote(message) + ",\"field\":" + quote(field) + "}");
    }

    public static String user(SessionUser user) {
        return "{\"id\":" + (user.id() == null ? "null" : user.id())
                + ",\"name\":" + quote(user.name())
                + ",\"email\":" + quote(user.email())
                + ",\"role\":" + quote(user.role())
                + "}";
    }

    public static String quote(String value) {
        if (value == null) {
            return "null";
        }
        StringBuilder out = new StringBuilder(value.length() + 2).append('"');
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                case '\b' -> out.append("\\b");
                case '\f' -> out.append("\\f");
                case '<' -> out.append("\\u003c");
                case '>' -> out.append("\\u003e");
                case '&' -> out.append("\\u0026");
                default -> {
                    if (c < 0x20) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
                }
            }
        }
        return out.append('"').toString();
    }
}
