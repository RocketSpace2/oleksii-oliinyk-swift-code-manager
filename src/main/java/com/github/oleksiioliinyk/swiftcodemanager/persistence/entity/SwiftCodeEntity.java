package com.github.oleksiioliinyk.swiftcodemanager.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

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

    @Column(name = "swift_code", length = 5, nullable = false, unique = true)
    private String swiftCode;

    @Column(name = "code_type", length = 10, nullable = false)
    private String codeType;

    @Column(name = "bank_name", length = 255, nullable = false)
    private String bankName;

    @Column(name = "address", nullable = false, columnDefinition = "TEXT")
    private String address;

    @Column(name = "town_name", length = 255)
    private String townName;

    @Column(name = "country_iso2_code", length = 2, nullable = false)
    private String countryIso2Code;

    @Column(name = "country_name", length = 100, nullable = false)
    private String countryName;

    // Self-referencing Many-to-One for headquarters.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "headquarter_swift_code_id")
    private SwiftCodeEntity headquarter;

    // One-to-Many to reference branches.
    @OneToMany(mappedBy = "headquarter", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SwiftCodeEntity> branches = new ArrayList<>();
}
