package com.example;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SecretController.class)
class SecretControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    SecretService secretService;

    @Test
    void indexPopulatesModel() throws Exception {
        Map<String, Object> config = Map.of("key", "value");
        when(secretService.getSpringConfig()).thenReturn(config);

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("secret", (Object) null))
                .andExpect(model().attribute("configMap", config));

        verify(secretService).getSpringConfig();
        verifyNoMoreInteractions(secretService);
    }

    @Test
    void fetchPopulatesModel() throws Exception {
        Map<String, Object> config = Map.of("k", "v");
        when(secretService.fetchSecret("notation")).thenReturn("secret");
        when(secretService.getSpringConfig()).thenReturn(config);

        mockMvc.perform(post("/").param("notation", "notation"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("secret", "secret"))
                .andExpect(model().attribute("configMap", config));

        verify(secretService).fetchSecret("notation");
        verify(secretService).getSpringConfig();
        verifyNoMoreInteractions(secretService);
    }
}
