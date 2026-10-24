package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.SkorAkumulasi
import com.example.util.StatsUtils
import org.junit.Assert.assertEquals
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
    assertEquals("ExacTop", appName)
  }

  @Test
  fun `parse skor akumulasi from comma delimited array format`() {
    val arrayFormat = "[180, 180, 260, 235, 265]"
    val parsed = SkorAkumulasi.fromJsonString(arrayFormat)
    assertEquals(5, parsed.numsesi)
    assertEquals(5, parsed.scores.size)
    assertEquals(1120.0, parsed.total, 0.001)
    assertEquals(224.0, parsed.mean, 0.001)
    assertEquals(180.0, parsed.modus, 0.001)
    assertEquals(235.0, parsed.median, 0.001)
    assertEquals("[180, 180, 260, 235, 265]", parsed.toStoredString())
  }

  @Test
  fun `parse skor akumulasi from json object with skor key`() {
    val jsonFormat = "{\"skor\": \"180, 180, 260, 235, 265\"}"
    val parsed = SkorAkumulasi.fromJsonString(jsonFormat)
    assertEquals(5, parsed.numsesi)
    assertEquals(1120.0, parsed.total, 0.001)
    assertEquals(224.0, parsed.mean, 0.001)
    assertEquals(180.0, parsed.modus, 0.001)
    assertEquals(235.0, parsed.median, 0.001)
  }

  @Test
  fun `append just-finished score to previous scores array`() {
    val previous = SkorAkumulasi.fromJsonString("[180, 180, 260, 235, 265]")
    val updated = StatsUtils.updateSkorAkumulasi(previous, 200.0)
    assertEquals(6, updated.numsesi)
    assertEquals(6, updated.scores.size)
    assertEquals(1320.0, updated.total, 0.001)
    assertEquals(220.0, updated.mean, 0.001)
    assertEquals(180.0, updated.modus, 0.001)
    assertEquals(217.5, updated.median, 0.001)
    assertEquals("[180, 180, 260, 235, 265, 200]", updated.toStoredString())
  }

  @Test
  fun `fallback parse legacy json format`() {
    val legacyJson = "{\"total\": 1120, \"numsesi\": 5, \"mean\": 224.00, \"modus\": 213.75, \"median\": 213.75}"
    val parsed = SkorAkumulasi.fromJsonString(legacyJson)
    assertEquals(1120.0, parsed.total, 0.001)
    assertEquals(5, parsed.numsesi)
    assertEquals(224.0, parsed.mean, 0.001)
  }
}
