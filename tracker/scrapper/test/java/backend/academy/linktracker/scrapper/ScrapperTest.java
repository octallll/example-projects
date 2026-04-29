package backend.academy.linktracker.scrapper;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import backend.academy.linktracker.scrapper.controller.ScrapperController;
import backend.academy.linktracker.scrapper.dto.AddLinkRequest;
import backend.academy.linktracker.scrapper.dto.LinkResponse;
import backend.academy.linktracker.scrapper.dto.ListLinksResponse;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import backend.academy.linktracker.scrapper.service.LinkService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class ScrapperTest {

    private MockMvc mockMvc;
    private LinkRepository linkRepository;
    private LinkService linkService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        linkRepository = mock(LinkRepository.class);
        linkService = mock(LinkService.class);

        ScrapperController controller = new ScrapperController(linkRepository, linkService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    private final String linkUrl = "https://github.com/sveshnikov/project";

    @Test
    void test_3_1_AddLink() throws Exception {
        when(linkRepository.existsChat(1L)).thenReturn(true);

        AddLinkRequest addRequest = new AddLinkRequest().link(URI.create(linkUrl));

        mockMvc.perform(post("/links")
                        .header("Tg-Chat-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void test_3_2_GetLinks() throws Exception {
        when(linkRepository.existsChat(1L)).thenReturn(true);
        ListLinksResponse expectedResponse = new ListLinksResponse()
                .links(List.of(new LinkResponse().url(URI.create(linkUrl)).tags(List.of())))
                .size(1);

        when(linkService.getByTgChatId(1L)).thenReturn(expectedResponse);

        mockMvc.perform(get("/links").header("Tg-Chat-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.links[0].url").value(linkUrl));
    }

    @Test
    void test_3_4_AddLinkToNonExistentChat() throws Exception {
        when(linkRepository.existsChat(2L)).thenReturn(false);

        AddLinkRequest addRequest = new AddLinkRequest().link(URI.create("https://github.com/test/repo"));

        mockMvc.perform(post("/links")
                        .header("Tg-Chat-Id", 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void test_3_6_DeleteNonExistentChat() throws Exception {
        when(linkRepository.existsChat(404L)).thenReturn(false);

        mockMvc.perform(delete("/tg-chat/404")).andExpect(status().isNotFound());
    }
}
