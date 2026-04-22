
import customers.ui.CustomerUI;
import quotes.ui.QuoteUI;
import analytics.facade.AnalyticsCommandFactory;
import java.util.Scanner;

// --- 1. ADD THE SDK IMPORTS ---
import com.erp.sdk.config.DatabaseConfig;
import com.erp.sdk.factory.SubsystemFactory;
import com.erp.sdk.subsystem.SubsystemName;

import java.nio.file.Paths; // This is required to convert the String to a Path
// Note: Your IDE might suggest auto-importing SubsystemName from com.erp.sdk.factory or com.erp.sdk.subsystem. 
// Accept whichever one is inside their JAR!


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
                System.out.print("Connecting to live AWS RDS Database (Attempt " + attempt + "/" + maxRetries + ")... ");
                
                DatabaseConfig dbConfig = DatabaseConfig.fromProperties(Paths.get("application-rds-template.properties"));
                
                // 1. Capture the SDK instance (cast it to the SalesManagement class from the SDK)
                com.erp.sdk.subsystem.SalesManagement erpSalesSubsystem = 
                    (com.erp.sdk.subsystem.SalesManagement) SubsystemFactory.create(SubsystemName.SALES_MANAGEMENT, dbConfig);
                
                // 2. Wrap it in our Adapter and register it globally
                shared.integration.SalesIntegrationService integrationService = 
                    new shared.integration.BISalesIntegrationServiceImpl(erpSalesSubsystem);
                shared.integration.IntegrationRegistry.setService(integrationService);

                System.out.println("SUCCESS!");
                connected = true;
                break;
            } catch (Exception e) {
                if (attempt < maxRetries && e.getCause() != null && e.getCause().getMessage().contains("Too many connections")) {
                    System.out.println("FAILED (Too many connections)");
                    System.out.println("Waiting " + (retryDelay / 1000) + " seconds before retry...");
                    try {
                        Thread.sleep(retryDelay);
                        retryDelay += 5000; // Increase delay by 5 seconds each time
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                } else {
                    System.err.println("\n[WARNING] Cannot connect to the live AWS database (Host Unknown/Offline).");
                    System.err.println("[WARNING] Starting Sales System in OFFLINE mode. BI Integration is disabled.");
                    // DO NOT call System.exit(1) here!
                    break; // Exit the retry loop and start the app anyway
                }
            }
        }
        
        if (!connected) {
            System.err.println("Continuing without BI integration...");
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