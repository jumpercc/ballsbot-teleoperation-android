package cc.jumper.ballsbot_teleoperation.ui.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import cc.jumper.ballsbot_teleoperation.R
import cc.jumper.ballsbot_teleoperation.TeleoperationApplication
import cc.jumper.ballsbot_teleoperation.data.Connection
import cc.jumper.ballsbot_teleoperation.databinding.ConnectionInfoFragmentBinding
import cc.jumper.ballsbot_teleoperation.databinding.ConnectionsListFragmentBinding
import cc.jumper.ballsbot_teleoperation.models.ConnectionsViewModel
import cc.jumper.ballsbot_teleoperation.models.ConnectionsViewModelFactory

class ConnectionInfoFragment : Fragment() {

    private var _binding: ConnectionInfoFragmentBinding? = null
    private val binding get() = _binding!!

    private val navigationArgs: ConnectionInfoFragmentArgs by navArgs()

    private val viewModel: ConnectionsViewModel by activityViewModels {
        ConnectionsViewModelFactory(
            (activity?.application as TeleoperationApplication).database.connectionDao()
        )
    }

    private lateinit var connection: Connection

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ConnectionInfoFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val connectionId = navigationArgs.connectionId
        viewModel.getConnection(connectionId)
            .observe(this.viewLifecycleOwner) { selectedConnection ->
                connection = selectedConnection
                bindConnection()
            }
        binding.viewModel = viewModel
        binding.thisFragment = this
    }

    private fun bindConnection() {
        binding.apply {
            itemDescription.text = connection.description
            itemHostName.text = connection.hostName
            itemPort.text = connection.port.toString()
        }
    }

    fun editConnection() {
        val action = ConnectionInfoFragmentDirections.actionConnectionInfoFragmentToEditConnectionFragment(
            connection.id
        )
        findNavController().navigate(action)
    }

    fun deleteConnection() {
        viewModel.deleteConnection(connection)
        val action = ConnectionInfoFragmentDirections.actionConnectionInfoFragmentToConnectionsListFragment()
        findNavController().navigate(action)
    }

    fun connect() {
        val action = ConnectionInfoFragmentDirections.actionConnectionInfoFragmentToTeleoperationFragment(
            connection.id
        )
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}