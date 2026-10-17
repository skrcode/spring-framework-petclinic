package org.springframework.samples.petclinic.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.model.Specialty;
import org.springframework.samples.petclinic.model.Vet;
import org.springframework.samples.petclinic.model.Vets;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests for {@link VetController}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("VetController Tests")
class VetControllerTest {

    @Mock
    private ClinicService clinicService;

    @InjectMocks
    private VetController vetController;

    private MockMvc mockMvc;

    private Vet vet1;
    private Vet vet2;
  @BeforeEach
  void setUp() {
      mockMvc = MockMvcBuilders.standaloneSetup(vetController).build();
  
      vet1 = new Vet();
      vet1.setFirstName("James");
      vet1.setLastName("Carter");
  
      vet2 = new Vet();
      vet2.setFirstName("Helen");
      vet2.setLastName("Leary");
      Specialty radiology = new Specialty();
      radiology.setName("radiology");
      vet2.addSpecialty(radiology);
  }

  @Test
  @DisplayName("GET /vets returns 'vets/vetList' view")
  void testShowVetList_returnsVetListView() throws Exception {
      given(clinicService.findVets()).willReturn(Arrays.asList(vet1, vet2));
  
      mockMvc.perform(get("/vets"))
          .andExpect(status().isOk())
          .andExpect(view().name("vets/vetList"));
  }

  @Test
  @DisplayName("GET /vets populates model with 'vets' attribute")
  void testShowVetList_populatesModelWithVets() throws Exception {
      given(clinicService.findVets()).willReturn(Arrays.asList(vet1, vet2));
  
      MvcResult result = mockMvc.perform(get("/vets"))
          .andExpect(status().isOk())
          .andExpect(model().attributeExists("vets"))
          .andReturn();
  
      Vets vets = (Vets) result.getModelAndView().getModel().get("vets");
      assertThat(vets).isNotNull();
      assertThat(vets.getVetList()).hasSize(2);
  }

  @Test
  @DisplayName("GET /vets with empty vet list populates model with empty Vets")
  void testShowVetList_emptyVetList() throws Exception {
      given(clinicService.findVets()).willReturn(Collections.emptyList());
  
      MvcResult result = mockMvc.perform(get("/vets"))
          .andExpect(status().isOk())
          .andExpect(view().name("vets/vetList"))
          .andExpect(model().attributeExists("vets"))
          .andReturn();
  
      Vets vets = (Vets) result.getModelAndView().getModel().get("vets");
      assertThat(vets.getVetList()).isEmpty();
  }

  @Test
  @DisplayName("GET /vets with single vet populates model correctly")
  void testShowVetList_singleVet() throws Exception {
      given(clinicService.findVets()).willReturn(Collections.singletonList(vet1));
  
      MvcResult result = mockMvc.perform(get("/vets"))
          .andExpect(status().isOk())
          .andExpect(view().name("vets/vetList"))
          .andReturn();
  
      Vets vets = (Vets) result.getModelAndView().getModel().get("vets");
      assertThat(vets.getVetList()).hasSize(1);
      assertThat(vets.getVetList().get(0).getFirstName()).isEqualTo("James");
  }

  @Test
  @DisplayName("GET /vets invokes clinicService.findVets() exactly once")
  void testShowVetList_callsFindVets() throws Exception {
      given(clinicService.findVets()).willReturn(Arrays.asList(vet1, vet2));
  
      mockMvc.perform(get("/vets"))
          .andExpect(status().isOk());
  
      verify(clinicService, times(1)).findVets();
  }

