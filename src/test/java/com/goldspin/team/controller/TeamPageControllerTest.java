package com.goldspin.team.controller;

import com.goldspin.team.domain.Player;
import com.goldspin.team.repository.PlayerWhitelistRepository;
import com.goldspin.team.service.TeamBalancerService;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TeamPageControllerTest {

	@Test
	void buildTeams_returnsErrorWhenWhitelistValidationFails() {
		TeamPageController controller = new TeamPageController(
			new StubRepository(List.of(new Player("강민승", 8))),
			new TeamBalancerService()
		);
		Model model = new ExtendedModelMap();

		String view = controller.buildTeams("강민승,없는사람", 2, model);

		assertEquals("index", view);
		assertNotNull(model.getAttribute("errorMessage"));
	}

	@Test
	void buildTeams_returnsGroupsWhenValidInput() {
		TeamPageController controller = new TeamPageController(
			new StubRepository(List.of(new Player("강민승", 8), new Player("김민준", 3))),
			new TeamBalancerService()
		);
		Model model = new ExtendedModelMap();

		String view = controller.buildTeams("강민승,김민준", 2, model);

		assertEquals("index", view);
		assertNotNull(model.getAttribute("groups"));
	}

	@Test
	void renderPrintPage_returnsPrintViewWithPlayers() {
		TeamPageController controller = new TeamPageController(
			new StubRepository(List.of(new Player("강민승", 8), new Player("김민준", 3))),
			new TeamBalancerService()
		);
		Model model = new ExtendedModelMap();

		String view = controller.renderPrintPage("강민승,김민준", model);

		assertEquals("print-a4", view);
		assertNotNull(model.getAttribute("printPlayers"));
	}

	private static final class StubRepository extends PlayerWhitelistRepository {
		private final List<Player> players;

		private StubRepository(List<Player> players) {
			this.players = players;
		}

		@Override
		public List<Player> findAll() {
			return players;
		}

		@Override
		public List<String> findAllNames() {
			return players.stream().map(Player::name).toList();
		}

		@Override
		public List<Player> findByNames(List<String> names) {
			return players.stream().filter(player -> names.contains(player.name())).toList();
		}
	}
}
