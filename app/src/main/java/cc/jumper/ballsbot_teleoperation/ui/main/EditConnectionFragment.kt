package cc.jumper.ballsbot_teleoperation.ui.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import cc.jumper.ballsbot_teleoperation.R
import cc.jumper.ballsbot_teleoperation.TeleoperationApplication
import cc.jumper.ballsbot_teleoperation.data.Connection
import cc.jumper.ballsbot_teleoperation.databinding.EditConnectionFragmentBinding
import cc.jumper.ballsbot_teleoperation.models.ConnectionsViewModel
import cc.jumper.ballsbot_teleoperation.models.ConnectionsViewModelFactory
import cc.jumper.ballsbot_teleoperation.models.TeleoperationViewModel

class EditConnectionFragment : Fragment() {

    private var _binding: EditConnectionFragmentBinding? = null
    private val binding get() = _binding!!

    private val navigationArgs: EditConnectionFragmentArgs by navArgs()

    private val viewModel: ConnectionsViewModel by activityViewModels {
        ConnectionsViewModelFactory(
            (activity?.application as TeleoperationApplication).database.connectionDao()
        )
    }

    private var connection: Connection? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = EditConnectionFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val connectionId = navigationArgs.connectionId
        if (connectionId == -1) {
            (activity as AppCompatActivity?)?.supportActionBar?.title =
                getString(R.string.connection_create_title)
            connection = null
            bindConnection()
        } else {
            (activity as AppCompatActivity?)?.supportActionBar?.title =
                getString(R.string.connection_edit_title)
            viewModel.getConnection(connectionId)
                .observe(this.viewLifecycleOwner) { selectedConnection ->
                    connection = selectedConnection
                    bindConnection()
                }
        }

        binding.viewModel = viewModel
        binding.thisFragment = this
    }

    private fun bindConnection() {
        binding.apply {
            if (connection == null) {
                itemDescriptionName.setText("", TextView.BufferType.SPANNABLE)
                itemHostName.setText("", TextView.BufferType.SPANNABLE)
                itemPort.setText("", TextView.BufferType.SPANNABLE)
                itemKey.setText("", TextView.BufferType.SPANNABLE)
                itemCertificate.setText("", TextView.BufferType.SPANNABLE)
            } else {
                itemDescriptionName.setText(connection!!.description, TextView.BufferType.SPANNABLE)
                itemHostName.setText(connection!!.hostName, TextView.BufferType.SPANNABLE)
                itemPort.setText(connection!!.port.toString(), TextView.BufferType.SPANNABLE)
                itemKey.setText(connection!!.key, TextView.BufferType.SPANNABLE)
                itemCertificate.setText(connection!!.certificate, TextView.BufferType.SPANNABLE)
            }
        }
    }

    fun saveConnection() {
        if (isValidEntry()) {
            val action = if (connection == null) {
                viewModel.addConnection(
                    binding.itemDescriptionName.text.toString(),
                    binding.itemHostName.text.toString(),
                    binding.itemPort.text.toString().toInt(),
                    binding.itemKey.text.toString(),
                    binding.itemCertificate.text.toString(),
                )
                EditConnectionFragmentDirections.actionEditConnectionFragmentToConnectionsListFragment()
            } else {
                viewModel.updateConnection(
                    connection!!,
                    binding.itemDescriptionName.text.toString(),
                    binding.itemHostName.text.toString(),
                    binding.itemPort.text.toString().toInt(),
                    binding.itemKey.text.toString(),
                    binding.itemCertificate.text.toString(),
                )
                EditConnectionFragmentDirections.actionEditConnectionFragmentToConnectionInfoFragment(
                    connection!!.id
                )
            }
            findNavController().navigate(action)
        } else {
            showToast(getString(R.string.test_connection_invalid))
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(
            activity,
            message,
            Toast.LENGTH_LONG
        ).show()
    }

    private fun isValidEntry() = viewModel.isValidEntry(
        binding.itemDescriptionName.text.toString(),
        binding.itemHostName.text.toString(),
        binding.itemPort.text.toString(),
        binding.itemKey.text.toString(),
        binding.itemCertificate.text.toString(),
    )

    fun testConnection() {
        if (!isValidEntry()) {
            showToast(getString(R.string.test_connection_invalid))
            return
        }

        val aConnection = Connection(
            0,
            binding.itemDescriptionName.text.toString(),
            binding.itemHostName.text.toString(),
            binding.itemPort.text.toString().toInt(),
            binding.itemKey.text.toString(),
            binding.itemCertificate.text.toString(),
        )

        val aModel = TeleoperationViewModel()
        aModel.connectionInfo = aConnection

        aModel.connected.observeForever() {
            if (it != null) {
                val message = if (it) {
                    getString(R.string.test_connection_success)
                } else {
                    getString(R.string.test_connection_fail, aModel.previousHttpErrorMessage)
                }
                showToast(message)
            }
        }
        aModel.checkConnection()
    }

    fun cancel() {
        val action = if (connection == null) {
            EditConnectionFragmentDirections.actionEditConnectionFragmentToConnectionsListFragment()
        } else {
            EditConnectionFragmentDirections.actionEditConnectionFragmentToConnectionInfoFragment(
                connection!!.id
            )
        }
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}