package net.controller;

import net.model.Asset;
import net.model.BusinessUnit;
import net.repository.AssetRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;

@RestController
public class AssetController {

    private final AssetRepository assetRepository;

    public AssetController(AssetRepository assetRepository) {
        this.assetRepository = assetRepository;
    }

    @GetMapping("/simple")
    public ResponseEntity<?> foo() {
        return ResponseEntity.ok("foo");
    }

    @GetMapping("/assets")
    public ResponseEntity<?> findAssets(@RequestParam(name = "unit", required = false) String businessUnitParam,
                                        @AuthenticationPrincipal Jwt jwtPrincipal) {
        if (businessUnitParam == null) {
            return ResponseEntity.ok(assetRepository.findAll());
        }

        BusinessUnit unit;
        try {
            unit = BusinessUnit.valueOf(businessUnitParam);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid business unit");
        }
        return ResponseEntity.ok(assetRepository.findByBusinessUnit(unit));
    }
}
