package com.isw.fraudcheck.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Component
public class IpRequestFlagger {
    private final Map<String, Deque<Instant>> ipTimestamps = new ConcurrentHashMap<>();
    private static final Logger log = LoggerFactory.getLogger(IpRequestFlagger.class);

    private static final int THRESHOLD = 5;
    private static final Duration WINDOW = Duration.ofMinutes(1);


    public boolean isFlagged(String ip) {
        if (ip == null || ip.isBlank()) return false;

        Instant now = Instant.now();
        Deque<Instant> times = ipTimestamps.computeIfAbsent(ip, k -> new ConcurrentLinkedDeque<>());

        // Remove timestamps older than 1 minute (sliding window)
        while (!times.isEmpty() && times.peekFirst().isBefore(now.minus(WINDOW))) {
            times.pollFirst();
        }

        times.addLast(now);

        boolean flagged = times.size() > THRESHOLD;

        if (flagged) {
            log.warn("IP FLAGGED: {} ({} req/min)", ip, times.size());
        }

        return flagged;
    }
}
