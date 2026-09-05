package io.github.selenajiro.europolitik.importrun;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "import_run")
@Getter
@Setter
@NoArgsConstructor
public class ImportRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String source;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    private LocalDateTime finishedAt;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(nullable = false)
    private Integer recordsRead = 0;

    @Column(nullable = false)
    private Integer recordsInserted = 0;

    @Column(nullable = false)
    private Integer recordsUpdated = 0;

    @Column(nullable = false)
    private Integer recordsSkipped = 0;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}