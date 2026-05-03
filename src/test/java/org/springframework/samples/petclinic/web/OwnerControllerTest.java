package org.springframework.samples.petclinic.web;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests for {@link OwnerController}.
 */
@ExtendWith(MockitoExtension.class)
class OwnerControllerTest {

    @Mock
    private ClinicService clinicService;

    @InjectMocks
    private OwnerController ownerController;

    private MockMvc mockMvc;

    private Owner george;
    private Owner betty;
  @BeforeEach
  void setUp() {
      mockMvc = MockMvcBuilders.standaloneSetup(ownerController).build();
  
      george = new Owner();
      george.setId(1);
      george.setFirstName("George");
      george.setLastName("Franklin");
      george.setAddress("110 W. Liberty St.");
      george.setCity("Madison");
      george.setTelephone("6085551023");
  
      betty = new Owner();
      betty.setId(2);
      betty.setFirstName("Betty");
      betty.setLastName("Davis");
      betty.setAddress("638 Cardinal Ave.");
      betty.setCity("Sun Prairie");
      betty.setTelephone("6085551749");
  }

  @Test
  @DisplayName("GET /owners/new - returns creation form with empty owner")
  void testInitCreationForm() throws Exception {
      mockMvc.perform(get("/owners/new"))
          .andExpect(status().isOk())
          .andExpect(model().attributeExists("owner"))
          .andExpect(view().name("owners/createOrUpdateOwnerForm"));
  }

  @Test
  @DisplayName("POST /owners/new - valid owner saves and redirects")
  void testProcessCreationFormSuccess() throws Exception {
      doAnswer(invocation -> {
          Owner o = invocation.getArgument(0);
          o.setId(99);
          return null;
      }).when(clinicService).saveOwner(any(Owner.class));
  
      mockMvc.perform(post("/owners/new")
              .param("firstName", "George")
              .param("lastName", "Franklin")
              .param("address", "110 W. Liberty St.")
              .param("city", "Madison")
              .param("telephone", "6085551023"))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrlPattern("/owners/*"));
  
