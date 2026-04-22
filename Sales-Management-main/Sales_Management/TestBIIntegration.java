import com.likeseca.erp.database.facade.ErpDatabaseFacade;
import shared.integration.BISalesIntegrationServiceImpl;

public class TestBIIntegration {
    public static void main(String[] args) {
        System.out.println("--- Starting BI Integration Test ---");
        try {
            // 1. Initialize the Facade
            ErpDatabaseFacade facade = new ErpDatabaseFacade();

            // 2. Initialize the BI Integration Service
            BISalesIntegrationServiceImpl biService = new BISalesIntegrationServiceImpl(facade);

            // 3. Send a dummy record
            System.out.println("Publishing test sale to BI...");
            biService.publishSaleToBI("Test_Vehicle_Model", 5, 150000.0, "TEST_DEALER", "Test_Region", "Q1-Test");

            System.out.println("Test execution complete. Please check logs and database.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}