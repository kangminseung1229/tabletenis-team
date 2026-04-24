package com.goldspin.team.controller;

import com.goldspin.team.domain.Player;
import com.goldspin.team.domain.TeamGroup;
import com.goldspin.team.repository.PlayerWhitelistRepository;
import com.goldspin.team.service.TeamBalancerService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
@Slf4j
public class TeamPageController {
	private static final String INDEX_VIEW = "index";
	private static final Pattern TAGIFY_VALUE_PATTERN = Pattern.compile("\"value\"\\s*:\\s*\"([^\"]+)\"");
	private static final String DEFAULT_SELECTED_NAMES = String.join(",",
			"지청일",
			"김재균",
			"전성호",
			"박종백",
			"김광덕",
			"임웅성",
			"이영주",
			"송영신",
			"권정택",
			"홍지용",
			"신선숙",
			"정원준",
			"이승학",
			"임금옥",
			"신만용",
			"안성민",
			"강민승");

	private final PlayerWhitelistRepository whitelistRepository;
	private final TeamBalancerService teamBalancerService;

	public TeamPageController(
			PlayerWhitelistRepository whitelistRepository,
			TeamBalancerService teamBalancerService) {
		this.whitelistRepository = whitelistRepository;
		this.teamBalancerService = teamBalancerService;
	}

	@GetMapping("/")
	public String index(Model model) {
		addBaseModelAttributes(model, DEFAULT_SELECTED_NAMES, 2);
		return INDEX_VIEW;
	}

	@PostMapping("/teams")
	public String buildTeams(
			@RequestParam(name = "selectedNames", required = false) String selectedNames,
			@RequestParam(name = "groupCount") int groupCount,
			Model model) {
		addBaseModelAttributes(model, selectedNames == null ? "" : selectedNames, groupCount);

		List<String> requestedNames = parseSelectedNames(selectedNames);
		List<Player> selectedPlayers = whitelistRepository.findByNames(requestedNames);
		if (selectedPlayers.size() != requestedNames.stream().distinct().count()) {
			log.error("화이트리스트에 없는 인원이 포함되어 있습니다. requestedNames: {}, selectedPlayers: {}", requestedNames,
					selectedPlayers);
			model.addAttribute("errorMessage", "화이트리스트에 없는 인원이 포함되어 있습니다.");

			return INDEX_VIEW;
		}

		try {
			List<TeamGroup> groups = teamBalancerService.createBalancedTeams(selectedPlayers, groupCount);
			model.addAttribute("groups", groups);
		} catch (IllegalArgumentException e) {
			model.addAttribute("errorMessage", e.getMessage());
		}
		return INDEX_VIEW;
	}

	private void addBaseModelAttributes(Model model, String selectedNames, int groupCount) {
		model.addAttribute("whitelistPlayers", whitelistRepository.findAll());
		model.addAttribute("selectedNames", selectedNames);
		model.addAttribute("groupCount", groupCount);
		model.addAttribute("whitelistNames", whitelistRepository.findAllNames());
	}

	private List<String> parseSelectedNames(String selectedNames) {
		if (selectedNames == null || selectedNames.isBlank()) {
			return List.of();
		}
		String trimmed = selectedNames.trim();
		if (trimmed.startsWith("[")) {
			Matcher matcher = TAGIFY_VALUE_PATTERN.matcher(trimmed);
			List<String> names = matcher.results()
					.map(result -> result.group(1).trim())
					.filter(name -> !name.isEmpty())
					.toList();
			if (!names.isEmpty()) {
				return names;
			}
			log.warn("selectedNames JSON 형식 파싱 결과가 비었습니다. CSV 파서로 재시도합니다. payload={}", selectedNames);
		}
		return List.of(selectedNames.split(",")).stream()
				.map(String::trim)
				.filter(name -> !name.isEmpty())
				.toList();
	}
}
