package quotes.db;

import quotes.model.Quote;
import quotes.model.QuoteItem;
import quotes.exception.QuoteException.*;

import com.erp.sdk.config.DatabaseConfig;
import com.erp.sdk.factory.SubsystemFactory;
import com.erp.sdk.subsystem.SubsystemName;
import com.erp.sdk.subsystem.AbstractSubsystem;
import com.erp.sdk.exception.UnauthorizedResourceAccessException;

import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuoteDAO {

    private AbstractSubsystem facade;

    public QuoteDAO() {
        try {
            DatabaseConfig dbConfig = DatabaseConfig.fromProperties(Paths.get("application-rds-template.properties"));
            this.facade = (AbstractSubsystem) SubsystemFactory.create(SubsystemName.SALES_MANAGEMENT, dbConfig);
        } catch (Exception e) {
            System.err.println("Failed to initialize SDK in QuoteDAO");
        }
    }

    public void createQuote(Quote quote) throws QuoteGenerationFailed {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("customer_id", quote.getCustomerId());
            // If dealId is -1 (unlinked), insert NULL instead of -1 to satisfy FK
            // constraint
            data.put("deal_id", quote.getDealId() == -1 ? null : quote.getDealId());
            data.put("total_amount", quote.getTotalAmount());
            data.put("discount", quote.getDiscount());
            data.put("final_amount", quote.getFinalAmount());

            long generatedId = (long) facade.create("quotes", data, "integration_lead");

            // Set the generated quote_id on the quote object
            quote.setQuoteId((int) generatedId);

            // Persist quote items if any
            if (quote.getItems() != null && !quote.getItems().isEmpty()) {
                for (QuoteItem item : quote.getItems()) {
                    Map<String, Object> itemData = new HashMap<>();
                    itemData.put("quote_id", quote.getQuoteId());
                    itemData.put("product_name", item.getProductName());
                    itemData.put("quantity", item.getQuantity());
                    itemData.put("price", item.getPrice());
                    facade.create("quote_items", itemData, "integration_lead");
                }
            }

        } catch (Exception e) {
            throw new QuoteGenerationFailed("Could not generate quote in the RDS database.");
        }
    }

    public Quote getQuoteById(int quoteId) throws QuoteGenerationFailed {
        try {
            Map<String, Object> rs = facade.readById("quotes", "quote_id", quoteId, "integration_lead");

            if (rs != null && !rs.isEmpty()) {
                Quote quote = mapRowToQuote(rs);
                // Fetch and populate quote items
                List<QuoteItem> items = getQuoteItems(quoteId);
                quote.setItems(items);
                return quote;
            }
            throw new QuoteGenerationFailed("Quote ID " + quoteId + " not found.");
        } catch (QuoteGenerationFailed e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new QuoteGenerationFailed("Database error retrieving quote.");
        }
    }

    public List<Quote> getAllQuotes() {
        List<Quote> results = new ArrayList<>();
        try {
            List<Map<String, Object>> rows = facade.readAll("quotes", new HashMap<>(), "integration_lead");
            if (rows != null) {
                for (Map<String, Object> row : rows) {
                    results.add(mapRowToQuote(row));
                }
            }
            return results;
        } catch (Exception e) {
            e.printStackTrace();
            return results;
        }
    }

    public boolean updateQuoteDiscount(int quoteId, double newDiscount, double newFinalAmount) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("discount", newDiscount);
            data.put("final_amount", newFinalAmount);

            facade.update("quotes", "quote_id", quoteId, data, "integration_lead");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean deleteQuote(int quoteId) {
        try {
            facade.delete("quotes", "quote_id", quoteId, "integration_lead");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void deleteQuoteItem(int quoteItemId) {
        try {
            facade.delete("quote_items", "quote_item_id", quoteItemId, "integration_lead");
            System.out.println("Quote item deleted successfully.");

        } catch (UnauthorizedResourceAccessException e) {
            System.err.println("SECURITY BLOCK: The Integration Team has disabled DELETE permissions for Quote Items.");
        } catch (Exception e) {
            System.err.println("A database error occurred.");
        }
    }

    public void addItemToQuote(int quoteId, QuoteItem item) {
        try {
            Map<String, Object> itemData = new HashMap<>();
            itemData.put("quote_id", quoteId);
            itemData.put("product_name", item.getProductName());
            itemData.put("quantity", item.getQuantity());
            itemData.put("price", item.getPrice());
            facade.create("quote_items", itemData, "integration_lead");
            System.out.println("✔ Item added to Quote ID " + quoteId + ": " + item.getProductName());
        } catch (Exception e) {
            System.err.println("✘ Error adding item to quote: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private List<QuoteItem> getQuoteItems(int quoteId) {
        List<QuoteItem> items = new ArrayList<>();
        try {
            // Fetch all quote items and filter by quoteId in Java
            List<Map<String, Object>> rows = facade.readAll("quote_items", new HashMap<>(), "integration_lead");
            System.out.println("[DEBUG] getQuoteItems(" + quoteId + "): Total rows from DB = "
                    + (rows != null ? rows.size() : "null"));

            if (rows != null && !rows.isEmpty()) {
                for (Map<String, Object> row : rows) {
                    // Debug: print each row
                    System.out.println("[DEBUG] Row: " + row);

                    // Filter by quote_id
                    if (row.get("quote_id") != null) {
                        int itemQuoteId = ((Number) row.get("quote_id")).intValue();
                        System.out.println("[DEBUG] Item quote_id = " + itemQuoteId + ", looking for = " + quoteId);

                        if (itemQuoteId == quoteId) {
                            try {
                                String productName = (String) row.get("product_name");
                                int quantity = ((Number) row.get("quantity")).intValue();
                                double price = ((Number) row.get("price")).doubleValue();

                                QuoteItem item = new QuoteItem(quoteId, productName, quantity, price);
                                items.add(item);
                                System.out.println("[DEBUG] Added item: " + productName + ", qty=" + quantity
                                        + ", price=" + price);
                            } catch (Exception e) {
                                System.err.println("Error parsing quote item: " + e.getMessage());
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
            System.out.println("[DEBUG] Final items count: " + items.size());
        } catch (Exception e) {
            System.err.println("Error fetching quote items: " + e.getMessage());
            e.printStackTrace();
        }
        return items;
    }

    private Quote mapRowToQuote(Map<String, Object> rs) {
        Quote quote = new Quote();
        quote.setItems(new ArrayList<>()); // Initialize empty items list to prevent null reference
        if (rs.get("quote_id") instanceof Number) {
            quote.setQuoteId(((Number) rs.get("quote_id")).intValue());
        }
        if (rs.get("customer_id") instanceof Number) {
            quote.setCustomerId(((Number) rs.get("customer_id")).intValue());
        }
        if (rs.get("deal_id") instanceof Number) {
            quote.setDealId(((Number) rs.get("deal_id")).intValue());
        }
        if (rs.get("total_amount") instanceof Number) {
            quote.setTotalAmount(((Number) rs.get("total_amount")).doubleValue());
        }
        if (rs.get("discount") instanceof Number) {
            quote.setDiscount(((Number) rs.get("discount")).doubleValue());
        }
        if (rs.get("final_amount") instanceof Number) {
            quote.setFinalAmount(((Number) rs.get("final_amount")).doubleValue());
        }
        // Map created_at timestamp
        if (rs.get("created_at") != null) {
            if (rs.get("created_at") instanceof LocalDateTime) {
                quote.setCreatedAt((LocalDateTime) rs.get("created_at"));
            } else if (rs.get("created_at") instanceof java.sql.Timestamp) {
                quote.setCreatedAt(((java.sql.Timestamp) rs.get("created_at")).toLocalDateTime());
            } else if (rs.get("created_at") instanceof String) {
                try {
                    quote.setCreatedAt(LocalDateTime.parse((String) rs.get("created_at")));
                } catch (Exception e) {
                    // Parsing failed, leave as null
                }
            }
        }
        return quote;
    }
}