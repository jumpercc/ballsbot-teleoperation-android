package cc.jumper.ballsbot_teleoperation.ui.main

import android.R
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import cc.jumper.ballsbot_teleoperation.TeleoperationApplication
import cc.jumper.ballsbot_teleoperation.data.Connection
import cc.jumper.ballsbot_teleoperation.databinding.FragmentTeleoperationBinding
import cc.jumper.ballsbot_teleoperation.models.ConnectionsViewModel
import cc.jumper.ballsbot_teleoperation.models.ConnectionsViewModelFactory
import cc.jumper.ballsbot_teleoperation.models.TeleoperationViewModel
import cc.jumper.ballsbot_teleoperation.models.TeleoperationViewModelFactory


class TeleoperationFragment : Fragment() {
    private var _binding: FragmentTeleoperationBinding? = null
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
    ): View {
        _binding = FragmentTeleoperationBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val connectionId = navigationArgs.connectionId
        viewModelConnection.getConnection(connectionId)
            .observe(this.viewLifecycleOwner) { selectedConnection ->
                connection = selectedConnection
                (activity as AppCompatActivity?)?.supportActionBar?.title =
                    selectedConnection.description
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

        updateLayout(resources.configuration)
    }

    private fun updateLayout(newConfig: Configuration) {
        // TODO
//        val paramsFront = binding.frontCamera.layoutParams as ConstraintLayout.LayoutParams
//        val paramsRear = binding.rearCamera.layoutParams as ConstraintLayout.LayoutParams
//
//        paramsFront.startToStart = binding.parentLayout.id
//        paramsFront.topToTop = binding.parentLayout.id
//
//        paramsRear.endToEnd = binding.parentLayout.id
//        paramsRear.bottomToBottom = binding.parentLayout.id
//
//        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            paramsFront.bottomToBottom = binding.parentLayout.id
//            paramsFront.endToStart = binding.rearCamera.id
//
//            paramsRear.topToTop = binding.parentLayout.id
//            paramsRear.startToEnd = binding.frontCamera.id
//        } else {
//            paramsFront.bottomToTop = binding.rearCamera.id
//            paramsFront.endToEnd = binding.parentLayout.id
//
//            paramsRear.topToBottom = binding.frontCamera.id
//            paramsRear.startToStart = binding.parentLayout.id
//        }
//
//        binding.parentLayout.invalidate()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateLayout(newConfig)
    }

    override fun onResume() {
        super.onResume()
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
    }

    override fun onPause() {
        super.onPause()
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        viewModelTeleoperation.stopUpdates()
    }
}