import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class Db {
    static final String URL = "jdbc:sqlite:checks.db";

    static void init() throws Exception {
        try (Connection c = DriverManager.getConnection(URL);
             Statement s = c.createStatement()) {
            s.execute("CREATE TABLE IF NOT EXISTS checks ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "text TEXT NOT NULL, "
                + "score INTEGER NOT NULL, "
                + "verdict TEXT NOT NULL, "
                + "created_at TEXT DEFAULT CURRENT_TIMESTAMP)");
        }
    }

    static void save(String text, int score, String verdict) throws Exception {
        try (Connection c = DriverManager.getConnection(URL);
             PreparedStatement p = c.prepareStatement(
                 "INSERT INTO checks (text, score, verdict) VALUES (?, ?, ?)")) {
            p.setString(1, text);
            p.setInt(2, score);
            p.setString(3, verdict);
            p.executeUpdate();
        }
    }

    public static void main(String[] args) throws Exception {
        init();

        try (Connection c = DriverManager.getConnection(URL);
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery(
                 "SELECT id, text, score, verdict, created_at FROM checks")) {
            while (rs.next()) {
                System.out.println(rs.getInt("id") + " | " + rs.getString("text")
                    + " | " + rs.getInt("score") + " | " + rs.getString("verdict")
                    + " | " + rs.getString("created_at"));
            }
        }
    }
}