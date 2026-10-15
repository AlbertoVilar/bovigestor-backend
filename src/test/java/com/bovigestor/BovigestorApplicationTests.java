package com.bovigestor;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class BovigestorApplicationTests {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void contextLoads() {
	}

	@Test
	void shouldRunFarmMigrationAndCreateFarmTableAndSequence() {
		Integer successfulVersionOneMigrations = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM \"flyway_schema_history\" "
						+ "WHERE \"version\" = '1' AND \"success\" = TRUE",
				Integer.class
		);
		Integer farmTables = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES "
						+ "WHERE LOWER(TABLE_SCHEMA) = 'public' AND LOWER(TABLE_NAME) = 'farm'",
				Integer.class
		);
		Integer farmSequences = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM INFORMATION_SCHEMA.SEQUENCES "
						+ "WHERE LOWER(SEQUENCE_SCHEMA) = 'public' "
						+ "AND LOWER(SEQUENCE_NAME) = 'farm_id_seq'",
				Integer.class
		);

		assertThat(successfulVersionOneMigrations).isEqualTo(1);
		assertThat(farmTables).isEqualTo(1);
		assertThat(farmSequences).isEqualTo(1);
	}

}
