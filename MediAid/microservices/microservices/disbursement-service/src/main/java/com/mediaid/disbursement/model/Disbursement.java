package com.mediaid.disbursement.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "disbursement")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Disbursement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal amount;

    private LocalDateTime date;

    private String status;

    private Long claimId;

    private Long citizenId;

    private Long schemeId;
}
