package cc.jumper.ballsbot_teleoperation

import android.os.Bundle
import android.view.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import cc.jumper.ballsbot_teleoperation.models.GameControllerViewModel
import cc.jumper.ballsbot_teleoperation.models.GameControllerViewModelFactory
import cc.jumper.ballsbot_teleoperation.models.KeyName
import kotlin.math.abs


class MainActivity : AppCompatActivity(R.layout.main_activity) {
    private lateinit var navController: NavController

    private val viewModelGameController: GameControllerViewModel by viewModels {
        GameControllerViewModelFactory()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        setupActionBarWithNavController(navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun dispatchGenericMotionEvent(ev: MotionEvent?): Boolean {
        if (ev != null) {
            // Check that the event came from a game controller
            if (ev.source and InputDevice.SOURCE_JOYSTICK == InputDevice.SOURCE_JOYSTICK
                && ev.action == MotionEvent.ACTION_MOVE
            ) {

                // Process the movements starting from the
                // earliest historical position in the batch
                (0 until ev.historySize).forEach { i ->
                    // Process the event at historical position i
                    processJoystickInput(ev, i)
                }

                // Process the current movement sample in the batch (position -1)
                processJoystickInput(ev, -1)
                return true
            }
        }
        return super.dispatchGenericMotionEvent(ev)
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            if (myOnKeyChange(event.keyCode, event, true)) {
                return true
            }
        } else if (event.action == KeyEvent.ACTION_UP) {
            if (myOnKeyChange(event.keyCode, event, false)) {
                return true
            }
        }
        return super.dispatchKeyEvent(event)
    }

    private fun myOnKeyChange(keyCode: Int, event: KeyEvent, pressed: Boolean): Boolean {
        var handled = false
        if (event.source and InputDevice.SOURCE_GAMEPAD == InputDevice.SOURCE_GAMEPAD) {
            if (event.repeatCount == 0) {
                when (keyCode) {
                    KeyEvent.KEYCODE_BUTTON_A -> {
                        viewModelGameController.setKeyPressedState(KeyName.KEY_A, pressed)
                        handled = true
                    }
                    KeyEvent.KEYCODE_BUTTON_B -> {
                        viewModelGameController.setKeyPressedState(KeyName.KEY_B, pressed)
                        handled = true
                    }
                    KeyEvent.KEYCODE_BUTTON_X -> {
                        viewModelGameController.setKeyPressedState(KeyName.KEY_X, pressed)
                        handled = true
                    }
                    KeyEvent.KEYCODE_BUTTON_Y -> {
                        viewModelGameController.setKeyPressedState(KeyName.KEY_Y, pressed)
                        handled = true
                    }
                    KeyEvent.KEYCODE_BUTTON_R1 -> {
                        viewModelGameController.setKeyPressedState(KeyName.KEY_RB, pressed)
                        handled = true
                    }
                    KeyEvent.KEYCODE_BUTTON_L1 -> {
                        viewModelGameController.setKeyPressedState(KeyName.KEY_LB, pressed)
                        handled = true
                    }
                }
            }
        }
        return handled
    }

    private fun processJoystickInput(event: MotionEvent, historyPos: Int) {
        val inputDevice = event.device

        val xl = getCenteredAxis(event, inputDevice, MotionEvent.AXIS_X, historyPos)
        val yl = getCenteredAxis(event, inputDevice, MotionEvent.AXIS_Y, historyPos)

        val xr = getCenteredAxis(event, inputDevice, MotionEvent.AXIS_Z, historyPos)
        val yr = getCenteredAxis(event, inputDevice, MotionEvent.AXIS_RZ, historyPos)

        var lt = getTriggerValue(event, MotionEvent.AXIS_BRAKE, historyPos)
        if (lt == 0f) {
            lt = getTriggerValue(event, MotionEvent.AXIS_LTRIGGER, historyPos)
        }

        var rt = getTriggerValue(event, MotionEvent.AXIS_THROTTLE, historyPos)
        if (rt == 0f) {
            rt = getTriggerValue(event, MotionEvent.AXIS_RTRIGGER, historyPos)
        }
        if (rt == 0f) {
            rt = getTriggerValue(event, MotionEvent.AXIS_GAS, historyPos)
        }

        viewModelGameController.setAxisStates(
            xl = xl,
            yl = yl,
            xr = xr,
            yr = yr,
            lt = lt,
            rt = rt,
        )
    }

    private fun getTriggerValue(
        event: MotionEvent,
        axis: Int,
        historyPos: Int
    ): Float {
        return if (historyPos < 0) {
            event.getAxisValue(axis)
        } else {
            event.getHistoricalAxisValue(axis, historyPos)
        }
    }

    private fun getCenteredAxis(
        event: MotionEvent,
        device: InputDevice,
        axis: Int,
        historyPos: Int
    ): Float {
        val range: InputDevice.MotionRange? = device.getMotionRange(axis, event.source)

        // A joystick at rest does not always report an absolute position of
        // (0,0). Use the getFlat() method to determine the range of values
        // bounding the joystick axis center.
        range?.apply {
            val value: Float = if (historyPos < 0) {
                event.getAxisValue(axis)
            } else {
                event.getHistoricalAxisValue(axis, historyPos)
            }

            // Ignore axis values that are within the 'flat' region of the
            // joystick axis center.
            if (abs(value) > flat) {
                return value
            }
        }
        return 0f
    }
}