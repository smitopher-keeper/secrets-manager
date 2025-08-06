package com.example;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * Controller that renders the example page and processes secret lookups.
 */
@Controller
public class SecretController {

  private final SecretService secretService;

  /**
   * Creates a new controller with access to the secret service.
   *
   * @param secretService service used to fetch secrets and configuration
   */
  public SecretController(SecretService secretService) {
    this.secretService = secretService;
  }

  /**
   * Displays the initial page with configuration values.
   *
   * @param model model used to render the view
   * @return view name
   */
  @GetMapping("/")
  String index(Model model) {
    model.addAttribute("secret", null);
    model.addAttribute("configMap", secretService.getSpringConfig());
    return "index";
  }

  /**
   * Handles form submission to fetch a secret for the provided notation.
   *
   * @param notation Keeper notation supplied by the user
   * @param model model used to render the view
   * @return view name
   */
  @PostMapping("/")
  String fetch(String notation, Model model) {
    String secret = secretService.fetchSecret(notation);
    model.addAttribute("secret", secret);
    model.addAttribute("configMap", secretService.getSpringConfig());
    return "index";
  }
}
