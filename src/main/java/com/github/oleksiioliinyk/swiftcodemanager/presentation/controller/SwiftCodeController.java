package com.github.oleksiioliinyk.swiftcodemanager.presentation.controller;

import com.github.oleksiioliinyk.swiftcodemanager.application.service.SwiftCodeManagerService;
import com.github.oleksiioliinyk.swiftcodemanager.presentation.dto.CreateSwiftCodeRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/v1/swift-codes")
@Validated
public class SwiftCodeController {
    @Autowired
    private SwiftCodeManagerService swiftCodeManagerService;

    @GetMapping("/{swiftCode}")
    public ResponseEntity<?> findSwiftCode(
            @PathVariable
            @Pattern(regexp = "^[A-Z0-9]{11}$",message = "SWIFT code is incorrect or is not 11 characters long.")
            String swiftCode
    ){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(swiftCodeManagerService.findSwiftCode(swiftCode));
    }

    @GetMapping("/country/{countryISO2code}")
    public ResponseEntity<?> findSwiftCodeByCountryISO2Code(
            @PathVariable
            @Pattern(regexp = "^[A-Z]{2}$", message = "Country ISO2 code is incorrect or is not 2 characters long.")
            String countryISO2code
    ){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(swiftCodeManagerService.findSwiftCodeByCountryISO2Code(countryISO2code));
    }

    @PostMapping
    public ResponseEntity<?> createSwiftCode(@Valid @RequestBody CreateSwiftCodeRequest createSwiftCodeRequest){
        swiftCodeManagerService.createSwiftCode(createSwiftCodeRequest);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Map.of("message", "SWIFT code was successfully created."));
    }

    @DeleteMapping("/{swiftCode}")
    public ResponseEntity<?> deleteSwiftCode(
            @PathVariable
            @Pattern(regexp = "^[A-Z0-9]{11}$", message = "SWIFT code is incorrect or is not 11 characters long.")
            String swiftCode
    ){
        swiftCodeManagerService.deleteSwiftCode(swiftCode);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(Map.of("message", "SWIFT code was successfully deleted."));
    }
}
