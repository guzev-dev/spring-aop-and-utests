package guzev.petproj.bl.aspects;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;


@Aspect
@Component
public class ExceptionLoggingAspect {

    private final Set<Class<? extends Throwable>> excludedExceptions = Set.of(
            NoSuchElementException.class,
            DuplicateKeyException.class
    );

    @Pointcut("execution(* guzev.petproj.pl..*(..))")
    private void exceptionOccurredPointcut() {}

    @AfterThrowing(value = "exceptionOccurredPointcut()",
            throwing = "exception")
    public void logException(JoinPoint joinPoint, Throwable exception) {

        if (excludedExceptions.contains(exception.getClass())) {
            return;
        }

        String message = "Error occurred:\n" + "\tMETHOD: " + joinPoint.getSignature().toLongString() +
                "\n\tINPUT ARGS: " +
                String.join(" | ", Arrays.stream(joinPoint.getArgs()).map(x -> Objects.toString(x, "NULL")).toArray(String[]::new)) +
                "\n\tERROR:";
        Logger logger = LoggerFactory.getLogger(joinPoint.getTarget().getClass());

        logger.error(message, exception);

    }

}
