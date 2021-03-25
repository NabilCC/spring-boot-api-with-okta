package net.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Address {
    private String addressLine1;
    private String addressLine2;
    private String town;
    private String county;
    private String postcode;
}
