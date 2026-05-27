package com.goldspin.team.repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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

	static final List<Player> DEFAULT_PLAYERS = List.of(
			new Player("김두심", 9),
			new Player("노경인", 8),
			new Player("지청일", 8),
			new Player("김재균", 6),
			new Player("전성호", 6),
			new Player("박종백", 6),
			new Player("김광덕", 9),
			new Player("임웅성", 6),
			new Player("이영주", 8),
			new Player("송영신", 12),
			new Player("이홍구", 6),
			new Player("권정택", 9),
			new Player("홍지용", 6),
			new Player("신선숙", 9),
			new Player("정원준", 8),
			new Player("이승학", 6),
			new Player("임금옥", 12),
			new Player("신만용", 7),
			new Player("강민승", 8),
			new Player("안성민", 7),
			new Player("최성운", 8),
			new Player("안영희", 11),
			new Player("엄기성", 8),
			new Player("김은숙", 9),
			new Player("황태규", 7),
			new Player("함소희", 11),
			new Player("이희진", 10),
			new Player("이동혁", 6),
			new Player("고성기", 7),
			new Player("이미경", 10),
			new Player("지해준", 8));

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
		if (Files.exists(storagePath)) {
			loadFromFile();
			return;
		}
		replaceAll(DEFAULT_PLAYERS);
		persist();
	}

	private void loadFromFile() throws IOException {
		List<Player> loaded = Files.readAllLines(storagePath).stream()
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
			loadFromFile();
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
