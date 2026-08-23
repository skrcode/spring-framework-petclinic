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
package org.springframework.samples.petclinic.util;

import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test class for {@link CallMonitoringAspect}
 */
@ExtendWith(MockitoExtension.class)
class CallMonitoringAspectTests {

    @Mock
    private ProceedingJoinPoint joinPoint;

    private CallMonitoringAspect aspect;

    @BeforeEach
    void setup() {
        aspect = new CallMonitoringAspect();
    }

    @Test
    void shouldReturnResultAndRecordCallWhenEnabled() throws Throwable {
        Object expected = "result";
        when(joinPoint.proceed()).thenReturn(expected);

        Object actual = aspect.invoke(joinPoint);

        assertSame(expected, actual);
        assertEquals(1, aspect.getCallCount());
        verify(joinPoint, times(1)).proceed();
    }

    @Test
    void shouldPropagateExceptionAndStillRecordCallWhenEnabled() throws Throwable {
        RuntimeException failure = new RuntimeException("boom");
        when(joinPoint.proceed()).thenThrow(failure);

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> aspect.invoke(joinPoint));

        assertSame(failure, thrown);
        assertEquals(1, aspect.getCallCount());
    }

    @Test
    void shouldPassThroughWithoutRecordingWhenDisabled() throws Throwable {
        Object expected = "result";
        aspect.setEnabled(false);
        when(joinPoint.proceed()).thenReturn(expected);

        Object actual = aspect.invoke(joinPoint);

        assertSame(expected, actual);
        assertEquals(0, aspect.getCallCount());
        assertEquals(0, aspect.getCallTime());
        verify(joinPoint, times(1)).proceed();
    }

    @Test
    void shouldResetCallCountAndCallTime() throws Throwable {
        when(joinPoint.proceed()).thenReturn("result");
        aspect.invoke(joinPoint);
        aspect.invoke(joinPoint);
        assertEquals(2, aspect.getCallCount());

        aspect.reset();

        assertEquals(0, aspect.getCallCount());
        assertEquals(0, aspect.getCallTime());
    }

    @Test
    void shouldReturnZeroCallTimeWhenNoCallsRecorded() {
        assertEquals(0, aspect.getCallTime());
    }

    @Test
    void shouldComputeNonNegativeAverageCallTimeAfterInvocations() throws Throwable {
        when(joinPoint.proceed()).thenReturn("result");

        aspect.invoke(joinPoint);
        aspect.invoke(joinPoint);
        aspect.invoke(joinPoint);

        assertEquals(3, aspect.getCallCount());
        assertTrue(aspect.getCallTime() >= 0);
    }

    @Test
    void shouldBeEnabledByDefault() {
        assertTrue(aspect.isEnabled());
    }

}
