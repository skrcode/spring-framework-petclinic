package org.springframework.samples.petclinic.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the {@link Owner} class.
 */
class OwnerTests {

    @Test
    void shouldReturnPetsSortedByName() {
        // Given
        Owner owner = new Owner();

        Pet pet1 = new Pet();
        pet1.setName("Zephyr");

        Pet pet2 = new Pet();
        pet2.setName("Alpha");

        Pet pet3 = new Pet();
        pet3.setName("Max");

        // Add pets in non-alphabetical order
        owner.addPet(pet1);
        owner.addPet(pet2);
        owner.addPet(pet3);

        // When
        List<Pet> pets = owner.getPets();

        // Then
        assertThat(pets).hasSize(3);
        assertThat(pets.get(0).getName()).isEqualTo("Alpha");
        assertThat(pets.get(1).getName()).isEqualTo("Max");
        assertThat(pets.get(2).getName()).isEqualTo("Zephyr");
    }

    @Test
    void shouldReturnEmptyListWhenNoPets() {
        // Given
        Owner owner = new Owner();

        // When
        List<Pet> pets = owner.getPets();

        // Then
        assertThat(pets).isEmpty();
    }

    @Test
    void shouldReturnUnmodifiableList() {
        // Given
        Owner owner = new Owner();
        Pet pet = new Pet();
        pet.setName("Buddy");
        owner.addPet(pet);

        // When
        List<Pet> pets = owner.getPets();

        // Then
        assertThat(pets).isUnmodifiable();
    }

    @Test
    void shouldBeCaseInsensitiveSorting() {
        // Given
        Owner owner = new Owner();

        Pet pet1 = new Pet();
        pet1.setName("buddy");

        Pet pet2 = new Pet();
        pet2.setName("Alpha");

        Pet pet3 = new Pet();
        pet3.setName("ZEPHYR");

        owner.addPet(pet1);
        owner.addPet(pet2);
        owner.addPet(pet3);

        // When
        List<Pet> pets = owner.getPets();

        // Then
        assertThat(pets).hasSize(3);
        // Case-insensitive sorting: Alpha < buddy < ZEPHYR
        assertThat(pets.get(0).getName()).isEqualTo("Alpha");
        assertThat(pets.get(1).getName()).isEqualTo("buddy");
        assertThat(pets.get(2).getName()).isEqualTo("ZEPHYR");
    }

    @Test
    void shouldContainEveryPetNameWhenAllRequestedNamesMatch() {
        // Given
        Owner owner = new Owner();

        Pet pet1 = new Pet();
        pet1.setName("Buddy");

        Pet pet2 = new Pet();
        pet2.setName("Max");

        owner.addPet(pet1);
        owner.addPet(pet2);

        // When / Then
        assertThat(owner.containsEveryPetName(List.of("Buddy", "Max"))).isTrue();
    }

    @Test
    void shouldNotContainEveryPetNameWhenOneRequestedNameIsMissing() {
        // Given
        Owner owner = new Owner();

        Pet pet1 = new Pet();
        pet1.setName("Buddy");

        owner.addPet(pet1);

        // When / Then
        assertThat(owner.containsEveryPetName(List.of("Buddy", "Max"))).isFalse();
    }

    @Test
    void shouldContainEveryPetNameWhenRequestedNamesListIsEmpty() {
        // Given
        Owner owner = new Owner();

        Pet pet1 = new Pet();
        pet1.setName("Buddy");

        owner.addPet(pet1);

        // When / Then
        assertThat(owner.containsEveryPetName(List.of())).isTrue();
    }

    @Test
    void shouldNotContainEveryPetNameWhenOwnerHasNoPets() {
        // Given
        Owner owner = new Owner();

        // When / Then
        assertThat(owner.containsEveryPetName(List.of("Buddy"))).isFalse();
    }

    @Test
    void shouldMatchPetNamesCaseSensitively() {
        // Given
        Owner owner = new Owner();

        Pet pet1 = new Pet();
        pet1.setName("Buddy");

        owner.addPet(pet1);

        // When / Then
        assertThat(owner.containsEveryPetName(List.of("buddy"))).isFalse();
    }

    @Test
    void shouldContainNullRequestedNameWhenAPetHasNullName() {
        // Given
        Owner owner = new Owner();

        Pet pet1 = new Pet();
        pet1.setName(null);

        owner.addPet(pet1);

        // When / Then
        assertThat(owner.containsEveryPetName(Arrays.asList((String) null))).isTrue();
    }
}
