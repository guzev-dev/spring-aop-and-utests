package guzev.petproj.bl.services;

import guzev.petproj.dao.entities.Publisher;

import java.util.List;

public interface PublisherService {

    Publisher create(Publisher publisher);

    Publisher readByName(String name);

    List<Publisher> readAll(int page, int size);

    List<Publisher> readSubscribedPublishers(String email, int page, int size);

    Integer notifySubscribers(String publisherName, String messageText);

    Publisher update(Publisher publisher);

    boolean subscribe(String publisherName, String subscriberEmail);

    boolean unsubscribe(String publisherName, String subscriberEmail);

    void delete(String name);

}
