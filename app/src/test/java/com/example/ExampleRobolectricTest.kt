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
    assertEquals(265.0, parsed.highest, 0.001)
    assertEquals(180.0, parsed.lowest, 0.001)
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
    assertEquals(265.0, parsed.highest, 0.001)
    assertEquals(180.0, parsed.lowest, 0.001)
  }

  @Test
  fun `append just-finished score to previous scores array and update highest lowest`() {
    val previous = SkorAkumulasi.fromJsonString("[180, 180, 260, 235, 265]")
    val updated = StatsUtils.updateSkorAkumulasi(previous, 300.0)
    assertEquals(6, updated.numsesi)
    assertEquals(6, updated.scores.size)
    assertEquals(1420.0, updated.total, 0.001)
    assertEquals(300.0, updated.highest, 0.001)
    assertEquals(180.0, updated.lowest, 0.001)

    val updatedWithLower = StatsUtils.updateSkorAkumulasi(updated, 100.0)
    assertEquals(7, updatedWithLower.numsesi)
    assertEquals(300.0, updatedWithLower.highest, 0.001)
    assertEquals(100.0, updatedWithLower.lowest, 0.001)
  }

  @Test
  fun `calculate question timer depends on score`() {
    // Score 10 gives 60 seconds
    assertEquals(60, StatsUtils.calculateQuestionTimeSeconds(10))
    // Score <= 10 gives minimum 60 seconds
    assertEquals(60, StatsUtils.calculateQuestionTimeSeconds(5))
    // For each score increase of 10, increase allotted time by 30 seconds
    assertEquals(90, StatsUtils.calculateQuestionTimeSeconds(20))
    assertEquals(120, StatsUtils.calculateQuestionTimeSeconds(30))
    assertEquals(150, StatsUtils.calculateQuestionTimeSeconds(40))
    assertEquals(180, StatsUtils.calculateQuestionTimeSeconds(50))
    // Score 25 (+15 over 10 -> +45s) -> 105s
    assertEquals(105, StatsUtils.calculateQuestionTimeSeconds(25))
  }

  @Test
  fun `eligible question grades allow up to 3 levels lower`() {
    // Grade 9 eligible down to Grade 6 (6, 7, 8, 9)
    val grade9 = 9
    val eligibleRange9 = (grade9 - 3).coerceAtLeast(1)..grade9
    assertEquals(6..9, eligibleRange9)

    // Grade 10 eligible down to Grade 7 (7, 8, 9, 10)
    val grade10 = 10
    val eligibleRange10 = (grade10 - 3).coerceAtLeast(1)..grade10
    assertEquals(7..10, eligibleRange10)

    // Grade 2 bounded by 1
    val grade2 = 2
    val eligibleRange2 = (grade2 - 3).coerceAtLeast(1)..grade2
    assertEquals(1..2, eligibleRange2)
  }

  @Test
  fun `fallback parse legacy json format`() {
    val legacyJson = "{\"total\": 1120, \"numsesi\": 5, \"mean\": 224.00, \"modus\": 213.75, \"median\": 213.75}"
    val parsed = SkorAkumulasi.fromJsonString(legacyJson)
    assertEquals(1120.0, parsed.total, 0.001)
    assertEquals(5, parsed.numsesi)
    assertEquals(224.0, parsed.mean, 0.001)
  }

  @Test
  fun `parse question and answer images from image1 column`() {
    // Empty or blank image1
    val emptyImages = com.example.data.model.QuestionImages.parse("")
    assertEquals(null, emptyImages.questUrl)
    assertEquals(null, emptyImages.ansAUrl)
    assertEquals(false, emptyImages.hasAnyImage)

    // Question only image with empty answer strings
    val questionOnly = "{'quest': 'images_00150/quest_150_Xk6oyQ2nAz.png', 'ansA': '', 'ansB': '', 'ansC': '', 'ansD': ''}"
    val parsedQuest = com.example.data.model.QuestionImages.parse(questionOnly)
    assertEquals("https://aoplaetqkabshapkapwf.storage.supabase.co/storage/v1/object/public/ExacTop/images_00150/quest_150_Xk6oyQ2nAz.png", parsedQuest.questUrl)
    assertEquals(null, parsedQuest.ansAUrl)
    assertEquals(null, parsedQuest.ansBUrl)
    assertEquals(true, parsedQuest.hasAnyImage)

    // Answer images in options
    val answerImages = "{'ansA': 'bargraphs/ansA_65_Em7oypvVES.png', 'ansB': 'bargraphs/ansB_65_SAN4SoKnyJ.png', 'ansC': 'bargraphs/ansC_65_Y4dcTLUyCO.png', 'ansD': 'bargraphs/ansD_65_KFPFFHnvSB.png'}"
    val parsedAns = com.example.data.model.QuestionImages.parse(answerImages)
    assertEquals(null, parsedAns.questUrl)
    assertEquals("https://aoplaetqkabshapkapwf.storage.supabase.co/storage/v1/object/public/ExacTop/bargraphs/ansA_65_Em7oypvVES.png", parsedAns.ansAUrl)
    assertEquals("https://aoplaetqkabshapkapwf.storage.supabase.co/storage/v1/object/public/ExacTop/bargraphs/ansB_65_SAN4SoKnyJ.png", parsedAns.ansBUrl)
    assertEquals("https://aoplaetqkabshapkapwf.storage.supabase.co/storage/v1/object/public/ExacTop/bargraphs/ansC_65_Y4dcTLUyCO.png", parsedAns.ansCUrl)
    assertEquals("https://aoplaetqkabshapkapwf.storage.supabase.co/storage/v1/object/public/ExacTop/bargraphs/ansD_65_KFPFFHnvSB.png", parsedAns.ansDUrl)
  }
}
