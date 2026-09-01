package io.github.selenajiro.europolitik.importrun;

import java.time.LocalDateTime;

public record ImportRunResponse(
        Long id,
        String source,
        LocalDateTime startedAt,
        LocalDateTime finishedAt,
        String status,
        Integer recordsRead,
        Integer recordsInserted,
        Integer recordsUpdated,
        Integer recordsSkipped,
        String errorMessage,
        LocalDateTime createdAt
) {
    public static ImportRunResponse from(ImportRun run) {
        return new ImportRunResponse(
                run.getId(),
                run.getSource(),
                run.getStartedAt(),
                run.getFinishedAt(),
                run.getStatus(),
                run.getRecordsRead(),
                run.getRecordsInserted(),
                run.getRecordsUpdated(),
                run.getRecordsSkipped(),
                run.getErrorMessage(),
                run.getCreatedAt()
        );
    }
}
