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

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Boundary tests for {@link PetValidator}.
 *
 * @author skrcode
 */
class PetValidatorTests {

    private final PetValidator petValidator = new PetValidator();

    @Test
    void shouldRejectMissingName() {
        Pet pet = new Pet();
        pet.setType(new PetType());
        pet.setBirthDate(LocalDate.now());

        Errors errors = validate(pet);

        assertThat(errors.hasFieldErrors("name")).isTrue();
        assertThat(errors.getFieldError("name").getCode()).isEqualTo("required");
        assertThat(errors.hasFieldErrors("type")).isFalse();
        assertThat(errors.hasFieldErrors("birthDate")).isFalse();
    }

    @Test
    void shouldRejectInvalidBirthDate() {
        Pet pet = new Pet();
        pet.setName("Basil");
        pet.setType(new PetType());
        pet.setBirthDate(null);

        Errors errors = validate(pet);

        assertThat(errors.hasFieldErrors("birthDate")).isTrue();
        assertThat(errors.getFieldError("birthDate").getCode()).isEqualTo("required");
        assertThat(errors.hasFieldErrors("name")).isFalse();
        assertThat(errors.hasFieldErrors("type")).isFalse();
    }

    @Test
    void shouldRejectMissingType() {
        Pet pet = new Pet();
        pet.setName("Basil");
        pet.setBirthDate(LocalDate.now());
        pet.setType(null);

        Errors errors = validate(pet);

        assertThat(errors.hasFieldErrors("type")).isTrue();
        assertThat(errors.getFieldError("type").getCode()).isEqualTo("required");
        assertThat(errors.hasFieldErrors("name")).isFalse();
        assertThat(errors.hasFieldErrors("birthDate")).isFalse();
    }

    private Errors validate(Pet pet) {
        Errors errors = new BeanPropertyBindingResult(pet, "pet");
        petValidator.validate(pet, errors);
        return errors;
    }

}
