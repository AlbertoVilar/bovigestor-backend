package com.bovigestor.application.port.in;

import com.bovigestor.domain.model.BovineOrigin;
import com.bovigestor.domain.model.BovineRegistrationCategory;
import com.bovigestor.domain.model.BovineSex;
import com.bovigestor.domain.model.BovineStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CreateBovineCommandTest {

    @Test
    void shouldRejectNullOrigin() {
        assertThatThrownBy(() -> new CreateBovineCommand(
                "Mimosa",
                "BVL-26-0001",
                BovineSex.FEMALE,
                null,
                null,
                null,
                BovineStatus.ACTIVE,
                null,
                null,
                null,
                BovineRegistrationCategory.UNREGISTERED,
                null,
                null,
                null,
                null,
                null
        ))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("A origem do bovino é obrigatória");
    }

    @Test
    void shouldPreserveOrigin() {
        BovineOrigin origin = new BovineOrigin(
                "Criador de teste",
                "Fazenda de teste",
                null
        );

        CreateBovineCommand command = new CreateBovineCommand(
                "Mimosa",
                "BVL-26-0001",
                BovineSex.FEMALE,
                null,
                null,
                null,
                BovineStatus.ACTIVE,
                null,
                null,
                null,
                BovineRegistrationCategory.UNREGISTERED,
                null,
                null,
                null,
                null,
                origin
        );

        org.assertj.core.api.Assertions.assertThat(command.origin()).isSameAs(origin);
    }
}