      verify(clinicService, times(1)).saveOwner(any(Owner.class));
  }

  @Test
  @DisplayName("POST /owners/new - invalid owner returns form with errors")
  void testProcessCreationFormHasErrors() throws Exception {
      mockMvc.perform(post("/owners/new")
              .param("firstName", "")
              .param("lastName", "")
              .param("address", "")
              .param("city", "")
              .param("telephone", ""))
          .andExpect(status().isOk())
          .andExpect(model().attributeHasErrors("owner"))
          .andExpect(model().attributeHasFieldErrors("owner", "firstName", "lastName", "address", "city", "telephone"))
          .andExpect(view().name("owners/createOrUpdateOwnerForm"));
  
      verify(clinicService, never()).saveOwner(any(Owner.class));
  }

  @Test
  @DisplayName("POST /owners/new - missing firstName returns form")
  void testProcessCreationFormMissingFirstName() throws Exception {
      mockMvc.perform(post("/owners/new")
              .param("lastName", "Franklin")
              .param("address", "110 W. Liberty St.")
              .param("city", "Madison")
              .param("telephone", "6085551023"))
          .andExpect(status().isOk())
          .andExpect(model().attributeHasFieldErrors("owner", "firstName"))
          .andExpect(view().name("owners/createOrUpdateOwnerForm"));
  
      verify(clinicService, never()).saveOwner(any(Owner.class));
  }

  @Test
  @DisplayName("POST /owners/new - non-digit telephone returns form")
  void testProcessCreationFormInvalidTelephone() throws Exception {
      mockMvc.perform(post("/owners/new")
              .param("firstName", "George")
              .param("lastName", "Franklin")
              .param("address", "110 W. Liberty St.")
              .param("city", "Madison")
              .param("telephone", "abc-xyz"))
          .andExpect(status().isOk())
          .andExpect(model().attributeHasFieldErrors("owner", "telephone"))
          .andExpect(view().name("owners/createOrUpdateOwnerForm"));
  
      verify(clinicService, never()).saveOwner(any(Owner.class));
  }

  @Test
  @DisplayName("GET /owners/find - returns find form with empty owner")
  void testInitFindForm() throws Exception {
      mockMvc.perform(get("/owners/find"))
          .andExpect(status().isOk())
          .andExpect(model().attributeExists("owner"))
          .andExpect(view().name("owners/findOwners"));
  }

  @Test
  @DisplayName("GET /owners - no results found returns findOwners with error")
  void testProcessFindFormNoResults() throws Exception {
      when(clinicService.findOwnerByLastName(anyString())).thenReturn(Collections.emptyList());
  
      mockMvc.perform(get("/owners")
              .param("lastName", "Unknown"))
          .andExpect(status().isOk())
          .andExpect(model().attributeHasFieldErrors("owner", "lastName"))
          .andExpect(view().name("owners/findOwners"));
  
      verify(clinicService).findOwnerByLastName("Unknown");
  }

  @Test
  @DisplayName("GET /owners - single result redirects to owner detail")
  void testProcessFindFormSingleResult() throws Exception {
      when(clinicService.findOwnerByLastName("Franklin")).thenReturn(Collections.singletonList(george));
  
      mockMvc.perform(get("/owners")
              .param("lastName", "Franklin"))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/owners/1"));
  
      verify(clinicService).findOwnerByLastName("Franklin");
  }

  @Test
  @DisplayName("GET /owners - multiple results returns ownersList view")
  void testProcessFindFormMultipleResults() throws Exception {
      List<Owner> owners = Arrays.asList(george, betty);
      when(clinicService.findOwnerByLastName(anyString())).thenReturn(owners);
  
      mockMvc.perform(get("/owners")
              .param("lastName", "D"))
          .andExpect(status().isOk())
          .andExpect(model().attribute("selections", hasSize(2)))
          .andExpect(view().name("owners/ownersList"));
  
      verify(clinicService).findOwnerByLastName("D");
  }

  @Test
  @DisplayName("GET /owners - null lastName defaults to empty string (broadest search)")
  void testProcessFindFormNullLastName() throws Exception {
      List<Owner> owners = Arrays.asList(george, betty);
      when(clinicService.findOwnerByLastName("")).thenReturn(owners);
  
      mockMvc.perform(get("/owners"))
          .andExpect(status().isOk())
          .andExpect(model().attribute("selections", hasSize(2)))
          .andExpect(view().name("owners/ownersList"));
  
      verify(clinicService).findOwnerByLastName("");
  }

  @Test
  @DisplayName("GET /owners - null lastName, single result redirects")
  void testProcessFindFormSingleResultNullLastName() throws Exception {
      when(clinicService.findOwnerByLastName("")).thenReturn(Collections.singletonList(george));
  
      mockMvc.perform(get("/owners"))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/owners/1"));
  
      verify(clinicService).findOwnerByLastName("");
  }

  @Test
  @DisplayName("GET /owners/{ownerId}/edit - returns update form populated with owner")
  void testInitUpdateOwnerForm() throws Exception {
      when(clinicService.findOwnerById(1)).thenReturn(george);
  
      mockMvc.perform(get("/owners/1/edit"))
          .andExpect(status().isOk())
          .andExpect(model().attributeExists("owner"))
          .andExpect(model().attribute("owner", hasProperty("lastName", is("Franklin"))))
          .andExpect(model().attribute("owner", hasProperty("firstName", is("George"))))
          .andExpect(view().name("owners/createOrUpdateOwnerForm"));
  
      verify(clinicService).findOwnerById(1);
  }

  @Test
  @DisplayName("POST /owners/{ownerId}/edit - valid form saves and redirects")
  void testProcessUpdateOwnerFormSuccess() throws Exception {
      mockMvc.perform(post("/owners/1/edit")
              .param("firstName", "George")
              .param("lastName", "Franklin")
              .param("address", "110 W. Liberty St.")
              .param("city", "Madison")
              .param("telephone", "6085551023"))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/owners/1"));
  
      verify(clinicService, times(1)).saveOwner(any(Owner.class));
  }

  @Test
  @DisplayName("POST /owners/{ownerId}/edit - invalid form returns update form with errors")
  void testProcessUpdateOwnerFormHasErrors() throws Exception {
      mockMvc.perform(post("/owners/1/edit")
              .param("firstName", "")
              .param("lastName", "")
              .param("address", "")
              .param("city", "")
              .param("telephone", ""))
          .andExpect(status().isOk())
          .andExpect(model().attributeHasErrors("owner"))
          .andExpect(model().attributeHasFieldErrors("owner", "firstName", "lastName", "address", "city", "telephone"))
          .andExpect(view().name("owners/createOrUpdateOwnerForm"));
  
      verify(clinicService, never()).saveOwner(any(Owner.class));
  }

  @Test
  @DisplayName("POST /owners/{ownerId}/edit - missing city returns form")
  void testProcessUpdateOwnerFormMissingCity() throws Exception {
      mockMvc.perform(post("/owners/2/edit")
              .param("firstName", "Betty")
              .param("lastName", "Davis")
              .param("address", "638 Cardinal Ave.")
              .param("telephone", "6085551749"))
          .andExpect(status().isOk())
          .andExpect(model().attributeHasFieldErrors("owner", "city"))
          .andExpect(view().name("owners/createOrUpdateOwnerForm"));
  
      verify(clinicService, never()).saveOwner(any(Owner.class));
  }

  @Test
  @DisplayName("POST /owners/{ownerId}/edit - ownerId from path is set on owner before save")
  void testProcessUpdateOwnerFormSetsId() throws Exception {
      doAnswer(invocation -> {
          Owner saved = invocation.getArgument(0);
          // Verify that the id was set to the path variable value
          assert saved.getId() == 5;
          return null;
      }).when(clinicService).saveOwner(any(Owner.class));
  
      mockMvc.perform(post("/owners/5/edit")
              .param("firstName", "George")
              .param("lastName", "Franklin")
              .param("address", "110 W. Liberty St.")
              .param("city", "Madison")
              .param("telephone", "6085551023"))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/owners/5"));
  
      verify(clinicService).saveOwner(any(Owner.class));
  }

  @Test
  @DisplayName("GET /owners/{ownerId} - returns owner details view")
  void testShowOwner() throws Exception {
      when(clinicService.findOwnerById(1)).thenReturn(george);
  
      mockMvc.perform(get("/owners/1"))
          .andExpect(status().isOk())
          .andExpect(model().attributeExists("owner"))
          .andExpect(model().attribute("owner", hasProperty("lastName", is("Franklin"))))
          .andExpect(model().attribute("owner", hasProperty("firstName", is("George"))))
          .andExpect(view().name("owners/ownerDetails"));
  
      verify(clinicService).findOwnerById(1);
  }

  @Test
  @DisplayName("GET /owners/{ownerId} - returns correct owner for given id")
  void testShowOwnerDifferentId() throws Exception {
      when(clinicService.findOwnerById(2)).thenReturn(betty);
  
      mockMvc.perform(get("/owners/2"))
          .andExpect(status().isOk())
          .andExpect(model().attribute("owner", hasProperty("firstName", is("Betty"))))
          .andExpect(model().attribute("owner", hasProperty("lastName", is("Davis"))))
          .andExpect(view().name("owners/ownerDetails"));
  
      verify(clinicService).findOwnerById(2);
  }

  @Test
  @DisplayName("InitBinder - 'id' field is disallowed and ignored on binding")
  void testSetAllowedFields() throws Exception {
      // 'id' submitted in form should be ignored by the binder
      doAnswer(invocation -> {
          Owner saved = invocation.getArgument(0);
          // id must not have been bound from the request param
          assert saved.getId() == null;
          return null;
      }).when(clinicService).saveOwner(any(Owner.class));
  
      mockMvc.perform(post("/owners/new")
              .param("id", "999")
              .param("firstName", "George")
              .param("lastName", "Franklin")
              .param("address", "110 W. Liberty St.")
              .param("city", "Madison")
              .param("telephone", "6085551023"))
          .andExpect(status().is3xxRedirection());
  
      verify(clinicService).saveOwner(any(Owner.class));
  }
}