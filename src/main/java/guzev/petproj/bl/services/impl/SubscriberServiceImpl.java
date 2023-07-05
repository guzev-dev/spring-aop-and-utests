package guzev.petproj.bl.services.impl;

import guzev.petproj.bl.services.SubscriberService;
import guzev.petproj.dao.entities.Subscriber;
import guzev.petproj.dao.repositories.SubscriberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class SubscriberServiceImpl implements SubscriberService {

    private final SubscriberRepository subscriberRepo;

    @Override
    public Subscriber create(Subscriber subscriber) {

        if (subscriberRepo.findById(subscriber.getEmail()).isPresent()) {
            throw new DuplicateKeyException("A user with entered email already exists in system.");
        }

        return subscriberRepo.save(subscriber);
    }

    @Override
    public Subscriber readByEmail(String email) {
        return subscriberRepo.findById(email)
                .orElseThrow();
    }

    @Override
    public List<Subscriber> readAll(int page, int size) {
        return subscriberRepo.findAll(PageRequest.of(page, size))
                .getContent();
    }

    @Override
    public Subscriber update(Subscriber subscriber) {
        return subscriberRepo.save(subscriber);
    }

    @Override
    public void delete(String email) {
        if (subscriberRepo.findById(email).isPresent())
            subscriberRepo.deleteById(email);
        else
            throw new NoSuchElementException();
    }
}
