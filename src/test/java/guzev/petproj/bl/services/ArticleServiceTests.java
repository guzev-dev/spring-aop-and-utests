package guzev.petproj.bl.services;

import guzev.petproj.bl.services.impl.ArticleServiceImpl;
import guzev.petproj.dao.entities.Article;
import guzev.petproj.dao.entities.Publisher;
import guzev.petproj.dao.repositories.ArticleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ArticleServiceTests {

    @Mock
    private ArticleRepository articleRepo;

    @Mock
    private PublisherService publisherService;

    @InjectMocks
    private ArticleServiceImpl articleService;

    private Article testArticle;

    @BeforeEach
    public void setup() {
        testArticle = new Article("test-article", "test-article-content");
    }

    @Test
    public void Create_ShouldBeCreatedAndNotifySubscribers() {
        final String publisherName = "test-publisher";

        when(publisherService.readByName(publisherName))
                .thenReturn(new Publisher(publisherName, "test-publisher-link", "test-publisher-redactor", "test-publisher-phone", "test-publisher-address"));

        when(publisherService.notifySubscribers(anyString(), anyString()))
                .thenReturn(0);

        when(articleRepo.readArticleByTitleAndPublisherName(testArticle.getTitle(), publisherName))
                .thenReturn(Optional.empty());

        when(articleRepo.save(testArticle))
                .thenReturn(testArticle);

        Article createdArticle = articleService.create(testArticle, publisherName);

        assertNotNull(createdArticle);

        verify(articleRepo, times(1)).save(testArticle);
        verify(publisherService, times(1)).notifySubscribers(eq(publisherName), anyString());
    }

    @Test
    public void Create_ShouldThrowException_IfTitleAndPublisherDuplicated() {
        final String publisher = "test-publisher";

        when(articleRepo.readArticleByTitleAndPublisherName(testArticle.getTitle(), publisher))
                .thenReturn(Optional.of(testArticle));

        assertThrows(DuplicateKeyException.class, () -> articleService.create(testArticle, publisher));
    }

    @Test
    public void ReadById_ShouldReturnArticle() {
        ReflectionTestUtils.setField(testArticle, "id", "test-article-id");

        when(articleRepo.findById(testArticle.getId()))
                .thenReturn(Optional.of(testArticle));

        Article retrievedArticle = articleService.readById(testArticle.getId());

        assertNotNull(retrievedArticle);
        assertEquals(testArticle.getTitle(), retrievedArticle.getTitle());
    }

    @Test
    public void ReadById_ShouldThrowException_IfArticleNotExists() {
        ReflectionTestUtils.setField(testArticle, "id", "test-article-id");

        when(articleRepo.findById(testArticle.getId()))
                .thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> articleService.readById(testArticle.getId()));
    }

    @Test
    public void ReadByTitleAndPublisherName_ShouldReturnArticle() {
        final String publisher = "test-publisher";

        when(articleRepo.readArticleByTitleAndPublisherName(testArticle.getTitle(), publisher))
                .thenReturn(Optional.of(testArticle));

        Article retrievedArticle = articleService.readByTitleAndPublisherName(testArticle.getTitle(), publisher);

        assertNotNull(retrievedArticle);
        assertEquals(testArticle.getTitle(), retrievedArticle.getTitle());
    }

    @Test
    public void ReadByTitleAndPublisherName_ShouldThrowException_IfArticleNotExists() {
        final String publisher = "test-publisher";

        when(articleRepo.readArticleByTitleAndPublisherName(testArticle.getTitle(), publisher))
                .thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> articleService.readByTitleAndPublisherName(testArticle.getTitle(), publisher));
    }

    @Test
    public void ReadAll_ShouldReturnArticles() {
        List<Article> articles = List.of(new Article("test-article", "test-article-content"),
                new Article("test-article2", "test-article-content2"),
                new Article("test-article3", "test-article-content3"),
                new Article("test-article4", "test-article-content4"));

        when(articleRepo.findAll(PageRequest.of(0,5)))
                .thenReturn(new PageImpl<>(articles));

        List<Article> retrievedArticles = articleService.readAll(0,5);

        assertNotNull(retrievedArticles);
        assertEquals(4, retrievedArticles.size());
        assertInstanceOf(Article.class, retrievedArticles.get(0));
    }

    @Test
    public void ReadAllByPublisherName_ShouldReturnArticles() {
        final String publisherName = "test-publisher";
        List<Article> articles = List.of(new Article("test-article", "test-article-content"),
                new Article("test-article2", "test-article-content2"));

        when(articleRepo.readArticlesByPublisherName(publisherName, PageRequest.of(0,5)))
                .thenReturn(new PageImpl<>(articles));

        List<Article> retrievedArticles = articleService.readAllByPublisherName(publisherName, 0,5);

        assertNotNull(retrievedArticles);
        assertEquals(2, retrievedArticles.size());
        assertInstanceOf(Article.class, retrievedArticles.get(0));
    }

    @Test
    public void Update_UpdatesShouldBeSaved() {
        when(articleRepo.save(testArticle))
                .thenReturn(testArticle);

        Article updatedArticle = articleService.update(testArticle);

        assertNotNull(updatedArticle);
        assertEquals(testArticle.getTitle(), updatedArticle.getTitle());

        verify(articleRepo, times(1)).save(testArticle);
    }

    @Test
    public void Delete_ResultShouldBeSaved() {
        ReflectionTestUtils.setField(testArticle, "id", "test-article-id");

        when(articleRepo.findById(testArticle.getId()))
                .thenReturn(Optional.of(testArticle));

        articleService.delete(testArticle.getId());

        verify(articleRepo, times(1)).deleteById(testArticle.getId());
    }

    @Test
    public void Delete_ShouldThrowException_IfArticleNotExists() {
        when(articleRepo.findById(testArticle.getId()))
                .thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> articleService.delete(testArticle.getId()));
        verify(articleRepo, times(0)).deleteById(testArticle.getId());
    }



}
