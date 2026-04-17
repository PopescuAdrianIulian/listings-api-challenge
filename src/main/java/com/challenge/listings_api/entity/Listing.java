package com.challenge.listings_api.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "listings")
@Getter @Setter
public class Listing {

    @Id
    @Column(length = 64, nullable = false)
    @NotBlank(message = "ID-ul nu poate fi gol")
    private String id;

    @NotBlank(message = "Titlul este obligatoriu")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String title;

    @NotBlank(message = "Descrierea este obligatorie")
    @Column(columnDefinition = "MEDIUMTEXT", nullable = false)
    private String description;

    @NotNull(message = "Numărul de camere este obligatoriu")
    @PositiveOrZero(message = "Numărul de camere nu poate fi negativ")
    private Integer rooms;

    @NotNull(message = "Suprafața este obligatorie")
    @Positive(message = "Suprafața trebuie să fie mai mare decât 0")
    @Column(name = "area_sqm")
    private Double areaSqm;

    @NotNull(message = "Prețul este obligatoriu")
    @PositiveOrZero(message = "Prețul nu poate fi negativ")
    private Double price;

    @NotBlank(message = "Tipul anunțului este obligatoriu")
    @Pattern(regexp = "sale|rent", message = "Tipul trebuie să fie 'sale' sau 'rent'")
    @Column(name = "listing_type", length = 16, nullable = false)
    private String listingType;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String tags;

    @NotNull(message = "Latitudinea este obligatorie")
    @DecimalMin("-90.0") @DecimalMax("90.0")
    private Double lat;

    @NotNull(message = "Longitudinea este obligatorie")
    @DecimalMin("-180.0") @DecimalMax("180.0")
    private Double lon;

    @NotNull(message = "Etajul este obligatoriu")
    private Integer floor;
}