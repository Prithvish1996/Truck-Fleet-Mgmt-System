package com.saxion.proj.tfms.auth.dto;

import com.saxion.proj.tfms.commons.model.UserDao;

import java.time.ZonedDateTime;

public class UserProfileDto {
    private String email;
    private String username;
    private UserDao.UserType userType;
    private boolean active;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
}
