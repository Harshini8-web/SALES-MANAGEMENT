package deals.db;

import deals.exception.DealException.*;
import deals.model.Deal;

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

public class DealDAO {

    private Connection connection;

    public DealDAO() {
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
            } catch (Exception e) {
                throw new SQLException("Failed to initialize database connection", e);
            }
        }
    }

    public void addDeal(Deal deal) throws DealCreationFailed {
        createDeal(deal);
    }

    public void createDeal(Deal deal) throws DealCreationFailed {
        String sql = "INSERT INTO deals (customer_id, amount, stage, status) VALUES (?, ?, ?, ?)";
        try {
            ensureConnection();
            try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, deal.getCustomerId());
                stmt.setDouble(2, deal.getAmount());
                stmt.setString(3, deal.getStage());
                stmt.setString(4, deal.getStatus());
                stmt.executeUpdate();
                
                // Get the auto-generated deal_id
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        deal.setDealId(generatedKeys.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            throw new DealCreationFailed("Failed to save deal to database: " + e.getMessage());
        }
    }

    public Deal getDeal(int dealId) throws DealNotFound {
        String sql = "SELECT deal_id, customer_id, amount, stage, status, created_at FROM deals WHERE deal_id = ?";
        try {
            ensureConnection();
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, dealId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return mapRowToDeal(rs);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        throw new DealNotFound("Deal ID " + dealId + " not found.");
    }

    public Deal getDealById(int dealId) throws DealNotFound {
        return getDeal(dealId);
    }

    public List<Deal> getAllDeals() {
        List<Deal> results = new ArrayList<>();
        String sql = "SELECT deal_id, customer_id, amount, stage, status, created_at FROM deals";
        try {
            ensureConnection();
            try (PreparedStatement stmt = connection.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRowToDeal(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }

    public List<Deal> getDealsByCustomer(int customerId) {
        return getAllDeals().stream()
                .filter(deal -> deal.getCustomerId() == customerId)
                .collect(Collectors.toList());
    }

    public List<Deal> getDealsByStage(String stage) {
        return getAllDeals().stream()
                .filter(deal -> stage.equals(deal.getStage()))
                .collect(Collectors.toList());
    }

    public boolean updateDealStage(int dealId, String newStage, String newStatus) {
        String sql = "UPDATE deals SET stage = ?, status = ? WHERE deal_id = ?";
        try {
            ensureConnection();
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, newStage);
                stmt.setString(2, newStatus);
                stmt.setInt(3, dealId);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean deleteDeal(int dealId) {
        String sql = "DELETE FROM deals WHERE deal_id = ?";
        try {
            ensureConnection();
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, dealId);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            return false;
        }
    }

    private Deal mapRowToDeal(ResultSet rs) throws SQLException {
        Deal deal = new Deal();
        deal.setDealId(rs.getInt("deal_id"));
        deal.setCustomerId(rs.getInt("customer_id"));
        deal.setAmount(rs.getDouble("amount"));
        deal.setStage(rs.getString("stage"));
        deal.setStatus(rs.getString("status"));
        return deal;
    }
}