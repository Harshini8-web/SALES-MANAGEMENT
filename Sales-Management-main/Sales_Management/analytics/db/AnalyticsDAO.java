package analytics.db;

import com.erp.sdk.config.DatabaseConfig;
import com.erp.sdk.factory.SubsystemFactory;
import com.erp.sdk.subsystem.SubsystemName;
import com.erp.sdk.subsystem.AbstractSubsystem;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnalyticsDAO {

    private AbstractSubsystem facade;

    public AnalyticsDAO() {
        try {
            DatabaseConfig dbConfig = DatabaseConfig.fromProperties(Paths.get("application-rds-template.properties"));
            this.facade = (AbstractSubsystem) SubsystemFactory.create(SubsystemName.SALES_MANAGEMENT, dbConfig);
        } catch (Exception e) {
            System.err.println("Failed to initialize SDK in AnalyticsDAO");
        }
    }

    public void generateForecastReport() {
        try {
            List<Map<String, Object>> allDeals = facade.readAll("deals", new HashMap<>(), "integration_lead");
            
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
            List<Map<String, Object>> allDeals = facade.readAll("deals", new HashMap<>(), "integration_lead");
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
            List<Map<String, Object>> allLeads = facade.readAll("leads", new HashMap<>(), "integration_lead");
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