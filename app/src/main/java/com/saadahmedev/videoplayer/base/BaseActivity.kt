package com.saadahmedev.videoplayer.base

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.viewbinding.ViewBinding
import com.saadahmedev.videoplayer.R
import com.saadahmedev.videoplayer.databinding.CustomToolbarBinding
import com.saadahmedev.videoplayer.databinding.LayoutPopupDialogBinding
import com.saadahmedev.videoplayer.ui.CustomToolbarViewModel
import com.saadahmedev.videoplayer.util.extension.gone
import com.saadahmedev.videoplayer.util.extension.observe
import com.saadahmedev.videoplayer.util.extension.visible

abstract class BaseActivity<VM: BaseViewModel, BINDING : ViewBinding>(private val bindingInflater: (inflater: LayoutInflater) -> BINDING) : AppCompatActivity() {

    private lateinit var _binding: BINDING
    protected val binding: BINDING get() = _binding
    protected abstract val viewmodel: VM
    protected abstract val customToolbarBinding: CustomToolbarBinding
    private val customToolbarViewModel by viewModels<CustomToolbarViewModel>()

    protected abstract fun onActivityCreate(savedInstanceState: Bundle?)
    protected open fun initView() {}
    protected open fun clickListeners() {}
    protected abstract fun observeData()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = bindingInflater.invoke(layoutInflater)
        setContentView(binding.root)
        onActivityCreate(savedInstanceState)
        initToolbar()
        initView()
        observeData()
        clickListeners()
    }

    private fun initToolbar() {
        customToolbarBinding.apply {
            observe(customToolbarViewModel.title) {
                toolbarTitle.text = it
            }

            btnBack.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    fun showToolbar() {
        customToolbarBinding.root.visible()
    }

    fun hideToolbar() {
        customToolbarBinding.root.gone()
    }

    fun showBackButton() {
        customToolbarBinding.btnBack.visible()
    }

    fun hideBackButton() {
        customToolbarBinding.btnBack.gone()
    }

    fun showPopupDialog(
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
        val dialog = Dialog(this)
        val binding: LayoutPopupDialogBinding = DataBindingUtil.inflate(layoutInflater, R.layout.layout_popup_dialog, null, false)

        dialog.apply {
            dialog.setContentView(binding.root)
            setCancelable(cancelable)
        }

        binding.apply {
            tvTitle.text = title
            tvMessage.text = message

            if (icon != null) ivDialogIcon.setImageResource(icon)

            if (!showPositiveButton) {
                btnPositive.gone()
                divider.gone()
            }
            if (!showNegativeButton) {
                btnNegative.gone()
                divider.gone()
            }

            if (positiveButtonText != null) btnPositive.text = positiveButtonText
            if (negativeButtonText != null) btnNegative.text = negativeButtonText

            btnPositive.setOnClickListener {
                positiveButtonAction?.invoke()
                dialog.dismiss()
            }

            btnNegative.setOnClickListener {
                negativeButtonAction?.invoke()
                dialog.dismiss()
            }
        }

        dialog.apply {
            window?.apply {
                setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT
                )

                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            }
            show()
        }
    }
}