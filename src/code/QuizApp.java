import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


public class QuizApp extends Application
{
   private static final int VBOX_DISTANCE_PIXELS = 10;

   @Override
   public void start(final Stage stage)
   {
      final Label question;
      question = new Label("Press the start quiz button to start");

      final TextField userAnswer;
      userAnswer = new TextField();

      final Button submitButton;
      submitButton = new Button("Submit");
      submitButton.setOnAction(e -> System.out.println("Submit button pressed"));

      final Button startQuizButton;
      startQuizButton = new Button("Start Quiz");
      startQuizButton.setOnAction(e -> System.out.println("Start quiz button pressed"));

      VBox root = new VBox(VBOX_DISTANCE_PIXELS, question, userAnswer, submitButton, startQuizButton);

      Scene scene = new Scene(root, 300, 250);
      scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

      stage.setTitle("Quiz App");
      stage.setScene(scene);
      stage.show();
   }

   public static void main(final String[] args)
   {
      launch(args);
   }
}
