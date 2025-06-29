package dev.pato;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class MyIntegration {

    private static final String UPDATE_URL = "https://api.notion.com/v1/pages/";
    private static final String AUTHORIZATION = "Authorization";
    private static final ObjectMapper mapper = new ObjectMapper();

    private final RestTemplate restTemplate = new RestTemplate();
    private final WebClient integrationWebClient;

    void update(String apikey, String database) {

        JsonNode databaseJsonNode = fetchDatabase(database, apikey);

        databaseJsonNode.forEach(e ->

        {
            String pageId = e.get("id").asText();
            JsonNode jsonNode = e.get("properties").get("Ticker").get("title");
            if (jsonNode.isNull() || jsonNode.isEmpty()) {
                return;
            }
            String name = jsonNode.get(0).get("plain_text").asText();
            Model.ChartResponse stockPrices = getStockPrices(name);

            var result = stockPrices.getChart().getResult();
            if (result == null) {
                return;
            }
            result.stream()
                    .map(Model.Result::getMeta)
                    .filter(dMeta -> dMeta.getSymbol().equals(name))
                    .findFirst()
                    .ifPresent(meta -> {
                        var regularMarketPrice = meta.getRegularMarketPrice();
                        var currency = meta.getCurrency();
                        log.info("Current price is {}, {}, {} ", meta.getSymbol(), regularMarketPrice, meta.getCurrency());

                        createJson(Double.parseDouble(regularMarketPrice), currency, meta.getExchangeName())
                                .ifPresent(r -> integrationWebClient
                                        .patch()
                                        .uri(UPDATE_URL + pageId)
                                        .header(AUTHORIZATION, apikey)
                                        .bodyValue(r)
                                        .exchangeToMono(g -> g.bodyToMono(String.class))
                                        .block());
                    });
        });
    }

    private JsonNode fetchDatabase(String database, String apikey) {
        HttpHeaders headers = WebClientConfig.getHeaders(apikey);
        HttpEntity<String> entity = new HttpEntity<>("{}", headers);
        String url = "https://api.notion.com/v1/databases/" + database + "/query";
        return mapper.convertValue(restTemplate.exchange(url, HttpMethod.POST, entity, Map.class).getBody().get("results"), JsonNode.class);
    }

    private Model.ChartResponse getStockPrices(String name) {

        String url = String.format("https://query1.finance.yahoo.com/v8/finance/chart/%s?interval=1h&range=1h", name);

        var response = integrationWebClient.get()
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
            log.info("Error while creating json for {}", name, e);
            return Optional.empty();
        }
    }


}

