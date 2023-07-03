package guzev.petproj.bl.services;

import guzev.petproj.bl.services.impl.PublisherServiceImpl;
import guzev.petproj.dao.entities.Publisher;
import guzev.petproj.dao.entities.Subscriber;
import guzev.petproj.dao.repositories.PublisherRepository;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PublisherServiceTests {

    @Mock
    private PublisherRepository publisherRepo;

    @Mock
    private SubscriberService subscriberService;

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private PublisherServiceImpl publisherService;

    private Publisher testPublisher;

    @BeforeEach
    public void setup() {
        testPublisher = new Publisher("test-publisher", "test-link", "test-redactor", "test-phone", "test-address");
    }

    @Test
    public void Create_ResultShouldBeSaved() {
        when(publisherRepo.findById(testPublisher.getName()))
                .thenReturn(Optional.empty());

        when(publisherRepo.save(testPublisher))
                .thenReturn(testPublisher);

        Publisher createdPublisher = publisherService.create(testPublisher);

        assertNotNull(createdPublisher);
        assertEquals(testPublisher.getName(), createdPublisher.getName());

        verify(publisherRepo, times(1)).save(testPublisher);
    }

    @Test
    public void Create_ShouldThrowException_IfEmailDuplicated() {
        when(publisherRepo.findById(testPublisher.getName()))
                .thenReturn(Optional.of(testPublisher));

        assertThrows(DuplicateKeyException.class, () -> publisherService.create(testPublisher));
    }

    @Test
    public void ReadByName_ShouldReturnPublisher() {
        when(publisherRepo.findById(testPublisher.getName()))
                .thenReturn(Optional.of(testPublisher));

        Publisher retrievedPublisher = publisherService.readByName(testPublisher.getName());

        assertNotNull(retrievedPublisher);
        assertEquals(testPublisher.getName(), retrievedPublisher.getName());
    }

    @Test
    public void ReadByName_ShouldThrowException_IfNotExists() {
        when(publisherRepo.findById(testPublisher.getName()))
                .thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> publisherService.readByName(testPublisher.getName()));
    }

    @Test
    public void ReadAll_ShouldReturnPublishers() {
        List<Publisher> publishers = List.of(testPublisher,
                new Publisher("test-publisher-2", "test-link-2", "test-redactor-2", "test-phone-2", "test-address-2"));

        when(publisherRepo.findAll(PageRequest.of(0, 5)))
                .thenReturn(new PageImpl<>(publishers));

        List<Publisher> retrievedPublishers = publisherService.readAll(0, 5);

        assertNotNull(retrievedPublishers);
        assertEquals(2, retrievedPublishers.size());
        assertInstanceOf(Publisher.class, retrievedPublishers.get(0));
    }

    @Test
    public void ReadSubscribedPublishers_ShouldReturnPublishers() {
        List<Publisher> publishers = List.of(testPublisher,
                new Publisher("test-publisher-2", "test-link-2", "test-redactor-2", "test-phone-2", "test-address-2"));
        Subscriber subscriber = new Subscriber("test-subscriber", "test-subscriber-link");

        when(subscriberService.readByEmail(subscriber.getEmail()))
                .thenReturn(subscriber);

        when(publisherRepo.findPublishersBySubscribersContains(subscriber, PageRequest.of(0, 5)))
                .thenReturn(new PageImpl<>(publishers));

        List<Publisher> retrievedPublishers = publisherService.readSubscribedPublishers(subscriber.getEmail(), 0, 5);

        assertNotNull(retrievedPublishers);
        assertEquals(2, retrievedPublishers.size());
        assertInstanceOf(Publisher.class, retrievedPublishers.get(0));
    }

    @Test
    public void NotifySubscribers_ShouldSendMails() {
        Set<Subscriber> subscribers = Set.of(new Subscriber("test-subscriber", "test-subscriber-username"));
        testPublisher.setSubscribers(subscribers);

        when(publisherRepo.findById(testPublisher.getName()))
                .thenReturn(Optional.of(testPublisher));

        when(mailSender.createMimeMessage())
                .thenReturn(new MimeMessage((Session) null));

        Integer res = publisherService.notifySubscribers(testPublisher.getName(), "test-message");

        assertEquals(res, 1);

        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    public void NotifySubscribers_ShouldThrowException_IfMessagingExceptionThrown() {
        Set<Subscriber> subscribers = Set.of(new Subscriber("test-subscriber", "test-subscriber-username"));
        testPublisher.setSubscribers(subscribers);

        MimeMessage mimeMessage = new MimeMessage((Session) null);

        when(publisherRepo.findById(testPublisher.getName()))
                .thenReturn(Optional.of(testPublisher));

        when(mailSender.createMimeMessage())
                .thenReturn(mimeMessage);

        doThrow(MailSendException.class).when(mailSender).send(any(MimeMessage.class));

        assertThrows(RuntimeException.class, () -> publisherService.notifySubscribers(testPublisher.getName(), "test-message"));
    }

    @Test
    public void Update_UpdatesShouldBeSaved() {
        when(publisherRepo.save(testPublisher))
                .thenReturn(testPublisher);

        publisherService.update(testPublisher);

        verify(publisherRepo, times(1)).save(testPublisher);
    }

    @Test
    public void Subscribe_ShouldSaveSubscriptionIfNotSubbed() {
        final Subscriber subscriber = new Subscriber("test@email.com", "test-subscriber-username");

        when(publisherRepo.findById(testPublisher.getName()))
                .thenReturn(Optional.of(testPublisher));

        when(subscriberService.readByEmail(subscriber.getEmail()))
                .thenReturn(subscriber);

        when(publisherRepo.save(testPublisher))
                .thenReturn(testPublisher);

        boolean result = publisherService.subscribe(testPublisher.getName(), subscriber.getEmail());

        assertTrue(result);
        verify(publisherRepo, times(1)).save(testPublisher);
    }

    @Test
    public void Subscribe_ShouldNotSaveSubscriptionIfAlreadySubbed() {
        final Subscriber subscriber = new Subscriber("test@email.com", "test-subscriber-username");
        final Set<Subscriber> subscribers = new HashSet<>();
        subscribers.add(subscriber);
        testPublisher.setSubscribers(subscribers);

        when(publisherRepo.findById(testPublisher.getName()))
                .thenReturn(Optional.of(testPublisher));

        when(subscriberService.readByEmail(subscriber.getEmail()))
                .thenReturn(subscriber);

        boolean result = publisherService.subscribe(testPublisher.getName(), subscriber.getEmail());

        assertTrue(result);
        verify(publisherRepo, times(0)).save(testPublisher);
    }

    @Test
    public void Subscribe_ShouldThrowIfNotFound() {
        when(publisherRepo.findById(testPublisher.getName()))
                .thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> publisherService.subscribe(testPublisher.getName(), "test@email.com"));
    }

    @Test
    public void Unsubscribe_ShouldSaveSubscriptionIfNotSubbed() {
        final Subscriber subscriber = new Subscriber("test@email.com", "test-subscriber-username");
        Set<Subscriber> subscribers = new HashSet<>();
        subscribers.add(subscriber);
        testPublisher.setSubscribers(subscribers);

        when(publisherRepo.findById(testPublisher.getName()))
                .thenReturn(Optional.of(testPublisher));

        when(subscriberService.readByEmail(subscriber.getEmail()))
                .thenReturn(subscriber);

        when(publisherRepo.save(testPublisher))
                .thenReturn(testPublisher);

        boolean result = publisherService.unsubscribe(testPublisher.getName(), subscriber.getEmail());

        assertTrue(result);
        verify(publisherRepo, times(1)).save(testPublisher);
    }

    @Test
    public void Unsubscribe_ShouldNotSaveSubscriptionIfNotSubbed() {
        final Subscriber subscriber = new Subscriber("test@email.com", "test-subscriber-username");

        when(publisherRepo.findById(testPublisher.getName()))
                .thenReturn(Optional.of(testPublisher));

        when(subscriberService.readByEmail(subscriber.getEmail()))
                .thenReturn(subscriber);

        boolean result = publisherService.unsubscribe(testPublisher.getName(), subscriber.getEmail());

        assertFalse(result);
        verify(publisherRepo, times(0)).save(testPublisher);
    }

    @Test
    public void Unsubscribe_ShouldThrowIfNotFound() {
        when(publisherRepo.findById(testPublisher.getName()))
                .thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> publisherService.unsubscribe(testPublisher.getName(), "test@email.com"));
    }

    @Test
    public void Delete_ResultShouldBeSaved() {
        publisherService.delete(testPublisher.getName());

        verify(publisherRepo, times(1)).deleteById(testPublisher.getName());
    }
}
