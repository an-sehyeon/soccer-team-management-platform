package com.soccer.platform.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.soccer.platform.security.JwtAccessDeniedHandler;
import com.soccer.platform.security.JwtAuthenticationEntryPoint;
import com.soccer.platform.security.JwtAuthenticationFilter;



import lombok.RequiredArgsConstructor;
/*
 * Spring Security 설정 클래스
 * 
 * 회원가입/로그인 기능에서 사용하는 보안 설정을 관리.
 * 
 * 처리 흐름
 * - 회원가입과 로그인 API는 인증 없이 접근 가능하게 허용한다.
 * - 나머지 API는 JWT 인증이 필요하다.
 * - JWT 기반 인증을 사용하므로 세션은 사용하지 않는다.
 * - JwtAuthenticationFilter를 UsernamePasswordAuthenticationFilter 앞에 등록한다.

 */
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
	
	 private final JwtAuthenticationFilter jwtAuthenticationFilter;
	 private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
	 private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
	
	/*
	 * Security Filter Chain 설정
	 * 
	 * 회원가입/로그인 API는 인증 없이 접근 가능하고,
     * 그 외 API는 JWT 인증이 필요하다.
	 */
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				.csrf(csrf -> csrf.disable())
				.cors(cors -> cors.configurationSource(corsConfigurationSource()))
				.sessionManagement(session ->
						session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
				)
				.exceptionHandling(exception -> exception
	                    .authenticationEntryPoint(jwtAuthenticationEntryPoint)
	                    .accessDeniedHandler(jwtAccessDeniedHandler)
	           )         
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
						.requestMatchers("/api/auth/sign-up", "/api/auth/login").permitAll()
						.anyRequest().authenticated()
                )
				.addFilterBefore(
						jwtAuthenticationFilter, 
						UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
	
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
	    CorsConfiguration configuration = new CorsConfiguration();

	    configuration.setAllowedOrigins(List.of("http://localhost:5173"));
	    configuration.setAllowedMethods(List.of("GET", "POST", "PATCH", "DELETE", "OPTIONS"));
	    configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
	    configuration.setExposedHeaders(List.of("Authorization"));
	    configuration.setAllowCredentials(false);

	    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
	    source.registerCorsConfiguration("/**", configuration);

	    return source;
	}

	/*
	 * 비밀번호 암호화 Bean 등록
	 * 
	 * 회원가입 시 원문 비밀번호를 BCrypt 해시로 암호화하고,
	 * 로그인 시 입력 비밀번호화 DB에 저장된 해시 값을 비교.
	 */
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

}
