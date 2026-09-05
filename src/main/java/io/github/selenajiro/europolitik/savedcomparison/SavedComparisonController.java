package io.github.selenajiro.europolitik.savedcomparison;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/comparisons")
public class SavedComparisonController {

    private final SavedComparisonService savedComparisonService;

    public SavedComparisonController(SavedComparisonService savedComparisonService) {
        this.savedComparisonService = savedComparisonService;
    }

    @GetMapping
    public List<SavedComparisonResponse> findAll(Authentication authentication) {
        return savedComparisonService.findAllForUser(authentication.getName()).stream()
                .map(SavedComparisonResponse::from)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SavedComparisonResponse save(@Valid @RequestBody SaveComparisonRequest request, Authentication authentication) {
        return SavedComparisonResponse.from(
                savedComparisonService.save(authentication.getName(), request.countryAId(), request.countryBId()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Authentication authentication) {
        savedComparisonService.delete(authentication.getName(), id);
    }
}
