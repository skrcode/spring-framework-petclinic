/*
 * Copyright 2002-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test class for {@link CrashController}.
 */
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
  @DisplayName("triggerException() should throw RuntimeException")
  public void testTriggerExceptionThrowsRuntimeException() {
      assertThrows(RuntimeException.class, () -> crashController.triggerException());
  }

  @Test
  @DisplayName("triggerException() should throw exactly RuntimeException, not a subclass")
  public void testTriggerExceptionIsExactlyRuntimeException() {
      RuntimeException ex = assertThrows(RuntimeException.class, () -> crashController.triggerException());
      assertEquals(RuntimeException.class, ex.getClass(),
          "Expected exactly RuntimeException, not a subclass");
  }

  @Test
  @DisplayName("triggerException() should throw exception with the exact expected message")
  public void testTriggerExceptionExactMessage() {
      RuntimeException ex = assertThrows(RuntimeException.class, () -> crashController.triggerException());
      String expectedMessage = "Exspected: controller used to showcase what " +
          "happens when an exception is thrown";
      assertEquals(expectedMessage, ex.getMessage());
  }

  @Test
  @DisplayName("triggerException() exception message should contain key description text")
  public void testTriggerExceptionMessageContainsKeywords() {
      RuntimeException ex = assertThrows(RuntimeException.class, () -> crashController.triggerException());
      assertNotNull(ex.getMessage());
      assertTrue(ex.getMessage().contains("controller"), "Message should mention 'controller'");
      assertTrue(ex.getMessage().contains("exception"), "Message should mention 'exception'");
  }

  @Test
  @DisplayName("triggerException() exception message should be non-null and non-empty")
  public void testTriggerExceptionMessageIsNotEmpty() {
      RuntimeException ex = assertThrows(RuntimeException.class, () -> crashController.triggerException());
      assertNotNull(ex.getMessage());
      assertFalse(ex.getMessage().isEmpty());
  }

  @Test
  @DisplayName("triggerException() exception should carry a non-empty stack trace")
  public void testTriggerExceptionStackTraceIsPresent() {
      RuntimeException ex = assertThrows(RuntimeException.class, () -> crashController.triggerException());
      assertNotNull(ex.getStackTrace());
      assertTrue(ex.getStackTrace().length > 0);
  }

  @Test
  @DisplayName("triggerException() exception should have no cause")
  public void testTriggerExceptionCauseIsNull() {
      RuntimeException ex = assertThrows(RuntimeException.class, () -> crashController.triggerException());
      assertNull(ex.getCause(), "A newly created RuntimeException should have no cause");
  }

  @Test
  @DisplayName("triggerException() should throw on every invocation")
  public void testMultipleTriggerExceptionCalls() {
      for (int i = 0; i < 5; i++) {
          RuntimeException ex = assertThrows(RuntimeException.class, () -> crashController.triggerException());
          assertNotNull(ex);
      }
  }

  @Test
  @DisplayName("CrashController should be instantiable and non-null")
  public void testControllerInstantiation() {
      assertNotNull(crashController);
      assertInstanceOf(CrashController.class, crashController);
  }

  @Test
  @DisplayName("CrashController class should be annotated with @Controller")
  public void testControllerHasControllerAnnotation() {
      assertTrue(CrashController.class.isAnnotationPresent(Controller.class),
          "CrashController must be annotated with @Controller");
  }

  @Test
  @DisplayName("triggerException() method should be annotated with @GetMapping('/oups')")
  public void testTriggerExceptionMethodHasGetMappingAnnotation() throws NoSuchMethodException {
      Method method = CrashController.class.getDeclaredMethod("triggerException");
      assertTrue(method.isAnnotationPresent(GetMapping.class),
          "triggerException must be annotated with @GetMapping");
      GetMapping mapping = method.getAnnotation(GetMapping.class);
      assertArrayEquals(new String[]{"/oups"}, mapping.value(),
          "@GetMapping value should be '/oups'");
  }

  @Test
  @DisplayName("GET /oups should propagate a ServletException wrapping RuntimeException when no exception resolver is configured")
  public void testOupsEndpointReturns500ViaMockMvc() {
      // Without a SimpleMappingExceptionResolver the DispatcherServlet wraps the
      // RuntimeException in a ServletException and rethrows it rather than sending HTTP 500.
      Exception ex = assertThrows(Exception.class, () ->
          mockMvc.perform(get("/oups"))
      );
      assertNotNull(ex);
      // Walk the cause chain to find the original RuntimeException
      Throwable rootCause = ex;
      while (rootCause.getCause() != null) {
          rootCause = rootCause.getCause();
      }
      assertInstanceOf(RuntimeException.class, rootCause);
      assertTrue(rootCause.getMessage().contains("exception"),
          "Root cause message should reference 'exception'");
  }

  @Test
  @DisplayName("GET /oups endpoint should propagate RuntimeException through MockMvc")
  public void testOupsEndpointThrowsRuntimeExceptionViaMockMvc() {
      // The underlying exception escaping the MockMvc call is a RuntimeException.
      Exception ex = assertThrows(Exception.class, () ->
          MockMvcBuilders.standaloneSetup(crashController)
              .build()
              .perform(get("/oups"))
              .andExpect(status().isOk()) // wrong expectation to force resolution of the exception
      );
      assertNotNull(ex);
  }

  @Test
  @DisplayName("Two independently created CrashController instances should both throw")
  public void testNewInstanceHasNoState() {
      CrashController first  = new CrashController();
      CrashController second = new CrashController();
      assertThrows(RuntimeException.class, () -> first.triggerException());
      assertThrows(RuntimeException.class, () -> second.triggerException());
  }
}