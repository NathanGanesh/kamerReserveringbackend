package com.example.taskworklife.config;

import com.example.taskworklife.filter.JWTAuthorizationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
    private final UserDetailsService userDetailsService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JWTAuthorizationFilter jwtAuthorizationFilter;

    @Autowired
    public SecurityConfiguration(
            @Qualifier("userDetailsService") UserDetailsService userDetailsService,
            BCryptPasswordEncoder bCryptPasswordEncoder,
            JWTAuthorizationFilter jwtAuthorizationFilter
    ) {
        this.userDetailsService = userDetailsService;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.jwtAuthorizationFilter = jwtAuthorizationFilter;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(bCryptPasswordEncoder);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/health", "/user/login", "/user/register", "/users", "/users/register", "/user/image/**").permitAll()
                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .antMatchers(HttpMethod.GET, "/kamers", "/kamers/*", "/kamers/*/reserveringen/*").hasAnyAuthority("kamer:read")
                .antMatchers(HttpMethod.POST, "/kamers").hasAnyAuthority("kameradmin:write")
                .antMatchers(HttpMethod.PUT, "/kamers/*").hasAnyAuthority("kamer:update", "kameradmin:write")
                .antMatchers(HttpMethod.DELETE, "/kamers/*").hasAnyAuthority("kamer:delete")
                .antMatchers(HttpMethod.POST, "/kamers/*/reserveer").hasAnyAuthority("kameruser:write", "reservering:write")
                .antMatchers(HttpMethod.GET, "/user/all", "/user/*", "/users", "/users/*").hasAnyAuthority("userAdmin:read")
                .antMatchers(HttpMethod.PUT, "/user/*", "/users/*").hasAnyAuthority("user:update")
                .antMatchers(HttpMethod.DELETE, "/user/*", "/users/*").hasAnyAuthority("user:delete")
                .antMatchers(HttpMethod.GET, "/reservering/**", "/reserveringen/**").hasAnyAuthority("reservering:read")
                .antMatchers(HttpMethod.POST, "/reservering", "/reserveringen").hasAnyAuthority("reservering:write")
                .antMatchers(HttpMethod.PUT, "/reservering/*", "/reserveringen/*").hasAnyAuthority("reservering:update")
                .antMatchers(HttpMethod.DELETE, "/reservering/*", "/reserveringen/*").hasAnyAuthority("reservering:delete")
                .antMatchers(HttpMethod.GET, "/images/**").hasAnyAuthority("images:read")
                .anyRequest().authenticated()
                .and()
                .exceptionHandling()
                .authenticationEntryPoint((request, response, exception) -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED, exception.getMessage()))
                .accessDeniedHandler((request, response, exception) -> response.sendError(HttpServletResponse.SC_FORBIDDEN, exception.getMessage()));

        http.cors().and().csrf().disable();
        http.headers().frameOptions().disable();
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http.addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class);
    }

    @Override
    public void configure(WebSecurity web) {
        web.ignoring().antMatchers("/h2/**");
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
}
