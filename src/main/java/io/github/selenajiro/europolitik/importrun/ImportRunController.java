package io.github.selenajiro.europolitik.importrun;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/import-runs")
public class ImportRunController {

    private final ImportRunRepository importRunRepository;

    public ImportRunController(ImportRunRepository importRunRepository) {
        this.importRunRepository = importRunRepository;
    }

    @GetMapping
    public List<ImportRunResponse> findAll() {
        return importRunRepository.findAll().stream()
                .map(ImportRunResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public ImportRunResponse findById(@PathVariable Long id) {
        return importRunRepository.findById(id)
                .map(ImportRunResponse::from)
                .orElseThrow();
    }
}
