package com.goldspin.team.repository;

import com.goldspin.team.domain.Player;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
public class PlayerWhitelistRepository {
	private static final List<Player> WHITELIST = List.of(
		new Player("김두심", 9),
		new Player("노경인", 8),
		new Player("지청일", 8),
		new Player("김재균", 6),
		new Player("전성호", 6),
		new Player("박종백", 6),
		new Player("김광덕", 10),
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
		new Player("안성민", 7)
	);

	private static final Map<String, Player> PLAYER_BY_NAME = WHITELIST.stream()
		.collect(Collectors.toUnmodifiableMap(Player::name, Function.identity()));

	public List<Player> findAll() {
		return WHITELIST;
	}

	public List<String> findAllNames() {
		return WHITELIST.stream().map(Player::name).toList();
	}

	public List<Player> findByNames(List<String> names) {
		return names.stream()
			.distinct()
			.map(PLAYER_BY_NAME::get)
			.filter(player -> player != null)
			.toList();
	}
}
