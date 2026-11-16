package com.mycompany.levelup_code.business.service;

import com.mycompany.levelup_code.domain.model.Role;
import com.mycompany.levelup_code.domain.model.User;
import com.mycompany.levelup_code.support.InMemoryTestRepositories.FakeUserRepository;
import com.mycompany.levelup_code.util.PasswordHasher;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ProfileSecurityTest {
    @Test void verifiesCurrentPasswordBeforeReplacingStoredHash() {
        FakeUserRepository repository = new FakeUserRepository();
        User user = new User();
        user.setPasswordHash(PasswordHasher.hash("original123"));
        repository.save(user);
        UserService service = new UserService(repository, new Role(1, "Estudiante", "Estudiante"));
        String originalHash = user.getPasswordHash();
        assertThrows(IllegalArgumentException.class, () -> service.changePassword(user.getId(), "incorrecta", "replacement123"));
        assertEquals(originalHash, user.getPasswordHash());
        service.changePassword(user.getId(), "original123", "replacement123");
        assertTrue(PasswordHasher.verify("replacement123", user.getPasswordHash()));
        assertFalse(PasswordHasher.verify("original123", user.getPasswordHash()));
    }
}
