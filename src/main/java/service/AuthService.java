package service;

import dto.LoginDTO;
import exception.BusinessException;
import models.User;
import org.mindrot.jbcrypt.BCrypt;

public class AuthService {

    public LoginDTO autenticar(String username, String password) throws BusinessException {
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            throw new BusinessException("El nombre de usuario y la contraseña son requeridos.");
        }

        User ac = User.findFirst("name = ?", username);
        if (ac == null) {
            throw new BusinessException("Usuario o contraseña incorrectos.");
        }

        String storedHashedPassword = ac.getString("password");
        if (BCrypt.checkpw(password, storedHashedPassword)) {
            LoginDTO dto = new LoginDTO();
            dto.setId(ac.getId().toString());
            dto.setUsername(username);
            return dto;
        } else {
            throw new BusinessException("Usuario o contraseña incorrectos.");
        }
    }
}