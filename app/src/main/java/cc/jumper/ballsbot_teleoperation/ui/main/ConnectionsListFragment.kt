package cc.jumper.ballsbot_teleoperation.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import cc.jumper.ballsbot_teleoperation.ConnectionsListAdapter
import cc.jumper.ballsbot_teleoperation.TeleoperationApplication
import cc.jumper.ballsbot_teleoperation.databinding.ConnectionsListFragmentBinding
import cc.jumper.ballsbot_teleoperation.models.ConnectionsViewModel
import cc.jumper.ballsbot_teleoperation.models.ConnectionsViewModelFactory

/**
 * Main fragment displaying details for all items in the database.
 */
class ConnectionsListFragment : Fragment() {

    private var _binding: ConnectionsListFragmentBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ConnectionsViewModel by activityViewModels {
        ConnectionsViewModelFactory(
            (activity?.application as TeleoperationApplication).database.connectionDao()
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ConnectionsListFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = ConnectionsListAdapter {
            val action = ConnectionsListFragmentDirections.actionConnectionsListFragmentToConnectionInfoFragment(it.id)
            this.findNavController().navigate(action)
        }
        binding.recyclerView.adapter = adapter
        viewModel.allConnections.observe(this.viewLifecycleOwner) { connections ->
            connections.let {
                adapter.submitList(it)
            }
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this.context)
        binding.fabAddConnection.setOnClickListener {
            val action = ConnectionsListFragmentDirections.actionConnectionsListFragmentToEditConnectionFragment(
                -1
            )
            this.findNavController().navigate(action)
        }
        binding.fabTestGameController.setOnClickListener {
            val action = ConnectionsListFragmentDirections.actionConnectionsListFragmentToTestGameControllerFragment()
            this.findNavController().navigate(action)
        }
    }
}