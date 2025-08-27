package com.renewsim.backend.auth_service.infrastructure.policy;

import com.renewsim.backend.auth_service.application.port.out.ScopePolicy;
import com.renewsim.backend.role.RoleName;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@ConfigurationProperties(prefix = "auth")
public class YamlScopePolicy implements ScopePolicy {

    private static final Pattern SCOPE_PATTERN = Pattern.compile("^[a-z]+:[a-z]+$");

    private Map<RoleName, Set<String>> roleScopes = Map.of();

    public Map<RoleName, Set<String>> getRoleScopes() {
        return roleScopes;
    }

    public void setRoleScopes(Map<RoleName, Set<String>> source) {
        if (source == null || source.isEmpty()) {
            this.roleScopes = Map.of();
            return;
        }
        Map<RoleName, Set<String>> tmp = new EnumMap<>(RoleName.class);
        source.forEach((role, scopes) -> {
            Set<String> normalized = (scopes == null ? Set.<String>of() : scopes).stream()
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .peek(this::validateScopeFormat)
                    .collect(Collectors.toCollection(LinkedHashSet::new)); 
            tmp.put(role, Set.copyOf(normalized)); 
        });
        this.roleScopes = Collections.unmodifiableMap(tmp);
    }

    private void validateScopeFormat(String scope) {
        if (!SCOPE_PATTERN.matcher(scope).matches()) {
            throw new IllegalArgumentException(
                "Invalid scope format: " + scope + " (expected 'domain:action', lowercase letters only)"
            );
        }
    }

    @Override
    public Set<String> scopesFor(RoleName roleName) {
        return roleScopes.getOrDefault(roleName, Set.of());
    }
}
