package org.springframework.samples.petclinic.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.samples.petclinic.model.PetType;
import org.springframework.samples.petclinic.service.ClinicService;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test class for {@link PetTypeFormatter}
 *
 * @author Colin But
 */
@ExtendWith(MockitoExtension.class)
class PetTypeFormatterTests {

    @Mock
    private ClinicService clinicService;

    private PetTypeFormatter petTypeFormatter;

    @BeforeEach
    void setup() {
        petTypeFormatter = new PetTypeFormatter(clinicService);
    }

    @Test
    void testPrint() {
        PetType petType = new PetType();
        petType.setName("Hamster");
        String petTypeName = petTypeFormatter.print(petType, Locale.ENGLISH);
        assertEquals("Hamster", petTypeName);
    }

    @Test
    void shouldParseIgnoringCase() throws ParseException {
        Collection<PetType> petTypes = makePetTypes();
        PetType configuredPetType = petTypes.stream()
            .filter(petType -> petType.getName().equals("Bird"))
            .findFirst()
            .orElseThrow();
        Mockito.when(clinicService.findPetTypes()).thenReturn(petTypes);

        PetType parsedPetType = petTypeFormatter.parse("bIrD", Locale.ENGLISH);

        assertSame(configuredPetType, parsedPetType);
    }

    @Test
    void shouldRejectUnknownTypeWithSubmittedValue() {
        Mockito.when(clinicService.findPetTypes()).thenReturn(makePetTypes());
        String submittedValue = "Fish";

        ParseException exception = assertThrows(ParseException.class,
                () -> petTypeFormatter.parse(submittedValue, Locale.ENGLISH));

        assertTrue(exception.getMessage().contains(submittedValue));
    }

    /**
     * Helper method to produce some sample pet types just for test purpose
     *
     * @return {@link Collection} of {@link PetType}
     */
    private Collection<PetType> makePetTypes() {
        Collection<PetType> petTypes = new ArrayList<>();
        petTypes.add(new PetType(){
            {
                setName("Dog");
            }
        });
        petTypes.add(new PetType(){
            {
                setName("Bird");
            }
        });
        return petTypes;
    }

}
