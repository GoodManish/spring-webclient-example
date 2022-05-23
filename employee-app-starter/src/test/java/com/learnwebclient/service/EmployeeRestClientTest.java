package com.learnwebclient.service;

import com.learnwebclient.dto.Employee;
import com.learnwebclient.service.client.exception.ClientDataException;
import com.learnwebclient.service.client.exception.EmployeeServiceException;
import org.junit.jupiter.api.Test;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class EmployeeRestClientTest {

    private String baseUrl = "http://localhost:8081/employeeservice";
    private WebClient webClient = WebClient.create(baseUrl);

    EmployeeRestClient employeeRestClient = new EmployeeRestClient(webClient);

    @Test
    void retrieveAllEmployee(){
        List<Employee> employees = employeeRestClient.getAllEmployees();
        employees.forEach(System.out::println);
        Assert.notEmpty(employees);
        assertTrue(employees.size()>0);
        assertEquals(4, employees.size());
    }

    @Test
    void retrieveEmployeById_Found(){
        Employee employee = employeeRestClient.getEmployeeById(1);
        System.out.println(employee);
        assertTrue(employee!=null);
        assertTrue(employee.getFirstName().equalsIgnoreCase("Chris"));
        assertTrue(employee.getLastName().equalsIgnoreCase("Evans"));
    }

    @Test
    void retrieveEmployeById_notFound(){
        int employeeId = 10;
        assertThrows(WebClientResponseException.class, ()-> employeeRestClient.getEmployeeById(employeeId));
    }
    @Test
    void retrieveEmployeeById_with_custom_4xx_error_handler(){
        int employeeId = 10;
        assertThrows(ClientDataException.class, ()-> employeeRestClient.getEmployeeById_custom_error_handler(employeeId));
    }
    @Test
    void retrieveEmployeeById_with_custom_5xx_error_handler(){
        int employeeId = 10;
        assertThrows(EmployeeServiceException.class, ()-> employeeRestClient.errorEndpoint());
    }
    @Test
    void retrieveEmployeByName_Found(){
        String employeeName = "Chris";
        List<Employee> employees = employeeRestClient.getEmployeeByName(employeeName);
        System.out.println(employees);
        assertTrue(employees!=null);
        assertTrue(employees.stream()
                .anyMatch(emp-> emp.getFirstName().equalsIgnoreCase("chris")));
        assertTrue(employees.stream()
                .anyMatch(emp-> emp.getLastName().equalsIgnoreCase("evans")));
    }

    @Test
    void retrieveEmployeByName_notFound(){
        String employeeName = "ABC";
        assertThrows(WebClientResponseException.class, ()->employeeRestClient.getEmployeeByName(employeeName));
    }

    @Test
    void addEmployee_positive(){
        Employee e = Employee.builder()
                .firstName("Rakesh").lastName("Tiwari").age(30).gender("male").role("Teller").id(null)
                .build();
        Employee employee = employeeRestClient.addEmployee(e);
        System.out.println(employee);

        assertTrue(employee!=null);
        assertTrue(employee.getId()!=null);
        assertEquals(employee.getFirstName(), "Rakesh");
    }
    @Test
    void addEmployee_negetive(){
        Employee e = Employee.builder()
                .firstName(null).lastName("Tiwari").age(30).gender("male").role("Teller").id(null)
                .build();
        assertThrows(WebClientResponseException.class, ()->employeeRestClient.addEmployee(e));
    }

    @Test
    void updateEmployee_positive(){
        Employee emp = Employee.builder()
                .age(100).id(null)
                .build();
        Employee employee = employeeRestClient.updateEmployee(11, emp);
        System.out.println(employee);

        assertEquals(employee.getAge(), 100);

    }

    @Test
    void updateEmployee_badRequest(){
        Employee emp = Employee.builder()
                .age(100).id(null)
                .build();
        assertThrows(WebClientResponseException.class, ()->employeeRestClient.updateEmployee(999, emp));

    }

    @Test
    void deleteEmployee_Ok(){
        Employee e = Employee.builder()
                .firstName("Rakesh").lastName("Tiwari").age(30).gender("male").role("Teller").id(null)
                .build();
        Employee actual = employeeRestClient.addEmployee(e);
        System.out.println("Actual - "+actual);
        String expected = employeeRestClient.deleteEmployee(actual.getId());
        assertEquals(expected, "Employee deleted successfully");

    }
}
