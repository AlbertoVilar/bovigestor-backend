package com.bovigestor.adapter.in.web.mapper;

import com.bovigestor.adapter.in.web.dto.BovineRequest;
import com.bovigestor.application.port.in.CreateBovineCommand;
import com.bovigestor.domain.model.BovineId;
import com.bovigestor.domain.model.BovineOrigin;
import com.bovigestor.domain.model.BovineRegistrationCategory;
import com.bovigestor.domain.model.BovineSex;
import com.bovigestor.domain.model.BovineStatus;
import com.bovigestor.domain.model.FarmId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BovineWebMapperTest {

    @Test
    void shouldMapEveryRequestFieldToApplicationCommand() {
        BovineRequest request = new BovineRequest(
                "Bovina Web Mapper",
                "BVL-26-985731",
                BovineSex.FEMALE,
                "Nelore",
                "Branca",
                LocalDate.of(2024, 5, 12),
                BovineStatus.ACTIVE,
                "BRINCO-985731",
                "REG-2024-985731",
                "ABC",
                BovineRegistrationCategory.PO,
                120L,
                121L,
                null,
                null,
                new BovineRequest.OriginRequest("Ana Silva", "Fazenda Boa Vista", 15L)
        );

        CreateBovineCommand command = BovineWebMapper.toCommand(request);

        assertThat(command).isEqualTo(new CreateBovineCommand(
                "Bovina Web Mapper",
                "BVL-26-985731",
                BovineSex.FEMALE,
                "Nelore",
                "Branca",
                LocalDate.of(2024, 5, 12),
                BovineStatus.ACTIVE,
                "BRINCO-985731",
                "REG-2024-985731",
                "ABC",
                BovineRegistrationCategory.PO,
                new BovineId(120L),
                new BovineId(121L),
                null,
                null,
                new BovineOrigin("Ana Silva", "Fazenda Boa Vista", new FarmId(15L))
        ));
    }

    @Test
    void shouldPreserveExternalParentReferencesAndNullLocalParentIds() {
        BovineRequest request = new BovineRequest(
                "Bovina Referência Externa",
                "BVL-26-985732",
                BovineSex.FEMALE,
                null,
                null,
                null,
                BovineStatus.ACTIVE,
                null,
                "REG-985732",
                "ABC",
                BovineRegistrationCategory.PO,
                null,
                null,
                "EXT-F-985732",
                "EXT-M-985732",
                new BovineRequest.OriginRequest("José Pereira", "Fazenda Santa Rita", null)
        );

        CreateBovineCommand command = BovineWebMapper.toCommand(request);

        assertThat(command.fatherId()).isNull();
        assertThat(command.motherId()).isNull();
        assertThat(command.fatherRegistrationNumber()).isEqualTo("EXT-F-985732");
        assertThat(command.motherRegistrationNumber()).isEqualTo("EXT-M-985732");
        assertThat(command.breed()).isNull();
        assertThat(command.coatColor()).isNull();
        assertThat(command.birthDate()).isNull();
        assertThat(command.earTag()).isNull();
        assertThat(command.origin()).isEqualTo(
                new BovineOrigin("José Pereira", "Fazenda Santa Rita", null)
        );
    }

    @Test
    void shouldRejectNullRequest() {
        assertThatThrownBy(() -> BovineWebMapper.toCommand(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("A solicitação de criação do bovino é obrigatória");
    }

    @Test
    void shouldRejectNullOriginRequest() {
        BovineRequest request = new BovineRequest(
                "Bovina sem origem",
                "BVL-26-985734",
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
        );

        assertThatThrownBy(() -> BovineWebMapper.toCommand(request))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("A origem do bovino é obrigatória");
    }

    @ParameterizedTest
    @ValueSource(longs = {0L, -1L})
    void shouldRejectInvalidFatherId(long id) {
        BovineRequest request = requestWithParents(id, null);

        assertThatThrownBy(() -> BovineWebMapper.toCommand(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O identificador deve ser positivo");
    }

    @ParameterizedTest
    @ValueSource(longs = {0L, -1L})
    void shouldRejectInvalidMotherId(long id) {
        BovineRequest request = requestWithParents(null, id);

        assertThatThrownBy(() -> BovineWebMapper.toCommand(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O identificador deve ser positivo");
    }

    private static BovineRequest requestWithParents(Long fatherId, Long motherId) {
        return new BovineRequest(
                "Bovina com pais",
                "BVL-26-985733",
                BovineSex.FEMALE,
                null,
                null,
                null,
                BovineStatus.ACTIVE,
                null,
                null,
                null,
                BovineRegistrationCategory.UNREGISTERED,
                fatherId,
                motherId,
                null,
                null,
                new BovineRequest.OriginRequest("Criador teste", "Fazenda teste", null)
        );
    }
}
