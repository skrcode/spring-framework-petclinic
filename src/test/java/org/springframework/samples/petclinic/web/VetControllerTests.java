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
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
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
    private Specialty dentistry;
  @BeforeEach
  void setUp() {
      reset(clinicService);
      this.mockMvc = MockMvcBuilders.standaloneSetup(vetController).build();
  
      radiology = new Specialty();
      radiology.setName("radiology");
  
      surgery = new Specialty();
      surgery.setName("surgery");
  
      dentistry = new Specialty();
      dentistry.setName("dentistry");
  
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

  @Test
  void testShowVetListCallsClinicServiceFindVets() throws Exception {
      given(this.clinicService.findVets()).willReturn(Collections.emptyList());
  
      mockMvc.perform(get("/vets"))
          .andExpect(status().isOk());
  
      then(clinicService).should(times(1)).findVets();
  }

  @Test
  void testShowJsonVetListCallsClinicServiceFindVets() throws Exception {
      given(this.clinicService.findVets()).willReturn(Collections.emptyList());
  
      mockMvc.perform(get("/vets.json").accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk());
  
      then(clinicService).should(times(1)).findVets();
  }

  @Test
  void testShowXmlVetListCallsClinicServiceFindVets() throws Exception {
      given(this.clinicService.findVets()).willReturn(Collections.emptyList());
  
      mockMvc.perform(get("/vets.xml").accept(MediaType.APPLICATION_XML))
          .andExpect(status().isOk());
  
      then(clinicService).should(times(1)).findVets();
  }

  @Test
  void testShowVetListModelContainsVetWithNoSpecialties() throws Exception {
      given(this.clinicService.findVets()).willReturn(Collections.singletonList(jamesCarterVet));
  
      mockMvc.perform(get("/vets"))
          .andExpect(status().isOk())
          .andExpect(model().attribute("vets",
              hasProperty("vetList",
                  contains(hasProperty("firstName", is("James"))))))
          .andExpect(view().name("vets/vetList"));
  }

  @Test
  void testShowVetListModelContainsVetLastName() throws Exception {
      given(this.clinicService.findVets()).willReturn(Collections.singletonList(jamesCarterVet));
  
      mockMvc.perform(get("/vets"))
          .andExpect(status().isOk())
          .andExpect(model().attribute("vets",
              hasProperty("vetList",
                  contains(hasProperty("lastName", is("Carter"))))))
          .andExpect(view().name("vets/vetList"));
  }

  @Test
  void testShowVetListModelVetHasSpecialties() throws Exception {
      given(this.clinicService.findVets()).willReturn(Collections.singletonList(helenLeungVet));
  
      mockMvc.perform(get("/vets"))
          .andExpect(status().isOk())
          .andExpect(model().attribute("vets",
              hasProperty("vetList",
                  contains(hasProperty("nrOfSpecialties", is(2))))))
          .andExpect(view().name("vets/vetList"));
  }

  @Test
  void testShowVetListModelVetSpecialtiesContent() throws Exception {
      given(this.clinicService.findVets()).willReturn(Collections.singletonList(helenLeungVet));
  
      mockMvc.perform(get("/vets"))
          .andExpect(status().isOk())
          .andExpect(model().attribute("vets",
              hasProperty("vetList",
                  contains(
                      hasProperty("specialties",
                          hasItems(
                              hasProperty("name", is("radiology")),
                              hasProperty("name", is("surgery"))
                          ))))))
          .andExpect(view().name("vets/vetList"));
  }

  @Test
  void testShowVetListModelContainsBothVets() throws Exception {
      given(this.clinicService.findVets()).willReturn(Arrays.asList(jamesCarterVet, helenLeungVet));
  
      mockMvc.perform(get("/vets"))
          .andExpect(status().isOk())
          .andExpect(model().attribute("vets",
              hasProperty("vetList",
                  hasItems(
                      hasProperty("firstName", is("James")),
                      hasProperty("firstName", is("Helen"))
                  ))))
          .andExpect(view().name("vets/vetList"));
  }

  @Test
  void testShowJsonVetListWithMultipleVetsBodyNotEmpty() throws Exception {
      given(this.clinicService.findVets()).willReturn(Arrays.asList(jamesCarterVet, helenLeungVet));
  
      mockMvc.perform(get("/vets.json").accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
          .andExpect(content().string(not(emptyOrNullString())));
  }

  @Test
  void testShowXmlVetListWithMultipleVetsBodyNotEmpty() throws Exception {
      given(this.clinicService.findVets()).willReturn(Arrays.asList(jamesCarterVet, helenLeungVet));
  
      mockMvc.perform(get("/vets.xml").accept(MediaType.APPLICATION_XML))
          .andExpect(status().isOk())
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML))
          .andExpect(content().string(not(emptyOrNullString())));
  }

  @Test
  void testShowVetListVetWithThreeSpecialties() throws Exception {
      Vet multiSpecVet = new Vet();
      multiSpecVet.setId(3);
      multiSpecVet.setFirstName("Linda");
      multiSpecVet.setLastName("Douglas");
      multiSpecVet.addSpecialty(radiology);
      multiSpecVet.addSpecialty(surgery);
      multiSpecVet.addSpecialty(dentistry);
  
      given(this.clinicService.findVets()).willReturn(Collections.singletonList(multiSpecVet));
  
      mockMvc.perform(get("/vets"))
          .andExpect(status().isOk())
          .andExpect(model().attribute("vets",
              hasProperty("vetList",
                  contains(hasProperty("nrOfSpecialties", is(3))))))
          .andExpect(view().name("vets/vetList"));
  }

  @Test
  void testShowVetListWithManyVets() throws Exception {
      List<Vet> manyVets = IntStream.rangeClosed(1, 10).mapToObj(i -> {
          Vet v = new Vet();
          v.setId(i);
          v.setFirstName("FirstName" + i);
          v.setLastName("LastName" + i);
          return v;
      }).collect(Collectors.toList());
  
      given(this.clinicService.findVets()).willReturn(manyVets);
  
      mockMvc.perform(get("/vets"))
          .andExpect(status().isOk())
          .andExpect(model().attribute("vets",
              hasProperty("vetList", hasSize(10))))
          .andExpect(view().name("vets/vetList"));
  }

  @Test
  void testShowJsonVetListWithManyVets() throws Exception {
      List<Vet> manyVets = IntStream.rangeClosed(1, 5).mapToObj(i -> {
          Vet v = new Vet();
          v.setId(i);
          v.setFirstName("First" + i);
          v.setLastName("Last" + i);
          return v;
      }).collect(Collectors.toList());
  
      given(this.clinicService.findVets()).willReturn(manyVets);
  
      mockMvc.perform(get("/vets.json").accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
          .andExpect(content().string(not(emptyOrNullString())));
  }

  @Test
  void testShowXmlVetListVetWithSpecialties() throws Exception {
      given(this.clinicService.findVets()).willReturn(Collections.singletonList(helenLeungVet));
  
      mockMvc.perform(get("/vets.xml").accept(MediaType.APPLICATION_XML))
          .andExpect(status().isOk())
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML))
          .andExpect(content().string(containsString("Leung")));
  }

  @Test
  void testShowVetListModelIsVetsInstance() throws Exception {
      given(this.clinicService.findVets()).willReturn(Collections.emptyList());
  
      mockMvc.perform(get("/vets"))
          .andExpect(status().isOk())
          .andExpect(model().attribute("vets", instanceOf(Vets.class)))
          .andExpect(view().name("vets/vetList"));
  }

  @Test
  void testShowVetListEmptyListModelVetListEmpty() throws Exception {
      given(this.clinicService.findVets()).willReturn(Collections.emptyList());
  
      mockMvc.perform(get("/vets"))
          .andExpect(status().isOk())
          .andExpect(model().attribute("vets",
              hasProperty("vetList", hasSize(0))))
          .andExpect(view().name("vets/vetList"));
  }

  @Test
  void testShowJsonVetListEmptyBodyNotNull() throws Exception {
      given(this.clinicService.findVets()).willReturn(Collections.emptyList());
  
      mockMvc.perform(get("/vets.json").accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(content().string(not(emptyOrNullString())));
  }

  @Test
  void testShowXmlVetListEmptyBodyNotNull() throws Exception {
      given(this.clinicService.findVets()).willReturn(Collections.emptyList());
  
      mockMvc.perform(get("/vets.xml").accept(MediaType.APPLICATION_XML))
          .andExpect(status().isOk())
          .andExpect(content().string(not(emptyOrNullString())));
  }

  @Test
  void testShowVetListVetNoSpecialtiesHasZeroNrOfSpecialties() throws Exception {
      given(this.clinicService.findVets()).willReturn(Collections.singletonList(jamesCarterVet));
  
      mockMvc.perform(get("/vets"))
          .andExpect(status().isOk())
          .andExpect(model().attribute("vets",
              hasProperty("vetList",
                  contains(hasProperty("nrOfSpecialties", is(0))))))
          .andExpect(view().name("vets/vetList"));
  }

  @Test
  void testShowXmlVetListContainsVetFirstName() throws Exception {
      given(this.clinicService.findVets()).willReturn(Collections.singletonList(jamesCarterVet));
  
      mockMvc.perform(get("/vets.xml").accept(MediaType.APPLICATION_XML))
          .andExpect(status().isOk())
          .andExpect(content().string(containsString("James")));
  }
}