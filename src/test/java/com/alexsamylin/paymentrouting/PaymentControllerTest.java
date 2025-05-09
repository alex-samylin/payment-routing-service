package com.alexsamylin.paymentrouting;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RestTemplate restTemplate;

    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    void shouldHandleSuccessResponse() throws Exception {
        mockServer.expect(requestTo("http://localhost:8083/provider-b/pay"))
                .andRespond(withSuccess("""
                        {
                          "result": "SUCCESS",
                          "transactionId": "xyz-999"
                        }
                    """, MediaType.APPLICATION_JSON));

        mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestForProviderB()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value("xyz-999"))
                .andExpect(jsonPath("$.message").value("SUCCESS"))
                .andExpect(jsonPath("$.provider").value("ProviderB"));

        mockServer.verify();
    }

    @Test
    void shouldRetryOn503AndSucceed() throws Exception {
        mockServer.expect(ExpectedCount.times(2), requestTo("http://localhost:8082/provider-a/pay"))
                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

        mockServer.expect(ExpectedCount.once(), requestTo("http://localhost:8082/provider-a/pay"))
                .andRespond(withSuccess("""
                        {
                          "status": "SUCCESS",
                          "transactionId": "abc-123"
                        }
                    """, MediaType.APPLICATION_JSON));

        mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value("abc-123"))
                .andExpect(jsonPath("$.message").value("SUCCESS"))
                .andExpect(jsonPath("$.provider").value("ProviderA"));

        mockServer.verify();
    }

    @Test
    void shouldReturn409OnConflict() throws Exception {
        mockServer.expect(requestTo("http://localhost:8082/provider-a/pay"))
                .andRespond(withStatus(HttpStatus.CONFLICT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("""
                            {
                              "status": "FAILURE",
                              "errorCode": "EXPIRED_CARD",
                              "transactionId": "abc-123"
                            }
                        """));

        mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest()))
                .andExpect(status().isConflict())
                .andExpect(content().string(Matchers.containsString("EXPIRED_CARD")));

        mockServer.verify();
    }

    @Test
    void shouldReturn502OnTechnicalFailure() throws Exception {
        mockServer.expect(requestTo("http://localhost:8082/provider-a/pay"))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest()))
                .andExpect(status().isBadGateway())
                .andExpect(content().string(Matchers.containsString("Technical failure from provider: ProviderA")));

        mockServer.verify();
    }


    private String jsonRequest() {
        return """
            {
              "amount": 10000,
              "currency": "USD",
              "cardNumber": "4211111111111111"
            }
            """;
    }

    private String requestForProviderB() {
        return """
        {
          "amount": 10000,
          "currency": "USD",
          "cardNumber": "5311111111111111"
        }
        """;
    }
}
