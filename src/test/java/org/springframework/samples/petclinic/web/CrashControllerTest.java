package org.springframework.samples.petclinic.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("CrashController Tests")
public class CrashControllerTest {

    private CrashController crashController;

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        crashController = new CrashController();
        mockMvc = MockMvcBuilders.standaloneSetup(crashController).build();
    }

    @Test
    @DisplayName("triggerException should throw RuntimeException")
    public void testTriggerExceptionThrowsRuntimeException() {
        assertThrows(RuntimeException.class, () -> {
            crashController.triggerException();
        });
    }

    @Test
    @DisplayName("triggerException should throw exception with correct message")
    public void testTriggerExceptionWithCorrectMessage() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            crashController.triggerException();
        });
        assertTrue(exception.getMessage().contains("Exspected: controller used to showcase what happens when an exception is thrown"));
    }

    @Test
    @DisplayName("triggerException message should contain specific text")
    public void testTriggerExceptionMessageContent() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            crashController.triggerException();
        });
        assertNotNull(exception.getMessage());
        assertFalse(exception.getMessage().isEmpty());
    }

    @Test
    @DisplayName("CrashController should be instantiable")
    public void testControllerInstantiation() {
        assertNotNull(crashController);
        assertTrue(crashController instanceof CrashController);
    }

    @Test
    @DisplayName("triggerException should throw exception on multiple calls")
    public void testMultipleTriggerExceptionCalls() {
        for (int i = 0; i < 3; i++) {
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                crashController.triggerException();
            });
            assertNotNull(exception);
        }
    }

    @Test
    @DisplayName("triggerException exception should not be null")
    public void testExceptionIsNotNull() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            crashController.triggerException();
        });
        assertNotNull(exception);
        assertNotNull(exception.getMessage());
    }

    @Test
    @DisplayName("triggerException exception should have stack trace")
    public void testExceptionStackTraceIsPresent() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            crashController.triggerException();
        });
        StackTraceElement[] stackTrace = exception.getStackTrace();
        assertTrue(stackTrace.length > 0);
    }
}
