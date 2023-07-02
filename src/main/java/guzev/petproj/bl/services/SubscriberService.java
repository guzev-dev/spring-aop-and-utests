package guzev.petproj.bl.services;

import guzev.petproj.dao.entities.Subscriber;

import java.util.List;

public interface SubscriberService {

    Subscriber create(Subscriber subscriber);

    Subscriber readByEmail(String email);

    List<Subscriber> readAll(int page, int size);

    Subscriber update(Subscriber subscriber);

    void delete(String email);

}
