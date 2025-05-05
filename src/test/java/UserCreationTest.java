import client.BurgerClient;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.ValidatableResponse;
import model.User;
import org.junit.After;
import org.junit.Test;

public class UserCreationTest {
    private final BurgerClient client = new BurgerClient();
    String accessToken;
    boolean isUserCreated = false;

    @Test
    @DisplayName("Successful user creation")
    @Description("Пользователь создан, сервер вернул код 200")
    public void runUserCreationTest(){
        User user = new User("testikmail@mail.ru", "1212", "Testikman");

        ValidatableResponse response = client.createUser(user);
        accessToken = BurgerClient.successfulCreation(response);
        isUserCreated = true;

    }

    @Test
    @DisplayName("Create user when already registered")
    @Description("Пользователь уже зарегистрирован, сервер возвращает код  403 (Forbidden)")
    public void runAlreadyCreatedUserTest(){
        User user = new User("testikmail@mail.ru", "1212", "Testikman");

        ValidatableResponse responseA = client.createUser(user);
        accessToken = BurgerClient.successfulCreation(responseA);
        ValidatableResponse responseB = client.createUser(user);
        client.alreadyCreatedUser(responseB);

        isUserCreated = true;

    }

    @Test
    @DisplayName("Create user with missing email field")
    @Description("Пользователь создан с отсутствующим обязательным полем, сервер возвращает код 403 (Forbidden)")
    public void runUserCreationWithoutEmailTest(){
        User user = new User("", "1212", "Testikman");

        ValidatableResponse response = client.createUser(user);
        client.userCreationWithoutField(response);
    }

    @Test
    @DisplayName("Create user with missing password field")
    @Description("Пользователь создан с отсутствующим обязательным полем, сервер возвращает код 403 (Forbidden)")
    public void runUserCreationWithoutPasswordTest(){
        User user = new User("testikmail@mail.ru", "", "Testikman");

        ValidatableResponse response = client.createUser(user);
        client.userCreationWithoutField(response);
    }

    @Test
    @DisplayName("Create user with missing name field")
    @Description("Пользователь создан с отсутствующим обязательным полем, сервер возвращает код 403 (Forbidden)")
    public void runUserCreationWithoutNameTest(){
        User user = new User("testikmail@mail.ru", "1212", "");

        ValidatableResponse response = client.createUser(user);
        client.userCreationWithoutField(response);
    }

    @After
    public void deleteUser(){
        if (isUserCreated){
            client.deleteUser(accessToken);
        }
    }
}
