package com.example.taskworklife.config;

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

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
    private final UserDetailsService userDetailsService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    public SecurityConfiguration(@Qualifier("userDetailsService") UserDetailsService userDetailsService, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userDetailsService = userDetailsService;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(bCryptPasswordEncoder);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/user/login", "/user/register", "/user/image/**").permitAll()
                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .antMatchers(HttpMethod.GET, "/kamer/all", "/kamer/*", "/kamer/*/reserveringen/*").hasAnyAuthority("kamer:read")
                .antMatchers(HttpMethod.POST, "/kamer/new").hasAnyAuthority("kameradmin:write")
                .antMatchers(HttpMethod.PUT, "/kamer/edit/**").hasAnyAuthority("kamer:update", "kameradmin:write")
                .antMatchers(HttpMethod.DELETE, "/kamer/delete/**").hasAnyAuthority("kamer:delete")
                .antMatchers(HttpMethod.POST, "/kamer/*/reserveer").hasAnyAuthority("kameruser:write", "reservering:write")
                .antMatchers(HttpMethod.GET, "/user/all", "/user/*").hasAnyAuthority("userAdmin:read")
                .antMatchers(HttpMethod.PUT, "/user/*").hasAnyAuthority("user:update")
                .antMatchers(HttpMethod.DELETE, "/user/*").hasAnyAuthority("user:delete")
                .antMatchers(HttpMethod.GET, "/reservering/**").hasAnyAuthority("reservering:read")
                .antMatchers(HttpMethod.POST, "/reservering").hasAnyAuthority("reservering:write")
                .antMatchers(HttpMethod.PUT, "/reservering/*").hasAnyAuthority("reservering:update")
                .antMatchers(HttpMethod.DELETE, "/reservering/*").hasAnyAuthority("reservering:delete")
                .antMatchers(HttpMethod.GET, "/images/**").hasAnyAuthority("images:read")
                .anyRequest().authenticated()
                .and().httpBasic();

        http.cors().and().csrf().disable();
        http.headers().frameOptions().disable();
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
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
