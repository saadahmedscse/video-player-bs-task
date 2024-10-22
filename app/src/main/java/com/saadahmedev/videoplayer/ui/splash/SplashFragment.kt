package com.saadahmedev.videoplayer.ui.splash

import android.animation.Animator
import android.os.Bundle
import androidx.fragment.app.viewModels
import com.saadahmedev.videoplayer.R
import com.saadahmedev.videoplayer.base.BaseFragment
import com.saadahmedev.videoplayer.databinding.FragmentSplashBinding

class SplashFragment : BaseFragment<SplashViewModel, FragmentSplashBinding>(FragmentSplashBinding::inflate), Animator.AnimatorListener {

    override val toolbarTitle: String? get() = null
    override val viewmodel: SplashViewModel by viewModels()

    override fun onFragmentCreate(savedInstanceState: Bundle?) {
        binding.lottieAnimation.addAnimatorListener(this)
    }

    override fun observeData() {}

    override fun onAnimationStart(animation: Animator) {}

    override fun onAnimationEnd(animation: Animator) {
        navigate(
            destination = R.id.action_splashFragment_to_homeFragment,
            popSelf = true
        )
    }

    override fun onAnimationCancel(animation: Animator) {}

    override fun onAnimationRepeat(animation: Animator) {}
}