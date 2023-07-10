package guzev.petproj.pl.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import guzev.petproj.bl.services.SubscriberService;
import guzev.petproj.dao.entities.Subscriber;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class SubscriberControllerTests {

    private MockMvc mockMvc;

    @Mock
    private SubscriberService subscriberService;

    @InjectMocks
    private SubscriberController subscriberController;

    private Subscriber testSubscriber;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        testSubscriber = new Subscriber("test@email.com", "test-user");

        this.mockMvc = MockMvcBuilders.standaloneSetup(subscriberController).build();
    }

    @Test
    public void CreateSubscriber_ReturnCreatedUser_IfRequestIsOk() throws Exception {
        final String json = objectMapper.writeValueAsString(testSubscriber);

        when(subscriberService.create(testSubscriber))
                .thenReturn(testSubscriber);

        MvcResult requestResult = mockMvc.perform(post("/pet-proj/api/subscriber")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andReturn();

        String resultString = requestResult.getResponse().getContentAsString();
        Subscriber subscriber = objectMapper.readValue(resultString, Subscriber.class);

        assertEquals(testSubscriber.getEmail(), subscriber.getEmail());
        assertEquals(testSubscriber.getUsername(), subscriber.getUsername());

        verify(subscriberService, times(1)).create(testSubscriber);
    }

    @Test
    public void CreateSubscriber_BadRequest_IfPropertyIsNull() throws Exception {
        testSubscriber.setUsername(null);
        final String json = objectMapper.writeValueAsString(testSubscriber);

        mockMvc.perform(post("/pet-proj/api/subscriber")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

    }

    @Test
    public void ReadSubscriberByEmail_ShouldReturnSubscriber() throws Exception {
        when(subscriberService.readByEmail(testSubscriber.getEmail()))
                .thenReturn(testSubscriber);

        mockMvc.perform(get("/pet-proj/api/subscriber/{email}", testSubscriber.getEmail()))
                .andExpect(status().isOk());

        verify(subscriberService, times(1)).readByEmail(testSubscriber.getEmail());
    }

    @Test
    public void ReadSubscriberByEmail_ShouldThrowException_IfNotExists() throws Exception {
        when(subscriberService.readByEmail(testSubscriber.getEmail()))
                .thenThrow(NoSuchElementException.class);

        try {
            mockMvc.perform(get("/pet-proj/api/subscriber/{email}", testSubscriber.getEmail()));
        } catch (ServletException e) {
            assertEquals(NoSuchElementException.class, e.getRootCause().getClass());
        }
    }

    @Test
    public void ReadSubscribers_ShouldReturnListOfSubscribers() throws Exception {
        final List<Subscriber> subscribers = new ArrayList<>(3);
        subscribers.add(new Subscriber("test1@email.com", "test-user1"));
        subscribers.add(new Subscriber("test2@email.com", "test-user2"));
        subscribers.add(new Subscriber("test3@email.com", "test-user3"));

        when(subscriberService.readAll(1, 3))
                .thenReturn(subscribers);

        MvcResult requestResult = mockMvc.perform(get("/pet-proj/api/subscriber")
                        .param("page", "1").param("size", "3"))
                .andExpect(status().isOk())
                .andReturn();

        String resultString = requestResult.getResponse().getContentAsString();
        List<Subscriber> result = objectMapper.readValue(resultString, new TypeReference<List<Subscriber>>() {});

        assertEquals(result.size(), 3);
        assertInstanceOf(Subscriber.class, result.get(0));

        verify(subscriberService, times(1)).readAll(1, 3);
    }

    @Test
    public void ReadSubscribers_SetDefaultParams_IfParamsNotPresent() throws Exception {
        ReflectionTestUtils.setField(subscriberController, "defaultSize", 5);

        when(subscriberService.readAll(0, 5))
                .thenReturn(new ArrayList<>());

        mockMvc.perform(get("/pet-proj/api/subscriber"))
                .andExpect(status().isOk());

        verify(subscriberService, times(1)).readAll(0, 5);
    }

    @Test
    public void ReadSubscribers_ShouldThrowException_IfParamsLessThanZero() throws Exception {

        when(subscriberService.readAll(0, -1))
                .thenThrow(IllegalArgumentException.class);

        try {
            mockMvc.perform(get("/pet-proj/api/subscriber")
                            .param("page", "0").param("size", "-1"))
                    .andExpect(status().isBadRequest());
        } catch (ServletException e) {
            assertEquals(IllegalArgumentException.class, e.getRootCause().getClass());
        }
    }

    @Test
    public void UpdateSubscriber_ReturnUpdatedUser_IfRequestIsOk() throws Exception {
        final String json = objectMapper.writeValueAsString(testSubscriber);

        when(subscriberService.update(testSubscriber))
                .thenReturn(testSubscriber);

        MvcResult requestResult = mockMvc.perform(put("/pet-proj/api/subscriber")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andReturn();

        String resultString = requestResult.getResponse().getContentAsString();
        Subscriber subscriber = objectMapper.readValue(resultString, Subscriber.class);

        assertEquals(testSubscriber.getEmail(), subscriber.getEmail());
        assertEquals(testSubscriber.getUsername(), subscriber.getUsername());

        verify(subscriberService, times(1)).update(testSubscriber);
    }

    @Test
    public void UpdateSubscriber_BadRequest_IfPropertyIsNull() throws Exception {
        testSubscriber.setUsername(null);
        final String json = objectMapper.writeValueAsString(testSubscriber);

        mockMvc.perform(put("/pet-proj/api/subscriber")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(subscriberService);
    }

    @Test
    public void DeleteSubscriber_ResponseShouldBeTrue() throws Exception {
        MvcResult requestResult = mockMvc.perform(delete("/pet-proj/api/subscriber/{email}", testSubscriber.getEmail()))
                .andExpect(status().isOk())
                .andReturn();

        String resultString = requestResult.getResponse().getContentAsString();
        Map<String, Object> result = objectMapper.readValue(resultString, HashMap.class);

        assertTrue((Boolean) result.get("deleted"));

        verify(subscriberService, times(1)).delete(testSubscriber.getEmail());
    }

    @Test
    public void DeleteSubscriber_ShouldThrowException_IfSubscriberNotExists() throws Exception {
        doThrow(NoSuchElementException.class).when(subscriberService).delete(testSubscriber.getEmail());

        try {
            mockMvc.perform(delete("/pet-proj/api/subscriber/{email}", testSubscriber.getEmail()))
                    .andExpect(status().isBadRequest());
        } catch (ServletException e) {
            assertEquals(NoSuchElementException.class, e.getRootCause().getClass());
        }
    }

}
