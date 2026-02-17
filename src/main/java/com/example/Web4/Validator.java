package com.example.Web4;

import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class Validator {
    private static final Set<Integer> ALLOWED_RADIO_VALUES = Set.of(-3, -2, -1, 0, 1, 2, 3, 4, 5);

    public boolean validate(float x, float y, float r) {
        return Float.isFinite(x)
                && Float.isFinite(y)
                && Float.isFinite(r)
                && x >= -3.0f
                && x <= 5.0f
                && y >= -3.0f
                && y <= 3.0f
                && isAllowedRadioValue(r)
                && r >= 0.0f;
    }

    private boolean isAllowedRadioValue(float value) {
        int integer = Math.round(value);
        return Math.abs(value - integer) < 1e-6 && ALLOWED_RADIO_VALUES.contains(integer);
    }
}
