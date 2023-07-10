package guzev.petproj.bl.aspects.statistical;

import guzev.petproj.bl.stats.ArticleStatsChanger;
import guzev.petproj.bl.stats.PublisherStatsChanger;
import guzev.petproj.dao.entities.Article;
import guzev.petproj.dao.entities.Publisher;
import guzev.petproj.dao.repositories.ArticleRepository;
import guzev.petproj.dao.repositories.PublisherRepository;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * <h3>Aspect that response for updating statistical information about views of a specific resource.</h3>
 * To optimize interaction with database aspect write changes to db only when difference in views
 * reaches <b>{@code stats.views.step}</b> value <i>(from application.properties)</i>.*/

@Aspect
@Component
@RequiredArgsConstructor
public class ViewsAspect {

    private final PublisherRepository publisherRepo;

    private final ArticleRepository articleRepo;

    private final Map<Publisher, ViewsDifference> publishersMap = new HashMap<>();
    private final Map<Article, ViewsDifference> articlesMap = new HashMap<>();

    @Pointcut("execution(public * guzev.petproj.pl.controllers.*.readBy*(..))")
    private void readByPointcut() {}

    @Pointcut("execution(public org.springframework.http.ResponseEntity<guzev.petproj.dao.entities.Publisher> *(..))")
    private void returnPublisherResponseEntity() {}

    @Pointcut("execution(public org.springframework.http.ResponseEntity<guzev.petproj.dao.entities.Article> *(..))")
    private void returnArticleResponseEntity() {}


    @AfterReturning(value = "readByPointcut() && returnPublisherResponseEntity()",
            returning = "publisher")
    public void afterPublisherReadBy(ResponseEntity<Publisher> publisher) {

        if (increaseViews(publisher.getBody(), publishersMap)) {
            PublisherStatsChanger updatedPublisher = new PublisherStatsChanger(publisher.getBody());

            synchronized (publishersMap) {
                Long count = publishersMap.remove(publisher.getBody())
                        .getViews();
                updatedPublisher.increaseViews(count);
            }

            publisherRepo.save(updatedPublisher);
        }
    }

    @AfterReturning(value = "readByPointcut() && returnArticleResponseEntity()",
            returning = "article")
    public void afterArticleReadBy(ResponseEntity<Article> article) {

        if (increaseViews(article.getBody(), articlesMap)) {

            ArticleStatsChanger updatedArticle = new ArticleStatsChanger(article.getBody());

            synchronized (articlesMap) {
                Long count = articlesMap.remove(article.getBody())
                        .getViews();
                updatedArticle.increaseViews(count);
            }

            articleRepo.save(updatedArticle);
        }
    }

    /**
     * @return <b>true</b> means that views difference between database stored and
     * application data reached <b>{@code stats.views.step}</b> value <i>(from application.properties)</i>.
     */
    private <O> boolean increaseViews(O statsObject, Map<O, ViewsDifference> viewsDifferenceMap) {
        ViewsDifference viewsDifference;

        if (viewsDifferenceMap.containsKey(statsObject)) {
            viewsDifference = viewsDifferenceMap.get(statsObject);
        } else {
            viewsDifference = new ViewsDifference();
            viewsDifferenceMap.put(statsObject, viewsDifference);
        }

        return viewsDifference.increaseViews();
    }

    @Value("${stats.views.step}")
    private void setStep(Long viewsStep) {
        ViewsDifference.step = viewsStep;
    }

    private static class ViewsDifference {

        private static Long step;

        private final AtomicLong views;

        public ViewsDifference() {
            this.views = new AtomicLong(0L);
        }

        public Long getViews() {
            return this.views.get();
        }

        //returns true if goal is reached
        public boolean increaseViews() {
            return this.views.addAndGet(1L) >= step;
        }
    }

}
