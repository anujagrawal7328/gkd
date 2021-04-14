package store.gharkidukaan.ghar_ki_dukaan;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.ViewPropertyAnimatorCompat;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.transition.Fade;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.PathInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;

public class SplashScreen extends AppCompatActivity {
     ImageView splashImage;
    public static final int STARTUP_DELAY = 300;
    public static final int ANIM_ITEM_DURATION = 1000;
    public static final int ITEM_DELAY = 300;

  FrameLayout info_layout;
    ImageView splashscreen,info1,info2;
    private boolean animationStarted = false;
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
       getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
        info_layout=(FrameLayout)findViewById(R.id.info_layout_main) ;
        splashscreen = (ImageView) findViewById(R.id.SplashScreen);
       info1 = (ImageView) findViewById(R.id.info_Image1);
        info2 = (ImageView) findViewById(R.id.info_Image2);
        Glide.with(this).load(R.drawable.s1).into(splashscreen);
        Glide.with(this).load(R.drawable.sad).into(info1);
        Glide.with(this).load(R.drawable.safe).into(info2);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(SplashScreen.this, MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivityForResult(i, 0);
                overridePendingTransition(0,0);
                finish();
            }
        }, 2000);



    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {


        if (!hasFocus || animationStarted) {
            return;
        }

        animate();

        super.onWindowFocusChanged(hasFocus);
    }
    private void animate() {


        TranslateAnimation b= new TranslateAnimation(0,0,splashscreen.getHeight()/2-35,0);
        b.setDuration(1000);
        b.setInterpolator(new DecelerateInterpolator(1.2f));
        splashscreen.startAnimation(b);

        TranslateAnimation a= new TranslateAnimation(-info_layout.getWidth()/2,0,0,0);
        a.setDuration(1000);
        a.setInterpolator(new DecelerateInterpolator(1.2f));
        info_layout.startAnimation(a);
    }
}

