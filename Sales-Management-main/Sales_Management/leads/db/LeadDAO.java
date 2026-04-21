package leads.db;

import leads.exception.LeadException;
import leads.model.Lead;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class LeadDAO {

    private Connection connection;

    public LeadDAO() {
        // Don't connect yet - wait until first use
    }

    private void ensureConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Properties props = new Properties();
                props.load(Files.newInputStream(Paths.get("application-rds-template.properties")));

                String host = props.getProperty("db.host");
                String port = props.getProperty("db.port");
                String dbName = props.getProperty("db.name");
                String username = props.getProperty("db.username");
                String password = props.getProperty("db.password");

                String url = "jdbc:mysql://" + host + ":" + port + "/" + dbName;
                this.connection = DriverManager.getConnection(url, username, password);

                // Create table if not exists
                createTableIfNotExists();
            } catch (Exception e) {
                throw new SQLException("Failed to initialize database connection", e);
            }
        }
    }

    private void createTableIfNotExists() throws SQLException {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS leads (" +
            "lead_id INT PRIMARY KEY AUTO_INCREMENT, " +
            "name VARCHAR(100) NOT NULL, " +
            "company VARCHAR(100), " +
            "status VARCHAR(50), " +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ")";
        try (PreparedStatement stmt = connection.prepareStatement(createTableSQL)) {
            stmt.executeUpdate();
        }
        // Ensure columns exist
        String[] alterSQLs = {
            "ALTER TABLE leads ADD COLUMN IF NOT EXISTS company VARCHAR(100)",
            "ALTER TABLE leads ADD COLUMN IF NOT EXISTS status VARCHAR(50)",
            "ALTER TABLE leads ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
        };
        for (String sql : alterSQLs) {
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.executeUpdate();
            } catch (SQLException e) {
                // Column might already exist or other issue, ignore
            }
        }
    }

    public void createLead(Lead lead) {
        String sql = "INSERT INTO leads (name, company, status) VALUES (?, ?, ?)";
        try {
            ensureConnection();
            try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, lead.getName());
                stmt.setString(2, lead.getCompany());
                stmt.setString(3, lead.getStatus());
                stmt.executeUpdate();
                
                // Get the auto-generated lead_id
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        lead.setLeadId(generatedKeys.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            throw new LeadException.LeadCreationFailed("Database error: " + e.getMessage(), e);
        }
    }

    public Lead getLeadById(int leadId) {
        String sql = "SELECT lead_id, name, company, status, created_at FROM leads WHERE lead_id = ?";
        try {
            ensureConnection();
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, leadId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return mapRowToLead(rs);
                    }
                }
            }
        } catch (SQLException e) {
            throw new LeadException.LeadCreationFailed("DB error retrieving lead", e);
        }
        throw new LeadException.LeadNotFound(leadId);
    }

    public List<Lead> getAllLeads() {
        List<Lead> results = new ArrayList<>();
        String sql = "SELECT lead_id, name, company, status, created_at FROM leads";
        try {
            ensureConnection();
            try (PreparedStatement stmt = connection.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRowToLead(rs));
                }
            }
        } catch (SQLException e) {
            throw new LeadException.LeadCreationFailed("DB error retrieving all leads", e);
        }
        return results;
    }

    public List<Lead> getLeadsByStatus(String status) {
        return getAllLeads().stream()
                .filter(lead -> status.equals(lead.getStatus()))
                .collect(Collectors.toList());
    }

    public boolean updateLeadStatus(int leadId, String newStatus) {
        String sql = "UPDATE leads SET status = ? WHERE lead_id = ?";
        try {
            ensureConnection();
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, newStatus);
                stmt.setInt(2, leadId);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean deleteLead(int leadId) {
        String sql = "DELETE FROM leads WHERE lead_id = ?";
        try {
            ensureConnection();
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, leadId);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            return false;
        }
    }

    private Lead mapRowToLead(ResultSet rs) throws SQLException {
        Lead lead = new Lead();
        lead.setLeadId(rs.getInt("lead_id"));
        lead.setName(rs.getString("name"));
        lead.setCompany(rs.getString("company"));
        lead.setStatus(rs.getString("status"));
        lead.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return lead;
    }
}