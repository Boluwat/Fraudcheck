package com.isw.fraudcheck.DTOs;

public record CardStats(long txLast1MinCount,
                        long txLast24hCount,
                        double totalAmount24h,
                        double avgAmount24h) {
}
