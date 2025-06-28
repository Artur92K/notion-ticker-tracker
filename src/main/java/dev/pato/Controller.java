package dev.pato;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class Controller {

    private final MyIntegration myIntegration;

    @GetMapping("/")
    public ResponseEntity<String> test(@RequestParam String apikey, @RequestParam String database) {
        log.info("Started");
        myIntegration.update(apikey, database);
        log.info("Finished");
        return ResponseEntity.ok("OK");
    }

}
