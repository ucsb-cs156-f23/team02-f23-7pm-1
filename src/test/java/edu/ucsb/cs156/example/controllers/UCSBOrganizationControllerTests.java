package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.UCSBOrganization;
import edu.ucsb.cs156.example.repositories.UCSBOrganizationRepository;

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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = UCSBOrganizationController.class)
@Import(TestConfig.class)
public class UCSBOrganizationControllerTests extends ControllerTestCase {

    @MockBean
    UCSBOrganizationRepository ucsbOrganizationRepository;

    @MockBean
    UserRepository userRepository;

     // Tests for GET /api/ucsborganization/all

    @Test
    public void logged_out_users_cannot_get_all() throws Exception {
        mockMvc.perform(get("/api/ucsborganization/all"))
            .andExpect(status().is(403)); // logged out users can't get all
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_users_can_get_all() throws Exception {
        mockMvc.perform(get("/api/ucsborganization/all"))
            .andExpect(status().is(200)); // logged
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_user_can_get_all_ucsborganization() throws Exception {

        // arrange

        UCSBOrganization zpr = UCSBOrganization.builder()
                        .orgCode("ZPR")
                        .orgTranslationShort("ZETA PHI RHO")
                        .orgTranslation("ZETA PHI RHO FRAT")
                        .inactive(false)
                        .build();

        UCSBOrganization sky = UCSBOrganization.builder()
                        .orgCode("SKY")
                        .orgTranslationShort("SKYDIVING CLUB")
                        .orgTranslation("SKYDIVING CLUB AT UCSB")
                        .inactive(true)
                        .build();

        ArrayList<UCSBOrganization> expectedOrganization = new ArrayList<>();
        expectedOrganization.addAll(Arrays.asList(zpr, sky));

        when(ucsbOrganizationRepository.findAll()).thenReturn(expectedOrganization);

        // act
        MvcResult response = mockMvc.perform(get("/api/ucsborganization/all"))
            .andExpect(status().isOk()).andReturn();

        // assert

        verify(ucsbOrganizationRepository, times(1)).findAll();
        String expectedJson = mapper.writeValueAsString(expectedOrganization);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);
    }

    // Tests for POST /api/ucsborganization...

    @Test
    public void logged_out_users_cannot_post() throws Exception {
        mockMvc.perform(post("/api/ucsborganization/post"))
            .andExpect(status().is(403));
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_regular_users_cannot_post() throws Exception {
        mockMvc.perform(post("/api/ucsborganization/post"))
            .andExpect(status().is(403)); // only admins can post
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void an_admin_user_can_post_a_new_org() throws Exception {
        // arrange

        UCSBOrganization osli = UCSBOrganization.builder()
                        .orgCode("OSLI")
                        .orgTranslationShort("STUDENT")
                        .orgTranslation("OFFICE")
                        .inactive(true)
                        .build();

        when(ucsbOrganizationRepository.save(eq(osli))).thenReturn(osli);

        // act
        MvcResult response = mockMvc.perform(
            post("/api/ucsborganization/post?orgCode=OSLI&orgTranslationShort=STUDENT&orgTranslation=OFFICE&inactive=true")
                .with(csrf()))
            .andExpect(status().isOk()).andReturn();

        // assert
        verify(ucsbOrganizationRepository, times(1)).save(osli);
        String expectedJson = mapper.writeValueAsString(osli);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);
    }

    // Tests for PUT /api/ucsborganization?...

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_can_edit_an_existing_organization() throws Exception {
        // arrange

        UCSBOrganization zprOrig = UCSBOrganization.builder()
                        .orgCode("TPR")
                        .orgTranslationShort("THETA PHI RHO")
                        .orgTranslation("THETA PHI RHO")
                        .inactive(false)
                        .build();

        UCSBOrganization zprEdited = UCSBOrganization.builder()
                        .orgCode("TPR")
                        .orgTranslationShort("THETA PHI RHO FRAT")
                        .orgTranslation("UCSB THETA PHI RHO FRATERNITY")
                        .inactive(true)
                        .build();

        String requestBody = mapper.writeValueAsString(zprEdited);

        when(ucsbOrganizationRepository.findById(eq("TPR"))).thenReturn(Optional.of(zprOrig));

        // act
        MvcResult response = mockMvc.perform(
                    put("/api/ucsborganization?orgCode=TPR")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8")
                        .content(requestBody)
                        .with(csrf()))
                        .andExpect(status().isOk()).andReturn();

        // assert
        verify(ucsbOrganizationRepository, times(1)).findById("TPR");
        verify(ucsbOrganizationRepository, times(1)).save(zprEdited); // should be saved with updated info
        String responseString = response.getResponse().getContentAsString();
        assertEquals(requestBody, responseString);
    }


    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_cannot_edit_organization_that_does_not_exist() throws Exception {
            // arrange

            UCSBOrganization editedOrg = UCSBOrganization.builder()
                        .orgCode("test")
                        .orgTranslationShort("COOL CLUB")
                        .orgTranslation("THE UCSB COOL CLUB")
                        .inactive(true)
                        .build();

            String requestBody = mapper.writeValueAsString(editedOrg);

            when(ucsbOrganizationRepository.findById(eq("test"))).thenReturn(Optional.empty());

            // act
            MvcResult response = mockMvc.perform(
                            put("/api/ucsborganization?orgCode=test")
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .characterEncoding("utf-8")
                                            .content(requestBody)
                                            .with(csrf()))
                            .andExpect(status().isNotFound()).andReturn();

            // assert
            verify(ucsbOrganizationRepository, times(1)).findById("test");
            Map<String, Object> json = responseToJson(response);
            assertEquals("UCSBOrganization with id test not found", json.get("message"));
    }
}