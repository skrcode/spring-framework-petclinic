package org.springframework.samples.petclinic.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.Visit;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.WebDataBinder;

import java.util.ArrayList;
import java.util.List;

import org.mockito.Mockito;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.atLeastOnce;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the {@link VisitController}.
 */
@SpringJUnitWebConfig(locations = {"classpath:spring/mvc-core-config.xml", "classpath:spring/mvc-test-config.xml"})
class VisitControllerTests {

    private static final int TEST_PET_ID = 1;
    private static final int TEST_OWNER_ID = 1;

    @Autowired
    private VisitController visitController;

    @Autowired
    private ClinicService clinicService;

    private MockMvc mockMvc;
  @BeforeEach
  void setUp() {
      // Reset the shared Spring-managed Mockito proxy so invocation counts
      // do not accumulate across tests and stubs remain deterministic.
      Mockito.reset(clinicService);

      this.mockMvc = MockMvcBuilders
          .standaloneSetup(visitController)
          .build();

      Pet pet = new Pet();
      pet.setId(TEST_PET_ID);
      given(this.clinicService.findPetById(TEST_PET_ID)).willReturn(pet);
  }

  @Test
  void testInitNewVisitForm() throws Exception {
      mockMvc.perform(get("/owners/{ownerId}/pets/{petId}/visits/new", TEST_OWNER_ID, TEST_PET_ID))
          .andExpect(status().isOk())
          .andExpect(view().name("pets/createOrUpdateVisitForm"))
          .andExpect(model().attributeExists("visit"));
  }

  @Test
  void testProcessNewVisitFormSuccess() throws Exception {
      mockMvc.perform(post("/owners/{ownerId}/pets/{petId}/visits/new", TEST_OWNER_ID, TEST_PET_ID)
          .param("date", "2023/01/01")
          .param("description", "Annual check-up")
      )
          .andExpect(status().is3xxRedirection())
          .andExpect(view().name("redirect:/owners/{ownerId}"));
  
      verify(clinicService, times(1)).saveVisit(org.mockito.ArgumentMatchers.any(Visit.class));
  }

  @Test
  void testProcessNewVisitFormHasErrors() throws Exception {
      // description is @NotEmpty — submitting without it should trigger validation error
      mockMvc.perform(post("/owners/{ownerId}/pets/{petId}/visits/new", TEST_OWNER_ID, TEST_PET_ID)
          .param("date", "2023/01/01")
      )
          .andExpect(status().isOk())
          .andExpect(model().attributeHasErrors("visit"))
          .andExpect(model().attributeHasFieldErrors("visit", "description"))
          .andExpect(view().name("pets/createOrUpdateVisitForm"));
  
      verify(clinicService, never()).saveVisit(org.mockito.ArgumentMatchers.any(Visit.class));
  }

  @Test
  void testProcessNewVisitFormWithEmptyDescription() throws Exception {
      // Empty string for @NotEmpty field should also cause a validation error
      mockMvc.perform(post("/owners/{ownerId}/pets/{petId}/visits/new", TEST_OWNER_ID, TEST_PET_ID)
          .param("date", "2023/06/15")
          .param("description", "")
      )
          .andExpect(status().isOk())
          .andExpect(model().attributeHasErrors("visit"))
          .andExpect(model().attributeHasFieldErrors("visit", "description"))
          .andExpect(view().name("pets/createOrUpdateVisitForm"));
  
      verify(clinicService, never()).saveVisit(org.mockito.ArgumentMatchers.any(Visit.class));
  }

  @Test
  void testShowVisits() throws Exception {
      List<Visit> visits = new ArrayList<>();
      Visit v1 = new Visit();
      v1.setDescription("Check-up");
      Visit v2 = new Visit();
      v2.setDescription("Vaccination");
  
      Pet pet = new Pet();
      pet.setId(TEST_PET_ID);
      pet.addVisit(v1);
      pet.addVisit(v2);
      given(this.clinicService.findPetById(TEST_PET_ID)).willReturn(pet);
  
      mockMvc.perform(get("/owners/{ownerId}/pets/{petId}/visits", TEST_OWNER_ID, TEST_PET_ID))
          .andExpect(status().isOk())
          .andExpect(view().name("visitList"))
          .andExpect(model().attributeExists("visits"));
  }

  @Test
  void testShowVisitsWithNoVisits() throws Exception {
      // Pet with no visits — model should contain an empty visits collection
      Pet pet = new Pet();
      pet.setId(TEST_PET_ID);
      given(this.clinicService.findPetById(TEST_PET_ID)).willReturn(pet);
  
      mockMvc.perform(get("/owners/{ownerId}/pets/{petId}/visits", TEST_OWNER_ID, TEST_PET_ID))
          .andExpect(status().isOk())
          .andExpect(view().name("visitList"))
          .andExpect(model().attributeExists("visits"));
  }

  @Test
  void testLoadPetWithVisit() {
      // Verify the controller properly wires up with ClinicService
      // and that loadPetWithVisit creates a new Visit tied to the pet
      Pet pet = new Pet();
      pet.setId(TEST_PET_ID);
      given(this.clinicService.findPetById(TEST_PET_ID)).willReturn(pet);
  
      Visit visit = visitController.loadPetWithVisit(TEST_PET_ID);
  
      org.junit.jupiter.api.Assertions.assertNotNull(visit);
      org.junit.jupiter.api.Assertions.assertNotNull(visit.getPet());
      org.junit.jupiter.api.Assertions.assertEquals(TEST_PET_ID, visit.getPet().getId());
  }

  @Test
  void testSetAllowedFields() {
      WebDataBinder dataBinder = new WebDataBinder(new Visit());
      visitController.setAllowedFields(dataBinder);
  
      String[] disallowedFields = dataBinder.getDisallowedFields();
      org.junit.jupiter.api.Assertions.assertNotNull(disallowedFields);
      org.junit.jupiter.api.Assertions.assertEquals(1, disallowedFields.length);
      org.junit.jupiter.api.Assertions.assertEquals("id", disallowedFields[0]);
  }

  @Test
  void testInitNewVisitFormModelContainsVisit() throws Exception {
      // The visit in the model should be associated with the correct pet
      mockMvc.perform(get("/owners/*/pets/{petId}/visits/new", TEST_PET_ID))
          .andExpect(status().isOk())
          .andExpect(model().attributeExists("visit"))
          .andExpect(view().name("pets/createOrUpdateVisitForm"));
  
      verify(clinicService, times(1)).findPetById(TEST_PET_ID);
  }

  @Test
  void testProcessNewVisitFormSuccessCallsSaveVisit() throws Exception {
      // Confirm saveVisit is invoked exactly once on a valid submission
      mockMvc.perform(post("/owners/{ownerId}/pets/{petId}/visits/new", TEST_OWNER_ID, TEST_PET_ID)
          .param("description", "Teeth cleaning")
      )
          .andExpect(status().is3xxRedirection());
  
      verify(clinicService, times(1)).saveVisit(org.mockito.ArgumentMatchers.any(Visit.class));
  }
}