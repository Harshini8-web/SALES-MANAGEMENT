package com.designx.erp.gateway;

public class QuoteDetails {
    private final int quoteId;
    private final int customerId;
    private final String customerName;
    private final String vehicleModel;
    private final double orderValue;

    public QuoteDetails(int quoteId, int customerId, String customerName, String vehicleModel, double orderValue) {
        this.quoteId = quoteId;
        this.customerId = customerId;
        this.customerName = customerName;
        this.vehicleModel = vehicleModel;
        this.orderValue = orderValue;
    }

    public int getQuoteId() {
        return quoteId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getVehicleModel() {
        return vehicleModel;
    }

    public double getOrderValue() {
        return orderValue;
    }
}
