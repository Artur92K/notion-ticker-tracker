package dev.pato;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

public class Model {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ChartResponse {

        private Chart chart;

    }

    @Getter
    @Setter
    @NoArgsConstructor
    static class Chart {
        private List<Result> result;
        private Object error; // Assuming error can be null


    }

    @Getter
    @Setter
    @NoArgsConstructor
    static class Result {
        private Meta meta;
        private Indicators indicators;

    }

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    static class Meta {
        @ToString.Include
        private String currency;
        @ToString.Include
        private String symbol;
        private String exchangeName;
        private String fullExchangeName;
        private String instrumentType;
        private long firstTradeDate;
        private long regularMarketTime;
        private boolean hasPrePostMarketData;
        private int gmtoffset;
        private String timezone;

        private String exchangeTimezoneName;
        @ToString.Include
        private String regularMarketPrice;
        //
        @JsonProperty("currentTradingPeriod")
        private TradingPeriod currentTradingPeriod;

    }

    @Getter
    @Setter
    @NoArgsConstructor
    static class TradingPeriod {
        private Period pre;
        private Period regular;
        private Period post;


    }

    @Getter
    @Setter
    @NoArgsConstructor
    static class Period {
        private String timezone;
        private long start;
        private long end;
        private int gmtoffset;

    }

    @Getter
    @Setter
    @NoArgsConstructor
    static class Indicators {
        private List<Object> quote; // Assuming quote is an empty object
        private List<Object> adjclose; // Assuming adjclose is an empty object


    }
}
