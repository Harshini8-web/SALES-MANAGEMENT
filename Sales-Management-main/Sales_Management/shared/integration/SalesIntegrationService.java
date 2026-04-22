package shared.integration;

public interface SalesIntegrationService {
    void publishSaleToBI(String carModel, int unitsSold, double revenue, String dealerId, String region, String saleQuarter);
}