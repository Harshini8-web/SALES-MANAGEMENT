package shared.integration;

import com.erp.sdk.subsystem.SalesManagement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class BISalesIntegrationServiceImpl implements SalesIntegrationService {
    
    private static final Logger logger = Logger.getLogger(BISalesIntegrationServiceImpl.class.getName());
    private final SalesManagement erpClient;

    public BISalesIntegrationServiceImpl(SalesManagement erpClient) {
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

            // "sales_lead" is the ERP user specified in the BI document
            long newId = erpClient.create("sales_records", record, "sales_lead");
            logger.info("Successfully published sale to BI. Record ID: " + newId);

        } catch (Exception e) {
            // Graceful Degradation: Do not throw the error. Just log it so the system doesn't crash.
            logger.warning("Failed to publish sale to BI database. Error: " + e.getMessage());
        }
    }
}