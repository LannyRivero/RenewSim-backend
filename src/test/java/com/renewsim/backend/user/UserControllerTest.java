package com.renewsim.backend.user;

import com.renewsim.backend.role.Role;
import com.renewsim.backend.role.dto.RoleDTO;
import com.renewsim.backend.role.dto.UpdateRolesRequestDTO;
import com.renewsim.backend.security.UserDetailsImpl;
import com.renewsim.backend.user.dto.ChangePasswordDTO;
import com.renewsim.backend.user.dto.UserResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserControllerTest {

    private UserService userService;
    private UserUseCase userUseCase;
    private UserController userController;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        userUseCase = mock(UserUseCase.class);
        userController = new UserController(userUseCase, userService);
    }

    @Test
    @DisplayName("Should get all users")
    void testShouldGetAllUsers() {
        List<UserResponseDTO> users = List.of(new UserResponseDTO());
        when(userService.getAll()).thenReturn(users);

        var response = userController.getAllUsers();

        assertThat(response.getBody()).isEqualTo(users);
        verify(userService).getAll();
    }

    @Test
    @DisplayName("Should get user by ID")
    void testShouldGetUserById() {
        UserResponseDTO user = new UserResponseDTO();
        when(userService.getById(1L)).thenReturn(user);

        var response = userController.getUserById(1L);

        assertThat(response.getBody()).isEqualTo(user);
        verify(userService).getById(1L);
    }

    @Test
    @DisplayName("Should get user roles")
    void testShouldGetUserRoles() {

        List<RoleDTO> roles = List.of(new RoleDTO(1L, "ADMIN"));
        when(userService.getRolesByUserId(1L)).thenReturn(roles);

        var response = userController.getUserRoles(1L);

        assertThat(response.getBody()).isEqualTo(roles);
        verify(userService).getRolesByUserId(1L);
    }

    @Test
    @DisplayName("Should get current user roles")
    void testShouldGetCurrentUserRoles() {

        User user = new User();
        Role role = new Role();
        role.setId(1L);
        role.setName(com.renewsim.backend.role.RoleName.ADMIN);
        user.setRoles(Set.of(role));

        UserDetailsImpl userDetails = mock(UserDetailsImpl.class);
        when(userDetails.getUser()).thenReturn(user);

        var response = userController.getCurrentUserRoles(userDetails);

        assertThat(response.getBody())
                .hasSize(1)
                .extracting(RoleDTO::getName)
                .containsExactly("ADMIN");
    }

    @Test
    @DisplayName("Should delete user")
    void testShouldDeleteUser() {
        var response = userController.deleteUser(1L);
        assertThat(response.getBody()).isEqualTo("Usuario eliminado correctamente");
        verify(userUseCase).deleteUser(1L);
    }

    @Test
    @DisplayName("Should get users without roles")
    void testShouldGetUsersWithoutRoles() {
        List<UserResponseDTO> users = List.of(new UserResponseDTO());
        when(userUseCase.getUsersWithoutRoles()).thenReturn(users);

        var response = userController.getUsersWithoutRoles();

        assertThat(response.getBody()).isEqualTo(users);
        verify(userUseCase).getUsersWithoutRoles();
    }

    @Test
    @DisplayName("Should get current user")
    void testShouldGetCurrentUser() {
        User user = new User();

        UserResponseDTO responseDTO = new UserResponseDTO();

        UserDetailsImpl userDetails = mock(UserDetailsImpl.class);
        when(userDetails.getUser()).thenReturn(user);
        when(userService.getCurrentUser(user)).thenReturn(responseDTO);

        var response = userController.getCurrentUser(userDetails);

        assertThat(response.getBody()).isEqualTo(responseDTO);
        verify(userService).getCurrentUser(user);
    }

    @Test
    @DisplayName("Should update user roles")
    void testShouldUpdateUserRoles() {
        UpdateRolesRequestDTO request = new UpdateRolesRequestDTO();
        request.setRoles(List.of(1L, 2L).stream().map(String::valueOf).toList());

        var response = userController.updateUserRoles(1L, request);

        assertThat(response.getBody()).isEqualTo("Roles actualizados correctamente");
        verify(userUseCase).updateUserRoles(1L, request.getRoles());
    }

    @Test
    @DisplayName("Should change password successfully")
    void testShouldChangePasswordSuccessfully() {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn("testuser");

        ChangePasswordDTO request = new ChangePasswordDTO();
        request.setCurrentPassword("oldPass");
        request.setNewPassword("newPass");

        User user = new User();
        when(userService.findByUsername("testuser")).thenReturn(user);

        var response = userController.changePassword(jwt, request);

        assertThat(response.getBody()).isEqualTo("Contraseña cambiada correctamente");
        verify(userService).findByUsername("testuser");
        verify(userUseCase).changePassword(user, "oldPass", "newPass");
    }

    @Test
    @DisplayName("Should return BadRequest when IllegalArgumentException occurs during password change")
    void testShouldReturnBadRequestWhenIllegalArgumentExceptionOccurs() {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn("testuser");

        ChangePasswordDTO request = new ChangePasswordDTO();
        request.setCurrentPassword("wrongOldPass");
        request.setNewPassword("newPass");

        User user = new User();
        when(userService.findByUsername("testuser")).thenReturn(user);
        doThrow(new IllegalArgumentException("Current password incorrect")).when(userUseCase)
                .changePassword(any(User.class), anyString(), anyString());

        var response = userController.changePassword(jwt, request);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isEqualTo("Current password incorrect");

        verify(userService).findByUsername("testuser");
        verify(userUseCase).changePassword(user, "wrongOldPass", "newPass");
    }

    @Test
    @DisplayName("Should return InternalServerError when Exception occurs during password change")
    void testShouldReturnInternalServerErrorWhenExceptionOccurs() {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn("testuser");

        ChangePasswordDTO request = new ChangePasswordDTO();
        request.setCurrentPassword("oldPass");
        request.setNewPassword("newPass");

        when(userService.findByUsername("testuser")).thenThrow(new RuntimeException("Unexpected error"));

        var response = userController.changePassword(jwt, request);

        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(response.getBody()).isEqualTo("Error al cambiar la contraseña");

        verify(userService).findByUsername("testuser");
    }

}
