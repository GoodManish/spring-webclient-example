package com.learnwebclient.service;

import com.learnwebclient.dto.Employee;
import com.learnwebclient.service.client.exception.ClientDataException;
import com.learnwebclient.service.client.exception.EmployeeServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.List;

import static com.learnwebclient.constants.EmployeeConstants.*;

@Slf4j
public class EmployeeRestClient {

    private final WebClient webClient;

    public EmployeeRestClient(WebClient webClient) {
        this.webClient = webClient;
    }

    // http://localhost:8081/employeeservice/v1/allEmployees
    public List<Employee> getAllEmployees(){
        return webClient.get()
                .uri(GET_ALL_EMPLOYEE_V1)
                .retrieve()
                .bodyToFlux(Employee.class)
                .collectList()
                .block();

    }

    //http://localhost:8081/employeeservice/v1/employee/error
    public String errorEndpoint(){
        return webClient.get()
                .uri(ERROR_ENDPOINT)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, clientResponse -> handle4xxError(clientResponse))
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> handle5xxError(clientResponse))
                .bodyToMono(String.class)
                .block();

    }

    // http://localhost:8081/employeeservice/v1/employee/{id}
    public Employee getEmployeeById(Integer employeeId){
        try {
            return webClient.get()
                    .uri(EMPLOYEE_BY_ID_V1,employeeId)
                    .retrieve()
                    .bodyToMono(Employee.class)
                    .block();
        } catch (WebClientResponseException e) {
            log.error("Error Response Code: {} and body: {}",e.getStatusCode(),e.getResponseBodyAsString());
            log.error("WebClientResponseException in getEmployeeById()", e);
            throw e;
        }catch (Exception e) {
            log.error("Exception in getEmployeeById()",e);
            throw e;
        }

    }
    public Employee getEmployeeById_custom_error_handler(Integer employeeId){

        return webClient.get()
                .uri(EMPLOYEE_BY_ID_V1, employeeId)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, clientResponse -> handle4xxError(clientResponse))
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> handle5xxError(clientResponse))
                .bodyToMono(Employee.class)
                .block();

    }

    private Mono<? extends Throwable> handle5xxError(ClientResponse clientResponse) {
        Mono<String> errMsg = clientResponse.bodyToMono(String.class);
        return errMsg.flatMap(msg -> {
            log.error("Response StatusCode is: {} // error message is : {}", clientResponse.rawStatusCode(), msg);
            throw new EmployeeServiceException(msg);
        });
    }

    private Mono<? extends Throwable> handle4xxError(ClientResponse clientResponse) {
        Mono<String> errMsg = clientResponse.bodyToMono(String.class);
        return errMsg.flatMap(msg -> {
            log.error("Response StatusCode is: {} // error message is : {}", clientResponse.rawStatusCode(), msg);
            throw new ClientDataException(msg);
        });

    }

    // http://localhost:8081/employeeservice/v1/employeeName?employee_name=Evans
    public List<Employee> getEmployeeByName(String employeeName){
        try {
            String url = UriComponentsBuilder.fromUriString(EMPLOYEE_BY_NAME_V1)
                    .queryParam("employee_name", employeeName)
                    .build()
                    .toUriString();
            return webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToFlux(Employee.class)
                    .collectList()
                    .block();
        } catch (WebClientResponseException e) {
            log.error("Error Response Code: {} and body: {}",e.getStatusCode(),e.getResponseBodyAsString());
            log.error("WebClientResponseException in getEmployeeByName()", e);
            throw e;
        }catch (Exception e) {
            log.error("Exception in getEmployeeByName()",e);
            throw e;
        }

    }
    //http://localhost:8081/employeeservice/v1/employee
    public Employee addEmployee(Employee employee){
        try {
            return webClient.post()
                    .uri(ADD_EMPLOYEE_V1)
                    .syncBody(employee)
                    .retrieve()
                    .bodyToMono(Employee.class)
                    .block();
        } catch (WebClientResponseException e) {
            log.error("Error Response Code: {} --- Body: {}",e.getStatusCode(),e.getResponseBodyAsString());
            log.error("WebClientResponseException in addEmployee()", e);
            throw e;
        }catch (Exception e) {
            log.error("Exception in addEmployee()",e);
            throw e;
        }
    }

    //http://localhost:8081/employeeservice/v1/employee/11
    public Employee updateEmployee(int id, Employee employee){
        try {
            return webClient.put()
                    .uri(UPDATE_EMPLOYEE_V1,id)
                    .syncBody(employee)
                    .retrieve()
                    .bodyToMono(Employee.class)
                    .block();
        } catch (WebClientResponseException e) {
            log.error("Error Response Code: {} --- Body: {}",e.getStatusCode(),e.getResponseBodyAsString());
            log.error("WebClientResponseException in updateEmployee()", e);
            throw e;
        }catch (Exception e) {
            log.error("Exception in updateEmployee()",e);
            throw e;
        }
    }

    //http://localhost:8081/employeeservice/v1/employee/5
    public String deleteEmployee(int id){
        try {
            return webClient.delete()
                    .uri(DELETE_EMPLOYEE_V1,id)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (WebClientResponseException e) {
            log.error("Error Response Code: {} --- Body: {}",e.getStatusCode(),e.getResponseBodyAsString());
            log.error("WebClientResponseException in deleteEmployee()", e);
            throw e;
        }catch (Exception e) {
            log.error("Exception in deleteEmployee()",e);
            throw e;
        }
    }


}
