package com.github.oleksiioliinyk.swiftcodemanager.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "swift_code")
public class SwiftCodeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "swift_code_id")
    private Long id;

    @Column(name = "swift_code", length = 11, nullable = false, unique = true)
    private String swiftCode;

    @Column(name = "bank_name", length = 255, nullable = false)
    private String bankName;

    @Column(name = "address", nullable = false, columnDefinition = "TEXT")
    private String address;

    @Column(name = "country_iso2_code", length = 2, nullable = false)
    private String countryISO2Code;

    @Column(name = "country_name", length = 100, nullable = false)
    private String countryName;

    // Self-referencing Many-to-One for headquarters.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "headquarter_swift_code_id")
    private SwiftCodeEntity headquarter;
}
