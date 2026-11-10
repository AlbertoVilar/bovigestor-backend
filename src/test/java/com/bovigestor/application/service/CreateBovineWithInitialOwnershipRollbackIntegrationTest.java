package com.bovigestor.application.service;

import com.bovigestor.application.port.in.CreateBovineCommand;
import com.bovigestor.application.port.in.CreateBovineWithInitialOwnershipCommand;
import com.bovigestor.application.port.in.CreateBovineWithInitialOwnershipUseCase;
import com.bovigestor.application.port.out.SaveBovineOwnershipPeriodPort;
import com.bovigestor.domain.model.BovineOrigin;
import com.bovigestor.domain.model.BovineRegistrationCategory;
import com.bovigestor.domain.model.BovineSex;
import com.bovigestor.domain.model.BovineStatus;
import com.bovigestor.domain.model.FarmId;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

@SpringBootTest
@ActiveProfiles("test")
class CreateBovineWithInitialOwnershipRollbackIntegrationTest {
    private static final String INTERNAL_CODE = "RLB-26-999999";

    @Autowired CreateBovineWithInitialOwnershipUseCase useCase;
    @Autowired JdbcTemplate jdbcTemplate;
    @Autowired EntityManager entityManager;
    @MockitoBean SaveBovineOwnershipPeriodPort savePeriodPort;

    @Test
    void shouldRollBackBovineWhenSavingInitialPeriodFails() {
        jdbcTemplate.update("INSERT INTO farm (id, name, code_series, time_zone, status, created_at, updated_at) "
                + "VALUES (990001, 'Fazenda rollback', 'RLB', 'America/Sao_Paulo', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)");
        doAnswer(invocation -> {
            entityManager.flush();
            assertThat(jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM bovine WHERE internal_code = ?", Integer.class, INTERNAL_CODE
            )).isEqualTo(1);
            throw new IllegalStateException("falha simulada ao gravar o período");
        }).when(savePeriodPort).save(any());

        var command = new CreateBovineWithInitialOwnershipCommand(
                new CreateBovineCommand(
                        "Bovina rollback", INTERNAL_CODE, BovineSex.FEMALE,
                        null, null, null, BovineStatus.ACTIVE, null,
                        null, null, BovineRegistrationCategory.UNREGISTERED,
                        null, null, null, null,
                        new BovineOrigin("Criador", "Fazenda origem", null)
                ),
                new FarmId(990001L)
        );

        assertThatThrownBy(() -> useCase.create(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("falha simulada ao gravar o período");
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM bovine WHERE internal_code = ?", Integer.class, INTERNAL_CODE
        )).isZero();
    }
}
