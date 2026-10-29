package br.com.impactoverde.testes.executor;

import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * Ponto de entrada: o JUnit 5 (Platform) descobre esta suíte e delega ao engine do Cucumber.
 * Os plugins, o glue e a estratégia de nomes ficam em src/test/resources/junit-platform.properties.
 */
@Suite
@SuiteDisplayName("Impacto Verde ESG - Testes de API (BDD)")
@IncludeEngines("cucumber")
@SelectPackages("features")
public class ExecutorCucumberTest {
}
