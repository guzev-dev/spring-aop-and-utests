package guzev.petproj.bl.aspects.statistical;

import guzev.petproj.bl.stats.PublisherStatsChanger;
import guzev.petproj.dao.entities.Publisher;
import guzev.petproj.dao.repositories.PublisherRepository;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Aspect
@Component
@RequiredArgsConstructor
public class MailingAspect {

    private final PublisherRepository publisherRepo;

    @Pointcut("execution(public Integer guzev.petproj.bl.services.PublisherService.notifySubscribers(..))")
    private void subscribersNotificationMailsPointcut() {}

    @Around(value = "subscribersNotificationMailsPointcut() && args(publisherName, messageText)",
            argNames = "proceedingJoinPoint,publisherName,messageText")
    public Object aroundSubscribersNotificationMails(ProceedingJoinPoint proceedingJoinPoint,
                                                     String publisherName, String messageText) throws Throwable {

        Integer result = (Integer)proceedingJoinPoint.proceed();

        Optional<Publisher> publisherToUpdate = publisherRepo.findById(publisherName);

        if (publisherToUpdate.isPresent() && result > 0) {
            PublisherStatsChanger updatedPublisher = new PublisherStatsChanger(publisherToUpdate.get());
            updatedPublisher.increaseMailsSent(result);
            publisherRepo.save(updatedPublisher);
        }

        return result;
    }
}