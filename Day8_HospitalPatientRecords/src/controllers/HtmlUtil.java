package controllers;

public class HtmlUtil {
    private HtmlUtil() {
    }

    public static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;");
    }

    public static String shortDateTime(String value) {
        if (value == null || value.length() < 16) {
            return escape(value);
        }
        return escape(value.substring(0, 16));
    }

    public static String toDateTimeLocal(String value) {
        if (value == null || value.trim().isEmpty() || value.length() < 16) {
            return "";
        }
        return escape(value.substring(0, 16).replace(" ", "T"));
    }
}
