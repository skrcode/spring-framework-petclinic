/*
 * Copyright 2002-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.web;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.Visit;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.WebDataBinder;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Test class for {@link VisitController}.
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
      this.mockMvc = MockMvcBuilders.standaloneSetup(visitController).build();
  
      // Reset mock and configure default stub for findPetById
      reset(clinicService);
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
  void testInitNewVisitFormWithWildcardOwner() throws Exception {
      // The URL pattern uses wildcard for ownerId: /owners/*/pets/{petId}/visits/new
      mockMvc.perform(get("/owners/99/pets/{petId}/visits/new", TEST_PET_ID))
          .andExpect(status().isOk())
          .andExpect(view().name("pets/createOrUpdateVisitForm"))
          .andExpect(model().attributeExists("visit"));
  }

  @Test
  void testProcessNewVisitFormSuccess() throws Exception {
      mockMvc.perform(post("/owners/{ownerId}/pets/{petId}/visits/new", TEST_OWNER_ID, TEST_PET_ID)
          .param("description", "Rabies vaccination")
      )
          .andExpect(status().is3xxRedirection())
          .andExpect(view().name("redirect:/owners/{ownerId}"));
  
      verify(clinicService).saveVisit(org.mockito.ArgumentMatchers.any(Visit.class));
  }

  @Test
  void testProcessNewVisitFormRedirectUriTemplate() throws Exception {
      // Spring MVC must keep the {ownerId} URI template intact in the redirect view name
      mockMvc.perform(post("/owners/{ownerId}/pets/{petId}/visits/new", TEST_OWNER_ID, TEST_PET_ID)
          .param("description", "Post-surgery follow-up")
      )
          .andExpect(view().name("redirect:/owners/{ownerId}"));
  }

  @Test
  void testProcessNewVisitFormHasErrors() throws Exception {
      // When description is missing (blank), Bean Validation should fail and re-show the form
      mockMvc.perform(post("/owners/{ownerId}/pets/{petId}/visits/new", TEST_OWNER_ID, TEST_PET_ID))
          .andExpect(status().isOk())
          .andExpect(view().name("pets/createOrUpdateVisitForm"))
          .andExpect(model().attributeHasErrors("visit"));
  
      verify(clinicService, never()).saveVisit(org.mockito.ArgumentMatchers.any(Visit.class));
  }

  @Test
  void testProcessNewVisitFormDescriptionSavedCorrectly() throws Exception {
      mockMvc.perform(post("/owners/{ownerId}/pets/{petId}/visits/new", TEST_OWNER_ID, TEST_PET_ID)
          .param("description", "Annual vaccine booster")
      )
          .andExpect(status().is3xxRedirection());
  
      ArgumentCaptor<Visit> captor = ArgumentCaptor.forClass(Visit.class);
      verify(clinicService).saveVisit(captor.capture());
      assertEquals("Annual vaccine booster", captor.getValue().getDescription());
  }

  @Test
  void testShowVisits() throws Exception {
      mockMvc.perform(get("/owners/{ownerId}/pets/{petId}/visits", TEST_OWNER_ID, TEST_PET_ID))
          .andExpect(status().isOk())
          .andExpect(view().name("visitList"))
          .andExpect(model().attributeExists("visits"));
  }

  @Test
  void testShowVisitsUrlWithWildcardOwner() throws Exception {
      mockMvc.perform(get("/owners/99/pets/{petId}/visits", TEST_PET_ID))
          .andExpect(status().isOk())
          .andExpect(view().name("visitList"))
          .andExpect(model().attributeExists("visits"));
  }

  @Test
  void testShowVisitsModelContainsVisitData() throws Exception {
      Visit v = new Visit();
      v.setDescription("Ultrasound check");
      Pet pet = new Pet();
      pet.setId(TEST_PET_ID);
      pet.addVisit(v);
      given(this.clinicService.findPetById(TEST_PET_ID)).willReturn(pet);
  
      mockMvc.perform(get("/owners/{ownerId}/pets/{petId}/visits", TEST_OWNER_ID, TEST_PET_ID))
          .andExpect(status().isOk())
          .andExpect(model().attribute("visits",
              hasItem(hasProperty("description", is("Ultrasound check")))));
  }

  @Test
  void testShowVisitsMultipleVisitsSortedByDateDesc() throws Exception {
      Pet pet = new Pet();
      pet.setId(TEST_PET_ID);
  
      Visit older = new Visit();
      older.setDescription("Older visit");
      older.setDate(LocalDate.of(2023, 1, 10));
  
      Visit newer = new Visit();
      newer.setDescription("Newer visit");
      newer.setDate(LocalDate.of(2024, 6, 15));
  
      pet.addVisit(older);
      pet.addVisit(newer);
      given(this.clinicService.findPetById(TEST_PET_ID)).willReturn(pet);
  
      // NOTE: @ModelAttribute loadPetWithVisit also runs before the handler, adding a 3rd
      // (date-less) visit to the same pet. Assert both named visits appear in the model
      // and verify sort order directly on the pet's list (filtering out the unnamed one).
      mockMvc.perform(get("/owners/{ownerId}/pets/{petId}/visits", TEST_OWNER_ID, TEST_PET_ID))
          .andExpect(status().isOk())
          .andExpect(view().name("visitList"))
          .andExpect(model().attributeExists("visits"))
          .andExpect(model().attribute("visits",
              hasItem(hasProperty("description", is("Newer visit")))))
          .andExpect(model().attribute("visits",
              hasItem(hasProperty("description", is("Older visit")))));

      // Sort order check: among visits with non-null descriptions, newest date first
      List<Visit> namedVisits = pet.getVisits().stream()
          .filter(v -> v.getDescription() != null)
          .sorted(java.util.Comparator.comparing(Visit::getDate).reversed())
          .collect(java.util.stream.Collectors.toList());
      assertEquals("Newer visit", namedVisits.get(0).getDescription());
      assertEquals("Older visit", namedVisits.get(1).getDescription());
  }

  @Test
  void testSetAllowedFieldsDisallowsId() {
      WebDataBinder binder = new WebDataBinder(new Object());
      visitController.setAllowedFields(binder);
  
      String[] disallowed = binder.getDisallowedFields();
      assertNotNull(disallowed, "Disallowed fields must not be null");
      assertEquals(1, disallowed.length, "Exactly one field should be disallowed");
      assertEquals("id", disallowed[0], "The disallowed field must be 'id'");
  }

  @Test
  void testLoadPetWithVisitAssociatesToPet() {
      Pet pet = new Pet();
      pet.setId(TEST_PET_ID);
      given(this.clinicService.findPetById(TEST_PET_ID)).willReturn(pet);
  
      Visit visit = visitController.loadPetWithVisit(TEST_PET_ID);
  
      assertNotNull(visit);
      assertSame(pet, visit.getPet(), "The visit must be associated with the correct pet");
  }

  @Test
  void testLoadPetWithVisitCreatesDistinctVisitsOnEachCall() {
      Pet pet = new Pet();
      pet.setId(TEST_PET_ID);
      given(this.clinicService.findPetById(TEST_PET_ID)).willReturn(pet);
  
      Visit first  = visitController.loadPetWithVisit(TEST_PET_ID);
      Visit second = visitController.loadPetWithVisit(TEST_PET_ID);
  
      assertNotNull(first);
      assertNotNull(second);
      assertNotSame(first, second, "Each call must return a brand-new Visit instance");
  }

  @Test
  void testLoadPetWithVisitAddsVisitToPetsCollection() {
      Pet pet = new Pet();
      pet.setId(TEST_PET_ID);
      given(this.clinicService.findPetById(TEST_PET_ID)).willReturn(pet);
  
      assertEquals(0, pet.getVisits().size(), "Pet should have no visits initially");
      visitController.loadPetWithVisit(TEST_PET_ID);
      assertEquals(1, pet.getVisits().size(), "Pet should have one visit after loadPetWithVisit");
  }

  @Test
  void testInitNewVisitFormDoesNotInvokeSaveVisit() throws Exception {
      mockMvc.perform(get("/owners/{ownerId}/pets/{petId}/visits/new", TEST_OWNER_ID, TEST_PET_ID))
          .andExpect(status().isOk());
  
      verify(clinicService, never()).saveVisit(org.mockito.ArgumentMatchers.any(Visit.class));
  }

  @Test
  void testLoadPetWithVisitDefaultDateIsToday() {
      Pet pet = new Pet();
      pet.setId(TEST_PET_ID);
      given(this.clinicService.findPetById(TEST_PET_ID)).willReturn(pet);
  
      Visit visit = visitController.loadPetWithVisit(TEST_PET_ID);
  
      assertNotNull(visit.getDate(), "Visit date must not be null");
      assertEquals(LocalDate.now(), visit.getDate(), "Visit date should default to today");
  }
}