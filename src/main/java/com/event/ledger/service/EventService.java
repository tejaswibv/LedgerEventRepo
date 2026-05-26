package com.event.ledger.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.event.ledger.dto.BalanceResponse;
import com.event.ledger.dto.EventRequest;
import com.event.ledger.entity.EventEntity;
import com.event.ledger.entity.EventType;
import com.event.ledger.repo.EventRepo;
import com.fasterxml.jackson.databind.ObjectMapper;


@Service
public class EventService {

	private final EventRepo repository;
	private final ObjectMapper objectMapper;

	public EventService(EventRepo repository, ObjectMapper objectMapper) {
		this.repository = repository;
		this.objectMapper = objectMapper;
	}

	public EventEntity saveEvent(EventRequest request) {

		try {

			EventEntity entity = new EventEntity();

			entity.setEventId(request.getEventId());
			entity.setAccountId(request.getAccountId());
			entity.setType(request.getType());
			entity.setAmount(request.getAmount());
			entity.setCurrency(request.getCurrency());
			entity.setEventTimestamp(request.getEventTimestamp());
			entity.setMetadata(objectMapper.writeValueAsString(request.getMetadata()));

			return repository.save(entity);

		} catch (DataIntegrityViolationException ex) {

			return repository.findByEventId(request.getEventId()).orElseThrow();

		} catch (Exception ex) {

			throw new RuntimeException("Failed to save event", ex);
		}
	}

	public EventEntity getByEventId(String eventId) {

		return repository.findByEventId(eventId).orElseThrow(() -> new RuntimeException("Event not found"));
	}

	public List<EventEntity> getEventsByAccount(String accountId) {

		return repository.findByAccountIdOrderByEventTimestampAsc(accountId);
	}

	public BalanceResponse getBalance(String accountId) {

		List<EventEntity> events = repository.findByAccountIdOrderByEventTimestampAsc(accountId);

		BigDecimal balance = BigDecimal.ZERO;

		for (EventEntity event : events) {

			if (event.getType() == EventType.CREDIT) {

				balance = balance.add(event.getAmount());

			} else {

				balance = balance.subtract(event.getAmount());
			}
		}

		return new BalanceResponse(accountId, balance);
	}
}