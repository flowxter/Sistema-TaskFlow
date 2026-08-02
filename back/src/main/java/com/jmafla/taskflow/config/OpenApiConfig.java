package com.jmafla.taskflow.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Metadatos de la documentacion OpenAPI.
 *
 * <p>La especificacion se genera desde el codigo, de modo que no puede quedar
 * desactualizada respecto a los endpoints reales. Disponible en
 * {@code /swagger-ui.html}.</p>
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI taskflowOpenApi() {
        Info info = new Info()
                .title("TaskFlow API")
                .version("1.0.0")
                .description("""
                        API REST para la gestion de proyectos y tareas.

                        Incluye validacion de entrada, filtros dinamicos, paginacion,
                        manejo centralizado de errores y una maquina de estados que
                        controla las transiciones validas de cada tarea.
                        """)
                .contact(new Contact()
                        .name("Juan Jose Mafla Pacheco")
                        .email("juan.mafla@correounivalle.edu.co"))
                .license(new License().name("MIT"));

        return new OpenAPI().info(info);
    }
}
