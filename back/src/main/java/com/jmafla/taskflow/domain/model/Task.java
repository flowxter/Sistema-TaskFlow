package com.jmafla.taskflow.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Tarea perteneciente a un proyecto.
 *
 * <p>El cambio de estado no se hace asignando el campo directamente: pasa por
 * {@link #transitionTo(TaskStatus)}, que consulta la maquina de estados definida
 * en {@link TaskStatus} y deriva efectos secundarios como la fecha de cierre.</p>
 */
@Entity
@Table(
        name = "tasks",
        indexes = {
                @Index(name = "idx_task_project_status", columnList = "project_id, status"),
                @Index(name = "idx_task_due_date", columnList = "due_date")
        }
)
@EntityListeners(AuditingEntityListener.class)
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaskStatus status = TaskStatus.BACKLOG;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaskPriority priority = TaskPriority.MEDIUM;

    @Column(length = 80)
    private String assignee;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "completed_at")
    private Instant completedAt;

    /**
     * Carga perezosa deliberada: la mayoria de consultas de tareas no necesitan
     * el proyecto completo, y traerlo siempre generaria el problema N+1.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false, foreignKey = @jakarta.persistence.ForeignKey(name = "fk_task_project"))
    private Project project;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;

    protected Task() {
    }

    private Task(Builder builder) {
        this.title = builder.title;
        this.description = builder.description;
        this.status = builder.status;
        this.priority = builder.priority;
        this.assignee = builder.assignee;
        this.dueDate = builder.dueDate;
    }

    // ------------------------------------------------------------------
    // Comportamiento del dominio
    // ------------------------------------------------------------------

    /**
     * Aplica un cambio de estado si la maquina de estados lo permite.
     *
     * <p>Al alcanzar un estado final se sella {@code completedAt}; al salir de el
     * se limpia, para que la metrica de tareas cerradas nunca quede desalineada
     * con el estado real.</p>
     *
     * @param target estado destino
     * @return {@code true} si la transicion se aplico, {@code false} si era invalida
     */
    public boolean transitionTo(TaskStatus target) {
        if (target == this.status) {
            return true;
        }
        if (!this.status.canTransitionTo(target)) {
            return false;
        }
        this.status = target;
        this.completedAt = target.isFinal() ? Instant.now() : null;
        return true;
    }

    /** Indica si la tarea vencio sin haberse cerrado. */
    public boolean isOverdue() {
        return dueDate != null
                && !status.isFinal()
                && dueDate.isBefore(LocalDate.now());
    }

    /** Sincroniza el lado propietario de la relacion. Uso interno del agregado. */
    void assignToProject(Project project) {
        this.project = project;
    }

    public void updateDetails(String title,
                             String description,
                             TaskPriority priority,
                             String assignee,
                             LocalDate dueDate) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.assignee = assignee;
        this.dueDate = dueDate;
    }

    // ------------------------------------------------------------------
    // Accesores
    // ------------------------------------------------------------------

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public String getAssignee() {
        return assignee;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public Project getProject() {
        return project;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Task task)) {
            return false;
        }
        return id != null && Objects.equals(id, task.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Task{id=%d, title='%s', status=%s, priority=%s}"
                .formatted(id, title, status, priority);
    }

    // ------------------------------------------------------------------
    // Patron Builder
    // ------------------------------------------------------------------

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private String title;
        private String description;
        private TaskStatus status = TaskStatus.BACKLOG;
        private TaskPriority priority = TaskPriority.MEDIUM;
        private String assignee;
        private LocalDate dueDate;

        private Builder() {
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder status(TaskStatus status) {
            this.status = status;
            return this;
        }

        public Builder priority(TaskPriority priority) {
            this.priority = priority;
            return this;
        }

        public Builder assignee(String assignee) {
            this.assignee = assignee;
            return this;
        }

        public Builder dueDate(LocalDate dueDate) {
            this.dueDate = dueDate;
            return this;
        }

        public Task build() {
            Objects.requireNonNull(title, "El titulo de la tarea es obligatorio");
            return new Task(this);
        }
    }
}
