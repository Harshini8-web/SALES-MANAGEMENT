import com.erp.sdk.config.DatabaseConfig;
import com.erp.sdk.factory.SubsystemFactory;
import com.erp.sdk.subsystem.SubsystemName;
import com.erp.sdk.subsystem.SalesManagement;
import com.designx.erp.gateway.SdkSalesQuoteGateway;
import com.designx.erp.gateway.QuoteDetails;
import java.nio.file.Paths;

public class TestOrderIntegration {
    public static void main(String[] args) {
        System.out.println("--- Starting Integration Test ---");

        try {
            // 1. Initialize the SDK Database Connection
            DatabaseConfig dbConfig = DatabaseConfig.fromProperties(Paths.get("application-rds-template.properties"));

            // Use the correct initialization method for your SDK here
            SalesManagement salesSubsystem = (SalesManagement) SubsystemFactory.create(SubsystemName.SALES_MANAGEMENT,
                    dbConfig);

            // 2. Initialize your newly created Gateway
            // Note: Replace "test_user" with whatever username string the SDK expects
            SdkSalesQuoteGateway gateway = new SdkSalesQuoteGateway(salesSubsystem, "test_user");

            // 3. Define a Quote ID that you KNOW exists in your database
            int testQuoteId = 10; // Change this to a valid ID from your 'quotes' table

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