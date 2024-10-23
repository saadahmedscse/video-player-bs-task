package com.saadahmedev.videoplayer.base

import android.content.IntentFilter
import android.net.ConnectivityManager
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
import androidx.viewbinding.ViewBinding
import com.google.android.material.snackbar.Snackbar
import com.saadahmedev.videoplayer.R
import com.saadahmedev.videoplayer.receiver.NetworkChangeListener
import com.saadahmedev.videoplayer.receiver.NetworkChangeReceiver
import com.saadahmedev.videoplayer.ui.CustomToolbarViewModel
import com.saadahmedev.videoplayer.ui.MainActivityViewModel
import com.saadahmedev.videoplayer.util.isNetworkAvailable

abstract class BaseFragment<VM : BaseViewModel, BINDING: ViewBinding>(private val bindingInflater: (inflater: LayoutInflater) -> BINDING) : Fragment(), NetworkChangeListener {

    private val customToolbarViewModel by activityViewModels<CustomToolbarViewModel>()
    protected val sharedViewModel by activityViewModels<MainActivityViewModel>()
    private lateinit var _binding: BINDING
    protected val binding: BINDING get() = _binding
    protected abstract val toolbarTitle: String?
    protected abstract val viewmodel: VM

    private lateinit var networkChangeReceiver: NetworkChangeReceiver
    private var isInternetAvailable: Boolean = false

    protected abstract fun onFragmentCreate(savedInstanceState: Bundle?)
    protected open fun initView() {}
    protected open fun clickListeners() {}
    protected abstract fun observeData()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = bindingInflater.invoke(layoutInflater)

        isInternetAvailable = requireContext().isNetworkAvailable()
        networkChangeReceiver = NetworkChangeReceiver(this)

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
        clickListeners()
        return _binding.root
    }

    override fun onStart() {
        super.onStart()
        val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        requireActivity().registerReceiver(networkChangeReceiver, intentFilter)
    }

    override fun onStop() {
        super.onStop()
        requireActivity().unregisterReceiver(networkChangeReceiver)
    }

    override fun onNetworkAvailable() {
        if (!isInternetAvailable) {
            isInternetAvailable = true
            "Internet connection restored".showSnackBar()
        }
    }

    override fun onNetworkLost() {
        if (isInternetAvailable) {
            isInternetAvailable = false
            "No internet connection".showSnackBar()
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