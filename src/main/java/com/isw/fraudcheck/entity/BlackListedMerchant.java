package com.isw.fraudcheck.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;


@Entity
@Builder
@Data
@AllArgsConstructor
@Table(name="Black_listed_merchant")
@NoArgsConstructor
public class BlackListedMerchant {
    @Id
    private String merchantId;

    private String reason;

    @Column(updatable = false)
    private LocalDateTime blackListedAt = LocalDateTime.now();

}
