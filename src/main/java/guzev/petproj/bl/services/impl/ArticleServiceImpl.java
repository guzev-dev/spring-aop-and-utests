package guzev.petproj.bl.services.impl;

import guzev.petproj.bl.services.ArticleService;
import guzev.petproj.bl.services.PublisherService;
import guzev.petproj.dao.entities.Article;
import guzev.petproj.dao.entities.Publisher;
import guzev.petproj.dao.repositories.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.BinaryOperator;

@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    private final ArticleRepository articleRepo;
    private final PublisherService publisherService;

    @Override
    public Article create(Article article, String publisherName) {

        if (articleRepo.readArticleByTitleAndPublisherName(article.getTitle(), publisherName).isPresent()) {
            throw new DuplicateKeyException("An article with entered title already exists in system.");
        }

        Publisher publisher = publisherService.readByName(publisherName);
        article.setPublisher(publisher);
        final Article result = articleRepo.save(article);

        publisherService.notifySubscribers(publisherName,
                notificationMessage.apply(publisherName, article.getTitle()));

        return result;
    }


    @Override
    public Article readById(String id) {
        return articleRepo.findById(id)
                .orElseThrow();
    }

    @Override
    public Article readByTitleAndPublisherName(String title, String publisherName) {
        return articleRepo.readArticleByTitleAndPublisherName(title, publisherName)
                .orElseThrow();
    }

    @Override
    public List<Article> readAll(int page, int size) {
        return articleRepo.findAll(PageRequest.of(page, size))
                .getContent();
    }

    @Override
    public List<Article> readAllByPublisherName(String publisherName, int page, int size) {
        return articleRepo.readArticlesByPublisherName(publisherName, PageRequest.of(page, size))
                .getContent();
    }

    @Override
    public Article update(Article article) {
        return articleRepo.save(article);
    }

    @Override
    public void delete(String id) {
        articleRepo.deleteById(id);
    }

    private final BinaryOperator<String> notificationMessage = (publisherName, articleTitle) ->
            String.format("<h1 style=\"text-align: center; font: 3.5rem Garamond, Times New Roman;\"><i>New Publication</i></h1>\n" +
                    "    <hr>\n" +
                    "    <p style=\"text-align: justify; font: 2rem Georgia, sans-serif; margin-left: 10px;\">\n" +
                    "        <b>%s</b>\n" +
                    "        has released a new article:\n" +
                    "        <i>\"%s\"</i>\n" +
                    "    </p>", publisherName, articleTitle);

}
