package customers.ui;

import customers.facade.CustomerFacade;
import customers.command.CustomerCommand;
import customers.command.AddCustomerCommand;
import java.util.Scanner;

@SuppressWarnings("resource")
public class CustomerUI {
    // Added for integration with SalesManagementSystem
    public void displayMenu() {
        main(new String[0]);
    }

    public static void main(String[] args) {
        CustomerFacade customerFacade = new CustomerFacade();
        // Do not close this Scanner to avoid closing System.in
        Scanner scanner = new Scanner(System.in); // NOSONAR

        System.out.println("===================================");
        System.out.println("   SALES SYSTEM: CUSTOMER MODULE   ");
        System.out.println("===================================");

        boolean running = true;
        while (running) {
            System.out.println("\nSelect an option:");
            System.out.println("1. Add New Customer");
            System.out.println("2. View All Customers");
            System.out.println("3. View Customer by ID");
            System.out.println("4. Exit");
            System.out.print("Enter your choice (1-4): ");
            
            String choice = scanner.nextLine().trim();
            
            switch (choice) {
                case "1":
                    addNewCustomer(customerFacade, scanner);
                    break;
                case "2":
                    viewAllCustomers(customerFacade);
                    break;
                case "3":
                    viewCustomerById(customerFacade, scanner);
                    break;
                case "4":
                    running = false;
                    System.out.println("Exiting Customer Module...");
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private static void addNewCustomer(CustomerFacade customerFacade, Scanner scanner) {
        System.out.print("\nEnter Customer Name: ");
        String name = scanner.nextLine();

        System.out.print("Enter Customer Email: ");
        String email = scanner.nextLine();

        System.out.print("Enter Customer Phone: ");
        String phone = scanner.nextLine();

        System.out.print("Enter Customer Region: ");
        String region = scanner.nextLine();

        System.out.println("\nProcessing...");

        // Passing the real user input into the Command
        CustomerCommand addCmd = new AddCustomerCommand(
                customerFacade, name, email, phone, region);

        addCmd.execute();
        System.out.println("Customer added successfully!");
    }

    private static void viewAllCustomers(CustomerFacade customerFacade) {
        System.out.println("\n--- ALL CUSTOMERS ---");
        java.util.List<customers.model.Customer> customers = customerFacade.getAllCustomers();
        
        if (customers == null || customers.isEmpty()) {
            System.out.println("No customers found.");
            return;
        }

        System.out.println("\nTotal Customers: " + customers.size());
        System.out.println("========================================================");
        
        for (customers.model.Customer customer : customers) {
            System.out.println("ID: " + customer.getCustomerId());
            System.out.println("Name: " + customer.getName());
            System.out.println("Email: " + customer.getEmail());
            System.out.println("Phone: " + customer.getPhone());
            System.out.println("Region: " + customer.getRegion());
            System.out.println("Created At: " + customer.getCreatedAt());
            System.out.println("--------");
        }
    }

    private static void viewCustomerById(CustomerFacade customerFacade, Scanner scanner) {
        System.out.print("\nEnter Customer ID: ");
        String idInput = scanner.nextLine().trim();
        
        try {
            int customerId = Integer.parseInt(idInput);
            customers.model.Customer customer = customerFacade.getCustomerById(customerId);
            
            System.out.println("\n--- CUSTOMER DETAILS ---");
            System.out.println("ID: " + customer.getCustomerId());
            System.out.println("Name: " + customer.getName());
            System.out.println("Email: " + customer.getEmail());
            System.out.println("Phone: " + customer.getPhone());
            System.out.println("Region: " + customer.getRegion());
            System.out.println("Created At: " + customer.getCreatedAt());
        } catch (NumberFormatException e) {
            System.out.println("Invalid Customer ID format. Please enter a valid number.");
        } catch (customers.exception.CustomerException.CustomerNotFound e) {
            System.out.println("Customer not found: " + e.getMessage());
        }
    }
}