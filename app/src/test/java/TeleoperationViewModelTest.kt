package cc.jumper.ballsbot_teleoperation

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import cc.jumper.ballsbot_teleoperation.data.Connection
import cc.jumper.ballsbot_teleoperation.models.TeleoperationViewModel
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.lang.Thread.sleep

class TeleoperationViewModelTest {
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()
    lateinit var viewModel:  TeleoperationViewModel

    @Before
    fun setup() {
        val connectionInfo = Connection(
            0, "desc", "127.0.0.1", 8080, "key"
        )
        viewModel = TeleoperationViewModel(connectionInfo)
    }

    @Test
    fun test_is_connection_ok() {
        var result : Boolean? = null
        try {
            result = viewModel.isConnectionOk()
        } catch (e: Throwable) {
            fail("no exception for isConnectionOk: ${e.message}")
        }
        assertEquals(true, result)
    }

    @Test
    fun test_updates() {
        try {
            viewModel.startUpdates()
            sleep(3000)
            viewModel.stopUpdates()
        } catch (e: Throwable) {
            fail("no exception for startUpdates/stopUpdates: ${e.message}")
            return
        }
        assertTrue(true)
    }
}