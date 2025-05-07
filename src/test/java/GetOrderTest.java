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

public class GetOrderTest {

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
    @DisplayName("Get orders with authorization")
    @Description("Авторизованный пользователь успешно получает список своих заказов. Ожидаем статус 200 и success: true")
    public void runGetOrdersWithAuthTest() {
        Order order = client.generateRandomOrder();
        client.createOrderWithAuth(accessToken, order);

        ValidatableResponse response = client.getOrdersWithAuth(accessToken);
        client.successfulResponse(response);
    }

    @Test
    @DisplayName("Get orders without authorization")
    @Description("Неавторизованный пользователь не может получить заказы. Ожидаем ошибку 401 и success: false")
    public void runGetOrdersWithoutAuthTest() {
        ValidatableResponse response = client.getOrdersWithoutAuth();
        client.verifyOrdersNotFetchedWithoutAuth(response);
    }

    @After
    public void deleteUser(){
        if (isUserCreated) {
            client.deleteUser(accessToken);
        }
    }
}
