package com.jmafla.taskflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Punto de entrada de la aplicacion.
 *
 * <p>{@code @EnableJpaAuditing} activa el llenado automatico de los campos
 * {@code createdAt} y {@code updatedAt} declarados en las entidades, de modo que
 * ninguna capa superior tenga que recordar actualizarlos a mano.</p>
 */
@SpringBootApplication
@EnableJpaAuditing
public class TaskflowApplication {

    public static void main(String[] args) {
        SpringApplication.run(TaskflowApplication.class, args);
    }
}
