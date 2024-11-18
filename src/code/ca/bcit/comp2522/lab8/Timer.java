package ca.bcit.comp2522.lab8;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Label;
import javafx.util.Duration;

public class Timer
{
   private final Label timer;
   private Timeline timeline;
   private  int timeRemained;

   public Timer(final Label timer)
   {
      this.timer = timer;
   }

   public void startTimer(final int timeLimit, final Runnable onTimerEnd)
   {
      timeRemained = timeLimit;
      timer.setVisible(true);
      updateTimer();

      timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
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

   public void stopTimer()
   {
      if(timeline != null)
      {
         timeline.stop();
      }
   }

   public void resetTimer()
   {
      if(timeline != null)
      {
         timeline.stop();
      }

      timeRemained = 0;
      updateTimer();
   }

   public void updateTimer()
   {
      timer.setText("Time remained: " + timeRemained + "s");
   }
}
