package guzev.petproj.bl.services;

import guzev.petproj.bl.services.impl.SubscriberServiceImpl;
import guzev.petproj.dao.entities.Subscriber;
import guzev.petproj.dao.repositories.SubscriberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SubscriberServiceTests {

    @Mock
    private SubscriberRepository subscriberRepo;

    @InjectMocks
    private SubscriberServiceImpl subscriberService;

    private Subscriber testSubscriber;

    @BeforeEach
    public void setup() {
        testSubscriber = new Subscriber("test@email.com", "test-user");
    }

    @Test
    public void Create_ShouldBeCreated() {
        when(subscriberRepo.findById(testSubscriber.getEmail()))
                        .thenReturn(Optional.empty());
        when(subscriberRepo.save(testSubscriber))
                .thenReturn(testSubscriber);

        Subscriber createdSubscriber = subscriberService.create(testSubscriber);

        assertNotNull(createdSubscriber);
        assertEquals(testSubscriber.getEmail(), createdSubscriber.getEmail());
        assertEquals(testSubscriber.getUsername(), createdSubscriber.getUsername());

        verify(subscriberRepo, times(1)).save(testSubscriber);
    }

    @Test
    public void Create_ShouldThrowException_IfEmailDuplicated() {
        when(subscriberRepo.findById(testSubscriber.getEmail()))
                .thenReturn(Optional.of(testSubscriber));

        assertThrows(DuplicateKeyException.class, () -> subscriberService.create(testSubscriber));
    }

    @Test
    public void ReadByEmail_ShouldReturnSubscriber() {
        when(subscriberRepo.findById(testSubscriber.getEmail()))
                .thenReturn(Optional.of(testSubscriber));

        Subscriber retrievedSubscriber = subscriberService.readByEmail(testSubscriber.getEmail());

        assertNotNull(retrievedSubscriber);
        assertEquals(testSubscriber.getEmail(), retrievedSubscriber.getEmail());
    }

    @Test
    public void ReadByEmail_ShouldThrowException_IfNotExists() {
        when(subscriberRepo.findById(testSubscriber.getEmail()))
                .thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> subscriberService.readByEmail(testSubscriber.getEmail()));
    }

    @Test
    public void ReadAll_ShouldReturnSubscribers() {
        List<Subscriber> subscribers = List.of(new Subscriber("test1@email.com", "test-user1"),
                new Subscriber("test2@email.com", "test-user2"),
                new Subscriber("test3@email.com", "test-user3"));
        when(subscriberRepo.findAll(PageRequest.of(0,5)))
                .thenReturn(new PageImpl<>(subscribers));

        List<Subscriber> retrievedSubscribers = subscriberService.readAll(0, 5);

        assertNotNull(retrievedSubscribers);
        assertEquals(3, retrievedSubscribers.size());
    }

    @Test
    public void Update_ShouldBeUpdated() {
        when(subscriberRepo.save(testSubscriber))
                .thenReturn(testSubscriber);

        Subscriber updatedSubscriber = subscriberService.update(testSubscriber);

        assertNotNull(updatedSubscriber);
        assertEquals(testSubscriber.getEmail(), updatedSubscriber.getEmail());

        verify(subscriberRepo, times(1)).save(testSubscriber);
    }

    @Test
    public void Delete_ShouldBeDeleted() {

        subscriberService.delete(testSubscriber.getEmail());

        verify(subscriberRepo, times(1)).deleteById(testSubscriber.getEmail());
    }
}
