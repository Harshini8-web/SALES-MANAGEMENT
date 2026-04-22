package leads.db;

import leads.exception.LeadException;
import leads.model.Lead;
import com.erp.sdk.config.DatabaseConfig;
import com.erp.sdk.factory.SubsystemFactory;
import com.erp.sdk.subsystem.SubsystemName;
import com.erp.sdk.subsystem.AbstractSubsystem;

import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LeadDAO {

    private AbstractSubsystem facade;
    private static final String USER = "sales_lead";

    public LeadDAO() {
        try {
            DatabaseConfig dbConfig = DatabaseConfig.fromProperties(Paths.get("application-rds-template.properties"));
            this.facade = (AbstractSubsystem) SubsystemFactory.create(SubsystemName.SALES_MANAGEMENT, dbConfig);
        } catch (Exception e) {
            System.err.println("Failed to initialize SDK in LeadDAO");
        }
    }

    public void createLead(Lead lead) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("name", lead.getName());
            data.put("company", lead.getCompany());
            data.put("status", lead.getStatus());

            long newId = facade.create("leads", data, USER);
            lead.setLeadId((int) newId);
        } catch (Exception e) {
            throw new LeadException.LeadCreationFailed("Database SDK error: " + e.getMessage(), e);
        }
    }

    public Lead getLeadById(int leadId) {
        try {
            Map<String, Object> rs = facade.readById("leads", "lead_id", leadId, USER);
            if (rs != null && !rs.isEmpty()) {
                return mapRowToLead(rs);
            }
        } catch (Exception e) {
            throw new LeadException.LeadCreationFailed("DB SDK error retrieving lead", e);
        }
        throw new LeadException.LeadNotFound(leadId);
    }

    public List<Lead> getAllLeads() {
        List<Lead> results = new ArrayList<>();
        try {
            List<Map<String, Object>> rows = facade.readAll("leads", new HashMap<>(), USER);
            if (rows != null) {
                for (Map<String, Object> row : rows) {
                    results.add(mapRowToLead(row));
                }
            }
        } catch (Exception e) {
            throw new LeadException.LeadCreationFailed("DB SDK error retrieving all leads", e);
        }
        return results;
    }

    public List<Lead> getLeadsByStatus(String status) {
        return getAllLeads().stream()
            .filter(lead -> status.equals(lead.getStatus()))
            .collect(Collectors.toList());
    }

    public boolean updateLeadStatus(int leadId, String newStatus) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("status", newStatus);
            
            facade.update("leads", "lead_id", leadId, payload, USER);
            return true;
        } catch (Exception e) {
            System.err.println("Error updating lead status via SDK: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteLead(int leadId) {
        try {
            facade.delete("leads", "lead_id", leadId, USER);
            return true;
        } catch (Exception e) {
            System.err.println("Error deleting lead via SDK: " + e.getMessage());
            return false;
        }
    }

    private Lead mapRowToLead(Map<String, Object> row) {
        Lead lead = new Lead();
        lead.setLeadId(((Number) row.get("lead_id")).intValue());
        lead.setName((String) row.get("name"));
        lead.setCompany((String) row.get("company"));
        lead.setStatus((String) row.get("status"));
        
        // Handle timestamps safely from the map
        Object createdAtObj = row.get("created_at");
        if (createdAtObj instanceof Timestamp) {
            lead.setCreatedAt(((Timestamp) createdAtObj).toLocalDateTime());
        }
        
        return lead;
    }
}