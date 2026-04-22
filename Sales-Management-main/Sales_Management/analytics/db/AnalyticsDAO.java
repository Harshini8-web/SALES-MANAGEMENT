package analytics.db;

import com.likeseca.erp.database.facade.ErpDatabaseFacade;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnalyticsDAO {

    private ErpDatabaseFacade facade;

    public AnalyticsDAO() {
        try {
            this.facade = new ErpDatabaseFacade();
        } catch (Exception e) {
            System.err.println("Failed to initialize ErpDatabaseFacade in AnalyticsDAO: " + e.getMessage());
        }
    }

    public void generateForecastReport() {
        try {
            List<Map<String, Object>> allDeals = facade.salesManagementSubsystem().readAll("deals", new HashMap<>());
            
            double totalPipelineValue = 0;
            
            if (allDeals != null) {
                for (Map<String, Object> row : allDeals) {
                    if (row.get("amount") instanceof Number) {
                        totalPipelineValue += ((Number) row.get("amount")).doubleValue();
                    }
                }
            }
            
            System.out.println("-------------------------------------------------");
            System.out.println("ANALYTICS: Total System Pipeline Forecast: $" + totalPipelineValue);
            System.out.println("-------------------------------------------------");
            
        } catch (Exception e) {
            System.err.println("Failed to pull analytics data from the live server.");
            e.printStackTrace();
        }
    }

    public double calculateTotalRevenue() {
        double totalRevenue = 0;
        try {
            List<Map<String, Object>> allDeals = facade.salesManagementSubsystem().readAll("deals", new HashMap<>());
            if (allDeals != null) {
                for (Map<String, Object> deal : allDeals) {
                    if (deal.get("amount") instanceof Number) {
                        totalRevenue += ((Number) deal.get("amount")).doubleValue();
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Database error while calculating revenue.");
        }
        return totalRevenue;
    }

    public int getActiveLeadsCount() {
        int activeCount = 0;
        try {
            List<Map<String, Object>> allLeads = facade.salesManagementSubsystem().readAll("leads", new HashMap<>());
            if (allLeads != null) {
                for (Map<String, Object> lead : allLeads) {
                    String status = (String) lead.get("status");
                    if (status != null && !status.equalsIgnoreCase("LOST") && !status.equalsIgnoreCase("CONVERTED")) {
                        activeCount++;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Database error while counting leads.");
        }
        return activeCount;
    }
}