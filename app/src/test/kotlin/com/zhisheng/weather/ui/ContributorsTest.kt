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
            "你的心里没点高数吗",
            "周月星斗",
            "无敌战神暴王龙",
            "control3",
            "明珠有泪",
            "Gstar_",
            "伍拾两HZ",
            "寡欲老公猪",
        )
        confirmed.forEach { id -> assertTrue("Missing contributor: $id", id in CommunityContributors) }
        assertFalse("Typo must not remain", "的飞667" in CommunityContributors)
        assertEquals("PPQ1028", CommunityContributors.first())
        assertEquals("r1file", CommunityContributors[5])
        assertEquals(23, CommunityContributors.size)
        assertEquals(CommunityContributors.size, CommunityContributors.distinct().size)
    }
}
