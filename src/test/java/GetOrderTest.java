import client.BurgerClient;
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
    String accessToken;
    boolean isUserCreated = false;

    @Before
    public void setUp() {
        User user = new User("testikmail@mail.ru", "1212", "Testikman");

        ValidatableResponse response = client.createUser(user);
        accessToken = BurgerClient.successfulCreation(response);

        isUserCreated =true;
    }

    @Test
    @DisplayName("Get orders with authorization")
    @Description("Авторизованный пользователь успешно получает список своих заказов. Ожидаем статус 200 и success: true")
    public void runGetOrdersWithAuthTest() {
        Order order = client.generateRandomOrder();
        client.createOrderWithAuth(accessToken, order);

        ValidatableResponse response = client.getOrdersWithAuth(accessToken);
        client.verifyOrdersFetchedWithAuth(response);
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
