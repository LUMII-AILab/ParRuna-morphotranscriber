package lv.ailab.parruna.transcriber;

import org.junit.Test;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class ApplyParRunaRulesTest {

    @Test
    public void csvReadTest() throws IOException {
        ApplyParRunaRules a = new ApplyParRunaRules();
    }


    @Test
    public void rulesApplicationTest() throws IOException {
        ApplyParRunaRules a = new ApplyParRunaRules();
        a.applyRules("slazds");
        a.applyRules("mežs");
        a.applyRules("kooperatīvs");
//        assertEquals("slasc", a.applyRules("slazds"));
    }
}