package quotes.db;

import quotes.model.Quote;
import quotes.model.QuoteItem;
import quotes.exception.QuoteException.*;
import com.likeseca.erp.database.facade.ErpDatabaseFacade;
import quotes.engine.PricingEngine;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuoteDAO {

    private ErpDatabaseFacade facade;

    public QuoteDAO() {
        try {
            this.facade = new ErpDatabaseFacade();
        } catch (Exception e) {
            System.err.println("Failed to initialize ErpDatabaseFacade in QuoteDAO: " + e.getMessage());
        }
    }

    public void createQuote(Quote quote) throws QuoteGenerationFailed {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("customer_id", quote.getCustomerId());
            
            // Only add deal_id if it exists, otherwise leave it out of the insert
            if (quote.getDealId() != -1) {
                data.put("deal_id", quote.getDealId());
            }
            
            data.put("total_amount", quote.getTotalAmount());
            data.put("discount", quote.getDiscount());
            data.put("final_amount", quote.getFinalAmount());

            Object generatedId = facade.salesManagementSubsystem().create("quotes", data);
            quote.setQuoteId(((Number) generatedId).intValue());

            if (quote.getItems() != null && !quote.getItems().isEmpty()) {
                for (QuoteItem item : quote.getItems()) {
                    Map<String, Object> itemData = new HashMap<>();
                    itemData.put("quote_id", quote.getQuoteId());
                    itemData.put("product_name", item.getProductName());
                    itemData.put("quantity", item.getQuantity());
                    itemData.put("price", item.getPrice());
                    facade.salesManagementSubsystem().create("quote_items", itemData);
                }
            }

        } catch (Exception e) {
            // This will print the actual underlying DB permission error to your console
            e.printStackTrace();
            throw new QuoteGenerationFailed("DB Error: " + e.getMessage());
        }
    }

    public Quote getQuoteById(int quoteId) throws QuoteGenerationFailed {
        try {
            Map<String, Object> rs = facade.salesManagementSubsystem().readById("quotes", "quote_id", quoteId);

            if (rs != null && !rs.isEmpty()) {
                Quote quote = mapRowToQuote(rs);
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
            List<Map<String, Object>> rows = facade.salesManagementSubsystem().readAll("quotes", new HashMap<>());
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

            facade.salesManagementSubsystem().update("quotes", "quote_id", quoteId, data);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean deleteQuote(int quoteId) {
        try {
            facade.salesManagementSubsystem().delete("quotes", "quote_id", quoteId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void deleteQuoteItem(int quoteItemId) {
        try {
            facade.salesManagementSubsystem().delete("quote_items", "quote_item_id", quoteItemId);
            System.out.println("Quote item deleted successfully.");
        } catch (Exception e) {
            System.err.println("A database error occurred or lack permissions: " + e.getMessage());
        }
    }

    public void addItemToQuote(int quoteId, QuoteItem item) {
        try {
            // 1. Save the new line item to the database
            Map<String, Object> itemData = new HashMap<>();
            itemData.put("quote_id", quoteId);
            itemData.put("product_name", item.getProductName());
            itemData.put("quantity", item.getQuantity());
            itemData.put("price", item.getPrice());
            facade.salesManagementSubsystem().create("quote_items", itemData);
            System.out.println("✔ Item added to Quote ID " + quoteId + ": " + item.getProductName());

            // 2. Fetch the updated quote (which will now pull all items, including the new one)
            Quote updatedQuote = getQuoteById(quoteId);

            // 3. Recalculate the totals using your existing PricingEngine
            PricingEngine engine = new PricingEngine();
            double[] newTotals = engine.computeFinalPrice(updatedQuote.getItems(), updatedQuote.getDiscount());

            // 4. Update the parent quote record with the new accurate totals
            Map<String, Object> quoteUpdate = new HashMap<>();
            quoteUpdate.put("total_amount", newTotals[0]); // The new raw total
            quoteUpdate.put("final_amount", newTotals[1]); // The new discounted grand total
            
            facade.salesManagementSubsystem().update("quotes", "quote_id", quoteId, quoteUpdate);
            
        } catch (Exception e) {
            System.err.println("✘ Error adding item to quote: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private List<QuoteItem> getQuoteItems(int quoteId) {
        List<QuoteItem> items = new ArrayList<>();
        try {
            List<Map<String, Object>> rows = facade.salesManagementSubsystem().readAll("quote_items", new HashMap<>());

            if (rows != null && !rows.isEmpty()) {
                for (Map<String, Object> row : rows) {
                    if (row.get("quote_id") != null) {
                        int itemQuoteId = ((Number) row.get("quote_id")).intValue();

                        if (itemQuoteId == quoteId) {
                            try {
                                String productName = (String) row.get("product_name");
                                int quantity = ((Number) row.get("quantity")).intValue();
                                double price = ((Number) row.get("price")).doubleValue();

                                QuoteItem item = new QuoteItem(quoteId, productName, quantity, price);
                                items.add(item);
                            } catch (Exception e) {
                                System.err.println("Error parsing quote item: " + e.getMessage());
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error fetching quote items: " + e.getMessage());
        }
        return items;
    }

    private Quote mapRowToQuote(Map<String, Object> rs) {
        Quote quote = new Quote();
        quote.setItems(new ArrayList<>());
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
        if (rs.get("created_at") != null) {
            if (rs.get("created_at") instanceof LocalDateTime) {
                quote.setCreatedAt((LocalDateTime) rs.get("created_at"));
            } else if (rs.get("created_at") instanceof java.sql.Timestamp) {
                quote.setCreatedAt(((java.sql.Timestamp) rs.get("created_at")).toLocalDateTime());
            } else if (rs.get("created_at") instanceof String) {
                try {
                    quote.setCreatedAt(LocalDateTime.parse((String) rs.get("created_at")));
                } catch (Exception e) { }
            }
        }
        return quote;
    }
}