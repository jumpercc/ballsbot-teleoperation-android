package cc.jumper.ballsbot_teleoperation.ui.main

import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import cc.jumper.ballsbot_teleoperation.models.GameControllerViewModel
import cc.jumper.ballsbot_teleoperation.models.GameControllerViewModelFactory
import cc.jumper.ballsbot_teleoperation.databinding.TestGameControllerFragmentBinding
import cc.jumper.ballsbot_teleoperation.models.AxisName
import cc.jumper.ballsbot_teleoperation.models.KeyName

class TestGameControllerFragment : Fragment() {

    private var _binding: TestGameControllerFragmentBinding? = null
    private val binding get() = _binding!!

    private val viewModel: GameControllerViewModel by activityViewModels {
        GameControllerViewModelFactory()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = TestGameControllerFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel
        binding.thisFragment = this

        viewModel.keyStates.observe(this.viewLifecycleOwner) { keyStates ->
            binding.buttonA.setText(
                keyStates[KeyName.KEY_A].toString(),
                TextView.BufferType.SPANNABLE
            )
            binding.buttonB.setText(
                keyStates[KeyName.KEY_B].toString(),
                TextView.BufferType.SPANNABLE
            )
            binding.buttonX.setText(
                keyStates[KeyName.KEY_X].toString(),
                TextView.BufferType.SPANNABLE
            )
            binding.buttonY.setText(
                keyStates[KeyName.KEY_Y].toString(),
                TextView.BufferType.SPANNABLE
            )
            binding.buttonLt.setText(
                keyStates[KeyName.KEY_LT].toString(),
                TextView.BufferType.SPANNABLE
            )
            binding.buttonLb.setText(
                keyStates[KeyName.KEY_LB].toString(),
                TextView.BufferType.SPANNABLE
            )
            binding.buttonRt.setText(
                keyStates[KeyName.KEY_RT].toString(),
                TextView.BufferType.SPANNABLE
            )
            binding.buttonRb.setText(
                keyStates[KeyName.KEY_RB].toString(),
                TextView.BufferType.SPANNABLE
            )
        }
        viewModel.axisStates.observe(this.viewLifecycleOwner) { axisStates ->
            binding.axisLx.setText(
                axisStates[AxisName.AXIS_L_HORIZONTAL].toString(),
                TextView.BufferType.SPANNABLE
            )
            binding.axisLy.setText(
                axisStates[AxisName.AXIS_L_VERTICAL].toString(),
                TextView.BufferType.SPANNABLE
            )
            binding.axisRx.setText(
                axisStates[AxisName.AXIS_R_HORIZONTAL].toString(),
                TextView.BufferType.SPANNABLE
            )
            binding.axisRy.setText(
                axisStates[AxisName.AXIS_R_VERTICAL].toString(),
                TextView.BufferType.SPANNABLE
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}