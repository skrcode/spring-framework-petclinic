package org.springframework.samples.petclinic.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.model.Specialty;
import org.springframework.samples.petclinic.model.Vet;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for {@link VetController}.
 */
@SpringJUnitWebConfig(locations = {"classpath:spring/mvc-test-config.xml", "classpath:spring/mvc-core-config.xml"})
class VetControllerTests {

    @Autowired
    private VetController vetController;

    @Autowired
    private ClinicService clinicService;

    private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
      this.mockMvc = MockMvcBuilders.standaloneSetup(vetController).build();
  }

  private Vet buildVet(String firstName, String lastName) {
      Vet vet = new Vet();
      vet.setFirstName(firstName);
      vet.setLastName(lastName);
      return vet;
  }

  private Vet buildVetWithSpecialty(String firstName, String lastName, String specialtyName) {
      Vet vet = new Vet();
      vet.setFirstName(firstName);
      vet.setLastName(lastName);
      Specialty specialty = new Specialty();
      specialty.setName(specialtyName);
      vet.addSpecialty(specialty);
      return vet;
  }

  @Test
  void testShowVetListEmpty() throws Exception {
      given(clinicService.findVets()).willReturn(Collections.emptyList());
  
      mockMvc.perform(get("/vets"))
          .andExpect(status().isOk())
          .andExpect(view().name("vets/vetList"))
          .andExpect(model().attributeExists("vets"));
  
      verify(clinicService, times(1)).findVets();
  }

  @Test
  void testShowVetListWithVets() throws Exception {
      List<Vet> vets = Arrays.asList(
          buildVet("James", "Carter"),
          buildVetWithSpecialty("Helen", "Leary", "radiology")
      );
      given(clinicService.findVets()).willReturn(vets);
  
      mockMvc.perform(get("/vets"))
          .andExpect(status().isOk())
          .andExpect(view().name("vets/vetList"))
          .andExpect(model().attributeExists("vets"))
          .andExpect(model().attribute("vets",
              hasProperty("vetList", hasSize(2))));
  
      verify(clinicService, times(1)).findVets();
  }

  @Test
  void testShowJsonVetListEmpty() throws Exception {
      given(clinicService.findVets()).willReturn(Collections.emptyList());
  
      mockMvc.perform(get("/vets.json")
              .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
  
      verify(clinicService, times(1)).findVets();
  }

  @Test
  void testShowJsonVetListWithVets() throws Exception {
      List<Vet> vets = Arrays.asList(
          buildVet("James", "Carter"),
          buildVetWithSpecialty("Helen", "Leary", "radiology"),
          buildVetWithSpecialty("Linda", "Douglas", "dentistry")
      );
      given(clinicService.findVets()).willReturn(vets);
  
      mockMvc.perform(get("/vets.json")
              .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
  
      verify(clinicService, times(1)).findVets();
  }

  @Test
  void testShowXmlVetListEmpty() throws Exception {
      given(clinicService.findVets()).willReturn(Collections.emptyList());
  
      mockMvc.perform(get("/vets.xml")
              .accept(MediaType.APPLICATION_XML))
          .andExpect(status().isOk())
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML));
  
      verify(clinicService, times(1)).findVets();
  }

  @Test
  void testShowXmlVetListWithVets() throws Exception {
      List<Vet> vets = Arrays.asList(
          buildVet("James", "Carter"),
          buildVetWithSpecialty("Helen", "Leary", "radiology")
      );
      given(clinicService.findVets()).willReturn(vets);
  
      mockMvc.perform(get("/vets.xml")
              .accept(MediaType.APPLICATION_XML))
          .andExpect(status().isOk())
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML));
  
      verify(clinicService, times(1)).findVets();
  }

  @Test
  void testShowVetListModelContainsVetsAttribute() throws Exception {
      Vet vet = buildVetWithSpecialty("Rafael", "Ortega", "surgery");
      given(clinicService.findVets()).willReturn(Collections.singletonList(vet));
  
      mockMvc.perform(get("/vets"))
          .andExpect(status().isOk())
          .andExpect(model().attribute("vets",
              hasProperty("vetList", hasSize(1))))
          .andExpect(view().name("vets/vetList"));
  }

  @Test
  void testShowVetListWithMultipleSpecialties() throws Exception {
      Vet vet = buildVetWithSpecialty("Henry", "Stevens", "radiology");
      Specialty surgery = new Specialty();
      surgery.setName("surgery");
      vet.addSpecialty(surgery);
  
      given(clinicService.findVets()).willReturn(Collections.singletonList(vet));
  
      mockMvc.perform(get("/vets"))
          .andExpect(status().isOk())
          .andExpect(view().name("vets/vetList"))
          .andExpect(model().attribute("vets",
              hasProperty("vetList", hasSize(1))));
  }

  @Test
  void testShowJsonVetListSingleVet() throws Exception {
      Vet vet = buildVet("James", "Carter");
      given(clinicService.findVets()).willReturn(Collections.singletonList(vet));
  
      mockMvc.perform(get("/vets.json")
              .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
  }

  @Test
  void testShowXmlVetListSingleVet() throws Exception {
      Vet vet = buildVetWithSpecialty("Sharon", "Jenkins", "dentistry");
      given(clinicService.findVets()).willReturn(Collections.singletonList(vet));
  
      mockMvc.perform(get("/vets.xml")
              .accept(MediaType.APPLICATION_XML))
          .andExpect(status().isOk())
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML));
  }
}