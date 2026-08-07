package cn.artoria.mind_forge.auth;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public final class AuthenticatedUser implements UserDetails, CredentialsContainer, Serializable {

  private final Long id;
  private final String email;
  private final String username;
  private String passwordHash;
  private final List<GrantedAuthority> authorities;

  public AuthenticatedUser(Long id, String email, String username, String passwordHash) {
    this.id = id;
    this.email = email;
    this.username = username;
    this.passwordHash = passwordHash;
    this.authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
  }

  public Long id() {
    return id;
  }

  public String email() {
    return email;
  }

  public String displayName() {
    return username;
  }

  @Override
  public void eraseCredentials() {
    this.passwordHash = null;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return authorities;
  }

  @Override
  public @Nullable String getPassword() {
    return passwordHash;
  }

  @Override
  public String getUsername() {
    return email;
  }
  
}  
