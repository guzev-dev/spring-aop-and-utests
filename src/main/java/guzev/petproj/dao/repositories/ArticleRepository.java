package guzev.petproj.dao.repositories;

import guzev.petproj.dao.entities.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ArticleRepository extends MongoRepository<Article, String> {

    Optional<Article> readArticleByTitleAndPublisherName(String title, String publisherName);

    Page<Article> readArticlesByPublisherName(String publisherName, Pageable pageable);

}
