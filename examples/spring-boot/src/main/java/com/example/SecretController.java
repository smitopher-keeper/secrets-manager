package com.example;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class SecretController {

    private final SecretService secretService;

    public SecretController(SecretService secretService) {
        this.secretService = secretService;
    }

    @GetMapping("/")
    String index(Model model) {
        model.addAttribute("secret", null);
        model.addAttribute("configMap", secretService.getSpringConfig());
        return "index";
    }

    @PostMapping("/")
    String fetch(String notation, Model model) {
        String secret = secretService.fetchSecret(notation);
        model.addAttribute("secret", secret);
        model.addAttribute("configMap", secretService.getSpringConfig());
        return "index";
    }
}
