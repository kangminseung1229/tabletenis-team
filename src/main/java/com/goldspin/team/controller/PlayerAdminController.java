package com.goldspin.team.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.goldspin.team.repository.PlayerWhitelistRepository;
import com.goldspin.team.service.PlayerWhitelistService;

@Controller
@RequestMapping("/admin/players")
public class PlayerAdminController {

	private static final String ADMIN_VIEW = "admin-players";
	private static final String ERROR_MESSAGE_ATTRIBUTE = "errorMessage";
	private static final String SUCCESS_MESSAGE_ATTRIBUTE = "successMessage";

	private final PlayerWhitelistRepository repository;
	private final PlayerWhitelistService whitelistService;

	public PlayerAdminController(
			PlayerWhitelistRepository repository,
			PlayerWhitelistService whitelistService) {
		this.repository = repository;
		this.whitelistService = whitelistService;
	}

	@GetMapping
	public String list(Model model) {
		addListModel(model);
		return ADMIN_VIEW;
	}

	@PostMapping
	public String insert(
			@RequestParam String name,
			@RequestParam int rank,
			Model model) {
		try {
			whitelistService.insert(name, rank);
			model.addAttribute(SUCCESS_MESSAGE_ATTRIBUTE, "선수를 등록했습니다.");
		} catch (IllegalArgumentException e) {
			model.addAttribute(ERROR_MESSAGE_ATTRIBUTE, e.getMessage());
		}
		addListModel(model);
		return ADMIN_VIEW;
	}

	@PostMapping("/update")
	public String update(
			@RequestParam String currentName,
			@RequestParam String name,
			@RequestParam int rank,
			Model model) {
		try {
			whitelistService.update(currentName, name, rank);
			model.addAttribute(SUCCESS_MESSAGE_ATTRIBUTE, "선수 정보를 수정했습니다.");
		} catch (IllegalArgumentException e) {
			model.addAttribute(ERROR_MESSAGE_ATTRIBUTE, e.getMessage());
		}
		addListModel(model);
		return ADMIN_VIEW;
	}

	@PostMapping("/delete")
	public String delete(@RequestParam String name, Model model) {
		try {
			whitelistService.delete(name);
			model.addAttribute(SUCCESS_MESSAGE_ATTRIBUTE, "선수를 삭제했습니다.");
		} catch (IllegalArgumentException e) {
			model.addAttribute(ERROR_MESSAGE_ATTRIBUTE, e.getMessage());
		}
		addListModel(model);
		return ADMIN_VIEW;
	}

	private void addListModel(Model model) {
		model.addAttribute("players", repository.findAll());
	}
}
