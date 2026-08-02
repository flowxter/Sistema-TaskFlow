package com.jmafla.taskflow.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jmafla.taskflow.web.dto.request.ProjectRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

/**
 * Pruebas de integracion de los endpoints de proyecto.
 *
 * <p>Recorren el camino completo controlador, servicio, repositorio y base de
 * datos en memoria. Cada prueba corre dentro de una transaccion que se revierte
 * al terminar, de modo que el orden de ejecucion no afecta el resultado.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("API de proyectos")
class ProjectControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /api/v1/projects devuelve una pagina de proyectos")
    void shouldReturnPagedProjects() throws Exception {
        mockMvc.perform(get("/api/v1/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.totalElements").isNumber());
    }

    @Test
    @DisplayName("GET /api/v1/projects filtra por estado")
    void shouldFilterProjectsByStatus() throws Exception {
        mockMvc.perform(get("/api/v1/projects").param("status", "ARCHIVED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].status").value("ARCHIVED"));
    }

    @Test
    @DisplayName("POST /api/v1/projects crea el proyecto y devuelve la cabecera Location")
    void shouldCreateProjectAndReturnLocationHeader() throws Exception {
        ProjectRequest request = new ProjectRequest(
                "TF-NEW", "Proyecto de prueba", "Creado desde una prueba de integracion");

        mockMvc.perform(post("/api/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.code").value("TF-NEW"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("POST /api/v1/projects rechaza un codigo con formato invalido")
    void shouldRejectInvalidProjectCode() throws Exception {
        ProjectRequest request = new ProjectRequest("minusculas", "Nombre valido", null);

        mockMvc.perform(post("/api/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors.code").exists());
    }

    @Test
    @DisplayName("POST /api/v1/projects rechaza un codigo duplicado con 409")
    void shouldRejectDuplicateProjectCode() throws Exception {
        ProjectRequest request = new ProjectRequest("TF-CORE", "Duplicado", null);

        mockMvc.perform(post("/api/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Ya existe")));
    }

    @Test
    @DisplayName("GET /api/v1/projects/{id} devuelve 404 cuando no existe")
    void shouldReturnNotFoundForUnknownProject() throws Exception {
        mockMvc.perform(get("/api/v1/projects/{id}", 99_999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.path").value("/api/v1/projects/99999"));
    }

    @Test
    @DisplayName("GET /api/v1/projects/{id}/metrics calcula el avance del proyecto")
    void shouldCalculateProjectMetrics() throws Exception {
        mockMvc.perform(get("/api/v1/projects/{id}/metrics", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectCode").value("TF-CORE"))
                .andExpect(jsonPath("$.totalTasks").isNumber())
                .andExpect(jsonPath("$.completionRate").isNumber())
                .andExpect(jsonPath("$.tasksByStatus.DONE").isNumber());
    }

    @Test
    @DisplayName("PATCH /archive rechaza archivar un proyecto con tareas abiertas")
    void shouldRejectArchivingProjectWithPendingTasks() throws Exception {
        mockMvc.perform(patch("/api/v1/projects/{id}/archive", 1))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value(org.hamcrest.Matchers.containsString("sin finalizar")));
    }
}
