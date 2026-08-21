package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.QuietHoursSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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
    assertEquals("Client Portal", appName)
  }

  @Test
  fun `quiet hours overnight interval calculations`() {
    // 10:00 PM (22:00) to 7:00 AM (07:00)
    val overnight = QuietHoursSettings(isEnabled = true, startHour = 22, startMinute = 0, endHour = 7, endMinute = 0)
    assertEquals("10:00 PM – 07:00 AM", overnight.formattedTimeRange())

    // 11:30 PM (23:30 = 1410 min) should be active
    assertTrue(overnight.isInQuietInterval(23 * 60 + 30))

    // 03:15 AM (03:15 = 195 min) should be active
    assertTrue(overnight.isInQuietInterval(3 * 60 + 15))

    // 12:00 PM (12:00 = 720 min) should NOT be active
    assertFalse(overnight.isInQuietInterval(12 * 60))
  }

  @Test
  fun `quiet hours same day interval calculations`() {
    // 1:00 PM (13:00) to 5:00 PM (17:00)
    val daytime = QuietHoursSettings(isEnabled = true, startHour = 13, startMinute = 0, endHour = 17, endMinute = 0)
    assertEquals("01:00 PM – 05:00 PM", daytime.formattedTimeRange())

    // 2:30 PM (14:30 = 870 min) should be active
    assertTrue(daytime.isInQuietInterval(14 * 60 + 30))

    // 09:00 AM should NOT be active
    assertFalse(daytime.isInQuietInterval(9 * 60))
  }
}
