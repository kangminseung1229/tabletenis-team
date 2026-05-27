package com.goldspin.team.service;

import org.springframework.stereotype.Service;

import com.goldspin.team.domain.Player;
import com.goldspin.team.repository.PlayerWhitelistRepository;

@Service
public class PlayerWhitelistService {

	private static final int MIN_RANK = 1;
	private static final int MAX_RANK = 12;

	private final PlayerWhitelistRepository repository;

	public PlayerWhitelistService(PlayerWhitelistRepository repository) {
		this.repository = repository;
	}

	public Player insert(String name, int rank) {
		return repository.insert(new Player(name, validateRank(rank)));
	}

	public Player update(String currentName, String name, int rank) {
		return repository.update(currentName, new Player(name, validateRank(rank)));
	}

	public void delete(String name) {
		repository.delete(name);
	}

	private int validateRank(int rank) {
		if (rank < MIN_RANK || rank > MAX_RANK) {
			throw new IllegalArgumentException("부수는 " + MIN_RANK + "부부터 " + MAX_RANK + "부까지 입력할 수 있습니다.");
		}
		return rank;
	}
}
