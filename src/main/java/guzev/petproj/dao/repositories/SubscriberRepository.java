package guzev.petproj.dao.repositories;

import guzev.petproj.dao.entities.Subscriber;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubscriberRepository extends MongoRepository<Subscriber, String> {
}
