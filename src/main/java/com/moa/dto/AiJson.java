package com.moa.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AiJson {
    private List<Item> items;
    private String emotion;
    private String comment;
    private String place;
    private String transactionDate;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {
        private String category;
        private String name;
        private String raw_amount;
    }

}
