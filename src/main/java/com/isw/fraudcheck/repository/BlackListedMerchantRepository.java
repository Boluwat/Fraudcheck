package com.isw.fraudcheck.repository;

import com.isw.fraudcheck.entity.BlackListedMerchant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlackListedMerchantRepository extends JpaRepository<BlackListedMerchant, String> {
}
