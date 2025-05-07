package client;

import com.google.gson.Gson;
import io.qameta.allure.Step;
import io.restassured.response.ValidatableResponse;
import model.User;
import model.UserLogin;
import model.Order;

import java.util.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.apache.http.HttpStatus.*;

public class BurgerClient {

    private static final String BASE_URL = "https://stellarburgers.nomoreparties.site";
    private static final String USER_CREATION_PATH = "/api/auth/register";
    private static final String USER_LOGIN_PATH = "/api/auth/login";
    private static final String USER_CHANGE_PATH = "/api/auth/user";
    private static final String MAKE_ORDER_PATH = "/api/orders";
    private static final String GET_ORDER_PATH = "/api/orders";
    private static final String GET_INGREDIENTS_PATH = "/api/ingredients";
    private static final String DELETE_USER_PATH = "/api/auth/user";

    private final Gson gson = new Gson();

    @Step("Создание пользователя")
    public ValidatableResponse createUser(User user){
        return given()
                .log().all()
                .baseUri(BASE_URL)
                .header("Content-Type", "application/json")
                .body(user)
                .when()
                .post(USER_CREATION_PATH)
                .then()
                .log().all();
    }

    @Step("Логин пользователя")
    public ValidatableResponse loginUser(UserLogin credentials){
        return given()
                .log().all()
                .baseUri(BASE_URL)
                .header("Content-Type", "application/json")
                .body(credentials)
                .when()
                .post(USER_LOGIN_PATH)
                .then()
                .log().all();
    }

    @Step("Изменение данных пользователя")
    public ValidatableResponse updateUser(String accessToken, User user){
        return given()
                .log().all()
                .baseUri(BASE_URL)
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .body(user)
                .when()
                .patch(USER_CHANGE_PATH)
                .then()
                .log().all();
    }

    @Step("Изменение данных пользователя без авторизации")
    public ValidatableResponse updateUserWithoutAuth(User user){
        return given()
                .log().all()
                .baseUri(BASE_URL)
                .header("Content-Type", "application/json")
                .body(user)
                .when()
                .patch(USER_CHANGE_PATH)
                .then()
                .log().all();
    }

    @Step("Создать заказ с авторизацией")
    public ValidatableResponse createOrderWithAuth(String accessToken, Order order){
        return given()
                .log().all()
                .baseUri(BASE_URL)
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .body(order)
                .when()
                .post(MAKE_ORDER_PATH)
                .then()
                .log().all();
    }

    @Step("Создать заказ без авторизации")
    public ValidatableResponse createOrderWithoutAuth(Order order){
        return given()
                .log().all()
                .baseUri(BASE_URL)
                .header("Content-Type", "application/json")
                .body(order)
                .when()
                .post(MAKE_ORDER_PATH)
                .then()
                .log().all();
    }

    @Step("Получить заказы пользователя")
    public ValidatableResponse getOrdersWithAuth(String accessToken){
        return given()
                .log().all()
                .baseUri(BASE_URL)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get(GET_ORDER_PATH)
                .then()
                .log().all();
    }

    @Step("Получить заказы без авторизации")
    public ValidatableResponse getOrdersWithoutAuth(){
        return given()
                .log().all()
                .baseUri(BASE_URL)
                .when()
                .get(GET_ORDER_PATH)
                .then()
                .log().all();
    }

    @Step("Формируем заказ из случайных ингредиентов")
    public Order generateRandomOrder() {
        ValidatableResponse response = given()
                .baseUri(BASE_URL)
                .when()
                .get(GET_INGREDIENTS_PATH)
                .then()
                .statusCode(SC_OK)
                .body("success", equalTo(true));

        List<Map<String, Object>> ingredients = response.extract().path("data");

        List<String> ids = new ArrayList<>();
        for (Map<String, Object> ingredient : ingredients) {
            ids.add((String) ingredient.get("_id"));
        }

        if (ids.size() < 3) {
            throw new IllegalStateException("Недостаточно ингредиентов для формирования заказа");
        }

        Collections.shuffle(ids);
        return new Order(ids.subList(0, 3));
    }

    @Step("Удаление пользователя")
    public void deleteUser(String accessToken){
        given()
                .log().all()
                .baseUri(BASE_URL)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .delete(DELETE_USER_PATH)
                .then()
                .log().all();
    }

    @Step("Успешное создание пользователя")
    public static String successfulCreation(ValidatableResponse response) {
        response.assertThat().statusCode(SC_OK)
                .body("success", equalTo(true));

        return response.extract()
                .path("accessToken")
                .toString()
                .replace("Bearer ", "");
    }

    @Step("Успешный логин")
    public void successfulResponse(ValidatableResponse response){
        response.assertThat().statusCode(SC_OK)
                .body("success", equalTo(true));
    }

    @Step("Проверка, что поле '{field}' успешно изменено на значение '{expectedValue}'")
    public void verifyFieldUpdated(ValidatableResponse response, String field, String expectedValue) {
        response.assertThat()
                .statusCode(SC_OK)
                .body("success", equalTo(true))
                .body("user." + field, equalTo(expectedValue));
    }

    @Step("Проверка, что пароль поменялся")
    public void verifyPasswordUpdated(ValidatableResponse response) {
        response.assertThat()
                .statusCode(SC_OK)
                .body("success", equalTo(true));
    }

    @Step("Создание существующего пользователя")
    public void alreadyCreatedUser(ValidatableResponse response) {
        response.assertThat().statusCode(SC_FORBIDDEN)
                .body("success", equalTo(false))
                .body("message", equalTo("User already exists"));
    }

    @Step("Создание пользователя без одного поля")
    public void userCreationWithoutField(ValidatableResponse response) {
        response.assertThat().statusCode(SC_FORBIDDEN)
                .body("success", equalTo(false))
                .body("message", equalTo("Email, password and name are required fields"));
    }

    @Step("Неуспешный логин")
    public void invalidLogin(ValidatableResponse response){
        response.assertThat().statusCode(SC_UNAUTHORIZED)
                .body("success", equalTo(false))
                .body("message", equalTo("email or password are incorrect"));
    }

    @Step("Заказ не формляется без ингредиентов")
    public void verifyOrderCreationWithoutIngredients(ValidatableResponse response){
        response.assertThat().statusCode(SC_BAD_REQUEST)
                .body("success", equalTo(false))
                .body("message", equalTo("Ingredient ids must be provided"));
    }

    @Step("Заказ не оформляется с неправильными ингредиентами")
    public void verifyOrderCreationWithInvalidIngredients(ValidatableResponse response){
        response.assertThat().statusCode(SC_INTERNAL_SERVER_ERROR);
    }

    @Step("Неавторизованный пользователь не может получить заказы")
    public void verifyOrdersNotFetchedWithoutAuth(ValidatableResponse response) {
        response.assertThat()
                .statusCode(SC_UNAUTHORIZED)
                .body("success", equalTo(false))
                .body("message", equalTo("You should be authorised"));
    }

    @Step("Проверка, что поле не изменилось (без авторизации)")
    public void verifyUnauthorizedFieldUpdate(ValidatableResponse response) {
        response.assertThat()
                .statusCode(SC_UNAUTHORIZED)
                .body("success", equalTo(false))
                .body("message", equalTo("You should be authorised"));
    }
}
