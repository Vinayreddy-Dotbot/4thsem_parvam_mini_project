package migration;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DbMigration {
    private static final String SERVER_URL = "jdbc:mysql://localhost:3306/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "";
    private static final String SQL_FILE_PATH = "db/db_setup.sql";

    public static void main(String[] args) {
        System.out.println("====================================================");
        System.out.println("STARTING HOSPITAL PATIENT DATABASE MIGRATION");
        System.out.println("====================================================");

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            try (Connection conn = DriverManager.getConnection(SERVER_URL, USER, PASSWORD);
                 Statement stmt = conn.createStatement()) {
                System.out.println("[CONNECTED] XAMPP MySQL/MariaDB server is active.");

                String[] queries = readSqlFile().split(";");
                for (String query : queries) {
                    String cleaned = query.trim();
                    if (!cleaned.isEmpty()) {
                        System.out.println("[EXECUTING] " + cleaned);
                        stmt.execute(cleaned);
                    }
                }

                System.out.println("[DONE] hospital_patient_db is ready.");
            }
        } catch (Exception e) {
            System.err.println("[FAILED] Database migration failed.");
            e.printStackTrace();
        }
    }

    private static String readSqlFile() throws Exception {
        StringBuilder sqlContent = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(SQL_FILE_PATH))) {
            String line;
            while ((line = br.readLine()) != null) {
                int commentIdx = line.indexOf("--");
                if (commentIdx != -1) {
                    line = line.substring(0, commentIdx);
                }
                if (!line.trim().isEmpty()) {
                    sqlContent.append(line.trim()).append(" ");
                }
            }
        }
        return sqlContent.toString();
    }
}
