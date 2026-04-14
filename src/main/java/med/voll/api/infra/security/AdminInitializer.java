package med.voll.api.infra.security;

import med.voll.api.domain.usuario.Perfil;
import med.voll.api.domain.usuario.Usuario;
import med.voll.api.domain.usuario.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AdminInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInitializer.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.login}")
    private String adminLogin;

    @Value("${admin.password:}")
    private String adminPassword;

    public AdminInitializer(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("AdminInitializer: verificando admin. Login configurado: {}", adminLogin);

        if (usuarioRepository.existsByRole(Perfil.ROLE_ADMIN)) {
            log.info("AdminInitializer: admin já existe no banco, nenhuma ação necessária.");
            return;
        }

        if (adminPassword == null || adminPassword.isBlank()) {
            log.warn("AdminInitializer: ADMIN_PASSWORD não configurado. Admin não será criado.");
            return;
        }

        var admin = new Usuario();
        admin.setLogin(adminLogin);
        admin.setSenha(passwordEncoder.encode(adminPassword));
        admin.setRole(Perfil.ROLE_ADMIN);
        usuarioRepository.save(admin);

        log.info("Admin inicial criado com login: {}", adminLogin);
    }
}
