package com.github.oleksiioliinyk.swiftcodemanager.infrastructure.persistence.impl;

import com.github.oleksiioliinyk.swiftcodemanager.domain.model.SwiftCode;
import com.github.oleksiioliinyk.swiftcodemanager.infrastructure.persistence.contract.SwiftCodeEntityRepository;
import com.github.oleksiioliinyk.swiftcodemanager.infrastructure.persistence.entity.SwiftCodeEntity;
import com.github.oleksiioliinyk.swiftcodemanager.infrastructure.persistence.repository.SwiftCodeEntityJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = "spring.jpa.hibernate.ddl-auto=create-drop"
)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Transactional
public class SwiftCodeEntityRepositoryImplIntegrationTest {
    @Autowired
    private SwiftCodeEntityRepository repo;

    @Autowired
    private SwiftCodeEntityJpaRepository jpa;

    private SwiftCode makeDomain(String code, String country) {
        return new SwiftCode(
                null,
                code,
                code.startsWith("AAA") ? "BankA" : "BankB",
                "Addr",
                country,
                country.equals("US") ? "United States" : "Other",
                null
        );
    }

    @Test
    void count_and_saveAll_and_findBySwiftCode() {
        long before = repo.count();
        // save two new codes

        List<SwiftCode> codesToSave = List.of(
                new SwiftCode(
                        null,
                        "AAAABBCCXXX",
                        "BankA",
                        "Addr",
                        "US",
                        "United States",
                        null
                ),
                new SwiftCode(
                        null,
                        "QWERBBCCXXX",
                        "BankB",
                        "Addr",
                        "US",
                        "United States",
                        null
                )
        );
        repo.saveAll(codesToSave);

        long after = repo.count();
        assertThat(after).isEqualTo(before + 2);

        // find exact code ignoring case
        SwiftCode headquarter1 = repo.findSwiftCodeBySwiftCode("aaaabbccxxx");
        assertThat(headquarter1.swiftCode()).isEqualTo("AAAABBCCXXX");

        SwiftCode headquarter2 = repo.findSwiftCodeBySwiftCode("QWERBBCCXXX");
        assertThat(headquarter2.swiftCode()).isEqualTo("QWERBBCCXXX");
    }

    @Test
    void findBranches_filtersOutHeadquarterSuffix() {
        // insert one HQ + two branches
        SwiftCodeEntity headquarter = jpa.save(new SwiftCodeEntity(null, "XYZ12345XXX", "Bank A", "Address C", "US", "United States", null));
        jpa.save(new SwiftCodeEntity(null, "XYZ12345001", "Bank B", "Address B", "US", "United States", headquarter));
        jpa.save(new SwiftCodeEntity(null, "XYZ12345002", "Bank C", "Address C", "US", "United States", headquarter));

        // find branches for prefix "XYZ12345" should return the two non-XXX
        var branches = repo.findBranches("XYZ12345", "%XXX");
        assertThat(branches)
                .extracting(SwiftCode::swiftCode)
                .containsExactlyInAnyOrder("XYZ12345001", "XYZ12345002");
    }

    @Test
    void findByCountry_returnsOrThrows() {
        // none in DB ⇒ exception
        assertThatThrownBy(() -> repo.findSwiftCodeByCountryISO2Code("ZZ"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    var rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode().value()).isEqualTo(400);
                    assertThat(rse.getReason())
                            .isEqualTo("SWIFT codes for provided country ISO2 code were not found.");
                });

        // insert two for US
        jpa.save(new SwiftCodeEntity(null, "AAA00000XXX", "", "", "US", "United States", null));
        jpa.save(new SwiftCodeEntity(null, "BBB00000XXX", "", "", "US", "United States", null));

        var list = repo.findSwiftCodeByCountryISO2Code("US");
        assertThat(list).hasSize(2)
                .allSatisfy(s -> assertThat(s.countryISO2Code()).isEqualTo("US"));
    }

    @Test
    void createSwiftCode_allowsNew_and_rejectsDuplicates() {
        var domain = makeDomain("BBBBAA22XXX", "FR");
        // success first time
        var saved = repo.createSwiftCode(domain);
        assertThat(saved.swiftCode()).isEqualTo("BBBBAA22XXX");

        // duplicate throws ResponseStatusException
        assertThatThrownBy(() -> repo.createSwiftCode(domain))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    var rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode().value()).isEqualTo(400);
                    assertThat(rse.getReason())
                            .isEqualTo(
                                    "SWIFT code was not created. " +
                                            domain.swiftCode() +
                                            " already exists. Please provide correct data."
                            );
                });
    }

    @Test
    void deleteSwiftCode_removesExistingCode() {
        // given – a persisted SWIFT code
        var entity = new SwiftCodeEntity(
                null,
                "CBCCTT11XXX",
                "",
                "",
                "IT",
                "Italy",
                null);
        jpa.save(entity);

        SwiftCode existing = repo.findSwiftCodeBySwiftCode("CBCCTT11XXX");

        // when – delete it
        repo.deleteSwiftCode(existing);

        // then – record is gone
        assertThat(jpa.findSwiftCodeEntityBySwiftCodeIgnoreCase("CBCCTT11XXX"))
                .isNull();
    }
}
