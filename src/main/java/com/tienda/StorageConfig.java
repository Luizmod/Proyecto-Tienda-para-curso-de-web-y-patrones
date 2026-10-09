package com.tienda;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class StorageConfig {

    @Value("${firebase.json.path}")
    private String jsonPath;

    @Value("${firebase.json.file}")
    private String jsonFile;

    // NUEVO: ruta externa opcional (Render Secret File, variable de entorno, etc.)
    @Value("${firebase.credentials-path:}")
    private String credentialsPath;

    @Bean
    public Storage storage() throws IOException {
        InputStream inputStream = openCredentials();
        GoogleCredentials credentials = GoogleCredentials.fromStream(inputStream);
        return StorageOptions.newBuilder().setCredentials(credentials).build().getService();
    }

    private InputStream openCredentials() throws IOException {
        // 1) Si hay ruta externa configurada, úsala (producción / Render)
        if (credentialsPath != null && !credentialsPath.isBlank()) {
            File file = new File(credentialsPath);
            if (file.exists()) {
                System.out.println(">>> Firebase: cargando credenciales desde " + credentialsPath);
                return new FileInputStream(file);
            } else {
                System.out.println(">>> Firebase: ruta configurada no existe: " + credentialsPath);
            }
        }

        // 2) Fallback: classpath (desarrollo local)
        String resourcePath = jsonPath + File.separator + jsonFile;
        ClassPathResource resource = new ClassPathResource(resourcePath);
        if (resource.exists()) {
            System.out.println(">>> Firebase: cargando credenciales desde classpath: " + resourcePath);
            return resource.getInputStream();
        }

        throw new IllegalStateException(
            "No se encontraron credenciales de Firebase. " +
            "Define FIREBASE_CREDENTIALS_PATH o coloca el JSON en el classpath."
        );
    }
}
