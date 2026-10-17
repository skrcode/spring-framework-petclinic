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

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
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
    void invokeShouldReturnResultAndRecordCallWhenEnabled() throws Throwable {
        assertTrue(aspect.isEnabled());
        when(joinPoint.proceed()).thenReturn("result");

        Object result = aspect.invoke(joinPoint);

        assertEquals("result", result);
        assertEquals(1, aspect.getCallCount());
        verify(joinPoint, times(1)).proceed();
    }

    @Test
    void invokeShouldPropagateExceptionAndStillRecordCallWhenEnabled() throws Throwable {
        RuntimeException failure = new RuntimeException("boom");
        when(joinPoint.proceed()).thenThrow(failure);

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> aspect.invoke(joinPoint));

        assertSame(failure, thrown);
        assertEquals(1, aspect.getCallCount());
    }

    @Test
    void invokeShouldBypassMonitoringWhenDisabled() throws Throwable {
        aspect.setEnabled(false);
        when(joinPoint.proceed()).thenReturn("result");

        Object result = aspect.invoke(joinPoint);

        assertEquals("result", result);
        assertEquals(0, aspect.getCallCount());
        assertEquals(0, aspect.getCallTime());
        verify(joinPoint, never()).toShortString();
    }

    @Test
    void resetShouldClearCallCountAndCallTime() throws Throwable {
        when(joinPoint.proceed()).thenReturn("result");
        aspect.invoke(joinPoint);
        assertEquals(1, aspect.getCallCount());

        aspect.reset();

        assertEquals(0, aspect.getCallCount());
        assertEquals(0, aspect.getCallTime());
    }

    @Test
    void getCallTimeShouldReturnZeroWhenNoCallsRecorded() {
        assertEquals(0, aspect.getCallTime());
    }

    @Test
    void getCallTimeShouldAverageAccumulatedTimeAcrossCalls() throws Exception {
        setPrivateField("callCount", 4);
        setPrivateField("accumulatedCallTime", 20L);

        assertEquals(5, aspect.getCallTime());
    }

    private void setPrivateField(String name, Object value) throws Exception {
        Field field = CallMonitoringAspect.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(aspect, value);
    }

}
