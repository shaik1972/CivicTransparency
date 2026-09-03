package com.civictransparency.identity.repository;

import com.civictransparency.identity.entity.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class RoleRepositoryIntegrationTest {

    @Autowired
    private RoleRepository roleRepository;

    @Test
    void shouldCreateAndFindRoleByCode() {
        Role role = new Role(
                "V3_TEST_ROLE",
                "V3 Test Role");

        Role saved = roleRepository.save(role);

        assertThat(saved.getRoleId()).isNotNull();

        Optional<Role> found = roleRepository.findByCode("V3_TEST_ROLE");

        assertThat(found).isPresent();
        assertThat(found.get().getRoleId())
                .isEqualTo(saved.getRoleId());
        assertThat(found.get().getName())
                .isEqualTo("V3 Test Role");
    }
}