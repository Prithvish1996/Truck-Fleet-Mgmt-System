package com.saxion.proj.tfms.auth.dto;

import com.saxion.proj.tfms.commons.model.UserDao;
import lombok.Data;

@Data
public class CreateUserDto {
    private String username;
    private String email;
    private String password;
    private UserDao.UserType userType;
}
