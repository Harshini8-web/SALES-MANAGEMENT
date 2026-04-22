import customers.ui.CustomerUI;
import quotes.ui.QuoteUI;
import analytics.facade.AnalyticsCommandFactory;
import java.util.Scanner;

// --- 1. ADD THE SDK IMPORTS ---
import com.erp.sdk.config.DatabaseConfig;
import com.erp.sdk.factory.SubsystemFactory;
import com.erp.sdk.subsystem.SubsystemName;

import java.nio.file.Paths;

public class SalesManagementSystem {
    public static void main(String[] args) {

        System.out.println("=================================================");
        System.out.println("  ENTERPRISE SALES MANAGEMENT SYSTEM - STARTING  ");
        System.out.println("=================================================");

        // --- 2. BOOTSTRAP THE INTEGRATION SDK WITH RETRY LOGIC ---
        int maxRetries = 5;
        int retryDelay = 10000; // Start with 10 seconds
        boolean connected = false;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                System.out
                        .print("Connecting to live AWS RDS Database (Attempt " + attempt + "/" + maxRetries + ")... ");

                DatabaseConfig dbConfig = DatabaseConfig
                        .fromProperties(Paths.get("application-rds-template.properties"));
                // Create AND grab the instance at the same time
                com.erp.sdk.subsystem.SalesManagement salesSdk = (com.erp.sdk.subsystem.SalesManagement) SubsystemFactory
                        .create(SubsystemName.SALES_MANAGEMENT, dbConfig);

                System.out.println("SUCCESS!");
                connected = true;

                // Initialize their gateway
                com.designx.erp.gateway.SalesQuoteGateway orderGateway = new com.designx.erp.gateway.SdkSalesQuoteGateway(
                        salesSdk, "system_test_user");

                System.out.println("Order Team Gateway initialized successfully. Running test fetch...");

                // --- NEW FETCH TEST ---
                int testQuoteId = 1;
                com.designx.erp.gateway.QuoteDetails details = orderGateway.getQuoteDetails(testQuoteId);

                if (details != null) {
                    System.out.println("SUCCESS! Fetched Quote details via Order Gateway:");
                    System.out.println("  -> Quote ID: " + details.getQuoteId());
                    System.out.println("  -> Customer ID: " + details.getCustomerId());
                    System.out.println("  -> Customer Name: " + details.getCustomerName());
                    System.out.println("  -> Vehicle Model: " + details.getVehicleModel());
                    System.out.println("  -> Final Amount: $" + details.getOrderValue());
                } else {
                    System.out.println("Test Complete: Gateway is connected, but Quote ID " + testQuoteId
                            + " was not found in the database.");
                }

                // Break out of the retry loop after everything succeeds
                break;

            } catch (Exception e) {
                if (attempt < maxRetries) {
                    // Extract the core reason to print
                    String reason = (e.getCause() != null) ? e.getCause().getMessage() : e.getMessage();
                    System.out.println("FAILED (" + reason + ")");
                    System.out.println("Waiting " + (retryDelay / 1000) + " seconds before retry...");
                    try {
                        Thread.sleep(retryDelay);
                        retryDelay += 5000; // Increase delay by 5 seconds each time
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                } else {
                    System.err.println("FAILED! Cannot connect to the integration database.");
                    e.printStackTrace();
                }
            }
        }

        if (!connected) {
            System.err.println("FAILED! Could not connect to database after " + maxRetries + " attempts.");
            System.exit(1);
        }

        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        CustomerUI customerUI = new CustomerUI();
        LeadDealUI leadDealUI = new LeadDealUI();
        QuoteUI quoteUI = new QuoteUI();
        AnalyticsCommandFactory analyticsFactory = new AnalyticsCommandFactory();

        while (running) {
            System.out.println("\n--- MAIN SYSTEM MENU ---");
            System.out.println("1. Customer Management (Namratha)");
            System.out.println("2. Leads & Deals Management (Bhumika)");
            System.out.println("3. Quotes & Pricing (Dhatri)");
            System.out.println("4. System Analytics & Forecasting (Harshini)");
            System.out.println("5. Exit System");
            System.out.print("Enter choice: ");

            int choice = scanner.nextInt();

            switch (choice) {
                case 1:
                    customerUI.displayMenu();
                    break;
                case 2:
                    leadDealUI.displayMenu();
                    break;
                case 3:
                    quoteUI.displayMenu();
                    break;
                case 4:
                    System.out.println("\n--- ANALYTICS MENU ---");
                    System.out.println("1. Generate Full System Forecast Report");
                    System.out.print("Enter choice: ");
                    int analyticsChoice = scanner.nextInt();
                    analyticsFactory.executeCommand(analyticsChoice);
                    break;
                case 5:
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice.");
            }
        }
        scanner.close();
        System.exit(0);
    }
}