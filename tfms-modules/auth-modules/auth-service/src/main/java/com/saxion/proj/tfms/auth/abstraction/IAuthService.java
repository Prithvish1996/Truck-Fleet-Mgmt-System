package com.saxion.proj.tfms.auth.abstraction;

import com.saxion.proj.tfms.auth.dto.LoginRequestDto;
import com.saxion.proj.tfms.auth.dto.LoginResponseDto;
import com.saxion.proj.tfms.commons.dto.ApiResponse;

public interface IAuthService {
    ApiResponse<LoginResponseDto> authenticate(LoginRequestDto request);
}
