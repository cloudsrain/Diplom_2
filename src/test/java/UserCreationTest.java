import client.BurgerClient;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.ValidatableResponse;
import model.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.github.javafaker.Faker;

public class UserCreationTest {

    private final BurgerClient client = new BurgerClient();
    private final Faker faker = new Faker();
    private String accessToken;
    private boolean isUserCreated = false;

    private String generateEmail() {
        return faker.internet().emailAddress();
    }

    private String generatePassword() {
        return faker.internet().password(6, 12);
    }

    private String generateName() {
        return faker.name().firstName();
    }

    private User generateRandomUser() {
        return new User(generateEmail(), generatePassword(), generateName());
    }

    @Before
    public void setup() {
        accessToken = null;
        isUserCreated = false;
    }

    @Test
    @DisplayName("Successful user creation")
    @Description("Пользователь создан, сервер вернул код 200")
    public void runUserCreationTest() {
        User user = generateRandomUser();

        ValidatableResponse response = client.createUser(user);
        accessToken = BurgerClient.successfulCreation(response);
        isUserCreated = true;
    }

    @Test
    @DisplayName("Create user when already registered")
    @Description("Пользователь уже зарегистрирован, сервер возвращает код 403 (Forbidden)")
    public void runAlreadyCreatedUserTest() {
        User user = generateRandomUser();

        ValidatableResponse responseA = client.createUser(user);
        accessToken = BurgerClient.successfulCreation(responseA);
        ValidatableResponse responseB = client.createUser(user);
        client.alreadyCreatedUser(responseB);

        isUserCreated = true;
    }

    @Test
    @DisplayName("Create user with missing email field")
    @Description("Пользователь создан с отсутствующим обязательным полем, сервер возвращает код 403 (Forbidden)")
    public void runUserCreationWithoutEmailTest() {
        User user = new User("", generatePassword(), generateName());

        ValidatableResponse response = client.createUser(user);
        client.userCreationWithoutField(response);
    }

    @Test
    @DisplayName("Create user with missing password field")
    @Description("Пользователь создан с отсутствующим обязательным полем, сервер возвращает код 403 (Forbidden)")
    public void runUserCreationWithoutPasswordTest() {
        User user = new User(generateEmail(), "", generateName());

        ValidatableResponse response = client.createUser(user);
        client.userCreationWithoutField(response);
    }

    @Test
    @DisplayName("Create user with missing name field")
    @Description("Пользователь создан с отсутствующим обязательным полем, сервер возвращает код 403 (Forbidden)")
    public void runUserCreationWithoutNameTest() {
        User user = new User(generateEmail(), generatePassword(), "");

        ValidatableResponse response = client.createUser(user);
        client.userCreationWithoutField(response);
    }

    @After
    public void deleteUser() {
        if (isUserCreated) {
            client.deleteUser(accessToken);
        }
    }
}
