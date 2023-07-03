package guzev.petproj.bl.aspects.statistical;

import guzev.petproj.bl.stats.PublisherStatsChanger;
import guzev.petproj.dao.entities.Publisher;
import guzev.petproj.dao.repositories.PublisherRepository;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MailingAspectTests {

    @Mock
    private PublisherRepository publisherRepo;

    @InjectMocks
    private MailingAspect mailingAspect;

    private ProceedingJoinPoint joinPoint;

    @BeforeEach
    public void setup() {
        joinPoint = mock(ProceedingJoinPoint.class);
    }

    @Test
    public void AroundSubscribersNotificationMails_SaveResult() throws Throwable {
        final Publisher publisher = new Publisher("test-publisher", "test-link", "test-redactor", "test-phone", "test-address");
        final Integer num = 5;

        when(joinPoint.proceed())
                .thenReturn(num);

        when(publisherRepo.findById(publisher.getName()))
                .thenReturn(Optional.of(publisher));

        when(publisherRepo.save(any(Publisher.class)))
                .thenReturn(publisher);

        Integer result = (Integer) mailingAspect.aroundSubscribersNotificationMails(joinPoint, publisher.getName(), "test-message");

        assertEquals(result, num);
        verify(joinPoint, times(1)).proceed();
        verify(publisherRepo, times(1)).save(any(PublisherStatsChanger.class));
    }

    @Test
    public void AroundSubscribersNotificationMails_NotSaveIfResultLessThanOne() throws Throwable {
        when(joinPoint.proceed())
                .thenReturn(0);

        mailingAspect.aroundSubscribersNotificationMails(joinPoint, "test-name", "test-message");

        verify(joinPoint, times(1)).proceed();
        verifyNoInteractions(publisherRepo);
    }

    @Test
    public void SubscribersNotificationMails_PointcutPresent() {
        ReflectionTestUtils.invokeMethod(mailingAspect, "subscribersNotificationMailsPointcut");
    }

}
