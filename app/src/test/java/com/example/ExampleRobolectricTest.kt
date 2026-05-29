package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ui.AppViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("SilverStar Benz", appName)
  }

  @Test
  fun `diagnostic test - instantiate AppViewModel`() = runTest {
    val application = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = AppViewModel(application)
    assertNotNull(viewModel)
    
    // Allow any background initializations in init block to start/execute
    delay(1000)
    
    // Log outputs
    println("Diagnostic test - AppViewModel instantiated successfully!")
  }

  @Test
  fun `diagnostic test - launch MainActivity`() {
    val controller = org.robolectric.Robolectric.buildActivity(MainActivity::class.java).setup()
    assertNotNull(controller.get())
    println("Diagnostic test - MainActivity launched successfully!")
  }
}


