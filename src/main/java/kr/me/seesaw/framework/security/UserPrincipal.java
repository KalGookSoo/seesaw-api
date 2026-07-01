package kr.me.seesaw.framework.security;

import kr.me.seesaw.api.user.dto.UserResponse;
import kr.me.seesaw.api.role.dto.RoleResponse;
import kr.me.seesaw.core.domain.user.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

public final class UserPrincipal implements UserDetails {

    private final UserResponse user;

    public UserPrincipal(UserResponse user) {
        this.user = user;
    }

    public UserResponse getUser() {
        return user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getRoles()
                .stream()
                .map(RoleResponse::getName)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return user.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return user.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return user.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        boolean accountNonLocked = isAccountNonLocked();
        boolean accountNonExpired = isAccountNonExpired();
        boolean credentialsNonExpired = isCredentialsNonExpired();
        return accountNonLocked && accountNonExpired && credentialsNonExpired;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof User that) {
            return getUsername().equals(that.getUsername());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return this.getUsername().hashCode();
    }

    @Override
    public String toString() {
        return this.getClass().getName() + " [" +
                "Username=" + getUsername() + ", " +
                "Password=[PROTECTED], " +
                "Enabled=" + isEnabled() + ", " +
                "AccountNonExpired=" + isAccountNonExpired() + ", " +
                "CredentialsNonExpired=" + isCredentialsNonExpired() + ", " +
                "AccountNonLocked=" + isAccountNonLocked() + ", " +
                "Granted Authorities=" + getAuthorities() + "]";
    }

}
