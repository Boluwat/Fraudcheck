package com.isw.fraudcheck.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;

@Component
public class IpRequestFlagger {

    private final Map<String, Deque<Instant>> ipTimestamps = new ConcurrentHashMap<>();
    private static final Logger log = LoggerFactory.getLogger(IpRequestFlagger.class);

    private static final int THRESHOLD = 5;           // max 5 requests
    private static final Duration WINDOW = Duration.ofMinutes(1);

    public boolean isFlagged(String ip) {
        if (ip == null || ip.isBlank()) {
            return false;
        }

        Instant now = Instant.now();
        Deque<Instant> times = ipTimestamps.computeIfAbsent(ip, k -> new ConcurrentLinkedDeque<>());

        // Clean old timestamps FIRST (before adding new one)
        while (!times.isEmpty() && times.peekFirst().isBefore(now.minus(WINDOW))) {
            times.pollFirst();
        }

        // Add current request
        times.addLast(now);

        boolean flagged = times.size() > THRESHOLD;

        if (flagged) {
            log.warn("IP FLAGGED: {} ({} requests in last minute)", ip, times.size());
        }


        return flagged;
    }


    @Scheduled(fixedRate = 5, timeUnit = TimeUnit.MINUTES)
    public void cleanup() {
        Instant now = Instant.now();
        ipTimestamps.entrySet().removeIf(entry -> {
            Deque<Instant> times = entry.getValue();
            while (!times.isEmpty() && times.peekFirst().isBefore(now.minus(WINDOW))) {
                times.pollFirst();
            }
            return times.isEmpty();
        });
    }
}