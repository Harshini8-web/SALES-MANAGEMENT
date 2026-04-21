package customers.db;

import customers.model.Customer;
import customers.builder.CustomerBuilder;
import customers.exception.CustomerException;

import com.erp.sdk.config.DatabaseConfig;
import com.erp.sdk.factory.SubsystemFactory;
import com.erp.sdk.subsystem.SubsystemName;
import com.erp.sdk.subsystem.AbstractSubsystem;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomerDAO {

    private AbstractSubsystem facade;

    public CustomerDAO() {
        try {
            DatabaseConfig dbConfig = DatabaseConfig.fromProperties(Paths.get("application-rds-template.properties"));
            this.facade = (AbstractSubsystem) SubsystemFactory.create(SubsystemName.SALES_MANAGEMENT, dbConfig);
        } catch (Exception e) {
            System.err.println("Failed to initialize SDK in CustomerDAO");
        }
    }

    public void addCustomer(Customer customer) throws CustomerException.DuplicateCustomerEntry {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("name", customer.getName());
            data.put("email", customer.getEmail());
            data.put("phone", customer.getPhone());
            data.put("region", customer.getRegion());
            
            facade.create("customers", data, "integration_lead");
            
        } catch (Exception e) {
            // If the error is due to duplicate email (UNIQUE constraint), throw DuplicateCustomerEntry
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("duplicate")) {
                throw new CustomerException.DuplicateCustomerEntry("Customer with email already exists.");
            }
            throw new CustomerException.DuplicateCustomerEntry("Error adding customer: " + e.getMessage());
        }
    }

    public Customer getCustomer(int id) throws CustomerException.CustomerNotFound {
        try {
            Map<String, Object> rs = facade.readById("customers", "customer_id", id, "integration_lead");
            
            if (rs != null && !rs.isEmpty()) {
                return new CustomerBuilder()
                        .setCustomerId(((Number) rs.get("customer_id")).intValue())
                        .setName((String) rs.get("name"))
                        .setEmail((String) rs.get("email"))
                        .setPhone((String) rs.get("phone"))
                        .setRegion((String) rs.get("region"))
                        .build();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new CustomerException.CustomerNotFound("Customer record does not exist.");
    }

    public List<Customer> getAllCustomers() {
        List<Customer> customers = new ArrayList<>();
        try {
            List<Map<String, Object>> rows = facade.readAll("customers", new HashMap<>(), "integration_lead");
            
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