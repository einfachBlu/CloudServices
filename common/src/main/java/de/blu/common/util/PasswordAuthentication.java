package de.blu.common.util;

import com.google.inject.Singleton;
import org.springframework.security.crypto.bcrypt.BCrypt;

@Singleton
public final class PasswordAuthentication {

    public String hash(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(12));
    }

    public boolean authenticate(String password, String hash) {
        return BCrypt.checkpw(password, hash);
    }
}
