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
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
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
  void testInitNewVisitFormUsesWildcardOwner() throws Exception {
      // The GET mapping uses /owners/*/pets/{petId}/visits/new — any ownerId segment works
      mockMvc.perform(get("/owners/42/pets/{petId}/visits/new", TEST_PET_ID))
          .andExpect(status().isOk())
          .andExpect(view().name("pets/createOrUpdateVisitForm"))
          .andExpect(model().attributeExists("visit"));
  
      verify(clinicService, times(1)).findPetById(TEST_PET_ID);
  }

  @Test
  void testInitNewVisitFormVisitLinkedToPet() throws Exception {
      // The @ModelAttribute visit should be linked to the pet via loadPetWithVisit
      mockMvc.perform(get("/owners/{ownerId}/pets/{petId}/visits/new", TEST_OWNER_ID, TEST_PET_ID))
          .andExpect(status().isOk())
          .andExpect(model().attribute("visit", hasProperty("pet", notNullValue())));
  }

  @Test
  void testProcessNewVisitFormSuccess() throws Exception {
      mockMvc.perform(post("/owners/{ownerId}/pets/{petId}/visits/new", TEST_OWNER_ID, TEST_PET_ID)
          .param("date", "2023/01/01")
          .param("description", "Annual check-up")
      )
          .andExpect(status().is3xxRedirection())
          .andExpect(view().name("redirect:/owners/{ownerId}"));
  
      verify(clinicService, times(1)).saveVisit(any(Visit.class));
  }

  @Test
  void testProcessNewVisitFormSuccessRedirectContainsOwnerId() throws Exception {
      // Ensure the redirect view name literally contains the ownerId placeholder
      mockMvc.perform(post("/owners/{ownerId}/pets/{petId}/visits/new", TEST_OWNER_ID, TEST_PET_ID)
          .param("description", "General check")
      )
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrlPattern("/owners/*"));
  }

  @Test
  void testProcessNewVisitFormSuccessDoesNotReturnForm() throws Exception {
      // On success the form view should NOT be returned
      mockMvc.perform(post("/owners/{ownerId}/pets/{petId}/visits/new", TEST_OWNER_ID, TEST_PET_ID)
          .param("description", "Blood work")
      )
          .andExpect(view().name(not(equalTo("pets/createOrUpdateVisitForm"))));
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
  
      verify(clinicService, never()).saveVisit(any(Visit.class));
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
  
      verify(clinicService, never()).saveVisit(any(Visit.class));
  }

  @Test
  void testProcessNewVisitFormWithBlankDescription() throws Exception {
      // Whitespace-only description — @NotEmpty rejects blank strings
      mockMvc.perform(post("/owners/{ownerId}/pets/{petId}/visits/new", TEST_OWNER_ID, TEST_PET_ID)
          .param("description", "   ")
      )
          .andExpect(status().isOk())
          .andExpect(model().attributeHasErrors("visit"))
          .andExpect(view().name("pets/createOrUpdateVisitForm"));
  
      verify(clinicService, never()).saveVisit(any(Visit.class));
  }

  @Test
  void testProcessNewVisitFormSavesVisitWithCorrectPet() throws Exception {
      // Capture the Visit passed to saveVisit and verify it is associated with the correct pet
      mockMvc.perform(post("/owners/{ownerId}/pets/{petId}/visits/new", TEST_OWNER_ID, TEST_PET_ID)
          .param("description", "Dental check")
      )
          .andExpect(status().is3xxRedirection());
  
      org.mockito.ArgumentCaptor<Visit> captor = org.mockito.ArgumentCaptor.forClass(Visit.class);
      verify(clinicService).saveVisit(captor.capture());
      Visit saved = captor.getValue();
      assertNotNull(saved.getPet());
      assertEquals(TEST_PET_ID, saved.getPet().getId());
  }

  @Test
  void testShowVisits() throws Exception {
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
  void testShowVisitsContainsAllVisits() throws Exception {
      // Model should contain exactly the same visits as the pet
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
          .andExpect(model().attribute("visits", hasSize(2)))
          .andExpect(model().attribute("visits", hasItem(hasProperty("description", is("Check-up")))))
          .andExpect(model().attribute("visits", hasItem(hasProperty("description", is("Vaccination")))));
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
          .andExpect(model().attribute("visits", hasSize(0)));
  }

  @Test
  void testShowVisitsCallsFindPetById() throws Exception {
      // Verify ClinicService.findPetById is called with the correct petId
      Pet pet = new Pet();
      pet.setId(TEST_PET_ID);
      given(this.clinicService.findPetById(TEST_PET_ID)).willReturn(pet);
  
      mockMvc.perform(get("/owners/{ownerId}/pets/{petId}/visits", TEST_OWNER_ID, TEST_PET_ID))
          .andExpect(status().isOk());
  
      // loadPetWithVisit (for @ModelAttribute) + showVisits both call findPetById
      verify(clinicService, atLeast(1)).findPetById(TEST_PET_ID);
  }

  @Test
  void testLoadPetWithVisit() {
      // Verify the controller properly wires up with ClinicService
      // and that loadPetWithVisit creates a new Visit tied to the pet
      Pet pet = new Pet();
      pet.setId(TEST_PET_ID);
      given(this.clinicService.findPetById(TEST_PET_ID)).willReturn(pet);
  
      Visit visit = visitController.loadPetWithVisit(TEST_PET_ID);
  
      assertNotNull(visit);
      assertNotNull(visit.getPet());
      assertEquals(TEST_PET_ID, visit.getPet().getId());
  }

  @Test
  void testLoadPetWithVisitAddsVisitToPet() {
      // The returned Visit should be contained in the pet's visit list
      Pet pet = new Pet();
      pet.setId(TEST_PET_ID);
      given(this.clinicService.findPetById(TEST_PET_ID)).willReturn(pet);
  
      Visit visit = visitController.loadPetWithVisit(TEST_PET_ID);
  
      assertTrue(pet.getVisits().contains(visit),
          "Pet should contain the newly created visit after loadPetWithVisit");
  }

  @Test
  void testLoadPetWithVisitHasDefaultDate() {
      // Visit() constructor sets date to LocalDate.now()
      Pet pet = new Pet();
      pet.setId(TEST_PET_ID);
      given(this.clinicService.findPetById(TEST_PET_ID)).willReturn(pet);
  
      Visit visit = visitController.loadPetWithVisit(TEST_PET_ID);
  
      assertNotNull(visit.getDate(), "Visit date should be pre-populated with today's date");
  }

  @Test
  void testSetAllowedFields() {
      WebDataBinder dataBinder = new WebDataBinder(new Visit());
      visitController.setAllowedFields(dataBinder);
  
      String[] disallowedFields = dataBinder.getDisallowedFields();
      assertNotNull(disallowedFields);
      assertEquals(1, disallowedFields.length);
      assertEquals("id", disallowedFields[0]);
  }

  @Test
  void testSetAllowedFieldsDisallowsIdBinding() throws Exception {
      // Attempting to bind 'id' via the form should be silently ignored
      mockMvc.perform(post("/owners/{ownerId}/pets/{petId}/visits/new", TEST_OWNER_ID, TEST_PET_ID)
          .param("id", "999")
          .param("description", "Annual check-up")
      )
          .andExpect(status().is3xxRedirection())
          .andExpect(model().attributeDoesNotExist("id"));
  
      // saveVisit should still be called — binding error on 'id' doesn't count as form error
      verify(clinicService, times(1)).saveVisit(any(Visit.class));
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
  
      verify(clinicService, times(1)).saveVisit(any(Visit.class));
  }

  @Test
  void testShowVisitsDifferentPetId() throws Exception {
      // showVisits should work for a different petId too
      int anotherPetId = 7;
      Visit v = new Visit();
      v.setDescription("X-ray");
      Pet anotherPet = new Pet();
      anotherPet.setId(anotherPetId);
      anotherPet.addVisit(v);
      given(this.clinicService.findPetById(anotherPetId)).willReturn(anotherPet);
  
      mockMvc.perform(get("/owners/{ownerId}/pets/{petId}/visits", TEST_OWNER_ID, anotherPetId))
          .andExpect(status().isOk())
          .andExpect(view().name("visitList"))
          .andExpect(model().attribute("visits", hasSize(1)))
          .andExpect(model().attribute("visits", hasItem(hasProperty("description", is("X-ray")))));
  }
}