package io.github.selenajiro.europolitik.importrun;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ImportRunService {

    private final ImportRunRepository importRunRepository;

    public ImportRunService(ImportRunRepository importRunRepository) {
        this.importRunRepository = importRunRepository;
    }

    public List<ImportRun> findAll() {
        return importRunRepository.findAll();
    }

    public Optional<ImportRun> findById(Long id) {
        return importRunRepository.findById(id);
    }
}
