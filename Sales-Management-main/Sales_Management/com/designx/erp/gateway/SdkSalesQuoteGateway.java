package com.designx.erp.gateway;

import com.erp.sdk.subsystem.SalesManagement;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class SdkSalesQuoteGateway implements SalesQuoteGateway {

    private static final String TABLE_QUOTES = "quotes";
    private static final String TABLE_CUSTOMERS = "customers";
    private static final String TABLE_QUOTE_ITEMS = "quote_items";

    private final SalesManagement salesManagement;
    private final String sdkUser;

    public SdkSalesQuoteGateway(SalesManagement salesManagement, String sdkUser) {
        this.salesManagement = salesManagement;
        this.sdkUser = sdkUser;
    }

    @Override
    public QuoteDetails getQuoteDetails(int quoteId) throws SQLException {
        try {
            Map<String, Object> quote = salesManagement.readById(TABLE_QUOTES, "quote_id", quoteId, sdkUser);
            if (quote == null || quote.isEmpty()) {
                return null;
            }

            int customerId = asInt(quote.get("customer_id"));
            Map<String, Object> customer = null;
            if (customerId > 0) {
                customer = salesManagement.readById(TABLE_CUSTOMERS, "customer_id", customerId, sdkUser);
            }

            List<Map<String, Object>> quoteItems = salesManagement.readAll(TABLE_QUOTE_ITEMS,
                    Map.of("quote_id", quoteId), sdkUser);
            String vehicleModel = quoteItems.isEmpty() ? "" : asString(quoteItems.get(0).get("product_name"));
            String customerName = customer == null ? null : asString(customer.get("name"));
            double orderValue = asDouble(quote.get("final_amount"));

            return new QuoteDetails(quoteId, customerId, customerName, vehicleModel, orderValue);
        } catch (Exception e) {
            throw new SQLException("SDK operation failed: read quote details from SalesManagement", e);
        }
    }

    private int asInt(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        return Integer.parseInt(String.valueOf(value));
    }

    private double asDouble(Object value) {
        if (value == null) {
            return 0.0;
        }
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        return Double.parseDouble(String.valueOf(value));
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
