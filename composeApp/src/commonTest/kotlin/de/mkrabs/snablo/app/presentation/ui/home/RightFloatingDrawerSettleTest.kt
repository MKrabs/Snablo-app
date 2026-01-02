package de.mkrabs.snablo.app.presentation.ui.home

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RightFloatingDrawerSettleTest {

    private fun decideShouldOpen(
        offset: Float,
        closedOffset: Float,
        velocityX: Float
    ): Boolean {
        return when {
            velocityX < -800f -> true
            velocityX > 800f -> false
            else -> offset < (closedOffset * 0.5f)
        }
    }

    @Test
    fun `fling left opens`() {
        assertTrue(decideShouldOpen(offset = 200f, closedOffset = 296f, velocityX = -1200f))
    }

    @Test
    fun `fling right closes`() {
        assertFalse(decideShouldOpen(offset = 50f, closedOffset = 296f, velocityX = 1500f))
    }

    @Test
    fun `halfway rule opens when more than halfway open`() {
        assertTrue(decideShouldOpen(offset = 100f, closedOffset = 300f, velocityX = 0f))
    }

    @Test
    fun `halfway rule closes when more than halfway closed`() {
        assertFalse(decideShouldOpen(offset = 200f, closedOffset = 300f, velocityX = 0f))
    }
}

