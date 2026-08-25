package com.zhisheng.weather.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ContributorsTest {
    @Test
    fun communityListContainsConfirmedScreenshotContributors() {
        val confirmed = listOf(
            "飞667",
            "一杯冰美式、、",
            "M1ralce",
            "紅星照耀中國",
            "我爱跑步",
            "河鱼天雁",
        )
        confirmed.forEach { id -> assertTrue("Missing contributor: $id", id in CommunityContributors) }
        assertFalse("Typo must not remain", "的飞667" in CommunityContributors)
        assertEquals("PPQ1028", CommunityContributors.first())
        assertEquals("r1file", CommunityContributors[5])
        assertEquals(15, CommunityContributors.size)
        assertEquals(CommunityContributors.size, CommunityContributors.distinct().size)
    }
}
