package com.example.myapplication

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.AdaptiveIconDrawable
import android.os.Build
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class LauncherIconInstrumentedTest {
    @Test fun launcherIconsRenderAndProvideThemedSilhouette() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        for (id in listOf(R.mipmap.ic_launcher, R.mipmap.ic_launcher_round)) {
            val icon = context.getDrawable(id)!!
            val bitmap = Bitmap.createBitmap(216, 216, Bitmap.Config.ARGB_8888)
            icon.setBounds(0, 0, 216, 216)
            icon.draw(Canvas(bitmap))
            assertTrue(bitmap.getPixel(108, 108) != 0)
            File(context.getExternalFilesDir(null), "launcher-$id.png").outputStream().use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
            bitmap.recycle()
            if (Build.VERSION.SDK_INT >= 33) {
                val mono = (icon as AdaptiveIconDrawable).monochrome
                assertNotNull(mono)
                val silhouette = Bitmap.createBitmap(216, 216, Bitmap.Config.ARGB_8888)
                mono!!.setBounds(0, 0, 216, 216)
                mono.draw(Canvas(silhouette))
                assertTrue(silhouette.getPixel(108, 108) != 0)
                File(context.getExternalFilesDir(null), "launcher-monochrome.png").outputStream().use {
                    silhouette.compress(Bitmap.CompressFormat.PNG, 100, it)
                }
                silhouette.recycle()
            }
        }
    }
}
