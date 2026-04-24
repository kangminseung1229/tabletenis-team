package com.goldspin.team.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TeamGroup {
	private final int groupNumber;
	private final List<Player> players = new ArrayList<>();

	public TeamGroup(int groupNumber) {
		this.groupNumber = groupNumber;
	}

	public int getGroupNumber() {
		return groupNumber;
	}

	public List<Player> getPlayers() {
		return Collections.unmodifiableList(players);
	}

	public void addPlayer(Player player) {
		players.add(player);
	}

	public double getAverageRank() {
		if (players.isEmpty()) {
			return 0.0;
		}
		int sum = players.stream().mapToInt(Player::rank).sum();
		return (double) sum / players.size();
	}
}
