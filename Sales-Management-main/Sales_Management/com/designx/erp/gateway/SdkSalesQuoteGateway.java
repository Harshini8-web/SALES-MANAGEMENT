package com.designx.erp.gateway;

import com.likeseca.erp.database.facade.ErpDatabaseFacade;
import quotes.db.QuoteDAO;
import quotes.model.Quote;
import customers.db.CustomerDAO;
import customers.model.Customer;
import java.sql.SQLException;

public class SdkSalesQuoteGateway implements SalesQuoteGateway {

    private final QuoteDAO quoteDAO;
    private final CustomerDAO customerDAO;

    public SdkSalesQuoteGateway(ErpDatabaseFacade facade) {
        // We removed the unused 'facade' class variable.
        // Initialize DAOs directly
        this.quoteDAO = new QuoteDAO();
        this.customerDAO = new CustomerDAO();
    }

    @Override
    public QuoteDetails getQuoteDetails(int quoteId) throws SQLException {
        try {
            // 1. Use the correct method: getQuoteById
            Quote quote = quoteDAO.getQuoteById(quoteId);
            if (quote == null)
                return null;

            int customerId = quote.getCustomerId();

            // 2. Fetch Customer using getCustomer
            Customer customer = customerDAO.getCustomer(customerId);

            // 3. Map to QuoteDetails using the 5-argument constructor
            // Passing "N/A" as a placeholder for the missing vehicleModel
            return new QuoteDetails(
                    quoteId,
                    customerId,
                    (customer != null) ? customer.getName() : "Unknown Customer",
                    "N/A",
                    quote.getFinalAmount());

        } catch (Exception e) {
            throw new SQLException("Failed to fetch integration details: " + e.getMessage(), e);
        }
    }
}