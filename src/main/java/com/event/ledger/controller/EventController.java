package com.event.ledger.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.event.ledger.dto.BalanceResponse;
import com.event.ledger.dto.EventRequest;
import com.event.ledger.entity.EventEntity;
import com.event.ledger.service.EventService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class EventController {

    private final EventService service;

    @PostMapping("/events")
    @ResponseStatus(HttpStatus.CREATED)
    public EventEntity submitEvent(
            @Valid @RequestBody EventRequest request) {

        return service.saveEvent(request);
    }

    @GetMapping("/event/{id}")
    public EventEntity getEvent(
            @PathVariable String id) {

        return service.getByEventId(id);
    }

    @GetMapping("/events")
    public List<EventEntity> getEventsByAccount(
            @RequestParam String account) {

        return service.getEventsByAccount(account);
    }

    @GetMapping("/accounts/{accountId}/balance")
    public BalanceResponse getBalance(
            @PathVariable String accountId) {

        return service.getBalance(accountId);
    }
}