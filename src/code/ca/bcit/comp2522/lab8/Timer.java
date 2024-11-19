package ca.bcit.comp2522.lab8;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Label;
import javafx.util.Duration;


/**
 * A class to manage a countdown timer with visual updates and callbacks.
 *
 * @version 1.0
 * @author Linh Hoang
 * @author Pouyan Norouzi
 */
public class Timer
{
   private final Label timer;
   private Timeline timeline;
   private  int timeRemained;

   /**
    * Constructs a Timer with a label to display the countdown.
    *
    * @param timer the label used to display the remaining time
    */
   public Timer(final Label timer)
   {
      this.timer = timer;
   }

   /**
    * Starts the timer countdown.
    *
    * @param timeLimit   the duration of the timer in seconds
    * @param onTimerEnd  a callback to execute when the timer reaches zero
    */
   public void startTimer(final int timeLimit, final Runnable onTimerEnd)
   {
      timeRemained = timeLimit;
      timer.setVisible(true);
      updateTimer();

      timeline = new Timeline(new KeyFrame(Duration.seconds(1), e ->
      {
         timeRemained--;
         updateTimer();

         if(timeRemained <= 0)
         {
            timeline.stop();
            onTimerEnd.run();
         }
      }));

      timeline.setCycleCount(Animation.INDEFINITE);
      timeline.play();
   }

   /**
    * Stops the timer.
    */
   public void stopTimer()
   {
      if(timeline != null)
      {
         timeline.stop();
      }
   }


   /**
    * Resets the timer to zero and updates the label.
    */
   public void resetTimer()
   {
      if(timeline != null)
      {
         timeline.stop();
      }

      timeRemained = 0;
      updateTimer();
   }

   /**
    * Updates the label to reflect the current remaining time.
    */
   public void updateTimer()
   {
      timer.setText("Time remained: " + timeRemained + "s");
   }
}
