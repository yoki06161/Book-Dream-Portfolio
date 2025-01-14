package com.bookdream.sbb;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.bookdream.sbb.user.CustomLogoutHandler;

@EnableWebSecurity
// prod. 로그인안했을시 리뷰 막기용으로 씀.
@EnableMethodSecurity(prePostEnabled = true)
@Configuration
public class SecurityConfig {

    private final CustomLogoutHandler customLogoutHandler;

    public SecurityConfig(CustomLogoutHandler customLogoutHandler) {
        this.customLogoutHandler = customLogoutHandler;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // 일반 사용자
    	http
            .authorizeHttpRequests((authorizeHttpRequests) -> authorizeHttpRequests
                .requestMatchers(new AntPathRequestMatcher("/**")).permitAll()
            	.requestMatchers(new AntPathRequestMatcher("/trade/chat/leave")).authenticated())
            .headers((headers) -> headers
                .addHeaderWriter(new XFrameOptionsHeaderWriter(
                    XFrameOptionsHeaderWriter.XFrameOptionsMode.SAMEORIGIN)))
            .formLogin((formLogin) -> formLogin
                .loginPage("/user/login")
                .loginProcessingUrl("/user/login") 
                .failureUrl("/user/login?error=true")
                .defaultSuccessUrl("/main"))
            // 관리자페이지 로그인 처리
            .formLogin((formLogin) -> formLogin
                    .loginPage("/admin/login")
                    .loginProcessingUrl("/admin/login")
                    .failureUrl("/admin/login?error=true")
                    .defaultSuccessUrl("/admin/user"))
            .logout((logout) -> logout
            		.logoutRequestMatcher(new AntPathRequestMatcher("/user/logout"))
            		.logoutSuccessUrl("/main")
            		.invalidateHttpSession(true)
            	    .addLogoutHandler(customLogoutHandler))
            // 관리자페이지 로그아웃 처리
            .logout((logout) -> logout
                    .logoutRequestMatcher(new AntPathRequestMatcher("/admin/logout"))
                    .logoutSuccessUrl("/admin/user")
                    .invalidateHttpSession(true)
                    .addLogoutHandler(customLogoutHandler))
            .oauth2Login((oauth2Login) -> oauth2Login
                .loginPage("/oauth-login/login")
                .defaultSuccessUrl("/main")
                .failureUrl("/user/login")
                .permitAll())
        .csrf((csrf) -> csrf
                .ignoringRequestMatchers(new AntPathRequestMatcher("/trade/chat/leave")));

    	// 커스텀 필터 추가(시간되면)
        //http.addFilterBefore(new CustomAdminAuthenticationFilter(authenticationManager), UsernamePasswordAuthenticationFilter.class);
    	
    	return http.build();
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}