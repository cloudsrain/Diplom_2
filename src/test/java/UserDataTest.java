import client.BurgerClient;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.ValidatableResponse;
import model.User;
import model.UserLogin;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class UserDataTest {

    private User user;
    private final BurgerClient client = new BurgerClient();
    String accessToken;
    boolean isUserCreated = false;

    @Before
    public void createUser(){
        user = new User("testikmail@mail.ru", "1212", "Testikman");

        ValidatableResponse response = client.createUser(user);
        accessToken = BurgerClient.successfulCreation(response);

        isUserCreated =true;
    }

    @Test
    @DisplayName("Update user name with authorization")
    @Description("Пользователь с accessToken успешно изменил имя, сервер вернул код 200 и обновлённое имя")
    public void runUpdateUserNameTest(){
        String fieldName = "name";
        String newName = "Fluttershy";
        User updatedUser = new User(user.getEmail(), user.getPassword(),newName);

        ValidatableResponse response = client.updateUser(accessToken, updatedUser);
        client.verifyFieldUpdated(response, fieldName, newName);
    }

    @Test
    @DisplayName("Update user email with authorization")
    @Description("Пользователь с accessToken успешно изменил email, сервер вернул код 200 и обновлённый email")
    public void runUpdateUserEmailTest(){
        String fieldName = "email";
        String newEmail = "FluttershyBestPony@mail.eq";
        User updatedUser = new User(newEmail, user.getPassword(), user.getName());

        ValidatableResponse response = client.updateUser(accessToken, updatedUser);
        client.verifyFieldUpdated(response, fieldName, newEmail.toLowerCase());
    }

    @Test
    @DisplayName("Update user password with authorization")
    @Description("Пользователь с accessToken успешно изменил пароль, сервер вернул код 200")
    public void runUpdateUserPasswordTest(){
        String newPassword = "2121";
        User updatedUser = new User(user.getEmail(), newPassword, user.getName());

        ValidatableResponse response = client.updateUser(accessToken, updatedUser);
        client.verifyPasswordUpdated(response);

        UserLogin credentials = new UserLogin(user.getEmail(), newPassword);

        ValidatableResponse responseB = client.loginUser(credentials);
        client.successfulLogin(responseB);
    }

    @Test
    @DisplayName("Try to update user name without authorization")
    @Description("Неавторизованный пользователь не смог изменить имя, сервер вернул код 401")
    public void runUpdateUserNameWithoutAuthTest(){
        String newName = "Fluttershy";
        User updatedUser = new User(user.getEmail(), user.getPassword(),newName);

        ValidatableResponse response = client.updateUserWithoutAuth(updatedUser);
        client.verifyUnauthorizedFieldUpdate(response);
    }

    @Test
    @DisplayName("Try to update user email without authorization")
    @Description("Неавторизованный пользователь не смог изменить email, сервер вернул код 401")
    public void runUpdateUserEmailWithoutAuthTest(){
        String newEmail = "FluttershyBestPony@mail.eq";
        User updatedUser = new User(newEmail, user.getPassword(), user.getName());

        ValidatableResponse response = client.updateUserWithoutAuth(updatedUser);
        client.verifyUnauthorizedFieldUpdate(response);
    }

    @Test
    @DisplayName("Try to update user password without authorization")
    @Description("Неавторизованный пользователь не смог изменить пароль, сервер вернул код 401")
    public void runUpdateUserPasswordWithoutAuthTest(){
        String newPassword = "2121";
        User updatedUser = new User(newPassword, user.getPassword(), user.getName());

        ValidatableResponse response = client.updateUserWithoutAuth(updatedUser);
        client.verifyUnauthorizedFieldUpdate(response);
    }


    @After
    public void deleteUser(){
        if (isUserCreated) {
            client.deleteUser(accessToken);
        }
    }

}
