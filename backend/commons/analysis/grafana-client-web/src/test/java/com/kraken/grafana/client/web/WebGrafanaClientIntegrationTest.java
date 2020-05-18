package com.kraken.grafana.client.web;

import com.kraken.Application;
import com.kraken.config.influxdb.api.InfluxDBProperties;
import com.kraken.grafana.client.api.GrafanaAdminClient;
import com.kraken.grafana.client.api.GrafanaUser;
import com.kraken.grafana.client.api.GrafanaUserClient;
import com.kraken.grafana.client.api.GrafanaUserClientBuilder;
import com.kraken.influxdb.client.api.InfluxDBUser;
import com.kraken.tests.utils.ResourceUtils;
import com.kraken.tools.unique.id.IdGenerator;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.Instant;

import static org.mockito.BDDMockito.given;

//@Ignore("Start grafana before running")
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {Application.class})
@SpringBootTest
public class WebGrafanaClientIntegrationTest {

  @Autowired
  GrafanaAdminClient grafanaAdminClient;

  @Autowired
  GrafanaUserClientBuilder grafanaUserClientBuilder;

  @MockBean
  IdGenerator idGenerator;
  @MockBean
  InfluxDBProperties dbProperties;

  InfluxDBUser dbUser;
  GrafanaUser grafanaUser;
  Mono<GrafanaUserClient> grafanaUserClient;

  @Before
  public void setUp() {
    given(dbProperties.getUrl()).willReturn("http://localhost:8086");
    given(idGenerator.generate()).willReturn("password", "datasourceName");

//    Updated user attributes {databaseUsername=[kraken_eb274740_db1e_4c6c_8050_3f0e3073d487], databasePassword=[cfhzmjgopm], databaseName=[kraken_eb274740_db1e_4c6c_8050_3f0e3073d487], dashboardUserId=[2], dashboardEmail=[kojiro.sazaki@gmail.com], dashboardPassword=[rkapa5sfs1], dashboardDatasourceName=[eb274740-db1e-4c6c-8050-3f0e3073d487], dashboardOrgId=[2]}

    dbUser = InfluxDBUser.builder()
        .username("kraken_eb274740_db1e_4c6c_8050_3f0e3073d487")
        .password("cfhzmjgopm")
        .database("kraken_eb274740_db1e_4c6c_8050_3f0e3073d487")
        .build();
    grafanaUser = GrafanaUser.builder()
        .datasourceName("eb274740-db1e-4c6c-8050-3f0e3073d487")
        .email("kojiro.sazaki@gmail.com")
        .password("rkapa5sfs1")
        .id("2")
        .orgId("2")
        .build();

    grafanaUserClient = grafanaUserClientBuilder.grafanaUser(grafanaUser).influxDBUser(dbUser).build();
  }

  @Test
  public void shouldImportDashboard() throws IOException {
    final var dashboard = ResourceUtils.getResourceContent("grafana-gatling-dashboard-integration.json");
    final var imported = grafanaUserClient.flatMap(client -> client.importDashboard("c8m9z9pkdq", "Title", Instant.now().toEpochMilli(), dashboard)).block();
    System.out.println(imported);
  }

  @Test
  public void shouldCreateUser() {
    final var user = grafanaAdminClient.createUser("userId", "test1@octoperf.com").block();
    System.out.println(user);
  }

  @Test
  public void shouldCreateDatasource() {
    grafanaUserClient.flatMap(GrafanaUserClient::createDatasource).block();
  }


  @Test
  public void shouldDeleteUser() {
    grafanaAdminClient.deleteUser("e9a8b7ec-5b94-4155-8ca3-77c754d31322").block();
  }
}
