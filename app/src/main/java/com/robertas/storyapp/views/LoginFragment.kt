package com.robertas.storyapp.views

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.robertas.storyapp.R
import com.robertas.storyapp.databinding.FragmentLoginBinding
import com.robertas.storyapp.models.domain.User
import com.robertas.storyapp.models.enums.NetworkResult
import com.robertas.storyapp.viewmodels.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : Fragment(), View.OnClickListener {

    private val loginViewModel by viewModels<LoginViewModel>()

    private lateinit var navController: NavController

    private var _binding: FragmentLoginBinding? = null

    private val binding get() = _binding

    private var loginButton: Button ?= null

    private var loadingProgressBar : ProgressBar ?= null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)

        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = findNavController()

        loginButton = binding?.loginBtn

        loadingProgressBar = binding?.loadingProgress

        binding?.registerBtn?.setOnClickListener(this)

        binding?.loginBtn?.setOnClickListener(this)

        setupLoginObserver()
    }

    private fun setupLoginObserver() {
        val loginObserver = Observer<NetworkResult<User?>> { result ->
            when(result) {
                is NetworkResult.Loading -> {}

                is NetworkResult.Error -> {
                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()

                    toggleLoginButton(false)

                    toggleLoadingBar(false)

                    loginViewModel.doneNavigating()
                }

                is NetworkResult.Success -> {
                    val actionToHomeFragment = LoginFragmentDirections.actionLoginFragmentToHomeFragment()

                    navController.navigate(actionToHomeFragment)

                    toggleLoadingBar(false)

                    loginViewModel.doneNavigating()
                }
            }
        }

        loginViewModel.loginState.observe(viewLifecycleOwner, loginObserver)

        if (loginViewModel.isUserLoggedIn()) {
            val actionToHomeFragment = LoginFragmentDirections.actionLoginFragmentToHomeFragment()

            navController.navigate(actionToHomeFragment)
        }
    }

    private fun toggleLoginButton(isEnabled: Boolean){
        loginButton?.isEnabled = !isEnabled
    }

    private fun toggleLoadingBar(isGone: Boolean){
        if (!isGone){
            loadingProgressBar?.visibility = View.GONE
        } else {
            loadingProgressBar?.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        hideKeyBoard()

        _binding = null

        loginButton = null

        loadingProgressBar = null
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.register_btn -> navController.navigate(LoginFragmentDirections.actionLoginFragmentToRegisterFragment())

            R.id.login_btn -> {

                toggleLoginButton(true)

                toggleLoadingBar(true)

                loginViewModel.login(binding?.emailEt?.text.toString(), binding?.passwordEt?.text.toString())
            }

            else -> return
        }
    }

    private fun hideKeyBoard() {
        if (requireActivity().currentFocus == null) {
            return
        }
        val inputMethodManager =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        inputMethodManager.hideSoftInputFromWindow(requireActivity().currentFocus!!.windowToken, 0)
    }
}