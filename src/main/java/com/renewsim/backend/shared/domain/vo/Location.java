package com.renewsim.backend.shared.domain.vo;

import com.renewsim.backend.shared.domain.exception.InvalidLocationException;

public record Location(double latitude, double longitude) {
    
    public Location {
        if (latitude < -90 || latitude > 90) {
            throw new InvalidLocationException(
                "Latitude must be between -90 and 90, got: " + latitude
            );
        }
        if (longitude < -180 || longitude > 180) {
            throw new InvalidLocationException(
                "Longitude must be between -180 and 180, got: " + longitude
            );
        }
    }
}
