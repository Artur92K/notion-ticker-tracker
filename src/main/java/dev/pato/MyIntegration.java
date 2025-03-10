package dev.pato;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.PostConstruct;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;
import java.util.Optional;

@Service
public class MyIntegration {

    public static final String UPDATEURL = "https://api.notion.com/v1/pages/";

    private final RestTemplate restTemplate = new RestTemplate();
    private static final ObjectMapper mapper = new ObjectMapper();
    private final WebClient webClient = WebClient.builder()
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader("Notion-Version", "2022-06-28")
            .defaultHeader("Authorization", "Bearer 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")
            .build();

    @PostConstruct
    public void init() {
        update();
    }

    private void update() {

        fetchDatabase().forEach(e -> {
            String pageId = e.get("id").asText();
            JsonNode jsonNode = e.get("properties").get("Ticker").get("title");
            if (jsonNode.isNull() || jsonNode.isEmpty()) {
                return;
            }
            String name = jsonNode.get(0).get("plain_text").asText();
            Model.ChartResponse stockPrices = getStockPrices(name);

            try {

                var result = stockPrices.getChart().getResult();
                if (stockPrices.getChart().getResult() == null) {
                    return;
                }
                var meta = result.stream().map(Model.Result::getMeta).filter(dMeta -> dMeta.getSymbol().equals(name)).findFirst().get();
                var regularMarketPrice = meta.getRegularMarketPrice();

                var currency = meta.getCurrency();
                System.out.println(meta.getSymbol() + " " + regularMarketPrice + " " + meta.getCurrency());

                createJson(Double.parseDouble(regularMarketPrice), currency, meta.getExchangeName())
                        .ifPresent(r -> webClient.patch()
                                .uri(UPDATEURL + pageId)
                                .bodyValue(r)
                                .exchangeToMono(g -> g.bodyToMono(String.class))
                                .block()
                        );

            } catch (Exception ex) {
                System.out.println(ex);

            }
        });
    }

    private JsonNode fetchDatabase() {
        HttpHeaders headers = getHeaders();
        HttpEntity<String> entity = new HttpEntity<>("{}", headers);
        String url = "https://api.notion.com/v1/databases/" + "13a95ada-4c4f-808e-968d-f485dfe6595a"+ "/query";


        return mapper.convertValue(restTemplate.exchange(url, HttpMethod.POST, entity, Map.class).getBody().get("results"), JsonNode.class);


    }


    private Model.ChartResponse getStockPrices(String name) {

        String url = String.format("https://query1.finance.yahoo.com/v8/finance/chart/%s?interval=1h&range=1h", name);

        var response = webClient.get()
                .uri(url)
                .exchangeToMono(g -> g.bodyToMono(String.class))
                .block();

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
            return mapper.readValue(response, Model.ChartResponse.class);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + MyVariables.TOKEN);
        headers.set("Notion-Version", "2022-06-28");
        headers.set("Content-Type", "application/json");
        return headers;
    }

    public static Optional<String> createJson(double number, String currency, String name) {
        try {
            // Create the root node
            ObjectNode rootNode = mapper.createObjectNode();
            ObjectNode propertiesNode = mapper.createObjectNode();

            // Create the Value node
            ObjectNode valueNode = mapper.createObjectNode();
            valueNode.put("number", number);
            propertiesNode.set("Unit Price", valueNode);

            // Create the Currency node
            ObjectNode currencyNode = mapper.createObjectNode();
            ArrayNode currencyRichTextArray = mapper.createArrayNode();
            ObjectNode currencyTextNode = mapper.createObjectNode();
            ObjectNode currencyContentNode = mapper.createObjectNode();

            currencyContentNode.put("content", currency);
            currencyTextNode.set("text", currencyContentNode);
            currencyRichTextArray.add(currencyTextNode);

            currencyNode.set("rich_text", currencyRichTextArray);
            propertiesNode.set("Currency", currencyNode);

            // Set properties to root node
            rootNode.set("properties", propertiesNode);


            return Optional.ofNullable(mapper.writeValueAsString(rootNode));
        } catch (Exception e) {
            System.out.println(e);
            return Optional.empty();
        }
    }


}

