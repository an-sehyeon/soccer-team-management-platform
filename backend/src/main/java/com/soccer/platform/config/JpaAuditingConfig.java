package com.soccer.platform.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/*
 * JPA Auditing 설정
 *
 * BaseEntity의 createdAt, updatedAt 값을
 * Entity 저장/수정 시 자동으로 채우기 위해 사용한다.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}