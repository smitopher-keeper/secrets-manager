package com.example;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SecretController {

    private final SecretService secretService;

    public SecretController(SecretService secretService) {
        this.secretService = secretService;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("secret", null);
        model.addAttribute("configMap", secretService.getSpringConfig());
        return "index";
    }

    @PostMapping("/")
    public String fetch(@RequestParam("notation") String notation, Model model) {
        String secret = secretService.fetchSecret(notation);
        model.addAttribute("secret", secret);
        model.addAttribute("configMap", secretService.getSpringConfig());
        return "index";
    }
}
