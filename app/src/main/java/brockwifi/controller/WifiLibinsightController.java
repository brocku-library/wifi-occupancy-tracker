package brockwifi.controller;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import brockwifi.domain.PatronCount;
import brockwifi.repository.PatronCountRepository;

@Controller
@RequestMapping("/busylib")
@CrossOrigin(origins = "*") // replace * with the IP address of the machine that will have the Python script running, or leave it as * if you want to allow all origins.
public class WifiLibinsightController {

    Logger logger = LoggerFactory.getLogger(WifiLibinsightController.class);

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private PatronCountRepository repository;

    @GetMapping
    String view(ModelMap model) throws JsonProcessingException {
        Cache cache = cacheManager.getCache("response");

        try {
            model.put("data", cache.get("jsonStr").get());
        } catch (Exception e) {
            logger.error("No data has been received since the application has been restarted.");
        }

        return "wifi";
    }

    @PostMapping
    ResponseEntity<?> store(@RequestBody String jsonString) {
        String jsonResponse = URLDecoder.decode(jsonString, StandardCharsets.UTF_8)
                .replace("jsonString=", "");

        // Parse JSON to List of PatronCount and save
        parseAndStore(jsonResponse);

        jsonResponse = String.format("{\"payload\": { \"records\": %s}}", jsonResponse);

        Cache cache = cacheManager.getCache("response");
        cache.put("jsonStr", jsonResponse);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    private void parseAndStore(String jsonResponse) {
        ObjectMapper mapper = new ObjectMapper();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        mapper.setDateFormat(dateFormat);

        try {
            List<PatronCount> patronCountList = Arrays.asList(mapper.readValue(jsonResponse, PatronCount[].class));
            repository.saveAll(patronCountList);

        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
        }
    }

    @CacheEvict(value = "responseLib", allEntries = true)
    @Scheduled(fixedRateString = "${caching.ttl.wifi}")
    public void emptyCache() {
        logger.debug("Libinsight data cache cleared");
    }
}
