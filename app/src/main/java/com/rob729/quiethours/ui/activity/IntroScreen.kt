package com.rob729.quiethours.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.github.appintro.AppIntro
import com.github.appintro.AppIntroCustomLayoutFragment
import com.github.appintro.AppIntroPageTransformerType
import com.rob729.quiethours.R
import com.rob729.quiethours.util.AppConstants
import com.rob729.quiethours.util.StoreSession
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class IntroScreen : AppIntro() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setColorDoneText(ContextCompat.getColor(this, R.color.textColor))
        setColorSkipButton(ContextCompat.getColor(this, R.color.textColor))
        setNextArrowColor(ContextCompat.getColor(this, R.color.textColor))
        setIndicatorColor(
            selectedIndicatorColor = ContextCompat.getColor(this, R.color.textColor),
            unselectedIndicatorColor = ContextCompat.getColor(this, R.color.unSelected)
        )
        setTransformer(AppIntroPageTransformerType.Fade)
        showStatusBar(true)

        addSlide(AppIntroCustomLayoutFragment.newInstance(R.layout.intro_one))
        addSlide(AppIntroCustomLayoutFragment.newInstance(R.layout.intro_two))
        addSlide(AppIntroCustomLayoutFragment.newInstance(R.layout.intro_three))
    }

    override fun onSkipPressed(currentFragment: Fragment?) {
        super.onSkipPressed(currentFragment)
        navigateToMainActivity()
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        navigateToMainActivity()
    }

    private fun navigateToMainActivity() {
        StoreSession.writeBoolean(AppConstants.APP_INTRO_CHECK, true)
        startActivity(Intent(this@IntroScreen, MainActivity::class.java))
        finish()
    }
}