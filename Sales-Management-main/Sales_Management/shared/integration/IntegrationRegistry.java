package shared.integration;

public class IntegrationRegistry {
    private static SalesIntegrationService service;

    public static void setService(SalesIntegrationService s) {
        service = s;
    }

    public static SalesIntegrationService getService() {
        return service;
    }
}