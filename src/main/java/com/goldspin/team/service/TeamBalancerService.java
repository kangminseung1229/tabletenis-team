package com.goldspin.team.service;

import com.goldspin.team.domain.Player;
import com.goldspin.team.domain.TeamGroup;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TeamBalancerService {

	public List<TeamGroup> createBalancedTeams(List<Player> players, int groupCount) {
		validate(players, groupCount);

		List<Player> sortedPlayers = shuffleWithinSameRank(players);

		List<TeamGroup> groups = new ArrayList<>();
		for (int i = 1; i <= groupCount; i++) {
			groups.add(new TeamGroup(i));
		}

		for (int i = 0; i < sortedPlayers.size(); i++) {
			int groupIndex = i % groupCount;
			groups.get(groupIndex).addPlayer(sortedPlayers.get(i));
		}

		return groups;
	}

	private List<Player> shuffleWithinSameRank(List<Player> players) {
		Map<Integer, List<Player>> playersByRank = players.stream()
			.collect(Collectors.groupingBy(Player::rank));

		List<Player> result = new ArrayList<>();
		playersByRank.entrySet().stream()
			.sorted(Map.Entry.comparingByKey())
			.forEach(entry -> {
				List<Player> sameRankPlayers = new ArrayList<>(entry.getValue());
				Collections.shuffle(sameRankPlayers);
				result.addAll(sameRankPlayers);
			});

		return result;
	}

	private void validate(List<Player> players, int groupCount) {
		if (players == null || players.isEmpty()) {
			throw new IllegalArgumentException("최소 1명 이상 선택해야 합니다.");
		}
		if (groupCount < 1) {
			throw new IllegalArgumentException("조 개수는 1 이상이어야 합니다.");
		}
		if (groupCount > players.size()) {
			throw new IllegalArgumentException("조 개수는 선택 인원 수를 초과할 수 없습니다.");
		}
	}
}
