package com.goldspin.team.service;

import com.goldspin.team.domain.Player;
import com.goldspin.team.domain.TeamGroup;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TeamBalancerServiceTest {

	private final TeamBalancerService teamBalancerService = new TeamBalancerService();

	@Test
	void createBalancedTeams_distributesPlayersAcrossGroups() {
		List<Player> players = List.of(
			new Player("A", 1),
			new Player("B", 2),
			new Player("C", 9),
			new Player("D", 10),
			new Player("E", 11)
		);

		List<TeamGroup> groups = teamBalancerService.createBalancedTeams(players, 2);

		assertEquals(2, groups.size());
		assertEquals(3, groups.get(0).getPlayers().size());
		assertEquals(2, groups.get(1).getPlayers().size());
	}

	@Test
	void createBalancedTeams_handlesNonDivisibleCounts() {
		List<Player> players = List.of(
			new Player("A", 1),
			new Player("B", 3),
			new Player("C", 5),
			new Player("D", 7),
			new Player("E", 9),
			new Player("F", 11),
			new Player("G", 12)
		);

		List<TeamGroup> groups = teamBalancerService.createBalancedTeams(players, 3);
		int maxSize = groups.stream().mapToInt(group -> group.getPlayers().size()).max().orElse(0);
		int minSize = groups.stream().mapToInt(group -> group.getPlayers().size()).min().orElse(0);

		assertEquals(1, maxSize - minSize);
	}

	@Test
	void createBalancedTeams_throwsWhenGroupCountIsGreaterThanPlayers() {
		List<Player> players = List.of(new Player("A", 8));
		assertThrows(IllegalArgumentException.class, () -> teamBalancerService.createBalancedTeams(players, 2));
	}
}
