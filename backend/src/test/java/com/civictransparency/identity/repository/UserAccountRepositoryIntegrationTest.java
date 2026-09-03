package com.civictransparency.identity.repository;

import com.civictransparency.identity.entity.UserAccount;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class UserAccountRepositoryIntegrationTest {

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Test
    void shouldCreateAndFindUserAccountByEmail() {
        UserAccount userAccount = new UserAccount(
                "v3-test@example.com",
                "ACTIVE");

        userAccount.setUsername("v3-test-user");

        UserAccount saved = userAccountRepository.save(userAccount);

        assertThat(saved.getUserId()).isNotNull();

        Optional<UserAccount> found = userAccountRepository.findByEmail(
                "v3-test@example.com");

        assertThat(found).isPresent();
        assertThat(found.get().getUserId())
                .isEqualTo(saved.getUserId());
        assertThat(found.get().getUsername())
                .isEqualTo("v3-test-user");
        assertThat(found.get().getStatus())
                .isEqualTo("ACTIVE");
    }
}