package guzev.petproj.bl.aspects;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.mail.MailSendException;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ExceptionLoggingAspectTests {

    @InjectMocks
    private ExceptionLoggingAspect exceptionLoggingAspect;

    private JoinPoint joinPoint;

    @BeforeEach
    public void setup() {
        joinPoint = mock(JoinPoint.class);
    }

    @Test
    public void LogException_LogIfNotExcluded() {
        MethodSignature methodSignature = mock(MethodSignature.class);
        Object target = mock(Object.class);

        when(joinPoint.getSignature())
                .thenReturn(methodSignature);

        when(methodSignature.toLongString())
                .thenReturn("<test-method-signature>");

        when(joinPoint.getArgs())
                .thenReturn(new Object[]{"<test-method-args>"});

        when(joinPoint.getTarget())
                .thenReturn(target);

        try (MockedStatic<LoggerFactory> loggerFactory = mockStatic(LoggerFactory.class)) {

            Logger logger = mock(Logger.class);

            loggerFactory.when(() -> LoggerFactory.getLogger(any(Class.class)))
                    .thenReturn(logger);

            exceptionLoggingAspect.logException(joinPoint, new MailSendException("test"));

            verify(logger, times(1)).error(anyString(), any(Throwable.class));
        }
    }

    @Test
    public void LogException_NotLogIfExcluded() {
        try (MockedStatic<LoggerFactory> loggerFactory = mockStatic(LoggerFactory.class)) {

            Logger logger = mock(Logger.class);

            loggerFactory.when(() -> LoggerFactory.getLogger(any(Class.class)))
                    .thenReturn(logger);

            exceptionLoggingAspect.logException(joinPoint, new DuplicateKeyException(""));

            verifyNoInteractions(logger);
        }
    }

    @Test
    public void ExceptionOccurred_PointcutPresent() {
        ReflectionTestUtils.invokeMethod(exceptionLoggingAspect, "exceptionOccurredPointcut");
    }

}
