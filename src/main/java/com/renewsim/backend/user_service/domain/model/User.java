package com.renewsim.backend.user_service.domain.model;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

import com.renewsim.backend.shared.domain.vo.RoleName;

public class User {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern BCRYPT_PATTERN = Pattern.compile("^\\$2[aby]\\$.{56}$");

    private Long id;
    private String email;
    private String passwordHash;
    private String fullName;
    private String phone;
    private UserStatus status;
    private Set<RoleName> roles;
    private LocalDateTime createdAt;
    private LocalDateTime activatedAt;

    // Email verification fields
    private boolean emailVerified;
    private LocalDateTime emailVerifiedAt;

    private User(Long id, String email, String passwordHash, String fullName, String phone,
            UserStatus status, Set<RoleName> roles, LocalDateTime createdAt, LocalDateTime activatedAt,
            boolean emailVerified, LocalDateTime emailVerifiedAt) {
        this.id = id;
        this.email = requireValidEmail(email);
        this.passwordHash = requireValidPasswordHash(passwordHash);
        this.fullName = fullName;
        this.phone = phone;
        this.status = Objects.requireNonNull(status, "Status cannot be null");
        this.roles = new HashSet<>(Objects.requireNonNull(roles, "Roles cannot be null"));
        this.createdAt = Objects.requireNonNull(createdAt, "CreatedAt cannot be null");
        this.activatedAt = activatedAt;
        this.emailVerified = emailVerified;
        this.emailVerifiedAt = emailVerifiedAt;
    }

    public static User create(String email, String passwordHash, String fullName,
            String phone, Set<RoleName> roles) {
        return new User(null, email, passwordHash, fullName, phone,
                UserStatus.INACTIVE, roles, LocalDateTime.now(), null,
                false, null);
    }

    public static User reconstitute(Long id, String email, String passwordHash, String fullName,
            String phone, UserStatus status, Set<RoleName> roles,
            LocalDateTime createdAt, LocalDateTime activatedAt,
            boolean emailVerified, LocalDateTime emailVerifiedAt) {
        return new User(id, email, passwordHash, fullName, phone,
                status, roles, createdAt, activatedAt,
                emailVerified, emailVerifiedAt);
    }

    public void activate() {
        if (this.status == UserStatus.ACTIVE) {
            return;
        }
        if (this.status != UserStatus.INACTIVE) {
            throw new IllegalStateException("Only INACTIVE users can be activated");
        }
        this.status = UserStatus.ACTIVE;
        this.activatedAt = LocalDateTime.now();
    }

    public void suspend() {
        if (this.status == UserStatus.SUSPENDED) {
            return;
        }
        if (this.status != UserStatus.ACTIVE) {
            throw new IllegalStateException("Only ACTIVE users can be suspended");
        }
        this.status = UserStatus.SUSPENDED;
    }

    /**
     * Verifies the user's email address.
     * Sets emailVerified to true and records the verification timestamp.
     */
    public void verifyEmail() {
        if (this.emailVerified) {
            return; // Already verified, idempotent
        }
        this.emailVerified = true;
        this.emailVerifiedAt = LocalDateTime.now();
    }

    /**
     * Checks if the user can log in.
     * Login requires email verification.
     * 
     * @return true if email is verified
     */
    public boolean canLogin() {
        return this.emailVerified;
    }

    public boolean hasRole(RoleName roleName) {
        return roles.contains(roleName);
    }

    public void addRole(RoleName roleName) {
        Objects.requireNonNull(roleName, "RoleName cannot be null");
        this.roles.add(roleName);
    }

    public void removeRole(RoleName roleName) {
        Objects.requireNonNull(roleName, "RoleName cannot be null");
        this.roles.remove(roleName);
    }

    public void updateProfile(String fullName, String phone) {
        this.fullName = fullName;
        this.phone = phone;
    }

    public void changePassword(String newPasswordHash) {
        this.passwordHash = requireValidPasswordHash(newPasswordHash);
    }

    private String requireValidEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Email format is invalid: " + email);
        }
        return email.toLowerCase().trim();
    }

    private String requireValidPasswordHash(String passwordHash) {
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("PasswordHash cannot be null or empty");
        }
        if (!BCRYPT_PATTERN.matcher(passwordHash).matches()) {
            throw new IllegalArgumentException(
                    "PasswordHash must be a valid BCrypt hash (starts with $2a$, $2b$, or $2y$)");
        }
        return passwordHash;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPhone() {
        return phone;
    }

    public UserStatus getStatus() {
        return status;
    }

    public Set<RoleName> getRoles() {
        return Collections.unmodifiableSet(roles);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getActivatedAt() {
        return activatedAt;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public LocalDateTime getEmailVerifiedAt() {
        return emailVerifiedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof User))
            return false;
        User user = (User) o;
        return Objects.equals(id, user.id) && Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email);
    }

    @Override
    public String toString() {
        return "User{id=" + id + ", email='" + email + "', status=" + status +
                ", emailVerified=" + emailVerified + "}";
    }

    /**
     * Activates the account and marks email as verified atomically.
     * Used in local profile where email verification is bypassed.
     */
    public void activateWithEmailVerified() {
        this.emailVerified = true;
        this.emailVerifiedAt = LocalDateTime.now();
        activate();
    }
}