package guzev.petproj.bl.stats;

import guzev.petproj.dao.entities.Article;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * <h3>Class to change <b>{@link Article}</b> statistics.</h3>
 * Programmer can't change statistics via {@link Article} super class. Only via this class.
 * */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ArticleStatsChanger extends Article {

    public ArticleStatsChanger(Article article) {
        super(article);
    }

    public void increaseViews(Long count) {
        setViews(getViews() + count);
    }
}
