package cn.artoria.mind_forge.auth;

import java.util.Locale;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import cn.artoria.mind_forge.user.User;
import cn.artoria.mind_forge.user.UserRepository;

public class DatabaseUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  public DatabaseUserDetailsService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }
  
  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    String normalizedEmail = normalizeEmail(email);

    User user = userRepository.findByEmail(normalizedEmail)
        .orElseThrow(() -> new UsernameNotFoundException("Invalid email or password"));

    return new AuthenticatedUser(
      user.getId(),
      user.getEmail(),
      user.getUsername(),
      user.getPasswordHash()
    );
  }
  
  private String normalizeEmail(String email) {
    return email.trim().toLowerCase(Locale.ROOT);
  }
}
