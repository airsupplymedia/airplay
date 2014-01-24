package de.airsupply.airplay.web.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import de.airsupply.airplay.core.config.ApplicationConfiguration;

@Configuration
@Import(ApplicationConfiguration.class)
@EnableWebMvc
@ComponentScan("de.airsupply.airplay.web.controller")
public class WebConfiguration extends WebMvcConfigurerAdapter {
}