package cz.cesnet.shongo.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;

@Slf4j
@Configuration
@ComponentScan
@ActiveProfiles("test")
@RequiredArgsConstructor
public class TestConfig
{
}
