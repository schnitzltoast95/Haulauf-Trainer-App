package com.example.myapplication

import android.app.Application
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.myapplication.data.TrainingPreset
import com.example.myapplication.training.TrainingState
import com.example.myapplication.ui.HaulaufViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.FileInputStream

@RunWith(AndroidJUnit4::class)
class TrainingLifecycleInstrumentedTest {
    @Test
    fun foregroundServiceStopsWhenTrainingFinishes() = runBlocking {
        val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application
        val viewModel = HaulaufViewModel(app)
        viewModel.updatePreset(
            TrainingPreset(
                rounds = 1,
                unitsPerRound = 1,
                reactionIntervalMs = 1,
                initialCountdownMs = 1000,
                selectedMoveIds = setOf("oberhau"),
                endBeepEnabled = false
            )
        )

        try {
            viewModel.savePresetAndStart {}
            withTimeout(10000) {
                while (!isTrainingServiceRunning()) delay(50)
            }
            assertTrue(isTrainingServiceRunning())

            val finished = withTimeoutOrNull(10000) {
                viewModel.trainingState.first { it is TrainingState.Finished }
            }
            assertTrue("Training state: ${viewModel.trainingState.value}", finished != null)
            withTimeout(3000) {
                while (hasTrainingService()) delay(50)
            }
            assertFalse(hasTrainingService())
        } finally {
            viewModel.stopTraining()
        }
    }

    private fun isTrainingServiceRunning(): Boolean {
        val output = serviceDump()
        return output.contains("TrainingForegroundService") &&
            output.contains("isForeground=true")
    }

    private fun hasTrainingService(): Boolean = serviceDump().contains("TrainingForegroundService")

    private fun serviceDump(): String {
        val output = InstrumentationRegistry.getInstrumentation().uiAutomation
            .executeShellCommand("dumpsys activity services com.example.myapplication")
            .use { descriptor -> FileInputStream(descriptor.fileDescriptor).bufferedReader().use { it.readText() } }
        return output
    }
}
