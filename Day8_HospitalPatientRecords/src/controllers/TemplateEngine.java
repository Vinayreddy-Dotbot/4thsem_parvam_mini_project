// Organizes this class under the 'controllers' package namespace
package controllers;

// Imports the ServletContext interface to resolve relative paths inside the Tomcat container
import javax.servlet.ServletContext;
// Imports standard BufferedReader to read files efficiently line-by-line
import java.io.BufferedReader;
// Imports standard FileInputStream to open low-level read streams to physical HTML files
import java.io.FileInputStream;
// Imports standard InputStreamReader to decode file bytes into UTF-8 characters
import java.io.InputStreamReader;
// Imports IOException to handle file read and write anomalies safely
import java.io.IOException;

/**
 * 🎨 PARVAM TEMPLATING ENGINE
 * 🎯 GOAL: Dynamically stitch separate HTML views into the master layout at runtime.
 */
// Declares the public utility class TemplateEngine
public class TemplateEngine {

    /**
     * 🚀 RENDER WEB PAGE
     * Reads layout.html, loads the page-specific HTML, and infuses it in the layout.
     * 
     * @param context ServletContext to resolve real filesystem paths in Tomcat
     * @param pageHtmlName The name of the HTML file (e.g., "home.html")
     * @return Completed HTML string
     * @throws IOException if a file reading error occurs
     */
    // Declares the public static method render returning completed HTML and throwing IOException
    public static String render(ServletContext context, String pageHtmlName) throws IOException {
        
        // 1️⃣ Resolves absolute server filesystem path for the master layout.html template
        String layoutPath = context.getRealPath("/web/html/layout.html");
        
        // 1️⃣ Resolves absolute server filesystem path for the target views layout (e.g. home.html)
        String pagePath = context.getRealPath("/web/html/" + pageHtmlName);

        // 2️⃣ Reads the raw HTML content of the master layout file securely using UTF-8 encoding
        String layoutHtml = readPhysicalFile(layoutPath);
        
        // 2️⃣ Reads the raw HTML content of the targeted view template file securely using UTF-8 encoding
        String pageHtml = readPhysicalFile(pagePath);

        // 3️⃣ Injects the page-specific body content into the layout's dynamic placeholder tag and returns the result
        return layoutHtml.replace("{{CONTENT}}", pageHtml);
    } // Ends render method

    /**
     * 🚀 RENDER STANDALONE WEB PAGE
     * Reads a target view directly without wrapping it in layout.html.
     * 
     * @param context ServletContext to resolve real filesystem paths in Tomcat
     * @param pageHtmlName The name of the HTML file (e.g., "login.html")
     * @return Completed HTML string
     * @throws IOException if a file reading error occurs
     */
    public static String renderRaw(ServletContext context, String pageHtmlName) throws IOException {
        // Resolves absolute server filesystem path for the target template
        String pagePath = context.getRealPath("/web/html/" + pageHtmlName);
        // Reads and returns the raw file contents directly
        return readPhysicalFile(pagePath);
    }

    /**
     * 📖 READ PHYSICAL FILE UTILITY
     */
    // Declares the private utility method to read file contents from physical disk paths, throwing IOException
    private static String readPhysicalFile(String absolutePath) throws IOException {
        
        // Initializes StringBuilder to accumulate line contents efficiently
        StringBuilder sb = new StringBuilder();
        
        // Opens a try-with-resources statement to auto-manage BufferedReader, InputStreamReader and FileInputStream
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(absolutePath), "UTF-8"))) {
            
            // Declares a temporary string to hold each read line
            String line;
            
            // Reads line-by-line until the end of the file is reached
            while ((line = br.readLine()) != null) {
                
                // appends line contents followed by a standard newline character to preserve code formatting
                sb.append(line).append("\n");
            } // Ends while file loop
        } // Auto-closes file reader streams
        
        // Converts the accumulated character buffer into a standard String and returns it
        return sb.toString();
    } // Ends readPhysicalFile method
} // Ends TemplateEngine class declaration
