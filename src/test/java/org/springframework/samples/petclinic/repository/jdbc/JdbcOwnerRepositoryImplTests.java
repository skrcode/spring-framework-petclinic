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
package org.springframework.samples.petclinic.repository.jdbc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sql.DataSource;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.samples.petclinic.model.Owner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Regression test guarding against the re-introduction of a per-owner "N+1" query for
 * pet types when loading pets/visits for a batch of {@link Owner Owners} returned by
 * {@link JdbcOwnerRepositoryImpl#findByLastName(String)}.
 */
class JdbcOwnerRepositoryImplTests {

    private DataSource plainDataSource;

    private AtomicInteger typesQueryCount;

    private JdbcOwnerRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl("jdbc:h2:mem:jdbcOwnerRepositoryImplTests;DB_CLOSE_DELAY=-1");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        this.plainDataSource = dataSource;

        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("db/h2/schema.sql"));
        populator.addScript(new ClassPathResource("db/h2/data.sql"));
        populator.execute(dataSource);

        this.typesQueryCount = new AtomicInteger();
        DataSource countingDataSource = countingDataSource(this.plainDataSource, this.typesQueryCount);
        this.repository = new JdbcOwnerRepositoryImpl(countingDataSource, JdbcClient.create(countingDataSource));
    }

    @AfterEach
    void tearDown() throws Exception {
        try (Connection connection = this.plainDataSource.getConnection()) {
            connection.createStatement().execute("SHUTDOWN");
        }
    }

    @Test
    void findByLastNameLoadsPetTypesOnceForMultipleOwners() {
        Collection<Owner> owners = this.repository.findByLastName("Davis");

        assertThat(owners).hasSize(2);
        assertThat(owners).allSatisfy(owner -> assertThat(owner.getPets()).isNotEmpty());
        owners.forEach(owner -> owner.getPets().forEach(pet -> assertThat(pet.getType()).isNotNull()));

        assertThat(this.typesQueryCount)
            .as("the pet types table should be queried once per batch, not once per owner")
            .hasValue(1);
    }

    /**
     * Wraps the given {@link DataSource} so that every {@link Connection#prepareStatement(String)} call
     * whose SQL text targets the {@code types} table is counted in {@code counter}.
     */
    private static DataSource countingDataSource(DataSource target, AtomicInteger counter) {
        InvocationHandler handler = (proxy, method, args) -> {
            if ("getConnection".equals(method.getName())) {
                Connection realConnection = (Connection) method.invoke(target, args);
                return countingConnection(realConnection, counter);
            }
            return method.invoke(target, args);
        };
        return (DataSource) Proxy.newProxyInstance(JdbcOwnerRepositoryImplTests.class.getClassLoader(),
            new Class<?>[]{DataSource.class}, handler);
    }

    private static Connection countingConnection(Connection target, AtomicInteger counter) {
        InvocationHandler handler = (proxy, method, args) -> {
            if ("prepareStatement".equals(method.getName()) && args != null && args.length > 0
                && args[0] instanceof String sql && sql.toUpperCase().contains("FROM TYPES")) {
                counter.incrementAndGet();
            }
            try {
                return method.invoke(target, args);
            } catch (InvocationTargetException ex) {
                throw ex.getCause();
            }
        };
        return (Connection) Proxy.newProxyInstance(JdbcOwnerRepositoryImplTests.class.getClassLoader(),
            new Class<?>[]{Connection.class}, handler);
    }

}
