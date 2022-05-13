package cc.jumper.ballsbot_teleoperation.ui.main

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import cc.jumper.ballsbot_teleoperation.R
import cc.jumper.ballsbot_teleoperation.TeleoperationApplication
import cc.jumper.ballsbot_teleoperation.data.Connection
import cc.jumper.ballsbot_teleoperation.databinding.FragmentTeleoperationBinding
import cc.jumper.ballsbot_teleoperation.models.*
import cc.jumper.ballsbot_teleoperation.ui.views.ManipulatorProjection


class TeleoperationFragment : Fragment() {
    private var _binding: FragmentTeleoperationBinding? = null
    private val binding get() = _binding!!
    private lateinit var batteryChargeIcon: MenuItem

    private val navigationArgs: EditConnectionFragmentArgs by navArgs()

    private val viewModelConnection: ConnectionsViewModel by activityViewModels {
        ConnectionsViewModelFactory(
            (activity?.application as TeleoperationApplication).database.connectionDao()
        )
    }

    private val viewModelTeleoperation: TeleoperationViewModel by activityViewModels {
        TeleoperationViewModelFactory()
    }

    private val viewModelGameController: GameControllerViewModel by activityViewModels {
        GameControllerViewModelFactory()
    }

    private var connection: Connection? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

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
        viewModelTeleoperation.botState.observe(this.viewLifecycleOwner) {
            if (it != null) {
                val settings = viewModelTeleoperation.botSettings.value!!

                if (settings.lidar) {
                    binding.lidarView.updateCloud(
                        it.bot_size,
                        it.lidar,
                    )
                }

                if (settings.manipulator) {
                    binding.mainpulatorXy.updatePose(
                        it.bot_size,
                        it.manipulator!!
                    )
                    binding.mainpulatorXz.setProjection(ManipulatorProjection.XZ)
                    binding.mainpulatorXz.updatePose(
                        it.bot_size,
                        it.manipulator
                    )

                    if (settings.distance_sensors.contains("manipulator")) {
                        val manipulatorDistance = if (it.distance_sensors["manipulator"] != null) {
                            String.format("%.2f", it.distance_sensors["manipulator"]?.distance)
                        } else {
                            "-1"
                        }
                        binding.mainpulatorDistance.text =
                            getString(R.string.manipulator_distance, manipulatorDistance)
                    }
                }

                if (settings.ups) {
                    setBatteryChargeIcon(it.ups!!)
                }
            }
        }

        viewModelTeleoperation.errorMessage.observe(this.viewLifecycleOwner) {
            if (it != null) {
                Toast.makeText(
                    activity,
                    it,
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        binding.viewModelConnection = viewModelConnection
        binding.viewModelTeleoperation = viewModelTeleoperation
        binding.thisFragment = this

        viewModelGameController.keyStates.observe(this.viewLifecycleOwner) { keyStates ->
            viewModelTeleoperation.setControllerButtonsState(
                keyStates.map { it.key.index to it.value }.toMap(),
            )
        }
        viewModelGameController.axisStates.observe(this.viewLifecycleOwner) { axesStates ->
            viewModelTeleoperation.setControllerAxesState(
                axesStates.map { it.key.index to it.value }.toMap(),
            )
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.layout_menu, menu)
        batteryChargeIcon = menu.findItem(R.id.battery_charge_icon)
        setBatteryChargeIcon(-1.0)
    }

    private fun setBatteryChargeIcon(charge: Double) {
        val iconId = if (charge < 0.0) {
            R.drawable.ic_battery_unknown
        } else if (charge <= 12.5) {
            R.drawable.ic_battery_0_bar
        } else if (charge <= 25.0) {
            R.drawable.ic_battery_1_bar
        } else if (charge <= 37.5) {
            R.drawable.ic_battery_2_bar
        } else if (charge <= 50.0) {
            R.drawable.ic_battery_3_bar
        } else if (charge <= 62.5) {
            R.drawable.ic_battery_4_bar
        } else if (charge <= 75.0) {
            R.drawable.ic_battery_5_bar
        } else if (charge <= 87.5) {
            R.drawable.ic_battery_6_bar
        } else {
            R.drawable.ic_battery_full
        }
        batteryChargeIcon.icon = ContextCompat.getDrawable(this.requireContext(), iconId)
        batteryChargeIcon.title =
            getString(R.string.battery_charge_label, String.format("%.0f", charge))
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