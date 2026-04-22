import com.likeseca.erp.database.facade.ErpDatabaseFacade;
import com.designx.erp.gateway.SdkSalesQuoteGateway;
import com.designx.erp.gateway.QuoteDetails;

public class TestOrderIntegration {
    public static void main(String[] args) {
        System.out.println("--- Starting Integration Test ---");

        try {
            // 1. Initialize the new Facade Database Connection
            ErpDatabaseFacade facade = new ErpDatabaseFacade();

            // 2. Initialize your updated Gateway (we removed the "test_user" argument
            // previously)
            SdkSalesQuoteGateway gateway = new SdkSalesQuoteGateway(facade);

            // 3. Define a Quote ID that you KNOW exists in your local testing database
            int testQuoteId = 3; // Make sure this matches a Quote ID you recently created!

            System.out.println("Fetching details for Quote ID: " + testQuoteId + "...");
            QuoteDetails details = gateway.getQuoteDetails(testQuoteId);

            // 4. Verify the Results
            if (details != null) {
                System.out.println("✅ INTEGRATION SUCCESSFUL! Retrieved Quote Details:");
                System.out.println("   - Quote ID: " + details.getQuoteId());
                System.out.println("   - Customer ID: " + details.getCustomerId());
                System.out.println("   - Customer Name: " + details.getCustomerName());
                System.out.println("   - Vehicle Model/Product: " + details.getVehicleModel());
                System.out.println("   - Order Value: $" + details.getOrderValue());
            } else {
                System.out.println("⚠️ Quote found, but returned null. Make sure Quote ID " + testQuoteId
                        + " actually exists in the DB.");
            }

        } catch (Exception e) {
            System.err.println("❌ INTEGRATION FAILED with an exception:");
            e.printStackTrace();
        }
    }
}