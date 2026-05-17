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

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.reset;
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

    private Vet james;
    private Vet helen;
  @BeforeEach
  void setup() {
      this.mockMvc = MockMvcBuilders.standaloneSetup(vetController).build();
  
      reset(clinicService);
  
      james = new Vet();
      james.setId(1);
      james.setFirstName("James");
      james.setLastName("Carter");
  
      helen = new Vet();
      helen.setId(2);
      helen.setFirstName("Helen");
      helen.setLastName("Leary");
      Specialty radiology = new Specialty();
      radiology.setId(1);
      radiology.setName("radiology");
      helen.addSpecialty(radiology);
  }

  @Test
  void testShowVetListReturnsCorrectViewName() throws Exception {
      given(this.clinicService.findVets()).willReturn(Arrays.asList(james, helen));
  
      mockMvc.perform(get("/vets"))
          .andExpect(status().isOk())
          .andExpect(view().name("vets/vetList"));
  }

  @Test
  void testShowVetListAddsVetsToModel() throws Exception {
      given(this.clinicService.findVets()).willReturn(Arrays.asList(james, helen));
  
      mockMvc.perform(get("/vets"))
          .andExpect(status().isOk())
          .andExpect(model().attributeExists("vets"))
          .andExpect(model().attribute("vets", hasProperty("vetList", hasSize(2))));
  }

  @Test
  void testShowVetListWithEmptyVets() throws Exception {
      given(this.clinicService.findVets()).willReturn(Collections.emptyList());
  
      mockMvc.perform(get("/vets"))
          .andExpect(status().isOk())
          .andExpect(view().name("vets/vetList"))
          .andExpect(model().attributeExists("vets"))
          .andExpect(model().attribute("vets", hasProperty("vetList", empty())));
  }

  @Test
  void testShowVetListWithSingleVet() throws Exception {
      given(this.clinicService.findVets()).willReturn(Collections.singletonList(james));
  
      mockMvc.perform(get("/vets"))
          .andExpect(status().isOk())
          .andExpect(view().name("vets/vetList"))
          .andExpect(model().attribute("vets", hasProperty("vetList", hasSize(1))));
  }

  @Test
  void testShowJsonVetListReturnsJson() throws Exception {
      given(this.clinicService.findVets()).willReturn(Arrays.asList(james, helen));
  
      mockMvc.perform(get("/vets.json").accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
  }

  @Test
  void testShowJsonVetListWithVets() throws Exception {
      given(this.clinicService.findVets()).willReturn(Arrays.asList(james, helen));
  
      mockMvc.perform(get("/vets.json").accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.vetList").isArray())
          .andExpect(jsonPath("$.vetList", hasSize(2)));
  }

  @Test
  void testShowJsonVetListWithEmptyVets() throws Exception {
      given(this.clinicService.findVets()).willReturn(Collections.emptyList());
  
      mockMvc.perform(get("/vets.json").accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
  }

  @Test
  void testShowXmlVetListReturnsXml() throws Exception {
      given(this.clinicService.findVets()).willReturn(Arrays.asList(james, helen));
  
      mockMvc.perform(get("/vets.xml").accept(MediaType.APPLICATION_XML))
          .andExpect(status().isOk())
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML));
  }

  @Test
  void testShowXmlVetListWithVets() throws Exception {
      given(this.clinicService.findVets()).willReturn(Arrays.asList(james, helen));
  
      mockMvc.perform(get("/vets.xml").accept(MediaType.APPLICATION_XML))
          .andExpect(status().isOk())
          .andExpect(xpath("/vets/vet").nodeCount(2));
  }

  @Test
  void testShowXmlVetListWithEmptyVets() throws Exception {
      given(this.clinicService.findVets()).willReturn(Collections.emptyList());
  
      mockMvc.perform(get("/vets.xml").accept(MediaType.APPLICATION_XML))
          .andExpect(status().isOk())
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML));
  }

  @Test
  void testShowVetListVetsHaveCorrectSpecialties() throws Exception {
      given(this.clinicService.findVets()).willReturn(Arrays.asList(james, helen));
  
      mockMvc.perform(get("/vets"))
          .andExpect(status().isOk())
          .andExpect(model().attribute("vets",
              hasProperty("vetList", hasItem(
                  hasProperty("nrOfSpecialties", is(1))
              ))));
  }

  @Test
  void testShowJsonVetListVetHasSpecialties() throws Exception {
      given(this.clinicService.findVets()).willReturn(Arrays.asList(james, helen));
  
      mockMvc.perform(get("/vets.json").accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.vetList[0].lastName").exists())
          .andExpect(jsonPath("$.vetList[1].lastName").exists());
  }
}