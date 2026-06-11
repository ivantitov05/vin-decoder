package com.vin.decoder.model;

import com.vin.decoder.service.state.RequestStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Data
@Builder
@Entity
@Table(name = "vin-requests")
@AllArgsConstructor
@NoArgsConstructor
public class VinRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String vin;

    @Column(name = "user_id")
    private Long userId;

    @Enumerated(EnumType.STRING)
    private RequestStatus status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private CarInfo result;

    @Column(name = "error_message")
    private String errorMessage;
}