package ca.bcit.comp2522.lab8;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Represents a quiz application that manages questions, answers, and scoring.
 * The Quiz class allows loading questions, fetching a random question, and
 * checking user answers.
 *
 * @version 1.0
 * @author Linh Hoang
 * @author Pouyan Norouzi
 */
public class Quiz
{
   private final Map<String, String> quizMap;
   private final List<String> questionsList;
   private int score;

   /**
    * Constructs a new Quiz object with an empty question set and a score of 0.
    */
   public Quiz()
   {
      this.quizMap = new HashMap<>();
      this.questionsList = new ArrayList<>();
      this.score = 0;
   }

   /**
    * Retrieves the mapping of questions to their correct answers.
    *
    * @return a Map containing questions as keys and answers as values.
    */
   public Map<String, String> getQuizMap()
   {
      return quizMap;
   }

   /**
    * Retrieves the list of questions available in the quiz.
    *
    * @return a List of questions as Strings.
    */
   public List<String> getQuestionsList()
   {
      return questionsList;
   }

   /**
    * Retrieves the current score of the quiz.
    *
    * @return the score as an integer.
    */
   public int getScore()
   {
      return score;
   }

   /**
    * Loads a set of questions and answers into the quiz.
    *
    * @param quizMap a Map containing questions as keys and answers as values.
    *                If the provided map is null, the method does nothing.
    */
   public void loadQuiz(final Map<String, String> quizMap)
   {
      if(quizMap != null)
      {
         this.quizMap.putAll(quizMap);
         this.questionsList.addAll(quizMap.keySet());
      }
   }

   /**
    * Retrieves a random question from the quiz.
    *
    * @return a randomly selected question as a String.
    */
   public String getRandomQuestion()
   {
      final Random rand;
      final String question;
      final int size;
      final int index;

      rand = new Random();
      size = questionsList.size();
      index = rand.nextInt(size);
      question = questionsList.get(index);

      return question;
   }

   /**
    * Checks the user's answer against the correct answer for a given question.
    * If the answer is correct, increments the score.
    *
    * @param answer   the user's answer as a String.
    * @param question the question to check the answer against.
    */
   public void checkUserAnswer(final String answer, final String question)
   {
      final String correctAnswer;
      correctAnswer = quizMap.get(question);

      if(answer.trim().equalsIgnoreCase(correctAnswer))
      {
         score++;
      }
   }
}
