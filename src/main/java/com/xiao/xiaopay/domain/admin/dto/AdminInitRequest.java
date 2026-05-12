package com.xiao.xiaopay.domain.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminInitRequest(
        @NotBlank String username,
        @NotBlank @Size(min = 8, max = 64) String password,
        @NotBlank String nickname
) {
}
