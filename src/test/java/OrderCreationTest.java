import client.BurgerClient;
import com.github.javafaker.Faker;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.ValidatableResponse;
import model.Order;
import model.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

public class OrderCreationTest {

    private final BurgerClient client = new BurgerClient();
    private final Faker faker = new Faker();
    private String accessToken;
    private boolean isUserCreated = false;

    @Before
    public void setUp() {
        // Генерация случайных данных пользователя
        String email = faker.internet().emailAddress();
        String password = faker.internet().password(6, 12);
        String name = faker.name().firstName();

        User user = new User(email, password, name);

        ValidatableResponse response = client.createUser(user);
        accessToken = BurgerClient.successfulCreation(response);
        isUserCreated = true;
    }

    @Test
    @DisplayName("Create order with authorization and valid ingredients")
    @Description("Авторизованный пользователь создаёт заказ с валидными ингредиентами. Ожидаем 200 OK и success = true")
    public void createOrderWithAuthAndValidIngredientsTest() {
        Order order = client.generateRandomOrder();
        ValidatableResponse response = client.createOrderWithAuth(accessToken, order);
        client.successfulResponse(response);
    }

    @Test
    @DisplayName("Create order without authorization and with valid ingredients")
    @Description("Неавторизованный пользователь создаёт заказ с валидными ингредиентами. Ожидаем 200 OK и success = true")
    public void createOrderWithoutAuthAndWithValidIngredientsTest() {
        Order order = client.generateRandomOrder();
        ValidatableResponse response = client.createOrderWithoutAuth(order);
        client.successfulResponse(response);
    }

    @Test
    @DisplayName("Create order with authorization and no ingredients")
    @Description("Авторизованный пользователь пытается создать заказ без ингредиентов. Ожидаем 400 Bad Request или ошибку")
    public void createOrderWithAuthAndNoIngredientsTest() {
        Order emptyOrder = new Order(Collections.emptyList());
        ValidatableResponse response = client.createOrderWithAuth(accessToken, emptyOrder);
        client.verifyOrderCreationWithoutIngredients(response);
    }

    @Test
    @DisplayName("Create order without authorization and no ingredients")
    @Description("Неавторизованный пользователь пытается создать заказ без ингредиентов. Ожидаем ошибку")
    public void createOrderWithoutAuthAndNoIngredientsTest() {
        Order emptyOrder = new Order(Collections.emptyList());
        ValidatableResponse response = client.createOrderWithoutAuth(emptyOrder);
        client.verifyOrderCreationWithoutIngredients(response);
    }

    @Test
    @DisplayName("Create order with authorization and invalid ingredient IDs")
    @Description("Авторизованный пользователь отправляет заказ с невалидными ингредиентами. Ожидаем ошибку")
    public void createOrderWithAuthAndInvalidIngredientsTest() {
        Order invalidIds = new Order(Arrays.asList("invalid1", "invalid2"));
        ValidatableResponse response = client.createOrderWithAuth(accessToken, invalidIds);
        client.verifyOrderCreationWithInvalidIngredients(response);
    }

    @Test
    @DisplayName("Create order without authorization and invalid ingredient IDs")
    @Description("Неавторизованный пользователь отправляет заказ с невалидными ингредиентами. Ожидаем ошибку")
    public void createOrderWithoutAuthAndInvalidIngredientsTest() {
        Order invalidIds = new Order(Arrays.asList("invalid1", "invalid2"));
        ValidatableResponse response = client.createOrderWithoutAuth(invalidIds);
        client.verifyOrderCreationWithInvalidIngredients(response);
    }

    @After
    public void deleteUser(){
        if (isUserCreated) {
            client.deleteUser(accessToken);
        }
    }
}
