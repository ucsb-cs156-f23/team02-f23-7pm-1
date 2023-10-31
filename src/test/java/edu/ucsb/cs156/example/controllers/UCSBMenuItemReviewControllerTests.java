package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.UCSBDate;
import edu.ucsb.cs156.example.entities.UCSBMenuItemReview;
import edu.ucsb.cs156.example.repositories.UCSBDateRepository;
import edu.ucsb.cs156.example.repositories.UCSBMenuItemReviewRepository;

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

@WebMvcTest(controllers = UCSBMenuItemReviewController.class)
@Import(TestConfig.class)
public class UCSBMenuItemReviewControllerTests extends ControllerTestCase {

    @MockBean
    UCSBMenuItemReviewRepository ucsbMenuItemReviewRepository;

    @MockBean
    UserRepository userRepository;


    // Tests for GET

    @Test
    public void logged_out_users_cannot_get_all() throws Exception {
            mockMvc.perform(get("/api/ucsbmenuitemreview/all"))
                            .andExpect(status().is(403)); // logged out users can't get all
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_users_can_get_all() throws Exception {
            mockMvc.perform(get("/api/ucsbmenuitemreview/all"))
                            .andExpect(status().is(200)); // logged
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_user_can_get_all_ucsbdates() throws Exception {

            // arrange
            LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

            UCSBMenuItemReview ucsbReview1 = UCSBMenuItemReview.builder()
                            .itemId(1)
                            .stars(3)
                            .reviewerEmail("email@ucsb.edu")
                            .dateReviewed(ldt1)
                            .comments("some-comment")
                            .build();

            LocalDateTime ldt2 = LocalDateTime.parse("2022-03-11T00:00:00");

            UCSBMenuItemReview ucsbReview2 = UCSBMenuItemReview.builder()
                            .itemId(1)
                            .stars(2)
                            .reviewerEmail("email@ucsb.edu")
                            .dateReviewed(ldt2)
                            .comments("some-comment")
                            .build();

            ArrayList<UCSBMenuItemReview> expectedDates = new ArrayList<>();
            expectedDates.addAll(Arrays.asList(ucsbReview1, ucsbReview2));

            when(ucsbMenuItemReviewRepository.findAll()).thenReturn(expectedDates);

            // act
            MvcResult response = mockMvc.perform(get("/api/ucsbmenuitemreview/all"))
                            .andExpect(status().isOk()).andReturn();

            // assert

            verify(ucsbMenuItemReviewRepository, times(1)).findAll();
            String expectedJson = mapper.writeValueAsString(expectedDates);
            String responseString = response.getResponse().getContentAsString();
            assertEquals(expectedJson, responseString);
    }

    // Tests for POST /api/ucsbdates/post...

    @Test
    public void logged_out_users_cannot_post() throws Exception {
            mockMvc.perform(post("/api/ucsbmenuitemreview/post"))
                            .andExpect(status().is(403));
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_regular_users_cannot_post() throws Exception {
            mockMvc.perform(post("/api/ucsbmenuitemreview/post"))
                            .andExpect(status().is(403)); // only admins can post
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void an_admin_user_can_post_a_new_ucsbmenuitemreview() throws Exception {
            // arrange

            LocalDateTime ucsbDate1 = LocalDateTime.parse("2022-01-03T00:00:00");

            UCSBMenuItemReview ucsbReview1 = UCSBMenuItemReview.builder()
                            .itemId(1)
                            .stars(3)
                            .reviewerEmail("email@ucsb.edu")
                            .dateReviewed(ucsbDate1)
                            .comments("some-comment")
                            .build();


            when(ucsbMenuItemReviewRepository.save(eq(ucsbReview1))).thenReturn(ucsbReview1);

            // act
            MvcResult response = mockMvc.perform(
                            post("/api/ucsbmenuitemreview/post?itemId=1&stars=3&reviewerEmail=email@ucsb.edu&dateReviewed=2022-01-03T00:00:00&comments=some-comment")
                                            .with(csrf()))
                            .andExpect(status().isOk()).andReturn();

            // assert
            verify(ucsbMenuItemReviewRepository, times(1)).save(ucsbReview1);
            String expectedJson = mapper.writeValueAsString(ucsbReview1);
            String responseString = response.getResponse().getContentAsString();
            assertEquals(expectedJson, responseString);
    }

}
