package com.isw.fraudcheck.DTOs;

public record MerchantsStats(long txLast5MinCount,
                             long txLast10MinCount,
                             long tinyTxLast5MinCount) {
}
