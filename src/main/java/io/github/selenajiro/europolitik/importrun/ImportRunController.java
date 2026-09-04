package io.github.selenajiro.europolitik.importrun;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/import-runs")
public class ImportRunController {

    private final ImportRunService importRunService;

    public ImportRunController(ImportRunService importRunService) {
        this.importRunService = importRunService;
    }

    @GetMapping
    public List<ImportRunResponse> findAll() {
        return importRunService.findAll().stream()
                .map(ImportRunResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public ImportRunResponse findById(@PathVariable Long id) {
        return importRunService.findById(id)
                .map(ImportRunResponse::from)
                .orElseThrow();
    }
}
