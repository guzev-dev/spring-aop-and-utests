package guzev.petproj.pl.exceptions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import guzev.petproj.pl.controllers.SubscriberController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class ExceptionResponseHandlerTests {

    private MockMvc mockMvc;

    @Mock
    private SubscriberController subscriberController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(subscriberController)
                .setControllerAdvice(new ExceptionResponseHandler())
                .build();
    }

    @Test
    public void DuplicateKeyException_Handled() throws Exception {
        setupException(subscriberController, new DuplicateKeyException("Exception message"));

        MvcResult requestResult = mockMvc.perform(get("/pet-proj/api/subscriber")
                        .param("page", "0").param("size", "5"))
                .andExpect(status().isBadRequest())
                .andReturn();

        Map<String, Object> errorInfo = fetchErrorInfoFromString(requestResult);

        assertEquals("Exception message", errorInfo.get("message"));
        assertEquals(400, errorInfo.get("status"));
    }

    @Test
    public void NoSuchElementException_Handled() throws Exception {
        setupException(subscriberController, new NoSuchElementException());

        MvcResult requestResult = mockMvc.perform(get("/pet-proj/api/subscriber")
                        .param("page", "0").param("size", "5"))
                .andExpect(status().isNotFound())
                .andReturn();

        Map<String, Object> errorInfo = fetchErrorInfoFromString(requestResult);

        assertEquals("Cannot find resource with the given input.", errorInfo.get("message"));
        assertEquals(404, errorInfo.get("status"));
    }

    @Test
    public void HttpMessageNotReadableException_Handled() throws Exception {
        setupException(subscriberController, new HttpMessageNotReadableException("Exception message", (HttpInputMessage) null));

        MvcResult requestResult = mockMvc.perform(get("/pet-proj/api/subscriber")
                        .param("page", "0").param("size", "5"))
                .andExpect(status().isBadRequest())
                .andReturn();

        Map<String, Object> errorInfo = fetchErrorInfoFromString(requestResult);

        assertEquals("Exception message", errorInfo.get("message"));
        assertEquals(400, errorInfo.get("status"));
    }

    private static void setupException(SubscriberController subscriberController, Throwable exception) {
        when(subscriberController.read(0, Optional.of(5)))
                .thenThrow(exception);
    }

    private static Map<String, Object> fetchErrorInfoFromString(MvcResult requestResult) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        String resultString = requestResult.getResponse().getContentAsString();
        Map<String, Object> result = objectMapper.readValue(resultString, HashMap.class);
        return (Map<String, Object>) result.get("error");
    }

}
