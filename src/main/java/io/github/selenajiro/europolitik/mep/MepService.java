package io.github.selenajiro.europolitik.mep;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MepService {

    private final MepRepository mepRepository;

    public MepService(MepRepository mepRepository) {
        this.mepRepository = mepRepository;
    }

    public List<Mep> findAllWithCountry() {
        return mepRepository.findAllWithCountry();
    }

    public Optional<Mep> findByIdWithCountry(Long id) {
        return mepRepository.findByIdWithCountry(id);
    }
}
