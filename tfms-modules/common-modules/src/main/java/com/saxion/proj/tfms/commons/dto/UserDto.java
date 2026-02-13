package com.saxion.proj.tfms.commons.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.saxion.proj.tfms.commons.model.UserDao;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDto {
    private Long id;
    private String email;
    private String username;
    private UserDao.UserType userType;
    private boolean active;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    //Converters
    public static UserDto fromEntity(UserDao user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setUserType(user.getUserType());
        dto.setActive(user.isActive());
        return dto;
    }
}
