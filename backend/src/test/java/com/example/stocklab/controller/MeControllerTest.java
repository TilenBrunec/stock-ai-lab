package com.example.stocklab.controller;

import com.example.stocklab.config.SecurityConfig;
import com.example.stocklab.dto.MeDto;
import com.example.stocklab.service.UporabnikService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MeController.class)
@Import(SecurityConfig.class)
class MeControllerTest {

    static final UUID ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Autowired MockMvc mvc;

    @MockitoBean UporabnikService uporabnikService;
    @MockitoBean JwtDecoder jwtDecoder;

    MeDto me(boolean imaKljuc) {
        return new MeDto(ID, "test@example.com", "Test", null, "RAZISKOVALEC",
                imaKljuc, imaKljuc ? "a1b2" : null);
    }

    @Test
    void me_brezZetona_vrne401() throws Exception {
        mvc.perform(get("/api/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void me_neVraCaCelotnegaKljuca() throws Exception {
        when(uporabnikService.getOrCreate(any())).thenReturn(me(true));

        mvc.perform(get("/api/me").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imaKljuc").value(true))
                .andExpect(jsonPath("$.kljucZadnji4").value("a1b2"))
                .andExpect(jsonPath("$.kljuc").doesNotExist())
                .andExpect(jsonPath("$.openrouterKljucEnc").doesNotExist());
    }

    @Test
    void shraniKljuc_brezZetona_vrne401() throws Exception {
        mvc.perform(put("/api/me/openrouter-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"kljuc\":\"sk-or-v1-abc\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shraniKljuc_veljaven_vrne200() throws Exception {
        when(uporabnikService.shraniKljuc(any(), eq("sk-or-v1-veljaven-a1b2"))).thenReturn(me(true));

        mvc.perform(put("/api/me/openrouter-key").with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"kljuc\":\"sk-or-v1-veljaven-a1b2\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imaKljuc").value(true));
    }

    @Test
    void shraniKljuc_neveljaven_vrne400() throws Exception {
        when(uporabnikService.shraniKljuc(any(), any()))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "OpenRouter ključa ni sprejel"));

        mvc.perform(put("/api/me/openrouter-key").with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"kljuc\":\"napacen-kljuc\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void izbrisiKljuc_vrne200() throws Exception {
        when(uporabnikService.izbrisiKljuc(any())).thenReturn(me(false));

        mvc.perform(delete("/api/me/openrouter-key").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imaKljuc").value(false));
    }
}
