package com.isw.fraudcheck.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;


@Getter
@Setter
@Entity
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
