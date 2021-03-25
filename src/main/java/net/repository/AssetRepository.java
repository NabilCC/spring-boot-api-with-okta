package net.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.model.Address;
import net.model.Asset;
import net.model.AssetFormat;
import net.model.AssetOrientation;
import net.model.BusinessUnit;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

import static java.util.stream.Collectors.toList;

@Component
@Slf4j
public class AssetRepository implements InitializingBean {

    private static final Instant INSTANT_2000 = Instant.parse("2000-01-01T00:00:00Z");
    private static final Instant INSTANT_2021 = Instant.parse("2021-01-01T00:00:00Z");
    private static final List<BigDecimal> cptValues = buildCptValues();
    private static final List<BigDecimal> maxImpactValues = buildMaxImpactValues();
    private static final List<BigDecimal> minImpactValues = buildMinImpactValues();
    public  static final String ADDRESS_JSON_FILE = "addresses.json";

    private final List<AssetOrientation> assetEnvironments;
    private final List<AssetFormat> assetFormats;
    private final ThreadLocalRandom random;
    private final Map<Long, Asset> datastore;
    private final Map<BusinessUnit, List<Address>> addressByBusinessUnit;
    private final ObjectMapper objectMapper;
    private final AtomicLong idSequence;

    @Value("${asset.count}")
    private Integer assetCount;

    public AssetRepository(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.datastore = new HashMap<>();
        this.idSequence = new AtomicLong();
        this.assetEnvironments = Arrays.asList(AssetOrientation.values());
        this.assetFormats =  Arrays.asList(AssetFormat.values());
        this.addressByBusinessUnit = new HashMap<>();
        this.random = ThreadLocalRandom.current();
    }

    @Override
    @SneakyThrows
    public void afterPropertiesSet() {
        log.info("Reading address examples.");
        InputStream is = ClassLoader.getSystemResourceAsStream(ADDRESS_JSON_FILE);
        Objects.requireNonNull(is, "Invalid file: " + ADDRESS_JSON_FILE);
        List<Map<String, Object>> addresses = objectMapper.readValue(is, new TypeReference<List<Map<String, Object>>>() {});
        addresses.forEach(this::addAddressToMap);

        log.info("Populating in-memory datastore");

        for (int i = 0; i < assetCount; i++) {
            Arrays.stream(BusinessUnit.values()).forEach(this::createRandomAsset);
        }

        log.info("In-memory datastore was populated successfully.");
    }

    public List<Asset> findAll() {
        return new ArrayList<>(datastore.values());
    }

    public Optional<Asset> findById(Long id) {
        return Optional.ofNullable(datastore.get(id));
    }

    public List<Asset> findByBusinessUnit(BusinessUnit unit) {
        Objects.requireNonNull(unit, "Unit is mandatory");
        return datastore.values().stream().filter(asset -> unit.equals(asset.getBusinessUnit())).collect(toList());
    }

    private void createRandomAsset(BusinessUnit businessUnit) {
        long id = idSequence.getAndIncrement();
        long activationMillis = random.nextLong(INSTANT_2000.toEpochMilli(), INSTANT_2021.toEpochMilli());
        LocalDateTime activationDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(activationMillis), ZoneId.of("Europe/London"));
        AssetFormat format = assetFormats.get(random.nextInt(0, assetFormats.size()));
        AssetOrientation environment = assetEnvironments.get(random.nextInt(0, assetEnvironments.size()));
        List<Address> addressesForBusinessUnit = addressByBusinessUnit.get(businessUnit);
        Address address = addressesForBusinessUnit.get(random.nextInt(0, addressesForBusinessUnit.size()));

        Asset asset = Asset.builder().id(id).businessUnit(businessUnit).activationDate(activationDate)
                .address(address).format(format).orientation(environment).build();

        datastore.put(id, asset);
    }

    private void addAddressToMap(Map<String, Object> details) {
        String line1 = (String) details.get("addressLine1");
        String line2 = (String) details.get("addressLine2");
        String town =  (String) details.get("town");
        String county =  (String) details.get("county");
        String postcode =  (String) details.get("postcode");
        BusinessUnit businessUnit = BusinessUnit.valueOf((String) details.get("businessUnit"));

        Address address = Address.builder().addressLine1(line1).addressLine2(line2).town(town).county(county).postcode(postcode).build();
        addressByBusinessUnit.computeIfAbsent(businessUnit, bu -> new ArrayList<>()).add(address);
    }

    private static List<BigDecimal> buildCptValues() {
        return List.of(
                new BigDecimal("1.1"), new BigDecimal("1.5"), new BigDecimal("3.1"),
                new BigDecimal("2.2"), new BigDecimal("4.5"), new BigDecimal("6.1"),
                new BigDecimal("2.4"), new BigDecimal("2.7"), new BigDecimal("6.2"),
                new BigDecimal("2.5"), new BigDecimal("3.4"), new BigDecimal("1.7"),
                new BigDecimal("4.1"), new BigDecimal("1.2"), new BigDecimal("1.5")
        );
    }

    private static List<BigDecimal> buildMinImpactValues() {
        return List.of(
                new BigDecimal("0.1117"), new BigDecimal("0.1222"), new BigDecimal("0.1345"),
                new BigDecimal("0.1467"), new BigDecimal("0.1519"), new BigDecimal("0.1523"),
                new BigDecimal("0.1525"), new BigDecimal("0.1534"), new BigDecimal("0.1545"),
                new BigDecimal("0.1621"), new BigDecimal("0.1711"), new BigDecimal("0.1733"),
                new BigDecimal("0.1743"), new BigDecimal("0.1781"), new BigDecimal("0.1871")
        );
    }

    private static List<BigDecimal> buildMaxImpactValues() {
        return List.of(
                new BigDecimal("0.1217"), new BigDecimal("0.1322"), new BigDecimal("0.1425"),
                new BigDecimal("0.1564"), new BigDecimal("0.1566"), new BigDecimal("0.1601"),
                new BigDecimal("0.1575"), new BigDecimal("0.1545"), new BigDecimal("0.1598"),
                new BigDecimal("0.1633"), new BigDecimal("0.1811"), new BigDecimal("0.1789"),
                new BigDecimal("0.1747"), new BigDecimal("0.1890"), new BigDecimal("0.1920")
        );
    }
}

