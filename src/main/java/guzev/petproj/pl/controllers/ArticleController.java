package guzev.petproj.pl.controllers;

import guzev.petproj.bl.services.ArticleService;
import guzev.petproj.dao.entities.Article;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("pet-proj/api/article")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    @Value("${page.result.size}")
    private Integer defaultSize;

    @PostMapping("/{publisherName}")
    public ResponseEntity<Article> createArticle(@RequestBody Article article,
                                                 @PathVariable("publisherName") String publisherName) {

        return ResponseEntity.ok(articleService.create(article, publisherName));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Article> readById(@PathVariable("id") String id) {

        return ResponseEntity.ok(articleService.readById(id));
    }

    @GetMapping("/{publisherName}/{title}")
    public ResponseEntity<Article> readByTitleAndPublisherName(@PathVariable(value = "title") String title,
                                                                    @PathVariable(value = "publisherName") String publisherName) {

        return ResponseEntity.ok(articleService.readByTitleAndPublisherName(title, publisherName));
    }

    @GetMapping
    public ResponseEntity<List<Article>> read(@RequestParam(value = "publisherName", required = false) Optional<String> publisherName,
                                              @RequestParam(value = "page", defaultValue = "0") Integer page,
                                            @RequestParam(value = "size", required = false) Optional<Integer> size) {

        return publisherName.map(s -> ResponseEntity.ok(articleService.readAllByPublisherName(s, page, size.orElse(defaultSize))))
                .orElseGet(() -> ResponseEntity.ok(articleService.readAll(page, size.orElse(defaultSize))));
    }

    @PutMapping
    public ResponseEntity<Article> updateArticle(@RequestBody Article article) {

        return ResponseEntity.ok(articleService.update(article));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteArticle(@PathVariable("id") String id) {

        articleService.delete(id);

        return ResponseEntity.ok(Map.of("deleted", true));
    }

}