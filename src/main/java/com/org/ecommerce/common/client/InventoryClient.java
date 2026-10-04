package com.org.ecommerce.common.client;

import com.org.ecommerce.common.exception.InventoryServiceUnavailableException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class InventoryClient {
    private final RestClient restClient;
    private final HttpServletRequest httpServletRequest;

    public InventoryClient(RestClient.Builder builder, @Value("${inventory.service.url}") String inventoryServiceUrl,
                           HttpServletRequest httpServletRequest) {
        this.httpServletRequest = httpServletRequest;
        this.restClient = builder
                .baseUrl(inventoryServiceUrl)
                .build();
    }

    /**
     * Reserve/release stock → Kafka because it is part of the order Saga and should happen asynchronously between services.
     * It also needs reliable event delivery, retries, and idempotency.
     * Create inventory → REST because it's a direct admin/CRUD operation needing an immediate response.
     * Get inventory → REST because it's a simple query/read, where synchronous request-response is appropriate.
     */

    @CircuitBreaker(name = "inventoryService", fallbackMethod = "createInventoryFallback")
    public void createInventory(Long productId, int initialStockQuantity) {
        String jwtToken = httpServletRequest.getHeader("Authorization");
        restClient.post()
                .uri("/inventory/{productId}?quantity={quantity}", productId, initialStockQuantity)
                .header("Authorization", jwtToken)
                .retrieve()
                .toBodilessEntity();
    }

    @CircuitBreaker(name = "inventoryService", fallbackMethod = "getInventoryFallback")
    public InventoryResponse getInventory(Long productId) {
        String jwtToken = httpServletRequest.getHeader("Authorization");
        return restClient.get()
                .uri("/inventory/{productId}", productId)
                .header("Authorization", jwtToken)
                .retrieve()
                .body(InventoryResponse.class);
    }

    // THIS FALLBACK IS CREATED FOR createInventory METHOD.
    // FALLBACK PARAMETERS MUST MATCH THE ORIGINAL METHOD PARAMETERS,
    // PLUS A Throwable PARAMETER.
    // FALLBACK RETURN TYPE MUST MATCH THE ORIGINAL METHOD RETURN TYPE.
    private void createInventoryFallback(Long productId, int initialStockQuantity, Throwable throwable) {
        System.out.println("CIRCUIT BREAKER FALLBACK CALLED");
        throw new InventoryServiceUnavailableException();
    }

    // PRODUCT ID IS EXPECTED TO MATCH THE GET_INVENTORY METHOD
    // RESILIENCE 4J REQUIRES THROWABLE PARAMETER AS WELL
    private InventoryResponse getInventoryFallback(Long productId, Throwable throwable) {
        System.out.println("CIRCUIT BREAKER FALLBACK CALLED");
        throw new InventoryServiceUnavailableException();
    }
}
