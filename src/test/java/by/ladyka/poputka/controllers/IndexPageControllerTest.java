package by.ladyka.poputka.controllers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

class IndexPageControllerTest {
    IndexPageController solution = new IndexPageController();

    @Test
    void getIndexPage() {
        Map<String, Object> index = solution.index(null);
        Assertions.assertEquals(index.get("status"), "OK");
    }

}
