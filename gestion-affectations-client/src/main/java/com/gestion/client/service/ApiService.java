package com.gestion.client.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gestion.client.model.Affecter;
import com.gestion.client.model.AffecterResponse;
import com.gestion.client.model.Employee;
import com.gestion.client.model.Lieu;
import okhttp3.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ApiService {
    private static final String BASE_URL = "http://localhost:8080/api";
    private final OkHttpClient client;
    private final ObjectMapper mapper;

    public ApiService() {
        this.client = new OkHttpClient();
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    // ============ EMPLOYÉS ============

    public List<Employee> getAllEmployees() throws IOException {
        Request request = new Request.Builder()
                .url(BASE_URL + "/employees")
                .get()
                .build();
        try (Response response = client.newCall(request).execute()) {
            String json = response.body().string();
            return Arrays.asList(mapper.readValue(json, Employee[].class));
        }
    }

    public Employee createEmployee(Employee employee) throws IOException {
        String json = mapper.writeValueAsString(employee);
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(BASE_URL + "/employees")
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            return mapper.readValue(response.body().string(), Employee.class);
        }
    }

    public Employee updateEmployee(String code, Employee employee) throws IOException {
        String json = mapper.writeValueAsString(employee);
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(BASE_URL + "/employees/" + code)
                .put(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            return mapper.readValue(response.body().string(), Employee.class);
        }
    }

    public void deleteEmployee(String code) throws IOException {
        Request request = new Request.Builder()
                .url(BASE_URL + "/employees/" + code)
                .delete()
                .build();
        client.newCall(request).execute();
    }

    public List<Employee> searchEmployees(String keyword) throws IOException {
        Request request = new Request.Builder()
                .url(BASE_URL + "/employees/search?keyword=" + keyword)
                .get()
                .build();
        try (Response response = client.newCall(request).execute()) {
            String json = response.body().string();
            return Arrays.asList(mapper.readValue(json, Employee[].class));
        }
    }

    // ============ LIEUX ============

    public List<Lieu> getAllLieus() throws IOException {
        Request request = new Request.Builder()
                .url(BASE_URL + "/lieux")
                .get()
                .build();
        try (Response response = client.newCall(request).execute()) {
            String json = response.body().string();
            return Arrays.asList(mapper.readValue(json, Lieu[].class));
        }
    }

    public Lieu createLieu(Lieu lieu) throws IOException {
        String json = mapper.writeValueAsString(lieu);
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(BASE_URL + "/lieux")
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            return mapper.readValue(response.body().string(), Lieu.class);
        }
    }

    public Lieu updateLieu(String code, Lieu lieu) throws IOException {
        String json = mapper.writeValueAsString(lieu);
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(BASE_URL + "/lieux/" + code)
                .put(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            return mapper.readValue(response.body().string(), Lieu.class);
        }
    }

    public void deleteLieu(String code) throws IOException {
        Request request = new Request.Builder()
                .url(BASE_URL + "/lieux/" + code)
                .delete()
                .build();
        client.newCall(request).execute();
    }

    // ============ AFFECTATIONS (CORRIGÉE) ============

    public List<Affecter> getAllAffectations() throws IOException {
        Request request = new Request.Builder()
                .url(BASE_URL + "/affectations")
                .get()
                .build();
        try (Response response = client.newCall(request).execute()) {
            String json = response.body().string();

            // ⚡ 1. Parser les objets complets (avec employee et lieu imbriqués)
            AffecterResponse[] responses = mapper.readValue(json, AffecterResponse[].class);

            // ⚡ 2. Transformer en Affecter plat
            List<Affecter> affectations = new ArrayList<>();
            for (AffecterResponse resp : responses) {
                Affecter aff = new Affecter();
                aff.setId(resp.getId());
                aff.setDateAffectation(resp.getDateAffectation());

                // Extraire les données de l'employé
                if (resp.getEmployee() != null) {
                    Employee emp = resp.getEmployee();
                    aff.setCodeemp(emp.getCodeFormate());
                    aff.setNomEmploye(emp.getNom());
                    aff.setPrenomEmploye(emp.getPrenom());
                    aff.setPosteEmploye(emp.getPoste());
                }

                // Extraire les données du lieu
                if (resp.getLieu() != null) {
                    Lieu l = resp.getLieu();
                    aff.setCodelieu(l.getCodeFormate());
                    aff.setDesignationLieu(l.getDesignation());
                    aff.setProvinceLieu(l.getProvince());
                }

                affectations.add(aff);
            }

            return affectations;
        }
    }

    public Affecter createAffectation(String codeemp, String codelieu, LocalDate date) throws IOException {
        String url = String.format(BASE_URL + "/affectations?codeemp=%s&codelieu=%s&date=%s",
                codeemp, codelieu, date);
        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create("", MediaType.parse("application/json")))
                .build();
        try (Response response = client.newCall(request).execute()) {
            return mapper.readValue(response.body().string(), Affecter.class);
        }
    }
    public void updateAffectation(Long id, String codeemp, String codelieu, LocalDate date) throws IOException {
        String url = String.format(BASE_URL + "/affectations/%d?codeemp=%s&codelieu=%s&date=%s",
                id, codeemp, codelieu, date);
        Request request = new Request.Builder()
                .url(url)
                .put(RequestBody.create("", MediaType.parse("application/json")))
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Erreur lors de la mise à jour : " + response.code());
            }
        }
    }

    public void deleteAffectation(Long id) throws IOException {
        Request request = new Request.Builder()
                .url(BASE_URL + "/affectations/" + id)
                .delete()
                .build();
        client.newCall(request).execute();
    }

    public List<Affecter> getAffectationsByEmployee(String codeemp) throws IOException {
        Request request = new Request.Builder()
                .url(BASE_URL + "/affectations/byemployee/" + codeemp)
                .get()
                .build();
        try (Response response = client.newCall(request).execute()) {
            String json = response.body().string();
            return Arrays.asList(mapper.readValue(json, Affecter[].class));
        }
    }

    public Map<String, Long> getStatistics() throws IOException {
        Request request = new Request.Builder()
                .url(BASE_URL + "/affectations/statistics")
                .get()
                .build();
        try (Response response = client.newCall(request).execute()) {
            String json = response.body().string();
            return mapper.readValue(json, Map.class);
        }
    }
}