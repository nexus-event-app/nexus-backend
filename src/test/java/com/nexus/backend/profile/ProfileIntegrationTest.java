package com.nexus.backend.profile;

import com.nexus.backend.entity.Discussion;
import com.nexus.backend.entity.Testimonial;
import com.nexus.backend.entity.User;
import com.nexus.backend.enums.DiscussionCategory;
import com.nexus.backend.repository.DiscussionRepository;
import com.nexus.backend.repository.TestimonialRepository;
import com.nexus.backend.repository.UserRepository;
import com.nexus.backend.security.JwtUtil;
import com.nexus.backend.service.AvatarStorage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Perfil público: qué se muestra, quién puede editarlo y cómo se procesan las fotos. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProfileIntegrationTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DiscussionRepository discussionRepository;
    @Autowired
    private TestimonialRepository testimonialRepository;
    @Autowired
    private AvatarStorage avatarStorage;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void editingRequiresLogin() throws Exception {
        mvc.perform(get("/api/profile")).andExpect(status().isUnauthorized());
        mvc.perform(put("/api/profile").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Ana\"}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(multipart("/api/profile/avatar").file(png("file", 10, 10))).andExpect(status().isUnauthorized());
    }

    @Test
    void updatesAndShowsThePublicProfileWithoutPrivateData() throws Exception {
        User ana = user("Ana López");
        mvc.perform(put("/api/profile").header("Authorization", bearer(ana))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"  Ana   García ","headline":"Estudiante de DAW","bio":"Me encanta el frontend.",
                                 "technologies":["React"," java ","react","Spring, Boot"],
                                 "githubUrl":"https://github.com/ana-dev","linkedinUrl":"https://www.linkedin.com/in/ana-garcia"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ana García"))
                .andExpect(jsonPath("$.technologies", contains("React", "java", "Spring Boot")));

        mvc.perform(get("/api/users/" + ana.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value("ana-garcia"))
                .andExpect(jsonPath("$.headline").value("Estudiante de DAW"))
                .andExpect(jsonPath("$.githubUrl").value("https://github.com/ana-dev"))
                .andExpect(jsonPath("$.memberSince").exists())
                .andExpect(jsonPath("$.email").doesNotExist())
                .andExpect(jsonPath("$.role").doesNotExist());
    }

    @Test
    void rejectsLinksThatAreNotProfiles() throws Exception {
        String ana = bearer(user("Ana"));
        for (String body : new String[]{
                "{\"name\":\"Ana\",\"githubUrl\":\"https://evil.example/github.com/ana\"}",
                "{\"name\":\"Ana\",\"githubUrl\":\"javascript:alert(1)\"}",
                "{\"name\":\"Ana\",\"linkedinUrl\":\"https://www.linkedin.com/company/acme\"}",
                "{\"name\":\"Ana\",\"linkedinUrl\":\"http://www.linkedin.com/in/ana\"}"}) {
            mvc.perform(put("/api/profile").header("Authorization", ana)
                            .contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    void disabledAccountsHaveNoPublicProfile() throws Exception {
        User user = user("Desactivada");
        user.setEnabled(false);
        userRepository.save(user);
        mvc.perform(get("/api/users/" + user.getId())).andExpect(status().isNotFound());
        mvc.perform(get("/api/users/" + user.getId() + "/discussions")).andExpect(status().isNotFound());
        mvc.perform(get("/api/users/999999")).andExpect(status().isNotFound());
    }

    @Test
    void listsOnlyPublishedDiscussionsAndTestimonials() throws Exception {
        User luis = user("Luis");
        discussion(luis, "Visible", true);
        discussion(luis, "Oculta", false);
        testimonial(luis, "Publicado y aprobado por el equipo de moderación de la comunidad.", true);
        testimonial(luis, "Pendiente de moderación todavía, no debería verse en el perfil público.", false);

        mvc.perform(get("/api/users/" + luis.getId()))
                .andExpect(jsonPath("$.discussionCount").value(1))
                .andExpect(jsonPath("$.testimonialCount").value(1));
        mvc.perform(get("/api/users/" + luis.getId() + "/discussions"))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title").value("Visible"));
        mvc.perform(get("/api/users/" + luis.getId() + "/testimonials"))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].authorId").value(luis.getId()));
    }

    @Test
    void avatarIsCroppedResizedAndStrippedOfMetadata() throws Exception {
        User ana = user("Ana");
        String token = bearer(ana);

        String body = mvc.perform(multipart("/api/profile/avatar")
                        .file(new MockMultipartFile("file", "foto.jpg", "image/jpeg", jpegWithExif(1200, 800)))
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.avatarUrl", containsString(AvatarStorage.PUBLIC_PATH)))
                .andReturn().getResponse().getContentAsString();
        String firstUrl = userRepository.findById(ana.getId()).orElseThrow().getAvatarUrl();
        assertThat(body).contains(firstUrl);

        Path first = stored(firstUrl);
        byte[] saved = Files.readAllBytes(first);
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(saved));
        assertThat(image.getWidth()).isEqualTo(256);
        assertThat(image.getHeight()).isEqualTo(256);
        assertThat(new String(saved, StandardCharsets.ISO_8859_1)).doesNotContain("Exif").doesNotContain("GPS-SECRETO");

        // Se sirve como archivo público.
        mvc.perform(get(firstUrl)).andExpect(status().isOk());
        // Las discusiones muestran la foto del autor.
        Long discussionId = discussion(ana, "Con foto", true).getId();
        mvc.perform(get("/api/discussions/" + discussionId)).andExpect(jsonPath("$.authorAvatarUrl").value(firstUrl));

        // Cambiar la foto borra la anterior; quitarla borra la actual.
        mvc.perform(multipart("/api/profile/avatar").file(png("file", 300, 300)).header("Authorization", token))
                .andExpect(status().isOk());
        String secondUrl = userRepository.findById(ana.getId()).orElseThrow().getAvatarUrl();
        assertThat(secondUrl).isNotEqualTo(firstUrl);
        assertThat(first).doesNotExist();

        mvc.perform(delete("/api/profile/avatar").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.avatarUrl").doesNotExist());
        assertThat(stored(secondUrl)).doesNotExist();
    }

    @Test
    void rejectsFilesThatAreNotImages() throws Exception {
        String token = bearer(user("Ana"));
        mvc.perform(multipart("/api/profile/avatar")
                        .file(new MockMultipartFile("file", "foto.png", "image/png", "<script>alert(1)</script>".getBytes()))
                        .header("Authorization", token))
                .andExpect(status().isBadRequest());
        mvc.perform(multipart("/api/profile/avatar")
                        .file(new MockMultipartFile("file", "foto.png", "image/png", new byte[0]))
                        .header("Authorization", token))
                .andExpect(status().isBadRequest());
    }

    private Path stored(String url) {
        return avatarStorage.directory().resolve(url.substring(AvatarStorage.PUBLIC_PATH.length()));
    }

    private User user(String name) {
        User user = new User();
        user.setName(name);
        user.setEmail("profile-" + UUID.randomUUID() + "@test.com");
        user.setPassword(passwordEncoder.encode("Clave12345"));
        user.setRole(User.Role.USER);
        user.setEnabled(true);
        user.setEmailVerifiedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    private String bearer(User user) {
        return "Bearer " + jwtUtil.generateToken(user.getEmail(), user.getRole().name(), user.getId());
    }

    private Discussion discussion(User author, String title, boolean active) {
        Discussion discussion = new Discussion();
        discussion.setTitle(title);
        discussion.setContent("Contenido de la discusión de prueba");
        discussion.setCategory(DiscussionCategory.GENERAL);
        discussion.setAuthor(author);
        discussion.setIsActive(active);
        return discussionRepository.save(discussion);
    }

    private void testimonial(User user, String content, boolean published) {
        Testimonial testimonial = new Testimonial();
        testimonial.setName(user.getName());
        testimonial.setContent(content);
        testimonial.setUser(user);
        testimonial.setIsActive(published);
        testimonial.setIsPending(!published);
        testimonialRepository.save(testimonial);
    }

    private static MockMultipartFile png(String field, int width, int height) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image(width, height), "png", out);
        return new MockMultipartFile(field, "foto.png", "image/png", out.toByteArray());
    }

    /** JPEG con un bloque EXIF (APP1) insertado tras la cabecera, como las fotos de móvil. */
    private static byte[] jpegWithExif(int width, int height) throws Exception {
        ByteArrayOutputStream plain = new ByteArrayOutputStream();
        ImageIO.write(image(width, height), "jpeg", plain);
        byte[] jpeg = plain.toByteArray();
        byte[] payload = "Exif\0\0GPS-SECRETO 41.40N 2.17E".getBytes(StandardCharsets.ISO_8859_1);
        int length = payload.length + 2;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(jpeg, 0, 2);
        out.write(new byte[]{(byte) 0xFF, (byte) 0xE1, (byte) (length >> 8), (byte) length});
        out.write(payload);
        out.write(jpeg, 2, jpeg.length - 2);
        return out.toByteArray();
    }

    private static BufferedImage image(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(new Color(79, 70, 229));
        graphics.fillRect(0, 0, width, height);
        graphics.dispose();
        return image;
    }
}
