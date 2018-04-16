package co.axelrod.foodqueue.ui;

import co.axelrod.foodqueue.logic.QueueController;
import co.axelrod.foodqueue.logic.auth.UserAuthManager;
import co.axelrod.foodqueue.logic.enums.KitchenStatus;
import co.axelrod.foodqueue.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
public class WebController {
    private final QueueController queueController;

    private final UserAuthManager userAuthManager;

    private static final String PHOTO_URL = "https://cdn-cf-static.catery.ru/storage/menu/item/3/7/5/37547/preview_preview_image-cdjfMYAbH6olsP1D6jnhTJrWGzy5g-peeQ.jpg";

    @Value("${server.host}")
    private String host;

    @Value("${server.port}")
    private String port;

    @Autowired
    public WebController(QueueController queueController, UserAuthManager userAuthManager) {
        this.queueController = queueController;
        this.userAuthManager = userAuthManager;
    }

    @GetMapping("/telegramAuth")
    public final String helloTelegram(@RequestParam("telegramChatId") Long telegramChatId) {
        userAuthManager.createUserByTelegram(SecurityContextHolder.getContext().getAuthentication(), telegramChatId);
        return "<html>"
                + "<body>"
                + "<h1>Вы успешно авторизованы!</h1>"
                + "<h1>Вернитесь в бот для продолжения</h1>"
                + "</body>"
                + "</html>";
    }

    @GetMapping("/")
    public final String hello() {
        User user = userAuthManager.getOrCreateUser(SecurityContextHolder.getContext().getAuthentication());

        if(user.queueNumber == null) {
            return "<html>"
                    + "<body>"
                    + "<img src=\"" + PHOTO_URL + "\">"
                    + "<h1>Здравствуйте, " + user.login + "</h1>"
                    + "<h1>Текущий номер очереди: " + queueController.getCurrentQueueNumber() + "</h1>"
                    + "<h2><a href=\"/enqueue\">Занять очередь</a></h2>"
                    + "</body>"
                    + "</html>";
        }

        return "<html>"
                + "<body>"
                + "<img src=\"" + PHOTO_URL + "\">"
                + "<h1>Здравствуйте, " + user.login + "</h1>"
                + "<h1>Ваш номер в очереди: " + user.queueNumber + "</h1>"
                + "<h1>Текущий номер очереди: " + queueController.getCurrentQueueNumber() + "</h1>"
                + "<h2>Уже получили? Хочется еще? <a href=\"/enqueueAgain\">Занять очередь снова</a></h2>"
                + "</body>"
                + "</html>";
    }

    @GetMapping("/enqueue")
    public final void enqueue(HttpServletResponse response) throws IOException {
        User user = userAuthManager.getOrCreateUser(SecurityContextHolder.getContext().getAuthentication());
        queueController.enqueue(user);
        response.sendRedirect("http://" + host + ":" + port);
    }

    @GetMapping("/enqueueAgain")
    public final void gotIt(HttpServletResponse response) throws IOException {
        User user = userAuthManager.getOrCreateUser(SecurityContextHolder.getContext().getAuthentication());
        queueController.dequeue(user);
        queueController.enqueue(user);
        response.sendRedirect("http://" + host + ":" + port);
    }

    @GetMapping("/control/itReady")
    public final String itReady() {
        return "<div style=\"height:100%;width:100%;text-align:center;position:fixed;font-size:1000%;\">" + queueController.incrementCurrentQueueNumber() + "</div>";
    }

    @GetMapping("/control/start")
    public final String start() {
        queueController.setKitchenStatus(KitchenStatus.READY);
        return "<h1>Кухня готова!</h1>";
    }

    @GetMapping("/control/finish")
    public final String finish() {
        queueController.setKitchenStatus(KitchenStatus.CLOSED);
        return "<h1>Кухня закрыта!</h1>";
    }
}
