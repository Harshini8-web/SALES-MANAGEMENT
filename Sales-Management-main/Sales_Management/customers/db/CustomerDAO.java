package customers.db;

import customers.model.Customer;
import customers.builder.CustomerBuilder;
import customers.exception.CustomerException;

// 1. Import the new ErpDatabaseFacade instead of the old SDK
import com.likeseca.erp.database.facade.ErpDatabaseFacade;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomerDAO {

    // 2. Change the type from AbstractSubsystem to ErpDatabaseFacade
    private ErpDatabaseFacade facade;

    public CustomerDAO() {
        initializeFacade();
    }

    private void initializeFacade() {
        try {
            this.facade = new ErpDatabaseFacade();
        } catch (Throwable e) {
            System.err.println("Failed to initialize ErpDatabaseFacade in CustomerDAO: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void addCustomer(Customer customer) throws CustomerException.DuplicateCustomerEntry {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("name", customer.getName());
            data.put("email", customer.getEmail());
            data.put("phone", customer.getPhone());
            data.put("region", customer.getRegion());
            
            // 4. Use the salesManagementSubsystem() API and remove the "integration_lead" argument
            facade.salesManagementSubsystem().create("customers", data);
            
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("duplicate")) {
                throw new CustomerException.DuplicateCustomerEntry("Customer with email already exists.");
            }
            throw new CustomerException.DuplicateCustomerEntry("Error adding customer: " + e.getMessage());
        }
    }

    public Customer getCustomer(int id) throws CustomerException.CustomerNotFound {
        try {
            // 5. Update to use salesManagementSubsystem() and remove the 4th argument
            Map<String, Object> rs = facade.salesManagementSubsystem().readById("customers", "customer_id", id);
            
            if (rs != null && !rs.isEmpty()) {
                return mapRowToCustomer(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new CustomerException.CustomerNotFound("Customer record does not exist.");
    }

    public List<Customer> getAllCustomers() {
        List<Customer> customers = new ArrayList<>();
        try {
            // 6. Update to use salesManagementSubsystem() and remove the 3rd argument
            List<Map<String, Object>> rows = facade.salesManagementSubsystem().readAll("customers", new HashMap<>());
            
            if (rows != null) {
                for (Map<String, Object> row : rows) {
                    customers.add(mapRowToCustomer(row));
                }
            }
        } catch (Exception e) {
            System.err.println("Error retrieving all customers: " + e.getMessage());
        }
        return customers;
    }

    public Customer getCustomerByEmail(String email) {
        try {
            List<Customer> allCustomers = getAllCustomers();
            for (Customer customer : allCustomers) {
                if (customer.getEmail().equalsIgnoreCase(email)) {
                    return customer;
                }
            }
        } catch (Exception e) {
            System.err.println("Error searching customer by email: " + e.getMessage());
        }
        return null;
    }

    public List<Customer> getCustomersByRegion(String region) {
        List<Customer> results = new ArrayList<>();
        try {
            List<Customer> allCustomers = getAllCustomers();
            for (Customer customer : allCustomers) {
                if (customer.getRegion().equalsIgnoreCase(region)) {
                    results.add(customer);
                }
            }
        } catch (Exception e) {
            System.err.println("Error retrieving customers by region: " + e.getMessage());
        }
        return results;
    }

    // Refactored the row mapping into a helper method since it was duplicated in getCustomer
    private Customer mapRowToCustomer(Map<String, Object> row) {
        return new CustomerBuilder()
                .setCustomerId(((Number) row.get("customer_id")).intValue())
                .setName((String) row.get("name"))
                .setEmail((String) row.get("email"))
                .setPhone((String) row.get("phone"))
                .setRegion((String) row.get("region"))
                .build();
    }
}