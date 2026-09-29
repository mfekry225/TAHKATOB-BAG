package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.ChildEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ExampleRobolectricTest {

  @Test
  fun `application context and child entity validation`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    assertNotNull(context)

    val child = ChildEntity(
      id = 1,
      name = "طفل الاختبار",
      age = 6,
      mentalAge = 6,
      notes = "ملاحظات الاختبار",
      avatarColor = 0xFF0000
    )
    assertEquals("طفل الاختبار", child.name)
    assertEquals(6, child.age)
  }
}
