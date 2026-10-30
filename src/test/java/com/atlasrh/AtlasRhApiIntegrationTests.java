package com.atlasrh;

import com.atlasrh.dto.department.DepartmentRequest;
import com.atlasrh.dto.department.DepartmentResponse;
import com.atlasrh.dto.employee.EmployeeRequest;
import com.atlasrh.dto.employee.EmployeeResponse;
import com.atlasrh.dto.leave.LeaveRequestInput;
import com.atlasrh.dto.leave.LeaveRequestResponse;
import com.atlasrh.dto.leave.LeaveTypeResponse;
import com.atlasrh.entity.LeaveStatus;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AtlasRhApiIntegrationTests {
    @LocalServerPort int port;
    private final HttpClient client = HttpClient.newHttpClient();
    private final com.fasterxml.jackson.databind.ObjectMapper json = JsonMapper.builder().findAndAddModules().build();

    @Test
    void rhEmployeeAndManagerCanCompleteLeaveWorkflow() throws Exception {
        String root = "http://localhost:" + port + "/api";
        String suffix = UUID.randomUUID().toString().substring(0, 8);

        HttpResponse<String> departmentHttp = send("POST", root + "/departments",
                new DepartmentRequest("QA " + suffix, "Integration test department"));
        assertEquals(201, departmentHttp.statusCode());
        Long departmentId = json.readValue(departmentHttp.body(), DepartmentResponse.class).id();

        EmployeeResponse manager = post(root + "/employees",
                new EmployeeRequest("QA-M-" + suffix, "Mina", "Manager", "manager." + suffix + "@example.test",
                        "Manager", LocalDate.of(2022, 1, 10), departmentId, null), EmployeeResponse.class);
        EmployeeResponse employee = post(root + "/employees",
                new EmployeeRequest("QA-E-" + suffix, "Adam", "Employee", "employee." + suffix + "@example.test",
                        "Analyst", LocalDate.of(2023, 5, 15), departmentId, manager.id()), EmployeeResponse.class);
        assertEquals(manager.id(), employee.managerId());

        HttpResponse<String> teamResponse = send("GET", root + "/employees/" + manager.id() + "/team", null);
        assertEquals(200, teamResponse.statusCode());
        List<EmployeeResponse> team = json.convertValue(json.readTree(teamResponse.body()).path("content"), new TypeReference<>() {});
        assertTrue(team.stream().anyMatch(member -> member.id().equals(employee.id())));

        List<LeaveTypeResponse> types = getList(root + "/leave-types", new TypeReference<>() {});
        Long annualId = types.stream().filter(type -> type.code().name().equals("ANNUAL")).findFirst().orElseThrow().id();
        LocalDate monday = nextMonday();
        LeaveRequestResponse submitted = post(root + "/employees/" + employee.id() + "/leave-requests",
                new LeaveRequestInput(annualId, monday, monday.plusDays(1), "Integration test"), LeaveRequestResponse.class);
        assertEquals(LeaveStatus.EN_ATTENTE, submitted.status());

        List<LeaveRequestResponse> pending = getList(root + "/managers/" + manager.id() + "/leave-requests/pending", new TypeReference<>() {});
        assertTrue(pending.stream().anyMatch(request -> request.id().equals(submitted.id())));

        LeaveRequestResponse decision = patch(root + "/managers/" + manager.id() + "/leave-requests/" + submitted.id() + "/decision",
                Map.of("approved", true, "comment", "Approved for integration test"), LeaveRequestResponse.class);
        assertEquals(LeaveStatus.APPROUVEE, decision.status());

        HttpResponse<String> openApi = send("GET", "http://localhost:" + port + "/v3/api-docs", null);
        assertEquals(200, openApi.statusCode());
        assertTrue(openApi.body().contains("Create a leave request"));
    }

    private <T> T post(String url, Object body, Class<T> type) throws Exception {
        HttpResponse<String> response = send("POST", url, body);
        assertEquals(201, response.statusCode(), response.body());
        return json.readValue(response.body(), type);
    }

    private <T> T patch(String url, Object body, Class<T> type) throws Exception {
        HttpResponse<String> response = send("PATCH", url, body);
        assertEquals(200, response.statusCode(), response.body());
        return json.readValue(response.body(), type);
    }

    private <T> List<T> getList(String url, TypeReference<List<T>> type) throws Exception {
        HttpResponse<String> response = send("GET", url, null);
        assertEquals(200, response.statusCode(), response.body());
        return json.readValue(response.body(), type);
    }

    private HttpResponse<String> send(String method, String url, Object body) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url)).header("Content-Type", "application/json");
        if (body == null) builder.method(method, HttpRequest.BodyPublishers.noBody());
        else builder.method(method, HttpRequest.BodyPublishers.ofString(json.writeValueAsString(body)));
        return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private LocalDate nextMonday() {
        LocalDate date = LocalDate.now().plusDays(1);
        while (date.getDayOfWeek() != DayOfWeek.MONDAY) date = date.plusDays(1);
        return date;
    }
}
