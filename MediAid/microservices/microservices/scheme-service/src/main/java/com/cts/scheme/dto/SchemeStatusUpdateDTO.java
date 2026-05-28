package com.cts.scheme.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SchemeStatusUpdateDTO {

    @NotBlank(message = "Status is required")
    private String status;
}
