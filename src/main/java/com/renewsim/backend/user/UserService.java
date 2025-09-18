package com.renewsim.backend.user;

import java.util.List;

import com.renewsim.backend.role_service.dto.RoleDTO;
import com.renewsim.backend.user.dto.UserRequestDTO;
import com.renewsim.backend.user.dto.UserResponseDTO;


public interface UserService {

    List<UserResponseDTO> getAll();

    UserResponseDTO getById(Long id);

    User getByIdEntity(Long id); 

    UserResponseDTO save(UserRequestDTO dto);

    void saveUser(User user); 

    void deleteUser(Long id);

    List<RoleDTO> getRolesByUserId(Long id);

    UserResponseDTO getCurrentUser(User user);

    List<UserResponseDTO> getUsersWithoutRoles();
    
    User findByUsername(String username);

    void changePassword(User user, String currentPassword, String newPassword);
}

