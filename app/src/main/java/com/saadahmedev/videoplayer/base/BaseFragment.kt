package com.saadahmedev.videoplayer.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewbinding.ViewBinding
import com.google.android.material.snackbar.Snackbar
import com.saadahmedev.videoplayer.R
import com.saadahmedev.videoplayer.ui.CustomToolbarViewModel
import com.saadahmedev.videoplayer.ui.MainActivityViewModel
import com.saadahmedev.videoplayer.ui.splash.SplashFragment
import com.saadahmedev.videoplayer.util.extension.delay
import com.saadahmedev.videoplayer.util.extension.observe

abstract class BaseFragment<VM : BaseViewModel, BINDING: ViewBinding>(private val bindingInflater: (inflater: LayoutInflater) -> BINDING) : Fragment() {

    private val customToolbarViewModel by activityViewModels<CustomToolbarViewModel>()
    protected val sharedViewModel by activityViewModels<MainActivityViewModel>()
    private lateinit var _binding: BINDING
    protected val binding: BINDING get() = _binding
    protected abstract val toolbarTitle: String?
    protected abstract val viewmodel: VM
    var swipeRefreshLayout: SwipeRefreshLayout? = null

    protected abstract fun onFragmentCreate(savedInstanceState: Bundle?)
    protected open fun initView() {}
    protected open fun clickListeners() {}
    protected abstract fun observeData()

    private companion object {
        private const val SWIPE_REFRESH_DISMISS_DELAY = 1500L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = bindingInflater.invoke(layoutInflater)
        onFragmentCreate(savedInstanceState)
        observeData()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        customToolbarViewModel.setTitle(toolbarTitle)
        initView()
        initToolbarBehaviour()
        internalObserver.invoke()
        internalListener.invoke()
        clickListeners()
        return _binding.root
    }

    private val internalObserver: () -> Unit = {
        observe(viewmodel.onSwipeRefresh) {
            it.peekContent?.let { isRefreshing ->
                swipeRefreshLayout?.isRefreshing = isRefreshing
                isRefreshing.delay(SWIPE_REFRESH_DISMISS_DELAY) {
                    swipeRefreshLayout?.isRefreshing = false
                }
            }
        }
    }

    private val internalListener: () -> Unit = {
        swipeRefreshLayout?.setOnRefreshListener {
            viewmodel.onSwipeRefreshed()
        }
    }

    private fun initToolbarBehaviour() {
        val activity = requireActivity()

        if (activity is BaseActivity<*, *>) {
            when (findNavController().currentDestination?.id) {
                R.id.splashFragment -> activity.hideToolbar()
                else -> activity.showToolbar()
            }
        }
    }

    protected fun navigate(@IdRes destination: Int, popSelf: Boolean = false) {
        val navOptions = if (popSelf) {
            NavOptions.Builder()
                .setPopUpTo(
                    findNavController().currentDestination?.id ?: return,
                    true
                )
                .build()
        } else {
            null
        }

        findNavController().navigate(destination, null, navOptions)
    }

    protected fun showPopupDialog(
        title: String,
        message: String,
        @DrawableRes icon: Int? = null,
        cancelable: Boolean = false,
        showPositiveButton: Boolean = true,
        showNegativeButton: Boolean = true,
        positiveButtonText: String? = null,
        negativeButtonText: String? = null,
        positiveButtonAction: (() -> Unit)? = null,
        negativeButtonAction: (() -> Unit)? = null
    ) {
        val activity = requireActivity()
        if (activity is BaseActivity<*, *>) {
            activity.showPopupDialog(
                title = title,
                message = message,
                icon = icon,
                cancelable = cancelable,
                showPositiveButton = showPositiveButton,
                showNegativeButton = showNegativeButton,
                positiveButtonText = positiveButtonText,
                negativeButtonText = negativeButtonText,
                positiveButtonAction = positiveButtonAction,
                negativeButtonAction = negativeButtonAction
            )
        }
    }

    protected fun String.showSnackBar() {
        Snackbar.make(requireView(), this, Snackbar.LENGTH_SHORT).show()
    }
}