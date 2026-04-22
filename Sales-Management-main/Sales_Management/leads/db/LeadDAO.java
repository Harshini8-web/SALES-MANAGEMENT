package leads.db;

import leads.exception.LeadException;
import leads.model.Lead;
import com.likeseca.erp.database.facade.ErpDatabaseFacade;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LeadDAO {

    private ErpDatabaseFacade facade;

    public LeadDAO() {
        initializeFacade();
    }

    private void initializeFacade() {
        try {
            this.facade = new ErpDatabaseFacade();
        } catch (Throwable e) {
            System.err.println("Failed to initialize ErpDatabaseFacade in LeadDAO: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void createLead(Lead lead) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("name", lead.getName());
            // REMOVED: data.put("company", lead.getCompany()); -> Sales lacks permission
            data.put("status", lead.getStatus());

            Object result = facade.salesManagementSubsystem().create("leads", data);
            lead.setLeadId(((Number) result).intValue());
        } catch (Exception e) {
            throw new LeadException.LeadCreationFailed("Database Facade error: " + e.getMessage(), e);
        }
    }

    public Lead getLeadById(int leadId) {
        try {
            Map<String, Object> rs = facade.salesManagementSubsystem().readById("leads", "lead_id", leadId);
            if (rs != null && !rs.isEmpty()) {
                return mapRowToLead(rs);
            }
        } catch (Exception e) {
            throw new LeadException.LeadCreationFailed("DB Facade error retrieving lead", e);
        }
        throw new LeadException.LeadNotFound(leadId);
    }

    public List<Lead> getAllLeads() {
        List<Lead> results = new ArrayList<>();
        try {
            List<Map<String, Object>> rows = facade.salesManagementSubsystem().readAll("leads", new HashMap<>());
            if (rows != null) {
                for (Map<String, Object> row : rows) {
                    results.add(mapRowToLead(row));
                }
            }
        } catch (Exception e) {
            throw new LeadException.LeadCreationFailed("DB Facade error retrieving all leads", e);
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
            
            facade.salesManagementSubsystem().update("leads", "lead_id", leadId, payload);
            return true;
        } catch (Exception e) {
            System.err.println("Error updating lead status via Facade: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteLead(int leadId) {
        try {
            facade.salesManagementSubsystem().delete("leads", "lead_id", leadId);
            return true;
        } catch (Exception e) {
            System.err.println("Error deleting lead via Facade: " + e.getMessage());
            return false;
        }
    }

    private Lead mapRowToLead(Map<String, Object> row) {
        Lead lead = new Lead();
        lead.setLeadId(((Number) row.get("lead_id")).intValue());
        lead.setName((String) row.get("name"));
        lead.setCompany((String) row.get("company"));
        lead.setStatus((String) row.get("status"));
        
        Object createdAtObj = row.get("created_at");
        if (createdAtObj instanceof Timestamp) {
            lead.setCreatedAt(((Timestamp) createdAtObj).toLocalDateTime());
        }
        
        return lead;
    }
}