  @Test
  @DisplayName("GET /vets.json returns JSON content type")
  void testShowJsonVetList_returnsJson() throws Exception {
      given(clinicService.findVets()).willReturn(Arrays.asList(vet1, vet2));
  
      mockMvc.perform(get("/vets.json").accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
  }

  @Test
  @DisplayName("GET /vets.json response body contains vet data")
  void testShowJsonVetList_containsVets() throws Exception {
      given(clinicService.findVets()).willReturn(Arrays.asList(vet1, vet2));
  
      mockMvc.perform(get("/vets.json").accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
          .andExpect(content().string(org.hamcrest.Matchers.containsString("James")))
          .andExpect(content().string(org.hamcrest.Matchers.containsString("Helen")));
  }

  @Test
  @DisplayName("GET /vets.json with empty list returns empty Vets")
  void testShowJsonVetList_emptyList() throws Exception {
      given(clinicService.findVets()).willReturn(Collections.emptyList());
  
      mockMvc.perform(get("/vets.json").accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
  }

  @Test
  @DisplayName("GET /vets.json invokes clinicService.findVets() exactly once")
  void testShowJsonVetList_callsFindVets() throws Exception {
      given(clinicService.findVets()).willReturn(Collections.singletonList(vet1));
  
      mockMvc.perform(get("/vets.json").accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk());
  
      verify(clinicService, times(1)).findVets();
  }

  @Test
  @DisplayName("GET /vets.xml returns XML content type")
  void testShowXmlVetList_returnsXml() throws Exception {
      given(clinicService.findVets()).willReturn(Arrays.asList(vet1, vet2));
  
      mockMvc.perform(get("/vets.xml").accept(MediaType.APPLICATION_XML))
          .andExpect(status().isOk())
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML));
  }

  @Test
  @DisplayName("GET /vets.xml with empty list returns XML response")
  void testShowXmlVetList_emptyList() throws Exception {
      given(clinicService.findVets()).willReturn(Collections.emptyList());
  
      mockMvc.perform(get("/vets.xml").accept(MediaType.APPLICATION_XML))
          .andExpect(status().isOk())
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML));
  }

  @Test
  @DisplayName("GET /vets.xml invokes clinicService.findVets() exactly once")
  void testShowXmlVetList_callsFindVets() throws Exception {
      given(clinicService.findVets()).willReturn(Collections.singletonList(vet2));
  
      mockMvc.perform(get("/vets.xml").accept(MediaType.APPLICATION_XML))
          .andExpect(status().isOk());
  
      verify(clinicService, times(1)).findVets();
  }

  @Test
  @DisplayName("GET /vets includes vet with specialty in model")
  void testShowVetList_vetWithSpecialty() throws Exception {
      given(clinicService.findVets()).willReturn(Arrays.asList(vet2));
  
      MvcResult result = mockMvc.perform(get("/vets"))
          .andExpect(status().isOk())
          .andReturn();
  
      Vets vets = (Vets) result.getModelAndView().getModel().get("vets");
      assertThat(vets.getVetList()).hasSize(1);
      assertThat(vets.getVetList().get(0).getNrOfSpecialties()).isEqualTo(1);
      assertThat(vets.getVetList().get(0).getSpecialties().get(0).getName()).isEqualTo("radiology");
  }

  @Test
  @DisplayName("showJsonVetList() returns Vets object with correct vets")
  void testShowJsonVetList_directReturn() {
      given(clinicService.findVets()).willReturn(Arrays.asList(vet1, vet2));
  
      Vets result = vetController.showJsonVetList();
  
      assertThat(result).isNotNull();
      assertThat(result.getVetList()).hasSize(2);
      assertThat(result.getVetList()).containsExactlyInAnyOrder(vet1, vet2);
  }

  @Test
  @DisplayName("showXmlVetList() returns Vets object with correct vets")
  void testShowXmlVetList_directReturn() {
      given(clinicService.findVets()).willReturn(Collections.singletonList(vet2));
  
      Vets result = vetController.showXmlVetList();
  
      assertThat(result).isNotNull();
      assertThat(result.getVetList()).hasSize(1);
      assertThat(result.getVetList().get(0).getLastName()).isEqualTo("Leary");
  }

  @Test
  @DisplayName("showVetList() returns 'vets/vetList' view name")
  void testShowVetList_directReturn() {
      given(clinicService.findVets()).willReturn(Arrays.asList(vet1, vet2));
  
      java.util.HashMap<String, Object> model = new java.util.HashMap<>();
      String viewName = vetController.showVetList(model);
  
      assertThat(viewName).isEqualTo("vets/vetList");
      assertThat(model).containsKey("vets");
      Vets vets = (Vets) model.get("vets");
      assertThat(vets.getVetList()).hasSize(2);
  }

  @Test
  @DisplayName("showVetList() model 'vets' attribute is not null")
  void testShowVetList_modelVetsNotNull() {
      given(clinicService.findVets()).willReturn(Collections.emptyList());
  
      java.util.HashMap<String, Object> model = new java.util.HashMap<>();
      vetController.showVetList(model);
  
      assertThat(model.get("vets")).isNotNull();
      assertThat(model.get("vets")).isInstanceOf(Vets.class);
  }
}