package com.moa.dto;

import java.util.List;

public record AutoInitRequest(
        List<Long> categoryIds
) {
}
