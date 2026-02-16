package com.example.Web4;

import org.springframework.stereotype.Service;

@Service
public class AreaChecker {
    public boolean checkArea(float x, float y, float r) {
        if (r <= 0.0f) {
            return false;
        }

        if (x >= 0.0f && y >= 0.0f) {
            return x <= r && y <= r / 2.0f;
        }

        if (x < 0.0f && y >= 0.0f) {
            return x * x + y * y <= r * r;
        }

        if (x < 0.0f && y < 0.0f) {
            return -x <= 2.0f * y + r;
        }

        if (x >= 0.0f && y < 0.0f) {
            return y >= x - r;
        }

        return false;
    }
}
