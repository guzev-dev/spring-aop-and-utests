package guzev.petproj.pl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import guzev.petproj.bl.services.ArticleService;
import guzev.petproj.dao.entities.Article;
import guzev.petproj.dao.entities.Publisher;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class ArticleControllerTests {

    private MockMvc mockMvc;

    @Mock
    private ArticleService articleService;

    @InjectMocks
    private ArticleController articleController;

    private Article testArticle;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        testArticle = new Article("test-article", "test-article-content");
        this.mockMvc = MockMvcBuilders.standaloneSetup(articleController).build();
    }

    @Test
    public void CreateArticle_ReturnCreatedArticle_IfRequestIsOk() throws Exception {
        final String json = objectMapper.writeValueAsString(testArticle);
        final Article article = new Article("test-article", "test-article-content");
        final Publisher publisher = new Publisher("test-publisher", "test-link", "test-redactor", "test-phone", "test-address");
        article.setPublisher(publisher);

        when(articleService.create(testArticle, "test-publisher"))
                .thenReturn(article);

        MvcResult requestResult = mockMvc.perform(post("/pet-proj/api/article/{publisherName}", publisher.getName())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andReturn();

        String resultString = requestResult.getResponse().getContentAsString();
        Article result = objectMapper.readValue(resultString, Article.class);

        assertEquals(article, result);
        verify(articleService, times(1)).create(testArticle, "test-publisher");
    }

    @Test
    public void CreateArticle_BadRequest_IfPropertyIsNull() throws Exception {
        testArticle.setContent(null);
        final String json = objectMapper.writeValueAsString(testArticle);

        mockMvc.perform(post("/pet-proj/api/article/{publisherName}", "test-publisher")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void ReadById_ShouldReturnArticle() throws Exception {
        ReflectionTestUtils.setField(testArticle, "id", "test-id");

        when(articleService.readById(testArticle.getId()))
                .thenReturn(testArticle);

        MvcResult requestResult = mockMvc.perform(get("/pet-proj/api/article/{id}", testArticle.getId()))
                .andExpect(status().isOk())
                .andReturn();

        String resultString = requestResult.getResponse().getContentAsString();
        Article result = objectMapper.readValue(resultString, Article.class);

        assertEquals(testArticle, result);
        verify(articleService, times(1)).readById(testArticle.getId());
    }

    @Test
    public void ReadById_ShouldThrowException_IfNotExists() throws Exception {
        ReflectionTestUtils.setField(testArticle, "id", "test-id");

        when(articleService.readById(testArticle.getId()))
                .thenThrow(NoSuchElementException.class);

        try {
            mockMvc.perform(get("/pet-proj/api/article/{id}", testArticle.getId()));
        } catch (ServletException e) {
            assertEquals(NoSuchElementException.class, e.getRootCause().getClass());
        }
    }

    @Test
    public void ReadByTitleAndPublisher_ShouldReturnArticle() throws Exception {
        final Publisher publisher = new Publisher("test-publisher", "test-link", "test-redactor", "test-phone", "test-address");
        testArticle.setPublisher(publisher);

        when(articleService.readByTitleAndPublisherName(testArticle.getTitle(), publisher.getName()))
                .thenReturn(testArticle);

        MvcResult requestResult = mockMvc.perform(get("/pet-proj/api/article/{publisherName}/{title}", publisher.getName(), testArticle.getTitle()))
                .andExpect(status().isOk())
                .andReturn();

        String resultString = requestResult.getResponse().getContentAsString();
        Article result = objectMapper.readValue(resultString, Article.class);

        assertEquals(testArticle, result);
        verify(articleService, times(1)).readByTitleAndPublisherName(testArticle.getTitle(), publisher.getName());
    }

    @Test
    public void ReadByTitleAndPublisher_ShouldThrowException_IfNotExists() throws Exception {
        when(articleService.readByTitleAndPublisherName(testArticle.getTitle(), "test-publisher"))
                .thenThrow(NoSuchElementException.class);

        try {
            mockMvc.perform(get("/pet-proj/api/article/{publisherName}/{title}", "test-publisher", testArticle.getTitle()));
        } catch (ServletException e) {
            assertEquals(NoSuchElementException.class, e.getRootCause().getClass());
        }
    }
}
