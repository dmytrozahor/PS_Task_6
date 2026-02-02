package com.dmytrozah.profitsoft.email;

import com.dmytrozah.profitsoft.email.integration.EmailControllerTest;
import com.dmytrozah.profitsoft.email.integration.EmailServiceIntegrationTest;
import com.dmytrozah.profitsoft.email.service.EmailMessageListenerTest;
import com.dmytrozah.profitsoft.email.service.EmailQueryServiceTest;
import com.dmytrozah.profitsoft.email.service.EmailServiceTest;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName("Email Service - Overall Test Suite")
@SelectClasses({
        // Unit tests

        EmailServiceTest.class,
        EmailQueryServiceTest.class,
        EmailMessageListenerTest.class,

        // Integration tests

        EmailServiceIntegrationTest.class,
        EmailControllerTest.class
})
public class OverallTestSuite { }