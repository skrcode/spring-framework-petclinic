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
    void containsEveryPetName_shouldReturnTrue_whenAllRequestedNamesMatchPets() {
        // Given
        Owner owner = new Owner();
        Pet pet1 = new Pet();
        pet1.setName("Alpha");
        Pet pet2 = new Pet();
        pet2.setName("Max");
        owner.addPet(pet1);
        owner.addPet(pet2);

        // Then
        assertThat(owner.containsEveryPetName(List.of("Alpha", "Max"))).isTrue();
    }

    @Test
    void containsEveryPetName_shouldReturnFalse_whenAnyRequestedNameIsMissing() {
        // Given
        Owner owner = new Owner();
        Pet pet1 = new Pet();
        pet1.setName("Alpha");
        owner.addPet(pet1);

        // Then
        assertThat(owner.containsEveryPetName(List.of("Alpha", "Ghost"))).isFalse();
    }

    @Test
    void containsEveryPetName_shouldBeCaseSensitive() {
        // Given
        Owner owner = new Owner();
        Pet pet = new Pet();
        pet.setName("Alpha");
        owner.addPet(pet);

        // Then
        assertThat(owner.containsEveryPetName(List.of("alpha"))).isFalse();
    }

    @Test
    void containsEveryPetName_shouldAllowDuplicateRequestsForSamePet() {
        // Given
        Owner owner = new Owner();
        Pet pet = new Pet();
        pet.setName("Alpha");
        owner.addPet(pet);

        // Then
        assertThat(owner.containsEveryPetName(List.of("Alpha", "Alpha"))).isTrue();
    }

    @Test
    void containsEveryPetName_shouldReturnTrue_whenRequestedNamesIsEmpty() {
        // Given
        Owner owner = new Owner();
        Pet pet = new Pet();
        pet.setName("Alpha");
        owner.addPet(pet);

        // Then
        assertThat(owner.containsEveryPetName(List.of())).isTrue();
    }

    @Test
    void containsEveryPetName_shouldReturnFalse_whenOwnerHasNoPetsAndNamesAreRequested() {
        // Given
        Owner owner = new Owner();

        // Then
        assertThat(owner.containsEveryPetName(List.of("Alpha"))).isFalse();
    }

    @Test
    void containsEveryPetName_shouldMatchNullRequestedNameAgainstPetWithNullName() {
        // Given
        Owner owner = new Owner();
        Pet pet = new Pet();
        // pet.getName() defaults to null
        owner.addPet(pet);

        // Then
        assertThat(owner.containsEveryPetName(Arrays.asList((String) null))).isTrue();
    }

    @Test
    void containsEveryPetName_shouldReturnFalse_whenNullRequestedNameHasNoMatchingPet() {
        // Given
        Owner owner = new Owner();
        Pet pet = new Pet();
        pet.setName("Alpha");
        owner.addPet(pet);

        // Then
        assertThat(owner.containsEveryPetName(Arrays.asList((String) null))).isFalse();
    }
}
