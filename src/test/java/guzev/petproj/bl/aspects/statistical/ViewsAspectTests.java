package guzev.petproj.bl.aspects.statistical;

import guzev.petproj.dao.entities.Article;
import guzev.petproj.dao.entities.Publisher;
import guzev.petproj.dao.repositories.ArticleRepository;
import guzev.petproj.dao.repositories.PublisherRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ViewsAspectTests {

    @Mock
    private PublisherRepository publisherRepo;

    @Mock
    private ArticleRepository articleRepo;

    @InjectMocks
    private ViewsAspect viewsAspect;

    private static Long step = 5L;

    private Publisher testPublisher;

    private Article testArticle;

    @BeforeAll
    public static void init() throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        Class<?> viewsDifferenceClass = Class.forName("guzev.petproj.bl.aspects.statistical.ViewsAspect$ViewsDifference");
        Field field = viewsDifferenceClass.getDeclaredField("step");

        field.setAccessible(true);
        field.set(null, step);

    }

    @BeforeEach
    public void setup() {
        testPublisher = new Publisher("test-publisher", "test-link", "test-redactor", "test-phone", "test-address");
        testArticle = new Article("test-article", "test-article-content");
    }

    @Test
    public void AfterPublisherReadBuy_SaveResultWhenReachStep() {
        when(publisherRepo.save(any(Publisher.class)))
                .thenReturn(testPublisher);

        for (int i = 0; i < step; i++)
            viewsAspect.afterPublisherReadBy(ResponseEntity.ofNullable(testPublisher));

        verify(publisherRepo, times(1)).save(testPublisher);
    }

    @Test
    public void AfterPublisherReadBuy_NotSaveResultWhenStepNotReached() {
        viewsAspect.afterPublisherReadBy(ResponseEntity.ofNullable(testPublisher));

        verifyNoInteractions(publisherRepo);
    }

    @Test
    public void AfterArticleReadBuy_SaveResultWhenReachStep() {
        when(articleRepo.save(any(Article.class)))
                .thenReturn(testArticle);

        for (int i = 0; i < step; i++)
            viewsAspect.afterArticleReadBy(ResponseEntity.ofNullable(testArticle));

        verify(articleRepo, times(1)).save(testArticle);
    }

    @Test
    public void AfterArticleReadBuy_NotSaveResultWhenStepNotReached() {
        viewsAspect.afterArticleReadBy(ResponseEntity.ofNullable(testArticle));

        verifyNoInteractions(articleRepo);
    }

    @Test
    public void ReadBy_PointcutPresent() {
        ReflectionTestUtils.invokeMethod(viewsAspect, "readByPointcut");
    }

    @Test
    public void ReturnPublisherResponseEntity_PointcutPresent() {
        ReflectionTestUtils.invokeMethod(viewsAspect, "returnPublisherResponseEntity");
    }

    @Test
    public void ReturnArticleResponseEntity_PointcutPresent() {
        ReflectionTestUtils.invokeMethod(viewsAspect, "returnArticleResponseEntity");
    }


}
