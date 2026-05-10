package pro.jeti.athenapress.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record LocationInfo(
        String world,
        Double x,
        Double y,
        Double z,
        String description
) {
}