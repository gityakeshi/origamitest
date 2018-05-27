package origamify.com.origamitest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;

import origamify.com.origamitest.slide.SlideMain;


public class Splashscreen extends Activity {

    ImageButton imageButton;

    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Window window = getWindow();
        window.setFormat(PixelFormat.RGBA_8888);
    }
    /** Called when the activity is first created. */
    Thread splashTread;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);
        StartAnimations();

        imageButton = findViewById(R.id.splash);
        imageButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Context context = getApplicationContext();
                Intent abc = new Intent(context, MainActivity.class);
                context.startActivity(abc);
            }
        });
    }
    private void StartAnimations() {
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.alpha);
        anim.reset();
        LinearLayout l=(LinearLayout) findViewById(R.id.lin_lay);
        l.clearAnimation();
        l.startAnimation(anim);

        anim = AnimationUtils.loadAnimation(this, R.anim.translate);
        anim.reset();
        ImageButton iv = (ImageButton) findViewById(R.id.splash);
        iv.clearAnimation();
        iv.startAnimation(anim);

//        splashTread = new Thread() {
//            @Override
//            public void run() {
//                try {
//                    int waited = 0;
//                    // Splash screen pause time
//                    while (waited < 3500) {
//                        sleep(100);
//                        waited += 100;
//                    }
//                    Intent intent = new Intent(Splashscreen.this,
//                            MainActivity.class);
//                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
//                    startActivity(intent);
//                    Splashscreen.this.finish();
//                } catch (InterruptedException e) {
//                    // do nothing
//                } finally {
//                    Splashscreen.this.finish();
//                }
//
//            }
//        };
//        splashTread.start();

    }

}