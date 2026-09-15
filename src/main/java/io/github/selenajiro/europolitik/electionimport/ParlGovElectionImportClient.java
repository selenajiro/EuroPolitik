package io.github.selenajiro.europolitik.electionimport;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Component
public class ParlGovElectionImportClient {

    private static final String RESOURCE_PATH = "import-data/view_election.csv";

    public List<ParlGovElectionRow> readRows() throws IOException {
        try (InputStream in = new ClassPathResource(RESOURCE_PATH).getInputStream()) {
            CsvMapper csvMapper = new CsvMapper();
            CsvSchema schema = CsvSchema.emptySchema().withHeader();
            MappingIterator<ParlGovElectionRow> iterator = csvMapper.readerFor(ParlGovElectionRow.class)
                    .with(schema)
                    .readValues(in);
            return iterator.readAll();
        }
    }
}
