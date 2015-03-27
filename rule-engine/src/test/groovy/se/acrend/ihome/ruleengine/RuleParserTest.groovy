package se.acrend.ihome.ruleengine

import org.junit.Before
import org.junit.Test

import static groovy.test.GroovyAssert.*

/**
 *
 */
class RuleParserTest {

    RuleParser parser

    @Before
    public void setUp() throws Exception {
        parser = new RuleParser()
    }

    @Test
    void testRuleParse() {
        def rule = parser.readRule(new File("src/test/resources/rules/test1.grule"))
        assertEquals("test-2", rule.name)
        rule.ruleLogic.call()
    }

}
