package quotes.ui;

import quotes.command.QuoteCommand;
import quotes.command.QuoteCommandFactory;
import quotes.db.QuoteDAO;
import quotes.model.QuoteItem;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@SuppressWarnings("resource")
public class QuoteUI {
    // Added for integration with SalesManagementSystem
    public void displayMenu() {
        main(new String[0]);
    }

    public static void main(String[] args) {
        QuoteDAO quoteDAO = new QuoteDAO();
        Scanner scanner = new Scanner(System.in); // NOSONAR

        System.out.println("===================================");
        System.out.println("   SALES SYSTEM: QUOTES MODULE     ");
        System.out.println("===================================");

        boolean running = true;
        while (running) {
            System.out.println("\nSelect an option:");
            System.out.println("1. Create New Quote");
            System.out.println("2. View Quote by ID");
            System.out.println("3. Update Quote Discount");
            System.out.println("4. Add Item to Existing Quote");
            System.out.println("5. Delete Quote");
            System.out.println("6. Exit");
            System.out.print("Enter your choice (1-6): ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    createNewQuote(quoteDAO, scanner);
                    break;
                case "2":
                    viewQuote(quoteDAO, scanner);
                    break;
                case "3":
                    updateDiscount(quoteDAO, scanner);
                    break;
                case "4":
                    addItemToQuote(quoteDAO, scanner);
                    break;
                case "5":
                    deleteQuote(quoteDAO, scanner);
                    break;
                case "6":
                    running = false;
                    System.out.println("Exiting Quotes Module...");
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private static void createNewQuote(QuoteDAO quoteDAO, Scanner scanner) {
        try {
            System.out.print("Enter Customer ID: ");
            int customerId = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("Enter Deal ID (or -1 if none): ");
            int dealId = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("Enter Discount (%): ");
            double discount = Double.parseDouble(scanner.nextLine().trim());

            List<QuoteItem> items = new ArrayList<>();
            boolean addingItems = true;
            while (addingItems) {
                System.out.print("Enter Product Name: ");
                String productName = scanner.nextLine().trim();

                System.out.print("Enter Quantity: ");
                int quantity = Integer.parseInt(scanner.nextLine().trim());

                System.out.print("Enter Unit Price: ");
                double unitPrice = Double.parseDouble(scanner.nextLine().trim());

                items.add(new QuoteItem(0, productName, quantity, unitPrice));

                System.out.print("Add another item? (y/n): ");
                addingItems = scanner.nextLine().trim().equalsIgnoreCase("y");
            }

            QuoteCommand cmd = QuoteCommandFactory.createQuote(customerId, dealId, items, discount, quoteDAO);
            cmd.execute();

        } catch (Exception e) {
            System.out.println("Error creating quote: " + e.getMessage());
        }
    }

    private static void viewQuote(QuoteDAO quoteDAO, Scanner scanner) {
        try {
            System.out.print("Enter Quote ID: ");
            int quoteId = Integer.parseInt(scanner.nextLine().trim());

            QuoteCommand cmd = QuoteCommandFactory.viewQuote(quoteId, quoteDAO);
            cmd.execute();

        } catch (Exception e) {
            System.out.println("Error viewing quote: " + e.getMessage());
        }
    }

    private static void updateDiscount(QuoteDAO quoteDAO, Scanner scanner) {
        try {
            System.out.print("Enter Quote ID: ");
            int quoteId = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("Enter New Discount (%): ");
            double newDiscount = Double.parseDouble(scanner.nextLine().trim());

            QuoteCommand cmd = QuoteCommandFactory.updateDiscount(quoteId, newDiscount, quoteDAO);
            cmd.execute();

        } catch (Exception e) {
            System.out.println("Error updating discount: " + e.getMessage());
        }
    }

    private static void deleteQuote(QuoteDAO quoteDAO, Scanner scanner) {
        try {
            System.out.print("Enter Quote ID: ");
            int quoteId = Integer.parseInt(scanner.nextLine().trim());

            QuoteCommand cmd = QuoteCommandFactory.deleteQuote(quoteId, quoteDAO);
            cmd.execute();

        } catch (Exception e) {
            System.out.println("Error deleting quote: " + e.getMessage());
        }
    }

    private static void addItemToQuote(QuoteDAO quoteDAO, Scanner scanner) {
        try {
            System.out.print("Enter Quote ID: ");
            int quoteId = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("Enter Product Name: ");
            String productName = scanner.nextLine().trim();

            System.out.print("Enter Quantity: ");
            int quantity = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("Enter Unit Price: ");
            double unitPrice = Double.parseDouble(scanner.nextLine().trim());

            QuoteItem item = new QuoteItem(quoteId, productName, quantity, unitPrice);
            quoteDAO.addItemToQuote(quoteId, item);
            System.out.println("✔ Item added successfully!");

        } catch (Exception e) {
            System.out.println("Error adding item to quote: " + e.getMessage());
        }
    }
}
