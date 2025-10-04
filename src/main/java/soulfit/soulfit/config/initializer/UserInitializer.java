package soulfit.soulfit.config.initializer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import soulfit.soulfit.authentication.entity.AccountStatus;
import soulfit.soulfit.authentication.entity.Role;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.authentication.repository.UserRepository;
import org.springframework.context.annotation.Profile;

@Component
@Profile("!test")
@Order(1) // Ensure this runs first
public class UserInitializer implements CommandLineRunner {

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

        if (!userRepository.existsByUsername("user2")) {
            UserAuth userAuth = new UserAuth();
            userAuth.setUsername("user2");
            userAuth.setPassword(passwordEncoder.encode("user234"));
            userAuth.setEmail("user2@example.com");
            userAuth.setRole(Role.USER);
            userAuth.setEnabled(true);
            userAuth.setAccountStatus(AccountStatus.ACTIVE);
            userRepository.save(userAuth);

            System.out.println("Regular user created: username=user2, password=user234");
        }

        for (int i = 3; i <= 12; i++) {
            String username = "user" + i;
            if (!userRepository.existsByUsername(username)) {
                UserAuth userAuth = new UserAuth();
                userAuth.setUsername(username);
                userAuth.setPassword(passwordEncoder.encode(username + "123"));
                userAuth.setEmail(username + "@example.com");
                userAuth.setRole(Role.USER);
                userAuth.setEnabled(true);
                userAuth.setAccountStatus(AccountStatus.ACTIVE);
                userRepository.save(userAuth);
                System.out.println("Regular user created: username=" + username);
            }
        }
    }
}
