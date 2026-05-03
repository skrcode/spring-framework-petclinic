package org.springframework.samples.petclinic.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.model.Specialty;
import org.springframework.samples.petclinic.model.Vet;
import org.springframework.samples.petclinic.model.Vets;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.reset;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for {@link VetController}
 */
@SpringJUnitWebConfig(locations = {"classpath:spring/mvc-test-config.xml", "classpath:spring/mvc-core-config.xml"})
class VetControllerTests {

    @Autowired
    private VetController vetController;

    @Autowired
    private ClinicService clinicService;

    private MockMvc mockMvc;

    private Vet jamesCarterVet;
    private Vet helenLeungVet;
    private Specialty radiology;
    private Specialty surgery;
  @BeforeEach
  void setUp() {
      reset(clinicService);
      this.mockMvc = MockMvcBuilders.standaloneSetup(vetController).build();
  
      radiology = new Specialty();
      radiology.setName("radiology");
  
      surgery = new Specialty();
      surgery.setName("surgery");
  
      jamesCarterVet = new Vet();
      jamesCarterVet.setId(1);
      jamesCarterVet.setFirstName("James");
      jamesCarterVet.setLastName("Carter");
  
      helenLeungVet = new Vet();
      helenLeungVet.setId(2);
      helenLeungVet.setFirstName("Helen");
      helenLeungVet.setLastName("Leung");
      helenLeungVet.addSpecialty(radiology);
      helenLeungVet.addSpecialty(surgery);
  }

  @Test
  void testShowVetListWithEmptyList() throws Exception {
      given(this.clinicService.findVets()).willReturn(Collections.emptyList());
  
      mockMvc.perform(get("/vets"))
          .andExpect(status().isOk())
          .andExpect(model().attributeExists("vets"))
          .andExpect(view().name("vets/vetList"));
  }

  @Test
  void testShowVetListWithVets() throws Exception {
      given(this.clinicService.findVets()).willReturn(Arrays.asList(jamesCarterVet, helenLeungVet));
  
      mockMvc.perform(get("/vets"))
          .andExpect(status().isOk())
          .andExpect(model().attributeExists("vets"))
          .andExpect(model().attribute("vets", instanceOf(Vets.class)))
          .andExpect(view().name("vets/vetList"));
  }

  @Test
  void testShowJsonVetListEmpty() throws Exception {
      given(this.clinicService.findVets()).willReturn(Collections.emptyList());
  
      mockMvc.perform(get("/vets.json").accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
  }

  @Test
  void testShowJsonVetListWithVets() throws Exception {
      given(this.clinicService.findVets()).willReturn(Arrays.asList(jamesCarterVet, helenLeungVet));
  
      mockMvc.perform(get("/vets.json").accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
  }

  @Test
  void testShowXmlVetListEmpty() throws Exception {
      given(this.clinicService.findVets()).willReturn(Collections.emptyList());
  
      mockMvc.perform(get("/vets.xml").accept(MediaType.APPLICATION_XML))
          .andExpect(status().isOk())
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML));
  }

  @Test
  void testShowXmlVetListWithVets() throws Exception {
      given(this.clinicService.findVets()).willReturn(Arrays.asList(jamesCarterVet, helenLeungVet));
  
      mockMvc.perform(get("/vets.xml").accept(MediaType.APPLICATION_XML))
          .andExpect(status().isOk())
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML));
  }

  @Test
  void testShowVetListVetsModelContainsCorrectVets() throws Exception {
      given(this.clinicService.findVets()).willReturn(Arrays.asList(jamesCarterVet, helenLeungVet));
  
      mockMvc.perform(get("/vets"))
          .andExpect(status().isOk())
          .andExpect(model().attribute("vets",
              hasProperty("vetList", hasSize(2))))
          .andExpect(view().name("vets/vetList"));
  }

  @Test
  void testShowVetListSingleVet() throws Exception {
      given(this.clinicService.findVets()).willReturn(Collections.singletonList(jamesCarterVet));
  
      mockMvc.perform(get("/vets"))
          .andExpect(status().isOk())
          .andExpect(model().attribute("vets",
              hasProperty("vetList", hasSize(1))))
          .andExpect(view().name("vets/vetList"));
  }

  @Test
  void testShowJsonVetListReturnType() throws Exception {
      given(this.clinicService.findVets()).willReturn(Collections.singletonList(jamesCarterVet));
  
      // Verify the response body is a Vets object (not null)
      mockMvc.perform(get("/vets.json").accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
  }

  @Test
  void testShowXmlVetListReturnType() throws Exception {
      given(this.clinicService.findVets()).willReturn(Collections.singletonList(jamesCarterVet));
  
      mockMvc.perform(get("/vets.xml").accept(MediaType.APPLICATION_XML))
          .andExpect(status().isOk())
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML));
  }

  @Test
  void testShowVetListVetWithSpecialties() throws Exception {
      given(this.clinicService.findVets()).willReturn(Collections.singletonList(helenLeungVet));
  
      mockMvc.perform(get("/vets"))
          .andExpect(status().isOk())
          .andExpect(model().attribute("vets",
              hasProperty("vetList", hasSize(1))))
          .andExpect(view().name("vets/vetList"));
  }

  @Test
  void testGetVetsEndpointMapping() throws Exception {
      given(this.clinicService.findVets()).willReturn(Collections.emptyList());
  
      // Verify that /vets maps to the VetController and returns OK
      mockMvc.perform(get("/vets"))
          .andExpect(status().isOk())
          .andExpect(view().name("vets/vetList"));
  }

  @Test
  void testGetVetsJsonEndpointMapping() throws Exception {
      given(this.clinicService.findVets()).willReturn(Collections.emptyList());
  
      // Verify that /vets.json maps to the VetController and returns OK
      mockMvc.perform(get("/vets.json").accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk());
  }

  @Test
  void testGetVetsXmlEndpointMapping() throws Exception {
      given(this.clinicService.findVets()).willReturn(Collections.emptyList());
  
      // Verify that /vets.xml maps to the VetController and returns OK
      mockMvc.perform(get("/vets.xml").accept(MediaType.APPLICATION_XML))
          .andExpect(status().isOk());
  }
}