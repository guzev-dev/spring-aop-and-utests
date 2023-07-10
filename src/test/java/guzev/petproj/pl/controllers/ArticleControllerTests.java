package guzev.petproj.pl.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
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

import java.util.*;

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
    public void ReadArticleById_ShouldReturnArticle() throws Exception {
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
    public void ReadArticleById_ShouldThrowException_IfNotExists() throws Exception {
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
    public void ReadArticleByTitleAndPublisher_ShouldReturnArticle() throws Exception {
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
    public void ReadArticleByTitleAndPublisher_ShouldThrowException_IfNotExists() throws Exception {
        when(articleService.readByTitleAndPublisherName(testArticle.getTitle(), "test-publisher"))
                .thenThrow(NoSuchElementException.class);

        try {
            mockMvc.perform(get("/pet-proj/api/article/{publisherName}/{title}", "test-publisher", testArticle.getTitle()));
        } catch (ServletException e) {
            assertEquals(NoSuchElementException.class, e.getRootCause().getClass());
        }
    }

    @Test
    public void ReadArticles_ShouldReturnListOfPublisherArticles_IfPublisherPresent() throws Exception {
        final Publisher publisher = new Publisher("test-publisher", "test-link", "test-redactor", "test-phone", "test-address");
        final List<Article> articles = new ArrayList<>(2);
        articles.add(new Article("test-article", "test-article-content"));
        articles.add(new Article("test-article2", "test-article-content2"));
        articles.forEach(article -> article.setPublisher(publisher));

        when(articleService.readAllByPublisherName(publisher.getName(), 3, 2))
                .thenReturn(articles);

        MvcResult requestResult = mockMvc.perform(get("/pet-proj/api/article")
                        .param("publisherName", publisher.getName()).param("page", "3").param("size", "2"))
                .andExpect(status().isOk())
                .andReturn();

        String resultString = requestResult.getResponse().getContentAsString();
        List<Article> result = objectMapper.readValue(resultString, new TypeReference<List<Article>>() {
        });

        assertEquals(result.size(), 2);
        assertInstanceOf(Article.class, result.get(0));
        assertEquals(publisher.getName(), result.get(0).getPublisher().getName());

        verify(articleService, times(1)).readAllByPublisherName(publisher.getName(), 3, 2);
    }

    @Test
    public void ReadArticles_ShouldReturnListOfArticles_IfPublisherNotPresent() throws Exception {
        final List<Article> articles = new ArrayList<>(4);
        articles.add(new Article("test-article", "test-article-content"));
        articles.add(new Article("test-article2", "test-article-content2"));
        articles.add(new Article("test-article3", "test-article-content3"));
        articles.add(new Article("test-article4", "test-article-content4"));

        when(articleService.readAll(10, 4))
                .thenReturn(articles);

        MvcResult requestResult = mockMvc.perform(get("/pet-proj/api/article")
                        .param("page", "10").param("size", "4"))
                .andExpect(status().isOk())
                .andReturn();

        String resultString = requestResult.getResponse().getContentAsString();
        List<Article> result = objectMapper.readValue(resultString, new TypeReference<List<Article>>() {
        });

        assertEquals(result.size(), 4);
        assertInstanceOf(Article.class, result.get(0));

        verify(articleService, times(1)).readAll(10, 4);
    }

    @Test
    public void ReadArticles_SetDefaultParams_IfParamsNotPresent() throws Exception {
        ReflectionTestUtils.setField(articleController, "defaultSize", 5);

        when(articleService.readAll(0, 5))
                .thenReturn(new ArrayList<>());

        mockMvc.perform(get("/pet-proj/api/article"))
                .andExpect(status().isOk());

        verify(articleService, times(1)).readAll(0, 5);
    }

    @Test
    public void ReadArticles_ShouldThrowException_IfParamsLessThanZero() throws Exception {
        when(articleService.readAll(-1, 5))
                .thenThrow(IllegalArgumentException.class);

        try {
            mockMvc.perform(get("/pet-proj/api/article")
                        .param("page", "-1").param("size", "5"))
                    .andExpect(status().isBadRequest());
        } catch (ServletException e) {
            assertEquals(IllegalArgumentException.class, e.getRootCause().getClass());
        }
    }

    @Test
    public void UpdateArticle_ReturnUpdatedArticle_IfRequestIsOk() throws Exception {
        final String json = objectMapper.writeValueAsString(testArticle);

        when(articleService.update(testArticle))
                .thenReturn(testArticle);

        MvcResult requestResult = mockMvc.perform(put("/pet-proj/api/article")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andReturn();

        String resultString = requestResult.getResponse().getContentAsString();
        Article result = objectMapper.readValue(resultString, Article.class);

        assertEquals(testArticle.getTitle(), result.getTitle());

        verify(articleService, times(1)).update(testArticle);
    }

    @Test
    public void UpdateArticle_BadRequest_IfPropertyIsNull() throws Exception {
        testArticle.setContent(null);
        final String json = objectMapper.writeValueAsString(testArticle);

        mockMvc.perform(put("/pet-proj/api/article")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(articleService);
    }

    @Test
    public void DeleteArticle_ResponseShouldBeTrue() throws Exception {
        ReflectionTestUtils.setField(testArticle, "id", "test-id");

        MvcResult requestResult = mockMvc.perform(delete("/pet-proj/api/article/{id}", testArticle.getId()))
                .andExpect(status().isOk())
                .andReturn();

        String resultString = requestResult.getResponse().getContentAsString();
        Map<String, Object> result = objectMapper.readValue(resultString, HashMap.class);

        assertTrue((Boolean) result.get("deleted"));

        verify(articleService, times(1)).delete(testArticle.getId());
    }

    @Test
    public void DeleteSubscriber_ShouldThrowException_IfSubscriberNotExists() throws Exception {
        ReflectionTestUtils.setField(testArticle, "id", "test-id");

        doThrow(NoSuchElementException.class).when(articleService).delete(testArticle.getId());

        try {
            mockMvc.perform(delete("/pet-proj/api/article/{id}", testArticle.getId()))
                    .andExpect(status().isBadRequest());
        } catch (ServletException e) {
            assertEquals(NoSuchElementException.class, e.getRootCause().getClass());
        }
    }


}
