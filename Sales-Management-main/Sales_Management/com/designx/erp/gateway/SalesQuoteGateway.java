package com.designx.erp.gateway;

import java.sql.SQLException;

public interface SalesQuoteGateway {
    QuoteDetails getQuoteDetails(int quoteId) throws SQLException;
}
