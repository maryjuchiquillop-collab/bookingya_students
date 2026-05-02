package com.project.bookingya.bdd.runner;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import net.serenitybdd.cucumber.CucumberWithSerenity;
import org.junit.runner.RunWith;

@RunWith(CucumberWithSerenity.class)
@CucumberOptions(
    features = "classpath:features",
    glue = "com.project.bookingya.bdd.steps",
    plugin = {"pretty", "html:target/cucumber-reports/report.html"}
)
public class CucumberRunnerTest {
}