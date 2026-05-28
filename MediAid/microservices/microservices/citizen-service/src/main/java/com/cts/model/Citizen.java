package com.cts.model;

import java.util.List;

import com.cts.enums.CitizenStatus;
import com.cts.validation.ValidDate;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Citizen {

    @Id
    private Long citizenId;

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Date of birth is required")
    @ValidDate
    private String dob;

    @NotBlank(message = "Gender is required")
    private String gender;

    @NotBlank(message = "Address is required")
    @Column(length = 500)
    private String address;

    @NotBlank(message = "Contact info is required")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid phone number")
    private String contactInfo;

    @NotNull(message="Status is required")
    @Enumerated(EnumType.STRING)
    private CitizenStatus status;

    @OneToMany(mappedBy = "citizen", cascade = CascadeType.ALL)
    private List<CitizenDocument> documents;

}