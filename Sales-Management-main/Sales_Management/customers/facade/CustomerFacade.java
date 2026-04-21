package customers.facade;

import customers.db.CustomerDAO;
import customers.model.Customer;
import customers.exception.CustomerException;

import java.util.List;

public class CustomerFacade {
    private CustomerDAO customerDAO;

    public CustomerFacade() {
        this.customerDAO = new CustomerDAO();
    }

    public void createCustomer(String name, String email, String phone, String region) 
            throws CustomerException.DuplicateCustomerEntry {
        Customer customer = new Customer();
        customer.setName(name);
        customer.setEmail(email);
        customer.setPhone(phone);
        customer.setRegion(region);
        customerDAO.addCustomer(customer);
    }

    public Customer getCustomerById(int id) throws CustomerException.CustomerNotFound {
        return customerDAO.getCustomer(id);
    }

    public List<Customer> getAllCustomers() {
        return customerDAO.getAllCustomers();
    }

    public Customer getCustomerByEmail(String email) {
        return customerDAO.getCustomerByEmail(email);
    }

    public List<Customer> getCustomersByRegion(String region) {
        return customerDAO.getCustomersByRegion(region);
    }
}