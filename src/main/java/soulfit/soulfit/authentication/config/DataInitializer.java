package soulfit.soulfit.authentication.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import soulfit.soulfit.authentication.entity.AccountStatus;
import soulfit.soulfit.authentication.repository.UserRepository;
import soulfit.soulfit.authentication.entity.Role;
import soulfit.soulfit.authentication.entity.UserAuth;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Create admin user if not exists
        if (!userRepository.existsByUsername("admin")) {
            UserAuth admin = new UserAuth();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setEmail("admin@example.com");
            admin.setRole(Role.ADMIN);
            admin.setEnabled(true);
            admin.setAccountStatus(AccountStatus.ACTIVE);
            userRepository.save(admin);

            System.out.println("Admin user created: username=admin, password=admin123");
        }

        // Create regular user if not exists
        if (!userRepository.existsByUsername("user")) {
            UserAuth userAuth = new UserAuth();
            userAuth.setUsername("user");
            userAuth.setPassword(passwordEncoder.encode("user123"));
            userAuth.setEmail("user@example.com");
            userAuth.setRole(Role.USER);
            userAuth.setEnabled(true);
            userAuth.setAccountStatus(AccountStatus.ACTIVE);
            userRepository.save(userAuth);

            System.out.println("Regular user created: username=user, password=user123");
        }
    }
}