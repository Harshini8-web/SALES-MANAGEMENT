package shared.integration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class BISalesIntegrationServiceImpl implements SalesIntegrationService {
    
    private static final Logger logger = Logger.getLogger(BISalesIntegrationServiceImpl.class.getName());
    private final Object erpClient;

    public BISalesIntegrationServiceImpl(Object erpClient) {
        this.erpClient = erpClient;
    }

    @Override
    public void publishSaleToBI(String carModel, int unitsSold, double revenue, String dealerId, String region, String saleQuarter) {
        try {
            Map<String, Object> record = new HashMap<>();
            record.put("car_model", carModel);
            record.put("units_sold", unitsSold);
            record.put("revenue", revenue);
            record.put("sales_date", LocalDate.now().toString()); 
            record.put("dealer_id", dealerId);
            record.put("region", region);
            record.put("sale_quarter", saleQuarter);
            record.put("created_by", "SALES_MANAGEMENT");
            record.put("created_at", LocalDateTime.now().toString());

            // TODO: Replace with ErpDatabaseFacade when SDK is properly integrated
            logger.info("BI Integration: Sale record prepared - " + record.toString());

        } catch (Exception e) {
            // Graceful Degradation: Do not throw the error. Just log it so the system doesn't crash.
            logger.warning("Failed to publish sale to BI database. Error: " + e.getMessage());
        }
    }
}