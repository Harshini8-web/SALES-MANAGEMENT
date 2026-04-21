import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class SetupDatabase {
    public static void main(String[] args) {
        try {
            // Load properties
            Properties props = new Properties();
            props.load(Files.newInputStream(Paths.get("application-rds-template.properties")));

            String host = props.getProperty("db.host");
            String port = props.getProperty("db.port");
            String dbName = props.getProperty("db.name");
            String username = props.getProperty("db.username");
            String password = props.getProperty("db.password");

            String url = "jdbc:mysql://" + host + ":" + port + "/" + dbName;

            // Connect
            Connection conn = DriverManager.getConnection(url, username, password);

            // Create leads table
            Statement stmt = conn.createStatement();
            
            String createLeadsTable = "CREATE TABLE IF NOT EXISTS leads (" +
                "lead_id INT PRIMARY KEY AUTO_INCREMENT, " +
                "name VARCHAR(100) NOT NULL, " +
                "company VARCHAR(100), " +
                "status VARCHAR(50), " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")";

            stmt.executeUpdate(createLeadsTable);
            System.out.println("✓ Leads table created successfully in " + dbName + " database!");

            // Ensure columns exist
            try {
                stmt.executeUpdate("ALTER TABLE leads ADD COLUMN company VARCHAR(100)");
                System.out.println("✓ Company column added");
            } catch (Exception e) {
                System.out.println("✓ Company column already exists");
            }

            try {
                stmt.executeUpdate("ALTER TABLE leads ADD COLUMN status VARCHAR(50)");
                System.out.println("✓ Status column added");
            } catch (Exception e) {
                System.out.println("✓ Status column already exists");
            }

            stmt.close();
            conn.close();

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
