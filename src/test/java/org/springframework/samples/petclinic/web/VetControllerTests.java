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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.reset;
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
      reset(clinicService);
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

  @Test
  void testShowVetListDirectlyPopulatesModel() throws Exception {
      Vet vet1 = buildVet("Alice", "Smith");
      Vet vet2 = buildVetWithSpecialty("Bob", "Jones", "surgery");
      given(clinicService.findVets()).willReturn(Arrays.asList(vet1, vet2));
  
      Map<String, Object> model = new HashMap<>();
      String view = vetController.showVetList(model);
  
      assertThat(view).isEqualTo("vets/vetList");
      assertThat(model).containsKey("vets");
      Vets vets = (Vets) model.get("vets");
      assertThat(vets.getVetList()).hasSize(2);
      verify(clinicService, times(1)).findVets();
  }

  @Test
  void testShowVetListDirectlyWithEmptyList() throws Exception {
      given(clinicService.findVets()).willReturn(Collections.emptyList());
  
      Map<String, Object> model = new HashMap<>();
      String view = vetController.showVetList(model);
  
      assertThat(view).isEqualTo("vets/vetList");
      Vets vets = (Vets) model.get("vets");
      assertThat(vets.getVetList()).isEmpty();
      verify(clinicService, times(1)).findVets();
  }

  @Test
  void testShowJsonVetListDirectlyReturnsVets() throws Exception {
      Vet vet1 = buildVet("Claire", "Wilson");
      Vet vet2 = buildVetWithSpecialty("Dave", "Brown", "radiology");
      given(clinicService.findVets()).willReturn(Arrays.asList(vet1, vet2));
  
      Vets result = vetController.showJsonVetList();
  
      assertThat(result).isNotNull();
      assertThat(result.getVetList()).hasSize(2);
      verify(clinicService, times(1)).findVets();
  }

  @Test
  void testShowJsonVetListDirectlyReturnsEmptyVets() throws Exception {
      given(clinicService.findVets()).willReturn(Collections.emptyList());
  
      Vets result = vetController.showJsonVetList();
  
      assertThat(result).isNotNull();
      assertThat(result.getVetList()).isEmpty();
      verify(clinicService, times(1)).findVets();
  }

  @Test
  void testShowXmlVetListDirectlyReturnsVets() throws Exception {
      Vet vet = buildVetWithSpecialty("Eva", "Green", "dentistry");
      given(clinicService.findVets()).willReturn(Collections.singletonList(vet));
  
      Vets result = vetController.showXmlVetList();
  
      assertThat(result).isNotNull();
      assertThat(result.getVetList()).hasSize(1);
      assertThat(result.getVetList().get(0).getLastName()).isEqualTo("Green");
      verify(clinicService, times(1)).findVets();
  }

  @Test
  void testShowXmlVetListDirectlyReturnsEmptyVets() throws Exception {
      given(clinicService.findVets()).willReturn(Collections.emptyList());
  
      Vets result = vetController.showXmlVetList();
  
      assertThat(result).isNotNull();
      assertThat(result.getVetList()).isEmpty();
      verify(clinicService, times(1)).findVets();
  }

  @Test
  void testShowVetListLargeCollection() throws Exception {
      List<Vet> vets = new ArrayList<>();
      for (int i = 0; i < 20; i++) {
          vets.add(buildVet("FirstName" + i, "LastName" + i));
      }
      given(clinicService.findVets()).willReturn(vets);
  
      mockMvc.perform(get("/vets"))
          .andExpect(status().isOk())
          .andExpect(view().name("vets/vetList"))
          .andExpect(model().attribute("vets",
              hasProperty("vetList", hasSize(20))));
  
      verify(clinicService, times(1)).findVets();
  }

  @Test
  void testShowJsonVetListLargeCollection() throws Exception {
      List<Vet> vets = new ArrayList<>();
      for (int i = 0; i < 15; i++) {
          vets.add(buildVetWithSpecialty("First" + i, "Last" + i, "specialty" + i));
      }
      given(clinicService.findVets()).willReturn(vets);
  
      Vets result = vetController.showJsonVetList();
  
      assertThat(result.getVetList()).hasSize(15);
      verify(clinicService, times(1)).findVets();
  }

  @Test
  void testShowXmlVetListMultipleSpecialties() throws Exception {
      Vet vet = buildVetWithSpecialty("Frank", "Miller", "surgery");
      Specialty radiology = new Specialty();
      radiology.setName("radiology");
      vet.addSpecialty(radiology);
      Specialty dentistry = new Specialty();
      dentistry.setName("dentistry");
      vet.addSpecialty(dentistry);
  
      given(clinicService.findVets()).willReturn(Collections.singletonList(vet));
  
      mockMvc.perform(get("/vets.xml")
              .accept(MediaType.APPLICATION_XML))
          .andExpect(status().isOk())
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML));
  
      Vets result = vetController.showXmlVetList();
      assertThat(result.getVetList()).hasSize(1);
      assertThat(result.getVetList().get(0).getNrOfSpecialties()).isEqualTo(3);
      verify(clinicService, times(2)).findVets();
  }

  @Test
  void testShowVetListServiceCalledExactlyOnce() throws Exception {
      given(clinicService.findVets()).willReturn(Collections.emptyList());
  
      mockMvc.perform(get("/vets"))
          .andExpect(status().isOk());
  
      verify(clinicService, times(1)).findVets();
  }

  @Test
  void testShowJsonVetListServiceCalledExactlyOnce() throws Exception {
      given(clinicService.findVets()).willReturn(Collections.emptyList());
  
      mockMvc.perform(get("/vets.json")
              .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk());
  
      verify(clinicService, times(1)).findVets();
  }

  @Test
  void testShowXmlVetListServiceCalledExactlyOnce() throws Exception {
      given(clinicService.findVets()).willReturn(Collections.emptyList());
  
      mockMvc.perform(get("/vets.xml")
              .accept(MediaType.APPLICATION_XML))
          .andExpect(status().isOk());
  
      verify(clinicService, times(1)).findVets();
  }

  @Test
  void testShowVetListVetsAttributeIsVetsType() throws Exception {
      given(clinicService.findVets()).willReturn(Collections.emptyList());
  
      Map<String, Object> model = new HashMap<>();
      vetController.showVetList(model);
  
      assertThat(model.get("vets")).isInstanceOf(Vets.class);
  }

  @Test
  void testGetVetsPopulatesAllVetsFromService() {
      Vet vet1 = buildVet("Grace", "Hopper");
      Vet vet2 = buildVetWithSpecialty("Alan", "Turing", "computing");
      Vet vet3 = buildVetWithSpecialty("Ada", "Lovelace", "mathematics");
      given(clinicService.findVets()).willReturn(Arrays.asList(vet1, vet2, vet3));
  
      Vets jsonResult = vetController.showJsonVetList();
      Vets xmlResult = vetController.showXmlVetList();
  
      assertThat(jsonResult.getVetList()).hasSize(3);
      assertThat(xmlResult.getVetList()).hasSize(3);
      verify(clinicService, times(2)).findVets();
  }

  @Test
  void testShowVetListVetWithNoSpecialties() throws Exception {
      Vet vet = buildVet("NoSpec", "Vet");
      given(clinicService.findVets()).willReturn(Collections.singletonList(vet));
  
      Map<String, Object> model = new HashMap<>();
      String view = vetController.showVetList(model);
  
      assertThat(view).isEqualTo("vets/vetList");
      Vets vets = (Vets) model.get("vets");
      assertThat(vets.getVetList()).hasSize(1);
      assertThat(vets.getVetList().get(0).getNrOfSpecialties()).isZero();
  }
}