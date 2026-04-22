package shared.integration;

import com.likeseca.erp.database.facade.ErpDatabaseFacade;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.time.LocalDateTime;

public class BISalesIntegrationServiceImpl implements SalesIntegrationService {
    
    private static final Logger logger = Logger.getLogger(BISalesIntegrationServiceImpl.class.getName());
    private final ErpDatabaseFacade facade;

    public BISalesIntegrationServiceImpl(ErpDatabaseFacade facade) {
        this.facade = facade;
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
            record.put("created_at", LocalDateTime.now().toString());

            // Use the Sales Management subsystem identity to CREATE the record
            Object newId = facade.salesManagementSubsystem().create("sales_records", record);
            logger.info("Successfully published sale to BI. Record ID: " + newId);

        } catch (Exception e) {
            logger.warning("Failed to publish sale to BI database. Error: " + e.getMessage());
        }
    }
}