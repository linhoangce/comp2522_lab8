package ca.bcit.comp2522.lab8;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.*;
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
      vbox.getStyleClass().add("vbox");
      scene = new Scene(vbox, 600, 400);
      scene.getStylesheets().add("/style.css");

      primaryStage.setScene(scene);
      primaryStage.setTitle("Simple Quiz App");
      primaryStage.show();
   }

   private VBox createLayout()
   {
      final Label label;
      final Label timerLabel;
      final Button startBtn;
      final Button submitBtn;
      final VBox vbox;
      final TextField textField;
      final Text scoreField;
      final TextArea textArea;
      final ComboBox<String> difficultyBox;
      final Timer timer;

      label = new Label("Press 'Start' to begin!");
      timerLabel = new Label();
      startBtn = new Button("Start Quiz");
      submitBtn = new Button("Submit");
      textField = new TextField();
      scoreField = new Text("Score: ");
      textArea = new TextArea();
      difficultyBox = new ComboBox<>();
      timer = new Timer(timerLabel);

      timerLabel.setVisible(false);
      difficultyBox.getItems().addAll("Easy", "Medium", "Hard");
      difficultyBox.setValue("Easy");

      textField.getStyleClass().add("text-field");
      submitBtn.getStyleClass().add("button");
      startBtn.getStyleClass().add("button");
      label.getStyleClass().add("label");

      timerLabel.setVisible(false);
      textArea.setVisible(false);
      startBtn.setOnAction(e -> {
         final int timeLimit;

         timeLimit = switch(difficultyBox.getValue()) {
            case "Easy" -> 90;
            case "Medium" -> 60;
            default -> 45;
         };

         difficultyBox.setVisible(false);
         startQuiz(label, timerLabel, textField, submitBtn, startBtn, scoreField, textArea, timer, difficultyBox, timeLimit);
      });

      vbox = new VBox(15, label, timerLabel, textField, submitBtn, startBtn, scoreField, difficultyBox, textArea);
      
      return vbox;
   }

   private void startQuiz(final Label label,
                          final Label timerLabel,
                          final TextField textField,
                          final Button submitBtn,
                          final Button startBtn,
                          final Text scoreDisplay,
                          final TextArea textArea,
                          final Timer timer,
                          final ComboBox<String> difficultyBox,
                          final int timeLimit
                          )
   {
      final Quiz quiz;
      final List<String> questionsList;
      final EventHandler<ActionEvent> onSubmit;
      final int[] currentQuiz = {0};
      final String[] currentQuestion;

      quiz = new Quiz();
      questionsList = new ArrayList<>();

      loadQuizFromFile(quiz);

      currentQuestion = new String[]{quiz.getRandomQuestion()};

      // Reset UI Components for New Quiz
      resetUIStates(startBtn, submitBtn, textField, textArea, scoreDisplay);

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
           while(questionsList.contains(currentQuestion[0]))
           {
              currentQuestion[0] = quiz.getRandomQuestion();
           }

           questionsList.add(currentQuestion[0]);

           label.setText(currentQuestion[0]);
           textField.clear();
        }
        else
        {
           timer.stopTimer();
           endQuiz(label, textField, submitBtn, startBtn, textArea, difficultyBox, quiz, questionsList);
        }
      };

      timer.startTimer(timeLimit, () -> {
         timerLabel.setVisible(true);
         timerLabel.setText("Time's up!");
         textField.setDisable(true);
         submitBtn.setDisable(true);
         endQuiz(label, textField, submitBtn, startBtn, textArea, difficultyBox, quiz, questionsList);
      });

      textField.setOnAction(onSubmit);
      submitBtn.setOnAction(onSubmit);
   }

   private void endQuiz(final Label label,
                        final TextField textField,
                        final Button submitBtn,
                        final Button startBtn,
                        final TextArea textArea,
                        final ComboBox<String> difficultyBox,
                        final Quiz quiz,
                        final List<String> questionsList)
   {
      difficultyBox.setVisible(true);
      textField.setDisable(true);
      submitBtn.setDisable(true);
      startBtn.setDisable(false);
      label.setText("Quiz Complete! Final Score: " + quiz.getScore() + "/" + QUIZ_ROUND);
      displayQuizAnswerKeys(questionsList, quiz.getQuizMap(), textArea);

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

   private void resetUIStates(final Button startBtn,
                              final Button submitBtn,
                              final TextField textField,
                              final TextArea textArea,
                              final Text scoreDisplay)
   {
      startBtn.setDisable(true);
      submitBtn.setDisable(false);
      textField.setDisable(false);
      textField.clear();
      textArea.setVisible(false);
      textArea.clear();
      scoreDisplay.setText("Score: 0");
   }

   private void loadQuizFromFile(final Quiz quiz)
   {
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
