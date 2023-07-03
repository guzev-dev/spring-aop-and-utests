package guzev.petproj.bl.services.impl;

import guzev.petproj.bl.services.PublisherService;
import guzev.petproj.bl.services.SubscriberService;
import guzev.petproj.dao.entities.Publisher;
import guzev.petproj.dao.entities.Subscriber;
import guzev.petproj.dao.repositories.PublisherRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.PageRequest;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PublisherServiceImpl implements PublisherService {

    private final PublisherRepository publisherRepo;
    private final SubscriberService subscriberService;
    private final JavaMailSender mailSender;

    @Override
    public Publisher create(Publisher publisher) {

        if (publisherRepo.findById(publisher.getName()).isPresent()) {
            throw new DuplicateKeyException("A publisher with entered name already exists in system.");
        }

        return publisherRepo.save(publisher);
    }

    @Override
    public Publisher readByName(String name) {
        return publisherRepo.findById(name)
                .orElseThrow();
    }

    @Override
    public List<Publisher> readAll(int page, int size) {
        return publisherRepo.findAll(PageRequest.of(page, size))
                .getContent();
    }

    @Override
    public List<Publisher> readSubscribedPublishers(String email, int page, int size) {
        return publisherRepo.findPublishersBySubscribersContains(subscriberService.readByEmail(email), PageRequest.of(page, size))
                .getContent();
    }

    @Override
    public Integer notifySubscribers(String publisherName, String messageText) {

        Publisher publisher = publisherRepo.findById(publisherName)
                .orElseThrow();

        if (publisher.getSubscribers().size() != 0)
            sendMail(
                    publisher.getSubscribers().stream().map(Subscriber::getEmail).toArray(String[]::new),
                    messageText
            );

        return publisher.getSubscribers().size();
    }

    @Override
    public Publisher update(Publisher publisher) {
        return publisherRepo.save(publisher);
    }

    @Override
    public boolean subscribe(String publisherName, String subscriberEmail) {

        final Publisher publisher = publisherRepo.findById(publisherName)
                .orElseThrow();
        final Subscriber subscriber = subscriberService.readByEmail(subscriberEmail);

        if (publisher.getSubscribers().add(subscriber))
            publisherRepo.save(publisher);

        return true;
    }

    @Override
    public boolean unsubscribe(String publisherName, String subscriberEmail) {

        final Publisher publisher = publisherRepo.findById(publisherName)
                .orElseThrow();
        final Subscriber subscriber = subscriberService.readByEmail(subscriberEmail);

        if (publisher.getSubscribers().contains(subscriber)) {

            publisher.getSubscribers().remove(subscriber);
            publisherRepo.save(publisher);
            return true;
        } else
            return false;
    }

    @Override
    public void delete(String name) {
        publisherRepo.deleteById(name);
    }

    private void sendMail(String[] emails, String messageText) {
        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");

            helper.setFrom("PetProj");
            helper.setSubject("Article publication");
            helper.setTo(emails);
            helper.setText(messageText, true);

            mailSender.send(message);
        } catch (MessagingException | MailException e) {
            throw new RuntimeException("Cannot send emails:" + e.getMessage());
        }
    }
}
