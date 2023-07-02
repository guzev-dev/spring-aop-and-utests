package guzev.petproj.bl.services;

import guzev.petproj.dao.entities.Article;

import java.util.List;

public interface ArticleService {

    Article create(Article article, String publisherName);

    Article readById(String id);

    Article readByTitleAndPublisherName(String title, String publisherName);

    List<Article> readAll(int page, int size);

    List<Article> readAllByPublisherName(String publisherName, int page, int size);

    Article update(Article article);

    void delete(String id);

}
