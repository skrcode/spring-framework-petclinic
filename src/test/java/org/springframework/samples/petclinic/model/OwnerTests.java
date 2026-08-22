package org.springframework.samples.petclinic.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Arrays;
import java.util.Collections;
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

    private Owner ownerWithPets(String... petNames) {
        Owner owner = new Owner();
        for (String petName : petNames) {
            Pet pet = new Pet();
            pet.setName(petName);
            owner.addPet(pet);
        }
        return owner;
    }

    @Test
    void containsEveryPetNameReturnsTrueWhenAllNamesMatch() {
        Owner owner = ownerWithPets("Buddy", "Max");

        assertThat(owner.containsEveryPetName(Arrays.asList("Buddy", "Max"))).isTrue();
    }

    @Test
    void containsEveryPetNameReturnsFalseWhenAnyNameIsMissing() {
        Owner owner = ownerWithPets("Buddy", "Max");

        assertThat(owner.containsEveryPetName(Arrays.asList("Buddy", "Zephyr"))).isFalse();
    }

    @Test
    void containsEveryPetNameReturnsTrueForDuplicateRequestedNamesMatchingOnePet() {
        Owner owner = ownerWithPets("Buddy");

        assertThat(owner.containsEveryPetName(Arrays.asList("Buddy", "Buddy"))).isTrue();
    }

    @Test
    void containsEveryPetNameReturnsTrueForEmptyRequestedNames() {
        Owner owner = ownerWithPets("Buddy");

        assertThat(owner.containsEveryPetName(Collections.emptyList())).isTrue();
    }

    @Test
    void containsEveryPetNameReturnsTrueForEmptyRequestedNamesWithNoPets() {
        Owner owner = new Owner();

        assertThat(owner.containsEveryPetName(Collections.emptyList())).isTrue();
    }

    @Test
    void containsEveryPetNameIsCaseSensitive() {
        Owner owner = ownerWithPets("Buddy");

        assertThat(owner.containsEveryPetName(Collections.singletonList("buddy"))).isFalse();
    }

    @Test
    void containsEveryPetNameReturnsFalseForMissingNameWhenOwnerHasNoPets() {
        Owner owner = new Owner();

        assertThat(owner.containsEveryPetName(Collections.singletonList("Buddy"))).isFalse();
    }

    @Test
    void containsEveryPetNameMatchesNullRequestedNameAgainstNullPetName() {
        Owner owner = ownerWithPets((String) null);

        assertThat(owner.containsEveryPetName(Collections.singletonList(null))).isTrue();
    }

    @Test
    void containsEveryPetNameReturnsFalseForNullRequestedNameWhenNoPetHasNullName() {
        Owner owner = ownerWithPets("Buddy");

        assertThat(owner.containsEveryPetName(Collections.singletonList(null))).isFalse();
    }

    @Test
    void containsEveryPetNameThrowsNullPointerExceptionForNullRequestedNamesList() {
        Owner owner = ownerWithPets("Buddy");

        assertThatThrownBy(() -> owner.containsEveryPetName(null)).isInstanceOf(NullPointerException.class);
    }
}
