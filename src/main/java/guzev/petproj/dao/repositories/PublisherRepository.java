package guzev.petproj.dao.repositories;

import guzev.petproj.dao.entities.Publisher;
import guzev.petproj.dao.entities.Subscriber;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PublisherRepository extends MongoRepository<Publisher, String> {

    Page<Publisher> findPublishersBySubscribersContains(Subscriber subscriber, Pageable pageable);

}
