package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.RecommendationRequestRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.RecommendationRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.time.LocalDateTime;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = RecommendationRequestController.class)
@Import(TestConfig.class)
public class RecommendationRequestControllerTests extends ControllerTestCase {

    @MockBean
    RecommendationRequestRepository recommendationRequestRepository;

    @MockBean
    UserRepository userRepository;

    // Tests for GET /api/recommendationrequests/all

    @Test
    public void logged_out_users_cannot_get_all() throws Exception {
        mockMvc.perform(get("/api/recommendationrequests/all"))
            .andExpect(status().is(403)); // logged out users can't get all
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_users_can_get_all() throws Exception {
        mockMvc.perform(get("/api/recommendationrequests/all"))
            .andExpect(status().is(200)); // logged
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_user_can_get_all() throws Exception {

        // arrange
        LocalDateTime ldt1 = LocalDateTime.parse("2022-07-25T07:03:23");
        LocalDateTime ldt2 = LocalDateTime.parse("2023-03-01T00:00:00");

        RecommendationRequest recRequest1 = RecommendationRequest.builder()
            .requesterEmail("abc@ucsb.edu")
            .professorEmail("xyz@ucsb.edu")
            .explanation("CS UCSB")
            .dateRequested(ldt1)
            .dateNeeded(ldt2)
            .done(true)
            .build();

        LocalDateTime ldt3 = LocalDateTime.parse("2022-01-11T11:11:11");
        LocalDateTime ldt4 = LocalDateTime.parse("2022-11-11T00:00:00");

        RecommendationRequest recRequest2 = RecommendationRequest.builder()
            .requesterEmail("abc@ucsb.edu")
            .professorEmail("def@ucsb.edu")
            .explanation("PhD CE UCSB")
            .dateRequested(ldt3)
            .dateNeeded(ldt4)
            .done(false)
            .build();

        ArrayList<RecommendationRequest> expectedRequests = new ArrayList<>();
        expectedRequests.addAll(Arrays.asList(recRequest1, recRequest2));

        when(recommendationRequestRepository.findAll()).thenReturn(expectedRequests);

        // act
        MvcResult response = mockMvc.perform(get("/api/recommendationrequests/all"))
            .andExpect(status().isOk()).andReturn();

        // assert

        verify(recommendationRequestRepository, times(1)).findAll();
        String expectedJson = mapper.writeValueAsString(expectedRequests);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);
    }

    // Tests for POST /api/recommendationrequests/post...

    @Test
    public void logged_out_users_cannot_post() throws Exception {
        mockMvc.perform(post("/api/recommendationrequests/post"))
            .andExpect(status().is(403));
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_regular_users_cannot_post() throws Exception {
        mockMvc.perform(post("/api/recommendationrequests/post"))
            .andExpect(status().is(403)); // only admins can post
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void an_admin_user_can_post_a_new_recommendationrequests() throws Exception {
        // arrange

        LocalDateTime ldt1 = LocalDateTime.parse("2023-01-11T01:01:01");
        LocalDateTime ldt2 = LocalDateTime.parse("2023-03-03T00:00:00");

        RecommendationRequest recRequest = RecommendationRequest.builder()
            .requesterEmail("abc@ucsb.edu")
            .professorEmail("cde@ucsb.edu")
            .explanation("MS CS UCLA")
            .dateRequested(ldt1)
            .dateNeeded(ldt2)
            .done(true)
            .build();

        when(recommendationRequestRepository.save(eq(recRequest))).thenReturn(recRequest);

        // act
        MvcResult response = mockMvc.perform(
            post("/api/recommendationrequests/post?requestorEmail=abc@ucsb.edu&professorEmail=cde@ucsb.edu&explanation=MS CS UCLA&dateRequested=2023-01-11T01:01:01&dateNeeded=2023-03-03T00:00:00&done=true")
                .with(csrf()))
            .andExpect(status().isOk()).andReturn();

        // assert
        verify(recommendationRequestRepository, times(1)).save(recRequest);
        String expectedJson = mapper.writeValueAsString(recRequest);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);
    }
}