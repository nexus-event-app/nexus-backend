package com.nexus.backend.profile;

import com.nexus.backend.entity.Discussion;
import com.nexus.backend.entity.Testimonial;
import com.nexus.backend.entity.User;
import com.nexus.backend.enums.DiscussionCategory;
import com.nexus.backend.repository.DiscussionRepository;
import com.nexus.backend.repository.TestimonialRepository;
import com.nexus.backend.repository.UserRepository;
import com.nexus.backend.service.AvatarStorage;
import com.nexus.backend.support.TestUsers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
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
    private TestUsers users;

    @Test
    void editingRequiresLogin() throws Exception {
        mvc.perform(get("/api/profile")).andExpect(status().isUnauthorized());
        mvc.perform(put("/api/profile").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Ana\"}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(multipart("/api/profile/avatar").file(png("file", 10, 10))).andExpect(status().isUnauthorized());
    }

    @Test
    void updatesAndShowsThePublicProfileWithoutPrivateData() throws Exception {
        User ana = users.create("Ana López", User.Role.USER, true);
        mvc.perform(put("/api/profile").header("Authorization", users.bearer(ana))
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
        String ana = users.bearer(users.create("Ana", User.Role.USER, true));
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
        User user = users.create("Desactivada", User.Role.USER, true);
        user.setEnabled(false);
        userRepository.save(user);
        mvc.perform(get("/api/users/" + user.getId())).andExpect(status().isNotFound());
        mvc.perform(get("/api/users/999999")).andExpect(status().isNotFound());
    }

    @Test
    void activityIsOnlyVisibleToItsOwner() throws Exception {
        User luis = users.create("Luis", User.Role.USER, true);
        discussion(luis, "Visible", true);
        discussion(luis, "Oculta", false);
        testimonial(luis, "Publicado y aprobado por el equipo de moderación de la comunidad.", true);
        testimonial(luis, "Pendiente de moderación todavía, no debería verse en el perfil público.", false);

        mvc.perform(get("/api/users/" + luis.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Luis"))
                .andExpect(jsonPath("$.discussionCount").doesNotExist())
                .andExpect(jsonPath("$.testimonialCount").doesNotExist());
        mvc.perform(get("/api/users/" + luis.getId() + "/discussions")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/users/" + luis.getId() + "/testimonials")).andExpect(status().isUnauthorized());
        String other = users.bearer(users.create("Otra", User.Role.USER, true));
        mvc.perform(get("/api/users/" + luis.getId() + "/discussions").header("Authorization", other))
                .andExpect(status().isNotFound());

        String token = users.bearer(luis);
        mvc.perform(get("/api/profile").header("Authorization", token))
                .andExpect(jsonPath("$.discussionCount").value(1))
                .andExpect(jsonPath("$.testimonialCount").value(1));
        mvc.perform(get("/api/profile/discussions").header("Authorization", token))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title").value("Visible"));
        mvc.perform(get("/api/profile/testimonials").header("Authorization", token))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].authorId").value(luis.getId()));
        mvc.perform(get("/api/profile/discussions")).andExpect(status().isUnauthorized());
    }

    @Test
    void avatarIsCroppedResizedAndStrippedOfMetadata() throws Exception {
        User ana = users.create("Ana", User.Role.USER, true);
        String token = users.bearer(ana);

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

        mvc.perform(get(firstUrl)).andExpect(status().isOk());
        Long discussionId = discussion(ana, "Con foto", true).getId();
        mvc.perform(get("/api/discussions/" + discussionId)).andExpect(jsonPath("$.authorAvatarUrl").value(firstUrl));

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
        String token = users.bearer(users.create("Ana", User.Role.USER, true));
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
