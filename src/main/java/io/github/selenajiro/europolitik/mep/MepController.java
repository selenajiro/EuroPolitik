package io.github.selenajiro.europolitik.mep;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/meps")
public class MepController {

    private final MepService mepService;

    public MepController(MepService mepService) {
        this.mepService = mepService;
    }

    @GetMapping
    public List<MepResponse> findAll() {
        return mepService.findAllWithCountry().stream()
                .map(MepResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public MepResponse findById(@PathVariable Long id) {
        return mepService.findByIdWithCountry(id)
                .map(MepResponse::from)
                .orElseThrow();
    }
}
