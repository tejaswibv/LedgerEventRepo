package com.event.ledger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.event.ledger.dto.EventRequest;
import com.event.ledger.entity.EventType;
import com.event.ledger.repo.EventRepo;
import com.event.ledger.service.EventService;

@SpringBootTest
@AutoConfigureMockMvc
class ConcurrentDuplicateEventTest {

    @Autowired
    private EventService service;

    @Autowired
    private EventRepo repository;
    
    @Autowired
    private MockMvc mockMvc;

//    @Test
//    void shouldHandleConcurrentDuplicateEvents() throws Exception {
//
//        int threadCount = 10;
//
//        ExecutorService executor =
//                Executors.newFixedThreadPool(threadCount);
//
//        CountDownLatch latch =
//                new CountDownLatch(threadCount);
//
//        for (int i = 0; i < threadCount; i++) {
//
//            executor.submit(() -> {
//
//                try {
//
//                    EventRequest request =
//                            new EventRequest();
//
//                    request.setEventId("evt-concurrent");
//                    request.setAccountId("acct-123");
//                    request.setType(EventType.CREDIT);
//                    request.setAmount(
//                            BigDecimal.valueOf(100));
//                    request.setCurrency("USD");
//                    request.setEventTimestamp(
//                            Instant.now());
//
//                    service.saveEvent(request);
//
//                } finally {
//
//                    latch.countDown();
//                }
//            });
//        }
//
//        latch.await();
//
//        assertEquals(
//                2,
//                repository.findAll().size()
//        );
//    }
    
    @Test
    @DisplayName("Should return zero balance for account with no events")
    void shouldReturnZeroBalanceForEmptyAccount() throws Exception {

        mockMvc.perform(
                get("/accounts/empty-account/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId")
                        .value("empty-account"))
                .andExpect(jsonPath("$.balance")
                        .value(0));
    }
    
//    @Test
//    void shouldHandleEventsWithSameTimestamp() throws Exception {
//
//        String e1 =
//                "{ \"eventId\":\"evt-1\", \"accountId\":\"acct-eq\", \"type\":\"CREDIT\", \"amount\":50, \"currency\":\"USD\", \"eventTimestamp\":\"2026-05-15T10:00:00Z\" }";
//
//        String e2 =
//                "{ \"eventId\":\"evt-2\", \"accountId\":\"acct-eq\", \"type\":\"DEBIT\", \"amount\":20, \"currency\":\"USD\", \"eventTimestamp\":\"2026-05-15T10:00:00Z\" }";
//
//        mockMvc.perform(post("/events").contentType(MediaType.APPLICATION_JSON).content(e1))
//                .andExpect(status().isCreated());
//
//        mockMvc.perform(post("/events").contentType(MediaType.APPLICATION_JSON).content(e2))
//                .andExpect(status().isCreated());
//
//        mockMvc.perform(get("/events")
//                        .param("account", "acct-eq"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.length()").value(2));
//    }
    
    @Test
    @DisplayName("Should reject malformed JSON")
    void shouldRejectMalformedJson() throws Exception {

    	String invalidJson =
    	        "{\n" +
    	        "  \"eventId\":\"evt-1\",\n" +
    	        "  \"accountId\":\"acct-1\",\n" +
    	        "  \"type\":\"CREDIT\",\n";

        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("Should reject unsupported HTTP method")
    void shouldRejectUnsupportedMethod()
            throws Exception {

        mockMvc.perform(put("/events"))
                .andExpect(status().isMethodNotAllowed());
    }
    
    @Test
    @DisplayName("Should return empty list for unknown account")
    void shouldReturnEmptyListForUnknownAccount()
            throws Exception {

        mockMvc.perform(get("/events")
                        .param("account", "unknown"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()")
                        .value(0));
    }
    
    @Test
    @DisplayName("Should compute balance across multiple events")
    void shouldComputeBalanceAcrossMultipleEvents() throws Exception {

        String[] payloads = {

                "{\n" +
                "  \"eventId\":\"evt-1\",\n" +
                "  \"accountId\":\"acct-multi\",\n" +
                "  \"type\":\"CREDIT\",\n" +
                "  \"amount\":500,\n" +
                "  \"currency\":\"USD\",\n" +
                "  \"eventTimestamp\":\"2026-05-15T14:01:00Z\"\n" +
                "}",

                "{\n" +
                "  \"eventId\":\"evt-2\",\n" +
                "  \"accountId\":\"acct-multi\",\n" +
                "  \"type\":\"DEBIT\",\n" +
                "  \"amount\":100,\n" +
                "  \"currency\":\"USD\",\n" +
                "  \"eventTimestamp\":\"2026-05-15T14:02:00Z\"\n" +
                "}",

                "{\n" +
                "  \"eventId\":\"evt-3\",\n" +
                "  \"accountId\":\"acct-multi\",\n" +
                "  \"type\":\"CREDIT\",\n" +
                "  \"amount\":200,\n" +
                "  \"currency\":\"USD\",\n" +
                "  \"eventTimestamp\":\"2026-05-15T14:03:00Z\"\n" +
                "}",

                "{\n" +
                "  \"eventId\":\"evt-4\",\n" +
                "  \"accountId\":\"acct-multi\",\n" +
                "  \"type\":\"DEBIT\",\n" +
                "  \"amount\":50,\n" +
                "  \"currency\":\"USD\",\n" +
                "  \"eventTimestamp\":\"2026-05-15T14:04:00Z\"\n" +
                "}"
        };

        for (String payload : payloads) {

            mockMvc.perform(post("/events")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload))
                    .andExpect(status().isCreated());
        }

        mockMvc.perform(get("/accounts/acct-multi/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(550));
    }
}