package com.weather.platform.backend.collection.client.dto;

import java.util.List;
import java.util.Map;

public record KmaResponse(Response response) {

    public record Response(Header header, Body body) {
    }

    public record Header(String resultCode, String resultMsg) {
    }

    public record Body(Items items, Integer totalCount) {
    }

    public record Items(List<Map<String, Object>> item) {
    }
}
