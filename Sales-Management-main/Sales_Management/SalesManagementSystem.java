import customers.ui.CustomerUI;
import quotes.ui.QuoteUI;
import analytics.facade.AnalyticsCommandFactory;
import java.util.Scanner;

// --- 1. ADD THE DATABASE FACADE IMPORT ---
import com.likeseca.erp.database.facade.ErpDatabaseFacade;

public class SalesManagementSystem {
    public static void main(String[] args) {

        System.out.println("=================================================");
        System.out.println("  ENTERPRISE SALES MANAGEMENT SYSTEM - STARTING  ");
        System.out.println("=================================================");

        // --- 2. BOOTSTRAP THE DATABASE FACADE WITH RETRY LOGIC ---
        int maxRetries = 5;
        int retryDelay = 2000; // Start with 2 seconds
        boolean connected = false;
        ErpDatabaseFacade facade = null;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                System.out.print("Connecting to local MySQL Database (Attempt " + attempt + "/" + maxRetries + ")... ");

                // Initialize the facade (it reads database.properties automatically)
                facade = new ErpDatabaseFacade();

                System.out.println("SUCCESS!");
                connected = true;

                System.out.println("Database facade initialized successfully.");

                break;

            } catch (Exception e) {
                if (attempt < maxRetries) {
                    String reason = (e.getCause() != null) ? e.getCause().getMessage() : e.getMessage();
                    System.out.println("FAILED (" + reason + ")");
                    System.out.println("Waiting " + (retryDelay / 1000) + " seconds before retry...");
                    try {
                        Thread.sleep(retryDelay);
                        retryDelay += 2000;
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                } else {
                    System.err.println("\n[WARNING] Cannot connect to the local MySQL database.");
                    System.err.println("[WARNING] Starting Sales System in OFFLINE mode.");
                    break;
                }
            }
        }

        if (!connected) {
            System.err.println("Continuing without database integration...");
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
        
        if (facade != null) {
            try {
                facade.close();
            } catch (Exception e) {
                System.err.println("Error closing database facade: " + e.getMessage());
            }
        }
        
        scanner.close();
        System.exit(0);
    }
}