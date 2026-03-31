package org.springframework.samples.petclinic.web;

import java.util.ArrayList;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class OwnerControllerTest {

    private static final int TEST_OWNER_ID = 1;

    @Mock
    private ClinicService clinicService;

    @InjectMocks
    private OwnerController ownerController;

    private MockMvc mockMvc;

    private Owner george;
  @BeforeEach
  void setUp() {
      mockMvc = MockMvcBuilders.standaloneSetup(ownerController).build();

      george = new Owner();
      george.setId(TEST_OWNER_ID);
      george.setFirstName("George");
      george.setLastName("Franklin");
      george.setAddress("110 W. Liberty St.");
      george.setCity("Madison");
      george.setTelephone("6085551023");
  }

  @Test
  @DisplayName("GET /owners/new — returns creation form with empty owner in model")
  void testInitCreationForm() throws Exception {
      mockMvc.perform(get("/owners/new"))
          .andExpect(status().isOk())
          .andExpect(model().attributeExists("owner"))
          .andExpect(view().name("owners/createOrUpdateOwnerForm"));
  }

  @Test
  @DisplayName("POST /owners/new — valid owner redirects to new owner's detail page")
  void testProcessCreationFormSuccess() throws Exception {
      mockMvc.perform(post("/owners/new")
              .param("firstName", "Joe")
              .param("lastName", "Bloggs")
              .param("address", "123 Caramel Street")
              .param("city", "London")
              .param("telephone", "0165431223"))
          .andExpect(status().is3xxRedirection());

      verify(clinicService, times(1)).saveOwner(any(Owner.class));
  }

  @Test
  @DisplayName("POST /owners/new — missing required fields returns form with errors")
  void testProcessCreationFormHasErrors() throws Exception {
      mockMvc.perform(post("/owners/new")
              .param("firstName", "Joe")
              .param("lastName", "")   // blank — @NotEmpty violation
              .param("address", "")    // blank — @NotEmpty violation
              .param("city", "London")
              .param("telephone", "0165431223"))
          .andExpect(status().isOk())
          .andExpect(model().attributeHasErrors("owner"))
          .andExpect(model().attributeHasFieldErrors("owner", "lastName", "address"))
          .andExpect(view().name("owners/createOrUpdateOwnerForm"));

      verify(clinicService, never()).saveOwner(any(Owner.class));
  }

  @Test
  @DisplayName("GET /owners/find — returns find-owners form with empty owner")
  void testInitFindForm() throws Exception {
      mockMvc.perform(get("/owners/find"))
          .andExpect(status().isOk())
          .andExpect(model().attributeExists("owner"))
          .andExpect(view().name("owners/findOwners"));
  }

  @Test
  @DisplayName("GET /owners — no matching owners → stays on findOwners with field error")
  void testProcessFindFormNoOwnersFound() throws Exception {
      given(clinicService.findOwnerByLastName("Unknown"))
          .willReturn(Collections.emptyList());

      mockMvc.perform(get("/owners")
              .param("lastName", "Unknown"))
          .andExpect(status().isOk())
          .andExpect(model().attributeHasFieldErrors("owner", "lastName"))
          .andExpect(view().name("owners/findOwners"));
  }

  @Test
  @DisplayName("GET /owners — exactly one match → redirect to that owner's page")
  void testProcessFindFormOneOwnerFound() throws Exception {
      given(clinicService.findOwnerByLastName("Franklin"))
          .willReturn(Collections.singletonList(george));

      mockMvc.perform(get("/owners")
              .param("lastName", "Franklin"))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/owners/" + TEST_OWNER_ID));
  }

  @Test
  @DisplayName("GET /owners — multiple matches → ownersList view with selections")
  void testProcessFindFormMultipleOwnersFound() throws Exception {
      Owner another = new Owner();
      another.setId(2);
      another.setFirstName("Betty");
      another.setLastName("Davis");
      another.setAddress("638 Cardinal Ave.");
      another.setCity("Sun Prairie");
      another.setTelephone("6085551749");

      List<Owner> owners = new ArrayList<>();
      owners.add(george);
      owners.add(another);

      given(clinicService.findOwnerByLastName("")).willReturn(owners);

      mockMvc.perform(get("/owners"))
          .andExpect(status().isOk())
          .andExpect(model().attributeExists("selections"))
          .andExpect(view().name("owners/ownersList"));
  }

  @Test
  @DisplayName("GET /owners — null lastName defaults to empty string (broadest search)")
  void testProcessFindFormNullLastName() throws Exception {
      given(clinicService.findOwnerByLastName("")).willReturn(Collections.singletonList(george));

      // No lastName param → owner.getLastName() is null; controller sets it to ""
      mockMvc.perform(get("/owners"))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/owners/" + TEST_OWNER_ID));
  }

  @Test
  @DisplayName("GET /owners/{ownerId}/edit — pre-populates form with existing owner")
  void testInitUpdateOwnerForm() throws Exception {
      given(clinicService.findOwnerById(TEST_OWNER_ID)).willReturn(george);

      mockMvc.perform(get("/owners/{ownerId}/edit", TEST_OWNER_ID))
          .andExpect(status().isOk())
          .andExpect(model().attributeExists("owner"))
          .andExpect(model().attribute("owner", hasProperty("lastName", is("Franklin"))))
          .andExpect(model().attribute("owner", hasProperty("firstName", is("George"))))
          .andExpect(view().name("owners/createOrUpdateOwnerForm"));
  }

  @Test
  @DisplayName("POST /owners/{ownerId}/edit — valid data redirects to owner detail")
  void testProcessUpdateOwnerFormSuccess() throws Exception {
      mockMvc.perform(post("/owners/{ownerId}/edit", TEST_OWNER_ID)
              .param("firstName", "Joe")
              .param("lastName", "Bloggs")
              .param("address", "123 Caramel Street")
              .param("city", "London")
              .param("telephone", "0165431223"))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/owners/" + TEST_OWNER_ID));

      verify(clinicService, times(1)).saveOwner(any(Owner.class));
  }

  @Test
  @DisplayName("POST /owners/{ownerId}/edit — validation errors return form")
  void testProcessUpdateOwnerFormHasErrors() throws Exception {
      mockMvc.perform(post("/owners/{ownerId}/edit", TEST_OWNER_ID)
              .param("firstName", "Joe")
              .param("lastName", "")    // blank — @NotEmpty violation
              .param("address", "123 Caramel Street")
              .param("city", "")        // blank — @NotEmpty violation
              .param("telephone", "0165431223"))
          .andExpect(status().isOk())
          .andExpect(model().attributeHasErrors("owner"))
          .andExpect(model().attributeHasFieldErrors("owner", "lastName", "city"))
          .andExpect(view().name("owners/createOrUpdateOwnerForm"));

      verify(clinicService, never()).saveOwner(any(Owner.class));
  }

  @Test
  @DisplayName("GET /owners/{ownerId} — renders owner detail view with correct owner")
  void testShowOwner() throws Exception {
      given(clinicService.findOwnerById(TEST_OWNER_ID)).willReturn(george);

      mockMvc.perform(get("/owners/{ownerId}", TEST_OWNER_ID))
          .andExpect(status().isOk())
          .andExpect(model().attribute("owner", hasProperty("lastName", is("Franklin"))))
          .andExpect(model().attribute("owner", hasProperty("firstName", is("George"))))
          .andExpect(model().attribute("owner", hasProperty("address", is("110 W. Liberty St."))))
          .andExpect(model().attribute("owner", hasProperty("city", is("Madison"))))
          .andExpect(model().attribute("owner", hasProperty("telephone", is("6085551023"))))
          .andExpect(view().name("owners/ownerDetails"));
  }

  @Test
  @DisplayName("InitBinder — 'id' field is disallowed and silently dropped from binding")
  void testSetAllowedFields() throws Exception {
      // 'id' is in the disallowed list; even if sent, it must not be bound.
      // Posting an 'id' param together with a valid owner should still succeed
      // (the id comes from the path variable, not the form body).
      mockMvc.perform(post("/owners/{ownerId}/edit", TEST_OWNER_ID)
              .param("id", "99")           // should be stripped by InitBinder
              .param("firstName", "George")
              .param("lastName", "Franklin")
              .param("address", "110 W. Liberty St.")
              .param("city", "Madison")
              .param("telephone", "6085551023"))
          .andExpect(status().is3xxRedirection());

      verifys(clinicService, times(1)).saveOwner(argThat(o -> !Integer.valueOf(99).equals(o.getId())));
  }
}
