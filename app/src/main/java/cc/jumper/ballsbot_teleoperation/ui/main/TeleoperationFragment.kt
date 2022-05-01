package cc.jumper.ballsbot_teleoperation.ui.main

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import cc.jumper.ballsbot_teleoperation.R
import cc.jumper.ballsbot_teleoperation.TeleoperationApplication
import cc.jumper.ballsbot_teleoperation.data.Connection
import cc.jumper.ballsbot_teleoperation.databinding.EditConnectionFragmentBinding
import cc.jumper.ballsbot_teleoperation.databinding.FragmentTeleoperationBinding
import cc.jumper.ballsbot_teleoperation.models.ConnectionsViewModel
import cc.jumper.ballsbot_teleoperation.models.ConnectionsViewModelFactory
import cc.jumper.ballsbot_teleoperation.models.TeleoperationViewModel
import cc.jumper.ballsbot_teleoperation.models.TeleoperationViewModelFactory

class TeleoperationFragment : Fragment() {
    private val hideHandler = Handler()

    @Suppress("InlinedApi")
    private val hidePart2Runnable = Runnable {
        val flags =
            View.SYSTEM_UI_FLAG_LOW_PROFILE or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        activity?.window?.decorView?.systemUiVisibility = flags
        (activity as? AppCompatActivity)?.supportActionBar?.hide()
    }
    private var visible: Boolean = false
    private val hideRunnable = Runnable { hide() }

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private val delayHideTouchListener = View.OnTouchListener { _, _ ->
        if (AUTO_HIDE) {
            delayedHide(AUTO_HIDE_DELAY_MILLIS)
        }
        false
    }

    private var fullscreenContent: View? = null

    private var _binding: FragmentTeleoperationBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val navigationArgs: EditConnectionFragmentArgs by navArgs()

    private val viewModelConnection: ConnectionsViewModel by activityViewModels {
        ConnectionsViewModelFactory(
            (activity?.application as TeleoperationApplication).database.connectionDao()
        )
    }

    private val viewModelTeleoperation: TeleoperationViewModel by activityViewModels {
        TeleoperationViewModelFactory()
    }

    private var connection: Connection? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentTeleoperationBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        visible = true

        // Set up the user interaction to manually show or hide the system UI.
        fullscreenContent?.setOnClickListener { toggle() }

        val connectionId = navigationArgs.connectionId
        viewModelConnection.getConnection(connectionId)
            .observe(this.viewLifecycleOwner) { selectedConnection ->
                connection = selectedConnection
                viewModelTeleoperation.connectionInfo = selectedConnection
                if (!viewModelTeleoperation.updatesRunning()) {
                    viewModelTeleoperation.startUpdates()
                }
            }

        viewModelTeleoperation.cameraImages.observe(this.viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                binding.frontCamera.setImageBitmap(it[0])
                if (it.size > 1) {
                    binding.rearCamera.setImageBitmap(it[1])
                }
            }
        }

        binding.viewModelConnection = viewModelConnection
        binding.viewModelTeleoperation = viewModelTeleoperation
        binding.thisFragment = this
    }

    override fun onResume() {
        super.onResume()
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100)
    }

    override fun onPause() {
        super.onPause()
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        // Clear the systemUiVisibility flag
        activity?.window?.decorView?.systemUiVisibility = 0
        show()
    }

    override fun onDestroy() {
        super.onDestroy()
        fullscreenContent = null
    }

    private fun toggle() {
        if (visible) {
            hide()
        } else {
            show()
        }
    }

    private fun hide() {
        // Hide UI first
        visible = false

        // Schedule a runnable to remove the status and navigation bar after a delay
        hideHandler.postDelayed(hidePart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    @Suppress("InlinedApi")
    private fun show() {
        // Show the system bar
        fullscreenContent?.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        visible = true

        // Schedule a runnable to display UI elements after a delay
        hideHandler.removeCallbacks(hidePart2Runnable)
        (activity as? AppCompatActivity)?.supportActionBar?.show()
    }

    /**
     * Schedules a call to hide() in [delayMillis], canceling any
     * previously scheduled calls.
     */
    private fun delayedHide(delayMillis: Int) {
        hideHandler.removeCallbacks(hideRunnable)
        hideHandler.postDelayed(hideRunnable, delayMillis.toLong())
    }

    companion object {
        /**
         * Whether or not the system UI should be auto-hidden after
         * [AUTO_HIDE_DELAY_MILLIS] milliseconds.
         */
        private const val AUTO_HIDE = true

        /**
         * If [AUTO_HIDE] is set, the number of milliseconds to wait after
         * user interaction before hiding the system UI.
         */
        private const val AUTO_HIDE_DELAY_MILLIS = 3000

        /**
         * Some older devices needs a small delay between UI widget updates
         * and a change of the status and navigation bar.
         */
        private const val UI_ANIMATION_DELAY = 300
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        viewModelTeleoperation.stopUpdates()
    }
}