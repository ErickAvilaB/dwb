package com.product.api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.product.api.entity.AuthAccount;

public interface RepoAuthAccount extends JpaRepository<AuthAccount, Long> {

    Optional<AuthAccount> findByUsername(String username);
}
