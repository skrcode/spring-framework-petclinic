package org.springframework.samples.petclinic.web;

import org.assertj.core.util.Lists;
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

import java.util.Collections;

import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the {@link VetController}.
 */
@SpringJUnitWebConfig(locations = {"classpath:spring/mvc-core-config.xml", "classpath:spring/mvc-test-config.xml"})
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

  @Test
  void testShowVetListReturnsVetListView() throws Exception {
      given(this.clinicService.findVets()).willReturn(Collections.emptyList());
  
      mockMvc.perform(get("/vets"))
          .andExpect(status().isOk())
          .andExpect(view().name("vets/vetList"))
          .andExpect(model().attributeExists("vets"));
  }

  @Test
  void testShowVetListWithMultipleVets() throws Exception {
      Vet vet1 = new Vet();
      vet1.setId(1);
      vet1.setFirstName("James");
      vet1.setLastName("Carter");
  
      Vet vet2 = new Vet();
      vet2.setId(2);
      vet2.setFirstName("Helen");
      vet2.setLastName("Leary");
      Specialty radiology = new Specialty();
      radiology.setName("radiology");
      vet2.addSpecialty(radiology);
  
      given(this.clinicService.findVets()).willReturn(Lists.newArrayList(vet1, vet2));
  
      mockMvc.perform(get("/vets"))
          .andExpect(status().isOk())
          .andExpect(view().name("vets/vetList"))
          .andExpect(model().attributeExists("vets"));
  }

  @Test
  void testShowVetListWithEmptyVets() throws Exception {
      given(this.clinicService.findVets()).willReturn(Collections.emptyList());
  
      mockMvc.perform(get("/vets"))
          .andExpect(status().isOk())
          .andExpect(view().name("vets/vetList"))
          .andExpect(model().attributeExists("vets"));
  }

  @Test
  void testShowJsonVetListReturnsJson() throws Exception {
      Vet vet = new Vet();
      vet.setId(1);
      vet.setFirstName("James");
      vet.setLastName("Carter");
  
      given(this.clinicService.findVets()).willReturn(Lists.newArrayList(vet));
  
      mockMvc.perform(get("/vets.json").accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
  }

  @Test
  void testShowJsonVetListWithEmptyList() throws Exception {
      given(this.clinicService.findVets()).willReturn(Collections.emptyList());
  
      mockMvc.perform(get("/vets.json").accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
  }

  @Test
  void testShowJsonVetListWithSpecialties() throws Exception {
      Vet vet = new Vet();
      vet.setId(3);
      vet.setFirstName("Linda");
      vet.setLastName("Douglas");
      Specialty dentistry = new Specialty();
      dentistry.setName("dentistry");
      Specialty surgery = new Specialty();
      surgery.setName("surgery");
      vet.addSpecialty(dentistry);
      vet.addSpecialty(surgery);
  
      given(this.clinicService.findVets()).willReturn(Lists.newArrayList(vet));
  
      mockMvc.perform(get("/vets.json").accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
  }

  @Test
  void testShowXmlVetListReturnsXml() throws Exception {
      Vet vet = new Vet();
      vet.setId(1);
      vet.setFirstName("James");
      vet.setLastName("Carter");
  
      given(this.clinicService.findVets()).willReturn(Lists.newArrayList(vet));
  
      mockMvc.perform(get("/vets.xml").accept(MediaType.APPLICATION_XML))
          .andExpect(status().isOk())
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML));
  }

  @Test
  void testShowXmlVetListWithEmptyList() throws Exception {
      given(this.clinicService.findVets()).willReturn(Collections.emptyList());
  
      mockMvc.perform(get("/vets.xml").accept(MediaType.APPLICATION_XML))
          .andExpect(status().isOk())
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML));
  }

  @Test
  void testShowXmlVetListWithMultipleVets() throws Exception {
      Vet vet1 = new Vet();
      vet1.setId(1);
      vet1.setFirstName("James");
      vet1.setLastName("Carter");
  
      Vet vet2 = new Vet();
      vet2.setId(2);
      vet2.setFirstName("Helen");
      vet2.setLastName("Leary");
      Specialty radiology = new Specialty();
      radiology.setName("radiology");
      vet2.addSpecialty(radiology);
  
      given(this.clinicService.findVets()).willReturn(Lists.newArrayList(vet1, vet2));
  
      mockMvc.perform(get("/vets.xml").accept(MediaType.APPLICATION_XML))
          .andExpect(status().isOk())
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML));
  }
}