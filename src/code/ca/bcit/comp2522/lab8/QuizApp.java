package ca.bcit.comp2522.lab8;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

public class QuizApp extends Application
{
   public static final int QUIZ_ROUND = 10;

   public void start(final Stage primaryStage)
   {
      final VBox vbox;
      final Scene scene;
      
      vbox = createLayout();
      scene = new Scene(vbox, 600, 400);
      scene.getStylesheets().add("/main/resources/style.css");

      primaryStage.setScene(scene);
      primaryStage.setTitle("Simple Quiz App");
      primaryStage.show();
   }

   private VBox createLayout()
   {
      final Label label;
      final Button startBtn;
      final Button submitBtn;
      final VBox vbox;
      final TextField textField;
      final Text scoreField;
      final TextArea textArea;

      label = new Label("Press 'Start' to begin!");
      startBtn = new Button("Start Quiz");
      submitBtn = new Button("Submit");
      textField = new TextField();
      scoreField = new Text("Score: ");
      textArea = new TextArea();

      textArea.setVisible(false);
      startBtn.setOnAction(e -> {
         startQuiz(label, textField, startBtn, scoreField, textArea);
         startBtn.setDisable(true);
      });
      startBtn.setDisable(false);
      vbox = new VBox(15, label, textField, submitBtn, startBtn, scoreField, textArea);
      
      return vbox;
   }

   private void startQuiz(final Label label,
                          final TextField textField,
                          final Button submitBtn,
                          final Text scoreDisplay,
                          final TextArea textArea)
   {
      final Quiz quiz;
      final List<String> questionsList;
      final EventHandler<ActionEvent> onSubmit;
      final int[] currentQuiz = {0};
      final String[] currentQuestion = new String[1];

      quiz = new Quiz();
      questionsList = new ArrayList<>();

      try
      {
         final Map<String, String> map;

         map = readQuizFile();

         if(map != null)
         {
            quiz.loadQuiz(map);
         }
      }
      catch(final IOException e)
      {
         System.out.println("Error initialing quiz map " + e.getMessage());
      }

      currentQuestion[0] = quiz.getRandomQuestion();
      questionsList.add(currentQuestion[0]);
      label.setText(currentQuestion[0]);

      onSubmit = (event) -> {
        final String userAnswer;

        userAnswer = textField.getText();

        quiz.checkUserAnswer(userAnswer, currentQuestion[0]);
        scoreDisplay.setText("Score: " + quiz.getScore());

        currentQuiz[0]++;

        if(currentQuiz[0] < QUIZ_ROUND)
        {
           questionsList.add(currentQuestion[0]);
           currentQuestion[0] = quiz.getRandomQuestion();
           label.setText(currentQuestion[0]);
           textField.clear();
        }
        else
        {
           label.setText("Quiz Complete! Final Score: " + quiz.getScore() + "/10");
           textField.setDisable(true);
           submitBtn.setDisable(true);
           displayQuizAnswerKeys(questionsList, quiz.getQuizMap(), textArea);
        }
      };

      textField.setOnAction(onSubmit);
      submitBtn.setOnAction(onSubmit);
   }

   private void displayQuizAnswerKeys(final List<String> questionsList,
                                      final Map<String, String> questionAnswerMap,
                                      final TextArea textArea)
   {
      for(final String q : questionsList)
      {
         final String answer;
         answer = questionAnswerMap.get(q);

         textArea.appendText("Q: " + q + System.lineSeparator());
         textArea.appendText("A: " + answer + System.lineSeparator());
         textArea.appendText(System.lineSeparator());
      }

      textArea.setVisible(true);
   }


//   private void submitAnswer(final TextField textField,
//                             final String question,
//                             final Quiz quiz,
//                             final Text scoreDisplay)
//   {
//      final String userAnswer;
//      final int score;
//      final boolean isCorrect;
//
//      userAnswer = textField.getText();
//      isCorrect = quiz.checkUserAnswer(question, userAnswer);
//      score = quiz.getScore();
//
//      if(isCorrect)
//      {
//         scoreDisplay.setText("Score: " + score);
//      }
//
//      textField.clear();
//   }

   private boolean checkAnswer(final String userAnswer,
                              final String question,
                              final Map<String, String> quizMap)
   {
      final String correctAnswer;

      correctAnswer = quizMap.get(question);

      return userAnswer.trim().equalsIgnoreCase(correctAnswer);

   }


   private void initQuizInterface()
   {

   }



   private Map<String, String> readQuizFile() throws IOException {
      final Map<String, String> questionMap;
      final Path filePath;

      questionMap = new HashMap<>();
      filePath = Paths.get("src", "main", "resources", "quiz.txt");

      if(Files.notExists(filePath))
      {
         throw new IOException("Files does not exist! " + filePath);
      }

      try
      {
         readFileToMap(filePath, questionMap);
      }
      catch(final IOException e)
      {
         System.out.println("Error mapping file content to Question Map! " + e.getMessage());
      }

      return questionMap;
   }

   private void readFileToMap(final Path filePath,
                              final Map<String, String> questionMap) throws IOException
   {
      if(Files.notExists(filePath))
      {
         throw new IOException("File does not exist" + filePath);
      }

      try(final Stream<String> lines = Files.lines(filePath))
      {
         filteredLine(lines).forEach(line -> putQuestionAnswerToMap(questionMap, line));
      }
      catch(final IOException e)
      {
         System.out.println("Error reading file! " + e.getMessage());
      }
   }

   private void putQuestionAnswerToMap(Map<String, String> map,
                                       final String questionAnswer)
   {
      final int questionIndex;
      final int answerIndex;
      final String[] parsedQuestionAnswer;

      questionIndex = 0;
      answerIndex   = 1;
      parsedQuestionAnswer = parseQuestionAnswerString(questionAnswer);

      map.put(parsedQuestionAnswer[questionIndex], parsedQuestionAnswer[answerIndex]);
   }

   private String[] parseQuestionAnswerString(final String line)
   {
      String[] questionAnswer;

      questionAnswer = null;

      if(line != null && !line.isBlank())
      {
         questionAnswer = line.trim().split("\\|");
      }

      if(questionAnswer == null) {
         System.out.println("Error parsing the string");
      }

      return questionAnswer;
   }

   private static Stream<String> filteredLine(final Stream<String> lines)
   {
      return lines.filter(line -> line != null)
              .filter(line -> !line.isBlank());
   }


}
