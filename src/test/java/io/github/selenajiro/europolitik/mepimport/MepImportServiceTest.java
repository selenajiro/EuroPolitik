package io.github.selenajiro.europolitik.mepimport;

import io.github.selenajiro.europolitik.country.Country;
import io.github.selenajiro.europolitik.country.CountryRepository;
import io.github.selenajiro.europolitik.importrun.ImportRun;
import io.github.selenajiro.europolitik.importrun.ImportRunRepository;
import io.github.selenajiro.europolitik.mep.Mep;
import io.github.selenajiro.europolitik.mep.MepRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

// This is a plain Mockito unit test (no Spring context, no Testcontainers)
// since it only exercises orchestration logic, not real HTTP or a real DB.
@ExtendWith(MockitoExtension.class)
class MepImportServiceTest {

    @Mock
    private MepImportClient mepImportClient;
    @Mock
    private MepRepository mepRepository;
    @Mock
    private CountryRepository countryRepository;
    @Mock
    private ImportRunRepository importRunRepository;

    private MepImportService service;
    private Country germany;

    @BeforeEach
    void setUp() {
        service = new MepImportService(mepImportClient, mepRepository, countryRepository, importRunRepository);

        germany = new Country();
        germany.setId(1L);
        germany.setIsoCode("DE");
        germany.setName("Germany");

        when(importRunRepository.save(any(ImportRun.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void insertsNewMepAndCountsIt() {
        EpMepDto dto = new EpMepDto("12345", "Maria Example", "DE", "PPE", "2024-07-16", null);
        when(mepImportClient.fetchCurrentMeps()).thenReturn(List.of(dto));
        when(countryRepository.findByIsoCode("DE")).thenReturn(Optional.of(germany));
        when(mepRepository.findByEpMemberId(12345L)).thenReturn(Optional.empty());

        ImportRun run = service.importCurrentMeps();

        assertThat(run.getStatus()).isEqualTo("SUCCESS");
        assertThat(run.getRecordsInserted()).isEqualTo(1);
        assertThat(run.getRecordsUpdated()).isZero();
        assertThat(run.getRecordsSkipped()).isZero();

        ArgumentCaptor<Mep> savedMep = ArgumentCaptor.forClass(Mep.class);
        Mockito.verify(mepRepository).save(savedMep.capture());
        assertThat(savedMep.getValue().getFullName()).isEqualTo("Maria Example");
        assertThat(savedMep.getValue().getPoliticalGroup()).isEqualTo("European People's Party (Christian Democrats)");
        assertThat(savedMep.getValue().getEpMemberId()).isEqualTo(12345L);
    }

    @Test
    void skipsRecordWithUnknownCountry() {
        EpMepDto dto = new EpMepDto("999", "Nobody", "ZZ", "NI", "2024-07-16", null);
        when(mepImportClient.fetchCurrentMeps()).thenReturn(List.of(dto));
        when(countryRepository.findByIsoCode("ZZ")).thenReturn(Optional.empty());

        ImportRun run = service.importCurrentMeps();

        assertThat(run.getStatus()).isEqualTo("SUCCESS");
        assertThat(run.getRecordsSkipped()).isEqualTo(1);
        assertThat(run.getRecordsInserted()).isZero();
        Mockito.verify(mepRepository, Mockito.never()).save(any());
    }

    @Test
    void recordsFailedRunWhenFetchThrows() {
        when(mepImportClient.fetchCurrentMeps()).thenThrow(new RuntimeException("EP API is down"));

        ImportRun run = service.importCurrentMeps();

        assertThat(run.getStatus()).isEqualTo("FAILED");
        assertThat(run.getErrorMessage()).contains("EP API is down");
    }
}
