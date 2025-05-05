import client.BurgerClient;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.ValidatableResponse;
import model.User;
import model.UserLogin;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

public class UserLoginTest {

    private User user;
    private UserLogin credentials;
    private final BurgerClient client = new BurgerClient();
    String accessToken;
    boolean isUserCreated = false;

    @Before
    public void createUser(){
        user = new User("testikmail@mail.ru", "1212", "Testikman");
        credentials = UserLogin.fromUser(user);

        ValidatableResponse response = client.createUser(user);
        accessToken = BurgerClient.successfulCreation(response);

        isUserCreated =true;
    }

    @Test
    @DisplayName("Successful user login")
    @Description("Пользователь успешно залогинился, сервер вернул код 200")
    public void runUserLoginTest(){
        ValidatableResponse response = client.loginUser(credentials);
        client.successfulLogin(response);
    }

    @Test
    @DisplayName("user login with invalid password")
    @Description("Пользователь не залогинился, сервер вернул код 401")
    public void runLoginWithoutPasswordTest(){
        String brokenPassword = user.getPassword() + UUID.randomUUID();
        UserLogin brokenCredentials = new UserLogin(user.getEmail(), brokenPassword);

        ValidatableResponse response = client.loginUser(brokenCredentials);
        client.invalidLogin(response);
    }

    @Test
    @DisplayName("user login with invalid email")
    @Description("Пользователь не залогинился, сервер вернул код 401")
    public void runLoginWithoutEmailTest(){
        String brokenEmail = user.getEmail() + UUID.randomUUID();
        UserLogin brokenCredentials = new UserLogin(brokenEmail, user.getPassword());

        ValidatableResponse response = client.loginUser(brokenCredentials);
        client.invalidLogin(response);
    }

    @After
    public void deleteUser(){
        if (isUserCreated) {
            client.deleteUser(accessToken);
        }
    }
}
