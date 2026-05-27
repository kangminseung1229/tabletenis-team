package com.goldspin.team.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.goldspin.team.domain.Player;
import com.goldspin.team.domain.TeamGroup;

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

	public List<PlayerPair> createPartners(List<Player> players) {

		if (players.size() % 2 != 0) {
			throw new IllegalArgumentException("파트너 수는 짝수여야 합니다.");
		}

		// 부수별로 정렬
		List<Player> sortByRanks = players.stream().sorted(Comparator.comparingInt(Player::rank)).toList();

		List<Player> highList = sortByRanks.subList(0, sortByRanks.size() / 2);
		List<Player> lowList = sortByRanks.subList(sortByRanks.size() / 2, sortByRanks.size());
		List<Player> lowListCopy = new ArrayList<>(lowList);

		// 1. 결과 리스트 초기화
		List<PlayerPair> partners = new ArrayList<>();

		// 2. 중요: lowList를 무작위로 섞습니다.
		// Math.random()을 사용한 무작위 섞기가 가장 정확합니다.
		Collections.shuffle(lowListCopy);

		// 3. 짝짓기 (Matching)
		int size = highList.size();

		for (int i = 0; i < size; i++) {
			// highList의 i번째 요소와, 섞여서 i번째 자리에 온 lowList의 요소를 짝짓습니다.
			Player highPlayer = highList.get(i);
			Player lowPlayer = lowListCopy.get(i);

			partners.add(new PlayerPair(highPlayer, lowPlayer));

		}

		return partners;

	}

	public record PlayerPair(Player first, Player second) {
	}

}
