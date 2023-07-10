package guzev.petproj.pl.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import guzev.petproj.bl.services.PublisherService;
import guzev.petproj.dao.entities.Publisher;
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
public class PublisherControllerTests {

    private MockMvc mockMvc;

    @Mock
    private PublisherService publisherService;

    @InjectMocks
    private PublisherController publisherController;

    private Publisher testPublisher;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        testPublisher = new Publisher("test-publisher", "test-link", "test-redactor", "test-phone", "test-address");
        this.mockMvc = MockMvcBuilders.standaloneSetup(publisherController).build();
    }

    @Test
    public void CreatePublisher_ReturnCreatedPublisher_IfRequestIsOk() throws Exception {
        final String json = objectMapper.writeValueAsString(testPublisher);

        when(publisherService.create(testPublisher))
                .thenReturn(testPublisher);

        MvcResult requestResult = mockMvc.perform(post("/pet-proj/api/publisher")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andReturn();

        String resultString = requestResult.getResponse().getContentAsString();
        Publisher result = objectMapper.readValue(resultString, Publisher.class);

        assertEquals(testPublisher, result);
        verify(publisherService, times(1)).create(testPublisher);
    }

    @Test
    public void CreatePublisher_ReturnBadRequest_IfPropertyIsNull() throws Exception {
        testPublisher.setLink(null);
        final String json = objectMapper.writeValueAsString(testPublisher);

        mockMvc.perform(post("/pet-proj/api/publisher")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void ReadPublisherByName_ReturnPublisher_IfRequestIsOk() throws Exception {
        when(publisherService.readByName(testPublisher.getName()))
                .thenReturn(testPublisher);

        MvcResult requestResult = mockMvc.perform(get("/pet-proj/api/publisher/{name}", testPublisher.getName()))
                .andExpect(status().isOk())
                .andReturn();

        String resultString = requestResult.getResponse().getContentAsString();
        Publisher result = objectMapper.readValue(resultString, Publisher.class);

        assertEquals(testPublisher, result);
        verify(publisherService, times(1)).readByName(testPublisher.getName());
    }

    @Test
    public void ReadPublisherByName_ShouldThrowException_IfPublisherNotExists() throws Exception {
        when(publisherService.readByName(testPublisher.getName()))
                .thenThrow(NoSuchElementException.class);

        try {
            mockMvc.perform(get("/pet-proj/api/publisher/{name}", testPublisher.getName()));
        } catch (ServletException e) {
            assertEquals(NoSuchElementException.class, e.getRootCause().getClass());
        }
    }

    @Test
    public void ReadPublishers_ShouldReturnListOfPublishers() throws Exception {
        final List<Publisher> publishers = new ArrayList<>(3);
        publishers.add(new Publisher("test-publisher", "test-link", "test-redactor", "test-phone", "test-address"));
        publishers.add(new Publisher("test-publisher-2", "test-link-2", "test-redactor-2", "test-phone-2", "test-address-2"));
        publishers.add(new Publisher("test-publisher-3", "test-link-3", "test-redactor-3", "test-phone-3", "test-address-3"));

        when(publisherService.readAll(0, 3))
                .thenReturn(publishers);

        MvcResult requestResult = mockMvc.perform(get("/pet-proj/api/publisher")
                        .param("page", "0").param("size", "3"))
                .andExpect(status().isOk())
                .andReturn();

        String resultString = requestResult.getResponse().getContentAsString();
        List<Publisher> result = objectMapper.readValue(resultString, new TypeReference<List<Publisher>>() {
        });

        assertEquals(result.size(), 3);
        assertInstanceOf(Publisher.class, result.get(0));

        verify(publisherService, times(1)).readAll(0, 3);
    }

    @Test
    public void ReadPublishers_SetDefaultParams_IfParamsNotPresent() throws Exception {
        ReflectionTestUtils.setField(publisherController, "defaultSize", 5);

        when(publisherService.readAll(0, 5))
                .thenReturn(new ArrayList<>());

        mockMvc.perform(get("/pet-proj/api/publisher"))
                .andExpect(status().isOk());

        verify(publisherService, times(1)).readAll(0, 5);
    }

    @Test
    public void ReadPublishers_ShouldThrowException_IfParamsLessThanZero() throws Exception {
        when(publisherService.readAll(0, -1))
                .thenThrow(NoSuchElementException.class);

        try {
            mockMvc.perform(get("/pet-proj/api/publisher")
                            .param("page", "0").param("size", "-1"))
                    .andExpect(status().isBadRequest());
        } catch (ServletException e) {
            assertEquals(NoSuchElementException.class, e.getRootCause().getClass());
        }
    }

    @Test
    public void ReadSubscribedPublishers_ShouldReturnListOfSubscriberPublishers() throws Exception {
        final List<Publisher> publishers = new ArrayList<>(2);
        publishers.add(new Publisher("test-publisher", "test-link", "test-redactor", "test-phone", "test-address"));
        publishers.add(new Publisher("test-publisher-2", "test-link-2", "test-redactor-2", "test-phone-2", "test-address-2"));
        final Subscriber subscriber = new Subscriber("test@email.com", "test-user");
        final Set<Subscriber> subscribers = new HashSet<>(1);
        subscribers.add(subscriber);
        publishers.forEach(publisher -> publisher.setSubscribers(subscribers));

        when(publisherService.readSubscribedPublishers(subscriber.getEmail(), 3, 2))
                .thenReturn(publishers);

        MvcResult requestResult = mockMvc.perform(get("/pet-proj/api/publisher/subscribed")
                        .param("email", subscriber.getEmail()).param("page", "3").param("size", "2"))
                .andExpect(status().isOk())
                .andReturn();

        String resultString = requestResult.getResponse().getContentAsString();
        List<Publisher> result = objectMapper.readValue(resultString, new TypeReference<List<Publisher>>() {});

        assertEquals(result.size(), 2);
        assertInstanceOf(Publisher.class, result.get(0));
        assertTrue(result.get(0).getSubscribers().contains(subscriber));

        verify(publisherService, times(1)).readSubscribedPublishers(subscriber.getEmail(), 3, 2);
    }

    @Test
    public void ReadSubscribedPublishers_SetDefaultParams_IfParamsNotPresent() throws Exception {
        ReflectionTestUtils.setField(publisherController, "defaultSize", 5);
        final String email = "test@email.com";

        when(publisherService.readSubscribedPublishers(email, 0, 5))
                .thenReturn(new ArrayList<>());

        mockMvc.perform(get("/pet-proj/api/publisher/subscribed")
                .param("email", email))
                .andExpect(status().isOk());

        verify(publisherService, times(1)).readSubscribedPublishers(email, 0, 5);
    }

    @Test
    public void ReadSubscribedPublishers_ShouldThrowException_IfParamsLessThanZero() throws Exception {
        when(publisherService.readSubscribedPublishers("test@email.com", 0, -1))
                .thenThrow(NoSuchElementException.class);

        try {
            mockMvc.perform(get("/pet-proj/api/publisher/subscribed")
                            .param("email", "test@email.com").param("page", "0").param("size", "-1"))
                    .andExpect(status().isBadRequest());
        } catch (ServletException e) {
            assertEquals(NoSuchElementException.class, e.getRootCause().getClass());
        }
    }

    @Test
    public void UpdatePublisher_ReturnUpdatedPublisher_IfRequestIsOk() throws Exception {
        final String json = objectMapper.writeValueAsString(testPublisher);

        when(publisherService.update(testPublisher))
                .thenReturn(testPublisher);

        MvcResult requestResult = mockMvc.perform(put("/pet-proj/api/publisher")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andReturn();

        String resultString = requestResult.getResponse().getContentAsString();
        Publisher result = objectMapper.readValue(resultString, Publisher.class);

        assertEquals(testPublisher, result);
        verify(publisherService, times(1)).update(testPublisher);
    }

    @Test
    public void UpdatePublisher_ReturnBadRequest_IfPropertyIsNull() throws Exception {
        testPublisher.setLink(null);
        final String json = objectMapper.writeValueAsString(testPublisher);

        mockMvc.perform(put("/pet-proj/api/publisher")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(publisherService);
    }

    @Test
    public void SubscribePublisher_ResponseShouldBeTrue_IfPublisherAndSubscriberExist() throws Exception {
        final Subscriber subscriber = new Subscriber("test@email.com", "test-user");
        
        when(publisherService.subscribe(testPublisher.getName(), subscriber.getEmail()))
                .thenReturn(true);
        
        MvcResult requestResult = mockMvc.perform(patch("/pet-proj/api/publisher/{name}/subscribe", testPublisher.getName())
                .param("email", subscriber.getEmail()))
                .andExpect(status().isOk())
                .andReturn();
        
        String resultString = requestResult.getResponse().getContentAsString();
        Map<String, Object> result = objectMapper.readValue(resultString, HashMap.class);
        
        assertTrue((Boolean) result.get("subscribed"));
        
        verify(publisherService, times(1)).subscribe(testPublisher.getName(), subscriber.getEmail());
    }

    @Test
    public void SubscribePublisher_ShouldThrowException_IfPublisherOrSubscriberNotExist() throws Exception {
        when(publisherService.subscribe(testPublisher.getName(), "test-sub"))
                .thenThrow(NoSuchElementException.class);
        
        try {
            mockMvc.perform(patch("/pet-proj/api/publisher/{name}/subscribe", testPublisher.getName())
                    .param("email", "test-sub"))
                    .andExpect(status().isBadRequest());
        } catch (ServletException e) {
            assertEquals(NoSuchElementException.class, e.getRootCause().getClass());
        }
    }

    @Test
    public void UnsubscribePublisher_ResponseShouldBeTrue_IfPublisherAndSubscriberExist_AndSubscribed() throws Exception {
        final Subscriber subscriber = new Subscriber("test@email.com", "test-user");

        when(publisherService.unsubscribe(testPublisher.getName(), subscriber.getEmail()))
                .thenReturn(true);

        MvcResult requestResult = mockMvc.perform(patch("/pet-proj/api/publisher/{name}/unsubscribe", testPublisher.getName())
                        .param("email", subscriber.getEmail()))
                .andExpect(status().isOk())
                .andReturn();

        String resultString = requestResult.getResponse().getContentAsString();
        Map<String, Object> result = objectMapper.readValue(resultString, HashMap.class);

        assertTrue((Boolean) result.get("unsubscribed"));

        verify(publisherService, times(1)).unsubscribe(testPublisher.getName(), subscriber.getEmail());
    }

    @Test
    public void UnsubscribePublisher_ResponseShouldBeFalse_IfPublisherAndSubscriberExist_AndUnsubscribed() throws Exception {
        final Subscriber subscriber = new Subscriber("test@email.com", "test-user");

        when(publisherService.unsubscribe(testPublisher.getName(), subscriber.getEmail()))
                .thenReturn(false);

        MvcResult requestResult = mockMvc.perform(patch("/pet-proj/api/publisher/{name}/unsubscribe", testPublisher.getName())
                        .param("email", subscriber.getEmail()))
                .andExpect(status().isOk())
                .andReturn();

        String resultString = requestResult.getResponse().getContentAsString();
        Map<String, Object> result = objectMapper.readValue(resultString, HashMap.class);

        assertFalse((Boolean) result.get("unsubscribed"));

        verify(publisherService, times(1)).unsubscribe(testPublisher.getName(), subscriber.getEmail());
    }


    @Test
    public void UnsubscribePublisher_ShouldThrowException_IfPublisherOrSubscriberNotExist() throws Exception {
        when(publisherService.unsubscribe(testPublisher.getName(), "test-sub"))
                .thenThrow(NoSuchElementException.class);

        try {
            mockMvc.perform(patch("/pet-proj/api/publisher/{name}/unsubscribe", testPublisher.getName())
                            .param("email", "test-sub"))
                    .andExpect(status().isBadRequest());
        } catch (ServletException e) {
            assertEquals(NoSuchElementException.class, e.getRootCause().getClass());
        }
    }
    
    @Test
    public void DeletePublisher_ResponseShouldBeTrue() throws Exception {
        MvcResult requestResult = mockMvc.perform(delete("/pet-proj/api/publisher/{name}", testPublisher.getName()))
                .andExpect(status().isOk())
                .andReturn();

        String resultString = requestResult.getResponse().getContentAsString();
        Map<String, Object> result = objectMapper.readValue(resultString, HashMap.class);

        assertTrue((Boolean) result.get("deleted"));

        verify(publisherService, times(1)).delete(testPublisher.getName());
    }

    @Test
    public void DeletePublisher_ShouldThrowException_IfPublisherNotExists() throws Exception {
        doThrow(NoSuchElementException.class).when(publisherService).delete(testPublisher.getName());

        try {
            mockMvc.perform(delete("/pet-proj/api/publisher/{name}", testPublisher.getName()))
                    .andExpect(status().isBadRequest());
        } catch (ServletException e) {
            assertEquals(NoSuchElementException.class, e.getRootCause().getClass());
        }
    }
}
