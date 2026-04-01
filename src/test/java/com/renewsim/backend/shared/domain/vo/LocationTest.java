package com.renewsim.backend.shared.domain.vo;

import com.renewsim.backend.shared.domain.exception.InvalidLocationException;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class LocationTest {
    
    @Test
    void shouldCreateValidLocation() {
        Location location = new Location(40.7128, -74.0060);
        assertThat(location.latitude()).isEqualTo(40.7128);
        assertThat(location.longitude()).isEqualTo(-74.0060);
    }
    
    @Test
    void shouldAcceptLatitudeBoundaries() {
        assertThatCode(() -> new Location(-90, 0)).doesNotThrowAnyException();
        assertThatCode(() -> new Location(90, 0)).doesNotThrowAnyException();
    }
    
    @Test
    void shouldAcceptLongitudeBoundaries() {
        assertThatCode(() -> new Location(0, -180)).doesNotThrowAnyException();
        assertThatCode(() -> new Location(0, 180)).doesNotThrowAnyException();
    }
    
    @Test
    void shouldRejectLatitudeTooLow() {
        assertThatThrownBy(() -> new Location(-90.1, 0))
            .isInstanceOf(InvalidLocationException.class)
            .hasMessageContaining("Latitude must be between -90 and 90");
    }
    
    @Test
    void shouldRejectLatitudeTooHigh() {
        assertThatThrownBy(() -> new Location(90.1, 0))
            .isInstanceOf(InvalidLocationException.class)
            .hasMessageContaining("Latitude must be between -90 and 90");
    }
    
    @Test
    void shouldRejectLongitudeTooLow() {
        assertThatThrownBy(() -> new Location(0, -180.1))
            .isInstanceOf(InvalidLocationException.class)
            .hasMessageContaining("Longitude must be between -180 and 180");
    }
    
    @Test
    void shouldRejectLongitudeTooHigh() {
        assertThatThrownBy(() -> new Location(0, 180.1))
            .isInstanceOf(InvalidLocationException.class)
            .hasMessageContaining("Longitude must be between -180 and 180");
    }
}
