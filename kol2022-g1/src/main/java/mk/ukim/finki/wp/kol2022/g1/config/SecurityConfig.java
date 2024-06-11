package mk.ukim.finki.wp.kol2022.g1.config;

import mk.ukim.finki.wp.kol2022.g1.model.Employee;
import mk.ukim.finki.wp.kol2022.g1.repository.EmployeeRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.stereotype.Service;

import java.util.Collections;


/**
 * This class is used to configure user login on path '/login' and logout on path '/logout'.
 * The only public page in the application should be '/'.
 * All other pages should be visible only for a user with role 'ROLE_ADMIN'.
 * Furthermore, in the "list.html" template, the 'Edit', 'Delete', 'Add' buttons should only be
 * visible for a user with role 'ROLE_ADMIN'.
 * <p>
 * For login the employees from the database should be used, where username should be the email.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig{

    private final PasswordEncoder passwordEncoder;
    private final Auth auth;

    public SecurityConfig(PasswordEncoder passwordEncoder, Auth auth) {
        this.passwordEncoder = passwordEncoder;
        this.auth = auth;
    }


    //    @Bean
    // TODO: If you are implementing the security requirements, remove this following bean creation
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().anyRequest();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception  {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests( (requests) -> requests
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/"))
                        .permitAll()
                        .anyRequest()
                        .authenticated()
                )
                .formLogin((form) -> form
                        .permitAll()
                        .failureUrl("/login?error=BadCredentials")
                        .defaultSuccessUrl("/employees", true)
                )
                .logout((logout) -> logout
                        .logoutUrl("/logout")
                        .clearAuthentication(true)
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .logoutSuccessUrl("/")
                );

        return http.build();
    }

//    @Bean
//    public UserDetailsService userDetailsService() {
//        UserDetails user1 = User.builder()
//                .username("employee.1@wp.finki.ukim.mk")
//                .password(passwordEncoder.encode("emp1"))
//                .roles("ADMIN")
//                .build();
//        UserDetails user2 = User.builder()
//                .username("employee.2@wp.finki.ukim.mk")
//                .password(passwordEncoder.encode("emp2"))
//                .roles("USER")
//                .build();
//        return new InMemoryUserDetailsManager(user1, user2);
//    }

    @Bean
    public AuthenticationManager authManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.userDetailsService(auth);
        return authenticationManagerBuilder.build();
    }
}

@Service
class Auth implements UserDetailsService{

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    Auth(EmployeeRepository employeeRepository, PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Employee employee = employeeRepository.findByEmail(username);
        return new User(
                employee.getEmail(),
                passwordEncoder.encode(employee.getPassword()),
                Collections.singleton(employee.getType())
        );
    }
}