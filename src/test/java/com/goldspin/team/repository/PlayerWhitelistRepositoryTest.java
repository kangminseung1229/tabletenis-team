package com.goldspin.team.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.goldspin.team.domain.Player;

class PlayerWhitelistRepositoryTest {

	@TempDir
	java.nio.file.Path tempDir;

	@Test
	void insertUpdateDelete_persistsToFile() throws Exception {
		var storagePath = tempDir.resolve("players.csv");
		var repository = PlayerWhitelistRepository.withPersistedStorage(
				storagePath,
				List.of(new Player("강민승", 8)));

		repository.insert(new Player("김민준", 3));
		assertEquals(2, repository.findAll().size());

		repository.update("강민승", new Player("강민승", 9));
		assertEquals(9, repository.findByName("강민승").rank());

		repository.delete("김민준");
		assertEquals(1, repository.findAll().size());

		var reloaded = PlayerWhitelistRepository.loadPersistedStorage(storagePath);
		assertEquals(9, reloaded.findByName("강민승").rank());
	}

	@Test
	void loadOrSeed_copiesDefaultCsvWhenStorageMissing() throws Exception {
		var storagePath = tempDir.resolve("players.csv");
		var repository = PlayerWhitelistRepository.createWithStorage(storagePath);

		assertEquals(31, repository.findAll().size());
		assertEquals(8, repository.findByName("강민승").rank());
	}

	@Test
	void insert_throwsWhenNameAlreadyExists() {
		var repository = PlayerWhitelistRepository.forTest(List.of(new Player("강민승", 8)));
		assertThrows(IllegalArgumentException.class, () -> repository.insert(new Player("강민승", 3)));
	}
}
