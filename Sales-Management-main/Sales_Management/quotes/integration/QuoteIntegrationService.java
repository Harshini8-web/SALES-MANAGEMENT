package quotes.integration;

import quotes.db.QuoteDAO;
import quotes.model.Quote;
import quotes.model.QuoteItem;

/**
 * Service to allow external subsystems (like Order Processing) 
 * to fetch approved quotes securely.
 */
public class QuoteIntegrationService {
    
    private final QuoteDAO quoteDAO;

    public QuoteIntegrationService() {
        this.quoteDAO = new QuoteDAO();
    }

    /**
     * Called by the Order Processing team when a user enters a Quote ID
     * in the "New Order" UI.
     */
    public Quote fetchQuoteForOrder(int quoteId) {
        // You can add validation here later if needed (e.g., checking if it's already ordered)
        return quoteDAO.getQuoteById(quoteId);
    }
    
    /**
     * Helper for the Order team to format the quote items into a single string
     * for their 'order_details' database column.
     */
    public String generateOrderDetailsString(Quote quote) {
        if (quote == null || quote.getItems() == null) return "";
        
        StringBuilder details = new StringBuilder();
        for (QuoteItem item : quote.getItems()) {
            details.append(item.getQuantity()).append("x ")
                   .append(item.getProductName()).append(", ");
        }
        // Remove trailing comma
        if (details.length() > 0) {
            details.setLength(details.length() - 2);
        }
        return details.toString();
    }
}