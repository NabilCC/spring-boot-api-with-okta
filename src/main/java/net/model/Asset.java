package net.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@JsonPropertyOrder({"id", "businessUnit", "activationDate", "format", "orientation", "address"})
public class Asset {

    @EqualsAndHashCode.Include
    private Long id;
    private BusinessUnit businessUnit;
    private LocalDateTime activationDate;
    private AssetFormat format;
    private AssetOrientation orientation;
    private Address address;
}
