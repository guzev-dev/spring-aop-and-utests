package guzev.petproj.bl.stats;

import guzev.petproj.dao.entities.Publisher;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * <h3>Class to change <b>{@link Publisher}</b> statistics.</h3>
 * Programmer can't change statistics via {@link Publisher} super class. Only via this class.
 * */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PublisherStatsChanger extends Publisher {

    public PublisherStatsChanger(Publisher publisher) {
        super(publisher);
    }

    public void increaseViews(Long count) {
        setViews(getViews() + count);
    }

    public void increaseMailsSent(Integer count) {
        setMailsSent(getMailsSent() + count);
    }

}
