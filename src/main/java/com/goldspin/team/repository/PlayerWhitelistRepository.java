package com.goldspin.team.repository;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.goldspin.team.domain.Player;

import jakarta.annotation.PostConstruct;

@Repository
public class PlayerWhitelistRepository {

	private static final String DEFAULT_PLAYERS_CSV = "players.csv";

	private final Path storagePath;
	private final ConcurrentMap<String, Player> players = new ConcurrentHashMap<>();
	private final boolean persistEnabled;

	@Autowired
	public PlayerWhitelistRepository(
			@Value("${team.players.file:data/players.csv}") String filePath) {
		this(Path.of(filePath), true);
	}

	public static PlayerWhitelistRepository forTest(List<Player> seedPlayers) {
		PlayerWhitelistRepository repository = new PlayerWhitelistRepository(
				Path.of("target/test-players.csv"),
				false);
		repository.replaceAll(seedPlayers);
		return repository;
	}

	static PlayerWhitelistRepository withPersistedStorage(Path storagePath, List<Player> seedPlayers) {
		PlayerWhitelistRepository repository = new PlayerWhitelistRepository(storagePath, true);
		repository.replaceAll(seedPlayers);
		repository.persist();
		return repository;
	}

	static PlayerWhitelistRepository loadPersistedStorage(Path storagePath) throws IOException {
		PlayerWhitelistRepository repository = new PlayerWhitelistRepository(storagePath, true);
		repository.reloadFromStorage();
		return repository;
	}

	static PlayerWhitelistRepository createWithStorage(Path storagePath) throws IOException {
		PlayerWhitelistRepository repository = new PlayerWhitelistRepository(storagePath, true);
		repository.loadOrSeed();
		return repository;
	}

	private PlayerWhitelistRepository(Path storagePath, boolean persistEnabled) {
		this.storagePath = storagePath;
		this.persistEnabled = persistEnabled;
	}

	@PostConstruct
	void init() throws IOException {
		if (!persistEnabled) {
			return;
		}
		loadOrSeed();
	}

	public List<Player> findAll() {
		return players.values().stream()
				.sorted(Comparator.comparing(Player::name))
				.toList();
	}

	public List<String> findAllNames() {
		return findAll().stream().map(Player::name).toList();
	}

	public List<Player> findByNames(List<String> names) {
		return names.stream()
				.distinct()
				.map(players::get)
				.filter(player -> player != null)
				.toList();
	}

	public Player findByName(String name) {
		return players.get(normalizeName(name));
	}

	public Player insert(Player player) {
		String name = normalizeName(player.name());
		if (players.containsKey(name)) {
			throw new IllegalArgumentException("이미 등록된 선수입니다: " + name);
		}
		Player saved = new Player(name, player.rank());
		players.put(name, saved);
		persist();
		return saved;
	}

	public Player update(String currentName, Player player) {
		String oldName = normalizeName(currentName);
		String newName = normalizeName(player.name());
		if (!players.containsKey(oldName)) {
			throw new IllegalArgumentException("선수를 찾을 수 없습니다: " + oldName);
		}
		if (!oldName.equals(newName) && players.containsKey(newName)) {
			throw new IllegalArgumentException("이미 등록된 선수입니다: " + newName);
		}
		players.remove(oldName);
		Player updated = new Player(newName, player.rank());
		players.put(newName, updated);
		persist();
		return updated;
	}

	public void delete(String name) {
		String normalized = normalizeName(name);
		if (players.remove(normalized) == null) {
			throw new IllegalArgumentException("선수를 찾을 수 없습니다: " + normalized);
		}
		persist();
	}

	void replaceAll(List<Player> seedPlayers) {
		players.clear();
		for (Player player : seedPlayers) {
			players.put(normalizeName(player.name()), new Player(normalizeName(player.name()), player.rank()));
		}
	}

	private void loadOrSeed() throws IOException {
		if (!Files.exists(storagePath)) {
			copyDefaultCsvToStorage();
		}
		loadFromPath(storagePath);
	}

	private void copyDefaultCsvToStorage() throws IOException {
		try (InputStream input = getClass().getClassLoader().getResourceAsStream(DEFAULT_PLAYERS_CSV)) {
			if (input == null) {
				throw new IllegalStateException("기본 선수 목록을 찾을 수 없습니다: classpath:" + DEFAULT_PLAYERS_CSV);
			}
			Files.createDirectories(storagePath.getParent());
			Files.copy(input, storagePath, StandardCopyOption.REPLACE_EXISTING);
		}
	}

	private void loadFromPath(Path path) throws IOException {
		List<Player> loaded = Files.readAllLines(path).stream()
				.map(String::trim)
				.filter(line -> !line.isEmpty() && !line.startsWith("#"))
				.map(this::parseLine)
				.toList();
		replaceAll(loaded);
	}

	private Player parseLine(String line) {
		String[] parts = line.split(",", 2);
		if (parts.length != 2) {
			throw new IllegalStateException("잘못된 선수 데이터 형식입니다: " + line);
		}
		return new Player(parts[0].trim(), Integer.parseInt(parts[1].trim()));
	}

	void reloadFromStorage() throws IOException {
		if (Files.exists(storagePath)) {
			loadFromPath(storagePath);
		}
	}

	synchronized void persist() {
		if (!persistEnabled) {
			return;
		}
		try {
			Files.createDirectories(storagePath.getParent());
			List<String> lines = findAll().stream()
					.map(player -> player.name() + "," + player.rank())
					.toList();
			Files.write(storagePath, lines);
		} catch (IOException e) {
			throw new IllegalStateException("선수 목록 저장에 실패했습니다: " + storagePath, e);
		}
	}

	private String normalizeName(String name) {
		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("이름은 비울 수 없습니다.");
		}
		return name.trim();
	}
}
