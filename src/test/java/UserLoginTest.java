import client.BurgerClient;
import com.github.javafaker.Faker;
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
    private final Faker faker = new Faker();
    private String accessToken;
    private boolean isUserCreated = false;

    @Before
    public void createUser() {
        // Генерация случайных данных пользователя с помощью Faker
        String email = faker.internet().emailAddress();
        String password = faker.internet().password(6, 12);
        String name = faker.name().firstName();

        user = new User(email, password, name);
        credentials = UserLogin.fromUser(user);

        ValidatableResponse response = client.createUser(user);
        accessToken = BurgerClient.successfulCreation(response);

        isUserCreated = true;
    }

    @Test
    @DisplayName("Successful user login")
    @Description("Пользователь успешно залогинился, сервер вернул код 200")
    public void runUserLoginTest() {
        ValidatableResponse response = client.loginUser(credentials);
        client.successfulResponse(response);
    }

    @Test
    @DisplayName("User login with invalid password")
    @Description("Пользователь не залогинился, сервер вернул код 401")
    public void runLoginWithoutPasswordTest() {
        String brokenPassword = user.getPassword() + UUID.randomUUID();
        UserLogin brokenCredentials = new UserLogin(user.getEmail(), brokenPassword);

        ValidatableResponse response = client.loginUser(brokenCredentials);
        client.invalidLogin(response);
    }

    @Test
    @DisplayName("User login with invalid email")
    @Description("Пользователь не залогинился, сервер вернул код 401")
    public void runLoginWithoutEmailTest() {
        String brokenEmail = user.getEmail() + UUID.randomUUID();
        UserLogin brokenCredentials = new UserLogin(brokenEmail, user.getPassword());

        ValidatableResponse response = client.loginUser(brokenCredentials);
        client.invalidLogin(response);
    }

    @After
    public void deleteUser() {
        if (isUserCreated) {
            client.deleteUser(accessToken);
        }
    }
}
