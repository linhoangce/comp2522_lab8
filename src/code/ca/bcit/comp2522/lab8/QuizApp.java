package ca.bcit.comp2522.lab8;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * A simple JavaFX application that implements a quiz game.
 * The application displays questions, accepts user answers, tracks scores,
 * and provides a time-limited gameplay experience.
 * The quiz questions and answers are loaded from a text file in the format:
 * <pre>
 * Question|Answer
 * </pre>
 *
 * @version 1.0
 * @author Linh Hoang
 * @author Pouyan Norouzi
 */
public class QuizApp extends Application
{
   private static final int QUIZ_ROUND = 10;
   private static final int EASY_TIME_LIMIT = 90;
   private static final int MEDIUM_TIME_LIMIT = 60;
   private static final int HARD_TIME_LIMIT = 45;
   private static final int SCENE_WIDTH  = 600;
   private static final int SCENE_HEIGHT = 400;

   /**
    * Entry point for the JavaFX application.
    *
    * @param primaryStage the primary stage for the application
    */
   public void start(final Stage primaryStage)
   {
      final VBox vbox;
      final Scene scene;
      
      vbox = createLayout();
      vbox.getStyleClass().add("vbox");
      scene = new Scene(vbox, SCENE_WIDTH, SCENE_HEIGHT);
      scene.getStylesheets().add("/style.css");

      primaryStage.setScene(scene);
      primaryStage.setTitle("Simple Quiz App");
      primaryStage.show();
   }

   /**
    * Creates the main layout for the application, including buttons, labels,
    * and input fields.
    *
    * @return a VBox layout containing the UI components
    */
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
      startBtn.setOnAction(e ->
      {
         final int timeLimit;

         timeLimit = switch(difficultyBox.getValue())
         {
            case "Easy" -> EASY_TIME_LIMIT;
            case "Medium" -> MEDIUM_TIME_LIMIT;
            default -> HARD_TIME_LIMIT;
         };

         difficultyBox.setVisible(false);
         startQuiz(label, timerLabel, textField, submitBtn, startBtn, scoreField, textArea, timer, difficultyBox,
                   timeLimit);
      });

      vbox = new VBox(15, label, timerLabel, textField, submitBtn, startBtn, scoreField, difficultyBox, textArea);
      
      return vbox;
   }

   /**
    * Starts the quiz, initializing UI components, loading quiz data, and
    * setting up event handlers for user interaction.
    *
    * @param label         the label to display questions
    * @param timerLabel    the label to display the timer
    * @param textField     the text field for user input
    * @param submitBtn     the submit button for user answers
    * @param startBtn      the start button for starting the quiz
    * @param scoreDisplay  the text component to display the score
    * @param textArea      the text area for showing answer keys
    * @param timer         the timer to track time limit
    * @param difficultyBox the dropdown for difficulty selection
    * @param timeLimit     the time limit for the quiz in seconds
    */
   private void startQuiz(final Label label,
                          final Label timerLabel,
                          final TextField textField,
                          final Button submitBtn,
                          final Button startBtn,
                          final Text scoreDisplay,
                          final TextArea textArea,
                          final Timer timer,
                          final ComboBox<String> difficultyBox,
                          final int timeLimit)
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

      onSubmit = (event) ->
      {
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
         } else
         {
            timer.stopTimer();
            endQuiz(label, textField, submitBtn, startBtn, textArea, difficultyBox, quiz, questionsList);
         }
      };

      timer.startTimer(timeLimit, () ->
      {
         timerLabel.setVisible(true);
         timerLabel.setText("Time's up!");
         textField.setDisable(true);
         submitBtn.setDisable(true);
         endQuiz(label, textField, submitBtn, startBtn, textArea, difficultyBox, quiz, questionsList);
      });

      textField.setOnAction(onSubmit);
      submitBtn.setOnAction(onSubmit);
   }

   /**
    * Ends the quiz and displays the final score and answer keys.
    *
    * @param label         the label to display the final score
    * @param textField     the text field for user input
    * @param submitBtn     the submit button for user answers
    * @param startBtn      the start button for restarting the quiz
    * @param textArea      the text area to display the answer keys
    * @param difficultyBox the dropdown for difficulty selection
    * @param quiz          the Quiz object containing the questions and answers
    * @param questionsList the list of questions asked in the quiz
    */
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

   /**
    * Displays the questions and correct answers from the quiz in a text area.
    *
    * @param questionsList    the list of questions asked in the quiz
    * @param questionAnswerMap the map of questions to answers
    * @param textArea          the text area to display the questions and answers
    */
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

   /**
    * Resets the UI components to their default states for a new quiz round.
    *
    * @param startBtn     the start button
    * @param submitBtn    the submit button
    * @param textField    the text field for user input
    * @param textArea     the text area for displaying answers
    * @param scoreDisplay the text for displaying the score
    */
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

   /**
    * Loads quiz questions and answers from a file and adds them to the Quiz object.
    *
    * @param quiz the Quiz object to load questions into
    */
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
      } catch(final IOException e)
      {
         System.out.println("Error initialing quiz map " + e.getMessage());
      }
   }

   /**
    * Reads the quiz file and returns a map of questions to answers.
    *
    * @return a map containing questions and their respective answers
    * @throws IOException if an error occurs while reading the file
    */
   private Map<String, String> readQuizFile() throws IOException
   {
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
      } catch(final IOException e)
      {
         System.out.println("Error mapping file content to Question Map! " + e.getMessage());
      }

      return questionMap;
   }

   /**
    * Reads a file and populates a map with question-answer pairs.
    *
    * @param filePath      the path to the file to read
    * @param questionMap   the map to populate with question-answer pairs
    * @throws IOException  if the file does not exist or cannot be read
    */
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
      } catch(final IOException e)
      {
         System.out.println("Error reading file! " + e.getMessage());
      }
   }

   /**
    * Parses a line containing a question and an answer, and adds the pair to the map.
    *
    * @param map             the map to populate with the question and answer
    * @param questionAnswer  the string containing the question and answer, separated by '|'
    */
   private void putQuestionAnswerToMap(final Map<String, String> map,
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

   /**
    * Parses a string to extract a question-answer pair.
    *
    * @param line  the line containing the question and answer, separated by '|'
    * @return an array with the question at index 0 and the answer at index 1
    */
   private String[] parseQuestionAnswerString(final String line)
   {
      String[] questionAnswer;

      questionAnswer = null;

      if(line != null && !line.isBlank())
      {
         questionAnswer = line.trim().split("\\|");
      }

      if(questionAnswer == null)
      {
         System.out.println("Error parsing the string");
      }

      return questionAnswer;
   }

   /**
    * Filters a stream of strings, removing null and blank lines.
    *
    * @param lines  the stream of lines to filter
    * @return a filtered stream containing non-null and non-blank lines
    */
   private static Stream<String> filteredLine(final Stream<String> lines)
   {
      return lines.filter(Objects::nonNull)
              .filter(line -> !line.isBlank());
   }
}
