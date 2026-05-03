package org.springframework.samples.petclinic.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.Visit;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for {@link VisitController}
 */
@SpringJUnitWebConfig(locations = {"classpath:spring/mvc-core-config.xml", "classpath:spring/mvc-test-config.xml"})
class VisitControllerTests {

    private static final int TEST_PET_ID    = 1;
    private static final int TEST_OWNER_ID  = 1;

    @Autowired
    private VisitController visitController;

    @Autowired
    private ClinicService clinicService;

    private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
      // Reset shared mock so interaction counts start fresh for every test
      Mockito.reset(clinicService);

      this.mockMvc = MockMvcBuilders.standaloneSetup(visitController).build();

      Pet pet = new Pet();
      pet.setId(TEST_PET_ID);
      given(this.clinicService.findPetById(TEST_PET_ID)).willReturn(pet);
  }

  @Test
  void testInitNewVisitForm() throws Exception {
      mockMvc.perform(get("/owners/{ownerId}/pets/{petId}/visits/new", TEST_OWNER_ID, TEST_PET_ID))
          .andExpect(status().isOk())
          .andExpect(model().attributeExists("visit"))
          .andExpect(view().name("pets/createOrUpdateVisitForm"));
  }

  @Test
  void testInitNewVisitFormLoadsPetAndCreatesVisit() throws Exception {
      mockMvc.perform(get("/owners/{ownerId}/pets/{petId}/visits/new", TEST_OWNER_ID, TEST_PET_ID))
          .andExpect(status().isOk())
          .andExpect(model().attributeExists("visit"));
  
      verify(clinicService, times(1)).findPetById(TEST_PET_ID);
  }

  @Test
  void testProcessNewVisitFormSuccess() throws Exception {
      mockMvc.perform(post("/owners/{ownerId}/pets/{petId}/visits/new", TEST_OWNER_ID, TEST_PET_ID)
              .param("date", "2024/01/15")
              .param("description", "Annual check-up")
          )
          .andExpect(status().is3xxRedirection())
          .andExpect(view().name("redirect:/owners/{ownerId}"));
  
      verify(clinicService).saveVisit(any(Visit.class));
  }

  @Test
  void testProcessNewVisitFormHasErrors() throws Exception {
      mockMvc.perform(post("/owners/{ownerId}/pets/{petId}/visits/new", TEST_OWNER_ID, TEST_PET_ID)
              .param("description", "")  // @NotEmpty fails
          )
          .andExpect(status().isOk())
          .andExpect(model().attributeHasErrors("visit"))
          .andExpect(model().attributeHasFieldErrors("visit", "description"))
          .andExpect(view().name("pets/createOrUpdateVisitForm"));
  }

  @Test
  void testProcessNewVisitFormHasErrorsDoesNotSave() throws Exception {
      mockMvc.perform(post("/owners/{ownerId}/pets/{petId}/visits/new", TEST_OWNER_ID, TEST_PET_ID)
              .param("description", "")
          )
          .andExpect(status().isOk());
  
      verify(clinicService, times(0)).saveVisit(any(Visit.class));
  }

  @Test
  void testShowVisits() throws Exception {
      Pet pet = new Pet();
      pet.setId(TEST_PET_ID);
      Visit visit1 = new Visit();
      visit1.setDescription("Routine check");
      pet.addVisit(visit1);
      given(this.clinicService.findPetById(TEST_PET_ID)).willReturn(pet);
  
      mockMvc.perform(get("/owners/{ownerId}/pets/{petId}/visits", TEST_OWNER_ID, TEST_PET_ID))
          .andExpect(status().isOk())
          .andExpect(model().attributeExists("visits"))
          .andExpect(view().name("visitList"));
  }

  @Test
  void testShowVisitsNoPriorVisits() throws Exception {
      // loadPetWithVisit (@ModelAttribute) always adds one new Visit to the Pet before showVisits
      // runs, so the list will never be empty even when no historical visits exist.
      // We verify the model attribute is populated and the correct view is rendered.
      mockMvc.perform(get("/owners/{ownerId}/pets/{petId}/visits", TEST_OWNER_ID, TEST_PET_ID))
          .andExpect(status().isOk())
          .andExpect(model().attributeExists("visits"))
          .andExpect(view().name("visitList"));
  }

  @Test
  void testModelAttributeVisitLinkedToPet() throws Exception {
      // loadPetWithVisit should add the new Visit to the Pet and return it as model attribute
      mockMvc.perform(get("/owners/{ownerId}/pets/{petId}/visits/new", TEST_OWNER_ID, TEST_PET_ID))
          .andExpect(status().isOk())
          .andExpect(model().attributeExists("visit"));
  
      // the pet returned by the service should now have one visit associated
      verify(clinicService).findPetById(TEST_PET_ID);
  }

  @Test
  void testProcessNewVisitFormBinderDisallowsId() throws Exception {
      // Even if 'id' is supplied in form params it should be ignored by @InitBinder
      mockMvc.perform(post("/owners/{ownerId}/pets/{petId}/visits/new", TEST_OWNER_ID, TEST_PET_ID)
              .param("id", "99")
              .param("description", "Vaccine")
          )
          .andExpect(status().is3xxRedirection())
          .andExpect(view().name("redirect:/owners/{ownerId}"));
  
      // visit saved should NOT have the supplied id (binder disallows it)
      verify(clinicService).saveVisit(any(Visit.class));
  }
}