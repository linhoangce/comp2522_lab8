import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class QuizApp extends Application
{
   private static final int VBOX_DISTANCE_PIXELS = 10;
   private static final int QUESTION_NUMBER = 10;
   private static final int SCENE_WIDTH = 500;
   private static final int SCENE_HEIGHT = 500;

   @Override
   public void start(final Stage stage)
   {
      VBox root = getRoot();

      Scene scene = new Scene(root, SCENE_WIDTH, SCENE_HEIGHT);
      scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/style.css")).toExternalForm());

      stage.setTitle("Quiz App");
      stage.setScene(scene);
      stage.show();
   }

   private static VBox getRoot()
   {
      Map<String, String> questions;

      final Label question;
      question = new Label("Press the start quiz button to start");

      final TextField userAnswer;
      userAnswer = new TextField();
      userAnswer.setDisable(true);

      final Button submitButton;
      submitButton = new Button("Submit");
      submitButton.setDisable(true);

      final Label scoreLabel;
      scoreLabel = new Label("Your score is: 0");

      final TextArea resultTextArea;
      resultTextArea = new TextArea();
      resultTextArea.setVisible(false);
      resultTextArea.setEditable(false);

      final Button startQuizButton;
      startQuizButton = new Button("Start Quiz");
      startQuizButton.setOnAction(e ->
         startQuiz(question, userAnswer, submitButton, startQuizButton, scoreLabel, resultTextArea));


      final VBox root = new VBox(VBOX_DISTANCE_PIXELS, question, userAnswer, submitButton, startQuizButton, scoreLabel,
                                 resultTextArea);
      root.setOnKeyPressed(e -> submitButton.fire());
      root.setAlignment(Pos.TOP_CENTER);

      return root;
   }

   /**
    *
    * @return
    */
   private static Map<String, String> readQuestionsFromFile()
   {
      final Path quizPath;
      final List<String> lines;
      final Map<String, String> questionMap;

      quizPath = Paths.get("src", "resources", "quiz.txt");

      try
      {
         lines = Files.readAllLines(quizPath);

         questionMap = lines.stream().filter(Objects::nonNull)
            .filter(l -> !l.isBlank())
            .collect(HashMap::new,
               (m, l) ->
               {
                  final String question;
                  final String answer;

                  question = l.substring(0, l.indexOf('|'));
                  answer = l.substring(l.indexOf('|') + 1);

                  m.put(question, answer);
               },
               HashMap::putAll);
         return questionMap;
      } catch(final IOException e)
      {
         System.out.println("Error reading questions file: " + e.getMessage());
         return null;
      }
   }

   private static Map<String, String> getNRandomQuestions(final int n)
   {
      if(n < 0)
      {
         throw new IllegalArgumentException("Number of questions cannot be negative: " + n);
      }

      final Map<String, String> allQuestions;
      final Map<String, String> nQuestions;
      final List<String> questionList;
      final Random random;

      allQuestions = readQuestionsFromFile();
      if(allQuestions == null)
      {
         return null;
      }
      nQuestions = new HashMap<>();
      questionList = allQuestions.keySet().stream().toList();
      random = new Random();

      int i = 0;
      while(i < n)
      {
         final String currQuestion = questionList.get(random.nextInt(0, questionList.size()));

         if(!nQuestions.containsKey(currQuestion))
         {
            nQuestions.put(currQuestion, allQuestions.get(currQuestion));
            i++;
         }
      }

      return nQuestions;
   }

   /**
    * @param questionLabel
    * @param userAnswer
    * @param submitButton
    * @param startQuizButton
    * @param scoreLabel
    * @param resultTextArea
    */
   private static void startQuiz(final Label questionLabel,
                                 final TextField userAnswer,
                                 final Button submitButton,
                                 final Button startQuizButton,
                                 final Label scoreLabel,
                                 final TextArea resultTextArea)
   {
      if(questionLabel == null)
      {
         throw new IllegalArgumentException("question label cannot be null");
      }
      if(userAnswer == null)
      {
         throw new IllegalArgumentException("user answer text field cannot be null");
      }
      if(submitButton == null)
      {
         throw new IllegalArgumentException("submit button cannot be null");
      }
      if(startQuizButton == null)
      {
         throw new IllegalArgumentException("start quiz button cannot be null");
      }


      final Map<String, String> questionMap;
      final Iterator<String> questionIterator;
      final Set<String> userIncorrectQuestions;

      questionMap = getNRandomQuestions(QUESTION_NUMBER);
      userIncorrectQuestions = new HashSet<>();

      if(questionMap == null)
      {
         questionLabel.setText("Error occurred while trying to read the file");
         return;
      }
      questionIterator = questionMap.keySet().iterator();

      submitButton.setDisable(false);
      startQuizButton.setDisable(true);
      userAnswer.setDisable(false);


      final AtomicReference<String> question = new AtomicReference<>(questionIterator.next());
      final AtomicReference<String> answer = new AtomicReference<>(questionMap.get(question.get()));

      scoreLabel.setText("Your score is: 0");
      questionLabel.setText(question.get());

      final AtomicInteger score = new AtomicInteger();
      submitButton.setOnAction(e ->
      {
         if(userAnswer.getText().equalsIgnoreCase(answer.get()))
         {
            score.getAndIncrement();
            scoreLabel.setText("Your score is: " + score);
         } else
         {
            userIncorrectQuestions.add(question.get());
         }

         userAnswer.setText("");
         if(questionIterator.hasNext())
         {
            question.set(questionIterator.next());
            answer.set(questionMap.get(question.get()));

            questionLabel.setText(question.get());
         } else
         {
            questionLabel.setText("Quiz complete! Final score " + score + "/" + QUESTION_NUMBER);
            submitButton.setDisable(true);
            userAnswer.setDisable(true);
            startQuizButton.setDisable(false);

            StringBuilder resultText = new StringBuilder("Missed Questions:\n\n");
            for(final String q : userIncorrectQuestions)
            {
               resultText.append(String.format("Q: %s\n" +
                                                "A: %s\n\n", q, questionMap.get(q)));
            }
            resultTextArea.setVisible(true);
            resultTextArea.setText(resultText.toString());

            e.consume();
         }
      });
   }

   public static void main(final String[] args)
   {
      launch(args);
   }
}
