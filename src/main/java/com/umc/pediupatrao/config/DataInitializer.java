package com.umc.pediupatrao.config;

import com.umc.pediupatrao.entity.Usuario;
import com.umc.pediupatrao.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    CommandLineRunner initDatabase(UsuarioRepository repo,
                                   @Value("${app.admin.username:admin}") String adminUsername,
                                   @Value("${app.admin.password:}") String adminPassword) {
        return args -> {

            if (repo.findByUsername(adminUsername).isEmpty()) {

                BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

                Usuario user = new Usuario();
                user.setUsername(adminUsername);
                // ✅ Senha inicial definida por propriedade; se ausente, usa um valor aleatório
                String senha = (adminPassword == null || adminPassword.isBlank())
                        ? java.util.UUID.randomUUID().toString().substring(0, 12)
                        : adminPassword;
                user.setPassword(encoder.encode(senha));
                user.setRole("ADMIN");

                repo.save(user);

                log.warn("Usuário admin criado. Defina app.admin.password no application.properties "
                        + "(a senha inicial é: {})", senha);
            } else {
                // ✅ Garante que o admin existente tenha papel ADMIN
                repo.findByUsername(adminUsername).ifPresent(u -> {
                    if (!"ADMIN".equalsIgnoreCase(u.getRole())) {
                        u.setRole("ADMIN");
                        repo.save(u);
                        log.info("Papel do usuário {} atualizado para ADMIN", adminUsername);
                    }
                });
            }
        };
    }
}
