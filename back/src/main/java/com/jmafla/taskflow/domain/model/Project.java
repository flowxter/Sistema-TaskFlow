package com.jmafla.taskflow.domain.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.EntityListeners;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Proyecto: agrupador de tareas y raiz del agregado.
 *
 * <p>La entidad protege sus propias invariantes. Los metodos {@link #addTask(Task)}
 * y {@link #archive()} son la unica via para modificar el estado relacionado, de
 * modo que no exista forma de dejar el modelo inconsistente desde fuera.</p>
 */
@Entity
@Table(
        name = "projects",
        uniqueConstraints = @UniqueConstraint(name = "uk_project_code", columnNames = "code")
)
@EntityListeners(AuditingEntityListener.class)
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Codigo corto e inmutable del proyecto, por ejemplo {@code TF-CORE}. */
    @Column(nullable = false, length = 20, updatable = false)
    private String code;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProjectStatus status = ProjectStatus.ACTIVE;

    /**
     * {@code orphanRemoval} garantiza que al desvincular una tarea del proyecto
     * esta se elimine, evitando registros huerfanos en la tabla.
     */
    @OneToMany(
            mappedBy = "project",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<Task> tasks = new ArrayList<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;

    /** Constructor requerido por JPA. No usar directamente: preferir {@link #builder()}. */
    protected Project() {
    }

    private Project(Builder builder) {
        this.code = builder.code;
        this.name = builder.name;
        this.description = builder.description;
        this.status = builder.status;
    }

    // ------------------------------------------------------------------
    // Comportamiento del dominio
    // ------------------------------------------------------------------

    /**
     * Vincula una tarea al proyecto manteniendo sincronizados ambos lados de la
     * relacion bidireccional. Olvidar este detalle es la causa mas frecuente de
     * inconsistencias en el contexto de persistencia de JPA.
     *
     * @param task tarea a vincular
     */
    public void addTask(Task task) {
        tasks.add(task);
        task.assignToProject(this);
    }

    /** Desvincula una tarea; {@code orphanRemoval} se encarga de borrarla. */
    public void removeTask(Task task) {
        tasks.remove(task);
        task.assignToProject(null);
    }

    /** Marca el proyecto como archivado. La validacion previa vive en el servicio. */
    public void archive() {
        this.status = ProjectStatus.ARCHIVED;
    }

    /** Reactiva un proyecto archivado. */
    public void reactivate() {
        this.status = ProjectStatus.ACTIVE;
    }

    /** Indica si el proyecto admite modificaciones sobre sus tareas. */
    public boolean isEditable() {
        return this.status == ProjectStatus.ACTIVE;
    }

    /** Cantidad de tareas que aun no alcanzan un estado final. */
    public long countPendingTasks() {
        return tasks.stream()
                .filter(task -> !task.getStatus().isFinal())
                .count();
    }

    public void updateDetails(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // ------------------------------------------------------------------
    // Accesores
    // ------------------------------------------------------------------

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public ProjectStatus getStatus() {
        return status;
    }

    public List<Task> getTasks() {
        return Collections.unmodifiableList(tasks);
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    // ------------------------------------------------------------------
    // Igualdad basada en el identificador de negocio, no en el tecnico.
    // Comparar por "id" rompe cuando la entidad aun no se ha persistido.
    // ------------------------------------------------------------------

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Project project)) {
            return false;
        }
        return Objects.equals(code, project.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    @Override
    public String toString() {
        return "Project{code='%s', name='%s', status=%s}".formatted(code, name, status);
    }

    // ------------------------------------------------------------------
    // Patron Builder: construccion legible y sin constructores telescopicos.
    // ------------------------------------------------------------------

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private String code;
        private String name;
        private String description;
        private ProjectStatus status = ProjectStatus.ACTIVE;

        private Builder() {
        }

        public Builder code(String code) {
            this.code = code;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder status(ProjectStatus status) {
            this.status = status;
            return this;
        }

        public Project build() {
            Objects.requireNonNull(code, "El codigo del proyecto es obligatorio");
            Objects.requireNonNull(name, "El nombre del proyecto es obligatorio");
            return new Project(this);
        }
    }
}
