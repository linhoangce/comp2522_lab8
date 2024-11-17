package ca.bcit.comp2522.lab8;

import java.util.*;

public class Quiz
{
   private final Map<String, String> quizMap;
   private final List<String> questionsList;
   private int score;

   public Quiz()
   {
      this.quizMap = new HashMap<>();
      this.questionsList = new ArrayList<>();
      this.score = 0;
   }

   public Map<String, String> getQuizMap()
   {
      return quizMap;
   }

   public List<String> getQuestionsList()
   {
      return questionsList;
   }

   public int getScore()
   {
      return score;
   }

   public void loadQuiz(final Map<String, String> quizMap)
   {
      if(quizMap != null)
      {
         this.quizMap.putAll(quizMap);
         this.questionsList.addAll(quizMap.keySet());
      }
   }

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
