package com.racha.rachavoting.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.racha.rachavoting.util.logo.LogoStatus;
import com.racha.rachavoting.util.logo.LogoType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
public class Logo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private LogoType LogoType;

    @Enumerated(EnumType.STRING)
    private LogoStatus status = LogoStatus.TEMPORARY;

    @Column(nullable = false)
    private String Name;

    // saves the id of the entity the logo is saved to
    private Long logoTypeId;

    @Column(nullable = false)
    private boolean verified = false;

    @Column(unique = true)
    private String uniqueId;

    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private String contentType; // MIME type (e.g., image/png)

    // @Lob
    @Column(columnDefinition = "BYTEA", nullable = false)
    private byte[] data;
}
