package deals.db;

import deals.exception.DealException.*;
import deals.model.Deal;
import com.likeseca.erp.database.facade.ErpDatabaseFacade;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DealDAO {

    private ErpDatabaseFacade facade;

    public DealDAO() {
        initializeFacade();
    }

    private void initializeFacade() {
        try {
            this.facade = new ErpDatabaseFacade();
        } catch (Throwable e) {
            System.err.println("Failed to initialize ErpDatabaseFacade in DealDAO: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void addDeal(Deal deal) throws DealCreationFailed {
        createDeal(deal);
    }

    public void createDeal(Deal deal) throws DealCreationFailed {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("customer_id", deal.getCustomerId());
            data.put("amount", deal.getAmount());
            data.put("stage", deal.getStage());
            data.put("status", deal.getStatus());

            Object result = facade.salesManagementSubsystem().create("deals", data);
            deal.setDealId(((Number) result).intValue());
        } catch (Exception e) {
            throw new DealCreationFailed("Failed to save deal to database via Facade: " + e.getMessage());
        }
    }

    public Deal getDeal(int dealId) throws DealNotFound {
        try {
            Map<String, Object> rs = facade.salesManagementSubsystem().readById("deals", "deal_id", dealId);
            if (rs != null && !rs.isEmpty()) {
                return mapRowToDeal(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new DealNotFound("Deal ID " + dealId + " not found.");
    }

    public Deal getDealById(int dealId) throws DealNotFound {
        return getDeal(dealId);
    }

    public List<Deal> getAllDeals() {
        List<Deal> results = new ArrayList<>();
        try {
            List<Map<String, Object>> rows = facade.salesManagementSubsystem().readAll("deals", new HashMap<>());
            if (rows != null) {
                for (Map<String, Object> row : rows) {
                    results.add(mapRowToDeal(row));
                }
            }
        } catch (Exception e) {
            System.err.println("Error retrieving deals via Facade: " + e.getMessage());
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
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("stage", newStage);
            payload.put("status", newStatus);
            
            facade.salesManagementSubsystem().update("deals", "deal_id", dealId, payload);
            return true;
        } catch (Exception e) {
            System.err.println("Error updating deal stage: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteDeal(int dealId) {
        try {
            facade.salesManagementSubsystem().delete("deals", "deal_id", dealId);
            return true;
        } catch (Exception e) {
            System.err.println("Error deleting deal: " + e.getMessage());
            return false;
        }
    }

    private Deal mapRowToDeal(Map<String, Object> row) {
        Deal deal = new Deal();
        deal.setDealId(((Number) row.get("deal_id")).intValue());
        deal.setCustomerId(((Number) row.get("customer_id")).intValue());
        
        Object amountObj = row.get("amount");
        if (amountObj instanceof Number) {
            deal.setAmount(((Number) amountObj).doubleValue());
        }
        
        deal.setStage((String) row.get("stage"));
        deal.setStatus((String) row.get("status"));
        return deal;
    }
}