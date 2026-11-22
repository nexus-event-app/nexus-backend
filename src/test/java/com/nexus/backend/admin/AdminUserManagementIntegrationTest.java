package com.nexus.backend.admin;

import com.nexus.backend.entity.User;
import com.nexus.backend.repository.UserRepository;
import com.nexus.backend.support.TestUsers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;


import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Desde el panel: dar y quitar el permiso de administración y editar el perfil de cualquier cuenta. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminUserManagementIntegrationTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TestUsers users;

    @Test
    void anAdminCanPromoteAndDemoteOtherAccounts() throws Exception {
        String admin = users.bearer(users.create("Persona de prueba", User.Role.ADMIN, true));
        User amiga = users.create("Persona de prueba", User.Role.USER, true);

        role(admin, amiga.getId(), "ADMIN").andExpect(status().isOk()).andExpect(jsonPath("$.role").value("ADMIN"));
        assertThat(userRepository.findById(amiga.getId()).orElseThrow().getRole()).isEqualTo(User.Role.ADMIN);
        // Ya puede entrar en el panel con su sesión de siempre.
        mvc.perform(get("/api/admin/users").header("Authorization", users.bearer(amiga))).andExpect(status().isOk());

        role(admin, amiga.getId(), "USER").andExpect(status().isOk()).andExpect(jsonPath("$.role").value("USER"));
        mvc.perform(get("/api/admin/users").header("Authorization", users.bearer(amiga))).andExpect(status().isForbidden());
    }

    @Test
    void nobodyChangesTheirOwnRoleSoThereIsAlwaysAnAdmin() throws Exception {
        User admin = users.create("Persona de prueba", User.Role.ADMIN, true);
        role(users.bearer(admin), admin.getId(), "USER").andExpect(status().isForbidden());
        assertThat(userRepository.findById(admin.getId()).orElseThrow().getRole()).isEqualTo(User.Role.ADMIN);
    }

    @Test
    void onlyVerifiedAccountsCanBecomeAdmins() throws Exception {
        String admin = users.bearer(users.create("Persona de prueba", User.Role.ADMIN, true));
        User unverified = users.create("Persona de prueba", User.Role.USER, false);
        role(admin, unverified.getId(), "ADMIN").andExpect(status().isBadRequest());
    }

    @Test
    void regularUsersCannotManageRoles() throws Exception {
        User someone = users.create("Persona de prueba", User.Role.USER, true);
        role(users.bearer(someone), someone.getId(), "ADMIN").andExpect(status().isForbidden());
    }

    @Test
    void anAdminCanEditAnyProfileIncludingOtherAdmins() throws Exception {
        String admin = users.bearer(users.create("Persona de prueba", User.Role.ADMIN, true));
        User otherAdmin = users.create("Persona de prueba", User.Role.ADMIN, true);
        otherAdmin.setAvatarUrl("/uploads/avatars/no-existe.jpg");
        userRepository.save(otherAdmin);

        mvc.perform(get("/api/admin/users/" + otherAdmin.getId() + "/profile").header("Authorization", admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Persona de prueba"));

        mvc.perform(put("/api/admin/users/" + otherAdmin.getId() + "/profile").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Nombre corregido","headline":"Titular","bio":"Presentación",
                                 "technologies":["Java"],"githubUrl":null,"linkedinUrl":null}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Nombre corregido"))
                .andExpect(jsonPath("$.technologies", contains("Java")));

        mvc.perform(delete("/api/admin/users/" + otherAdmin.getId() + "/avatar").header("Authorization", admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.avatarUrl").value(nullValue()));
        assertThat(userRepository.findById(otherAdmin.getId()).orElseThrow().getAvatarUrl()).isNull();
    }

    private ResultActions role(String token, Long userId, String role) throws Exception {
        return mvc.perform(put("/api/admin/users/" + userId + "/role").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).content("{\"role\":\"" + role + "\"}"));
    }
}
