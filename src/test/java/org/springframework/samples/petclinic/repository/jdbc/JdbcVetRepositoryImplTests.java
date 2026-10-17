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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.DelegatingDataSource;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.samples.petclinic.model.Vet;
import org.springframework.samples.petclinic.util.EntityUtils;

import javax.sql.DataSource;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link JdbcVetRepositoryImpl}, focused on the {@link JdbcVetRepositoryImpl#findAll()}
 * query-count performance invariant: the number of SQL round trips must stay constant as the number
 * of vets grows, rather than issuing one extra query per vet (the classic N+1 problem).
 */
class JdbcVetRepositoryImplTests {

    private EmbeddedDatabase embeddedDatabase;

    private AtomicInteger statementCount;

    private JdbcVetRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        this.embeddedDatabase = new EmbeddedDatabaseBuilder()
            .setType(EmbeddedDatabaseType.H2)
            .addScript("classpath:db/h2/schema.sql")
            .addScript("classpath:db/h2/data.sql")
            .build();
        this.statementCount = new AtomicInteger();
        DataSource countingDataSource = new CountingDataSource(this.embeddedDatabase, this.statementCount);
        this.repository = new JdbcVetRepositoryImpl(JdbcClient.create(countingDataSource));
    }

    @AfterEach
    void tearDown() {
        this.embeddedDatabase.shutdown();
    }

    @Test
    void findAllReturnsVetsWithTheirSpecialties() {
        Collection<Vet> vets = this.repository.findAll();

        Vet vet = EntityUtils.getById(vets, Vet.class, 3);
        assertThat(vet.getLastName()).isEqualTo("Douglas");
        assertThat(vet.getNrOfSpecialties()).isEqualTo(2);
        assertThat(vet.getSpecialties().get(0).getName()).isEqualTo("dentistry");
        assertThat(vet.getSpecialties().get(1).getName()).isEqualTo("surgery");
    }

    @Test
    void findAllIssuesAConstantNumberOfQueriesRegardlessOfVetCount() {
        this.repository.findAll();
        int queriesForSixVets = this.statementCount.get();

        // Fixture data.sql already has 6 vets; add several more vets (with specialties)
        // to prove the query count does not grow with the number of vets (no N+1).
        JdbcClient rawClient = JdbcClient.create(this.embeddedDatabase);
        for (int i = 0; i < 20; i++) {
            rawClient.sql("INSERT INTO vets VALUES (default, 'First' || :i, 'Last' || :i)")
                .param("i", i)
                .update();
        }
        rawClient.sql("INSERT INTO vet_specialties SELECT v.id, 1 FROM vets v WHERE v.first_name LIKE 'First%'")
            .update();

        this.statementCount.set(0);
        Collection<Vet> vets = this.repository.findAll();

        assertThat(vets).hasSize(26);
        assertThat(this.statementCount.get())
            .as("query count must stay constant as vet count grows")
            .isEqualTo(queriesForSixVets);
    }

    /**
     * Minimal counting {@link DataSource} that tracks how many JDBC statements/prepared statements
     * are created across all connections, used to prove the absence of per-row (N+1) query patterns.
     */
    private static final class CountingDataSource extends DelegatingDataSource {

        private final AtomicInteger statementCount;

        CountingDataSource(DataSource targetDataSource, AtomicInteger statementCount) {
            super(targetDataSource);
            this.statementCount = statementCount;
        }

        @Override
        public Connection getConnection() throws SQLException {
            return countingConnection(super.getConnection());
        }

        private Connection countingConnection(Connection target) {
            InvocationHandler handler = (proxy, method, args) -> {
                if (method.getName().equals("prepareStatement") || method.getName().equals("createStatement")) {
                    statementCount.incrementAndGet();
                }
                try {
                    return method.invoke(target, args);
                } catch (java.lang.reflect.InvocationTargetException ex) {
                    throw ex.getCause();
                }
            };
            return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(), new Class<?>[]{Connection.class}, handler);
        }
    }
}
