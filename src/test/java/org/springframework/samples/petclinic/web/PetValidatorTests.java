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

import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.PetType;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Test class for {@link PetValidator}
 */
class PetValidatorTests {

    private final PetValidator petValidator = new PetValidator();

    @Test
    void shouldRejectWhenNameIsMissing() {
        Pet pet = new Pet();
        pet.setId(1);
        pet.setType(new PetType());
        pet.setBirthDate(LocalDate.now());

        Errors errors = new BeanPropertyBindingResult(pet, "pet");
        petValidator.validate(pet, errors);

        FieldError nameError = errors.getFieldError("name");
        assertEquals("required", nameError.getCode());
        assertNull(errors.getFieldError("birthDate"));
        assertNull(errors.getFieldError("type"));
    }

    @Test
    void shouldRejectWhenBirthDateIsMissing() {
        Pet pet = new Pet();
        pet.setId(1);
        pet.setName("Leo");
        pet.setType(new PetType());

        Errors errors = new BeanPropertyBindingResult(pet, "pet");
        petValidator.validate(pet, errors);

        FieldError birthDateError = errors.getFieldError("birthDate");
        assertEquals("required", birthDateError.getCode());
        assertNull(errors.getFieldError("name"));
        assertNull(errors.getFieldError("type"));
    }

    @Test
    void shouldRejectWhenTypeIsMissingForNewPet() {
        Pet pet = new Pet();
        pet.setName("Leo");
        pet.setBirthDate(LocalDate.now());

        Errors errors = new BeanPropertyBindingResult(pet, "pet");
        petValidator.validate(pet, errors);

        FieldError typeError = errors.getFieldError("type");
        assertEquals("required", typeError.getCode());
        assertNull(errors.getFieldError("name"));
        assertNull(errors.getFieldError("birthDate"));
    }

}
