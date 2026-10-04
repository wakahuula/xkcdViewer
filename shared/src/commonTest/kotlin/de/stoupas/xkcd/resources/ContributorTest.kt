package de.stoupas.xkcd.resources

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ContributorTest {
    @Test
    fun preservesNamesProfilesAndOrder() {
        val contributors = parseContributors(
            """{"contributors":[{"name":"Zoë","profile":"https://github.com/zoe"},{"name":"ahmdaeyz","profile":"https://github.com/ahmdaeyz"}]}""",
        )
        assertEquals(listOf("Zoë", "ahmdaeyz"), contributors.map { it.name })
        assertEquals(listOf("https://github.com/zoe", "https://github.com/ahmdaeyz"), contributors.map { it.profile })
    }

    @Test
    fun loadsPackagedOriginalContributors() = runTest {
        val contributors = loadContributors()
        assertEquals(
            listOf("ahmdaeyz", "deatheguard", "guastallaigor", "HD97300", "Himanshuranjan30", "JafetBenitez", "jerold", "rakvium", "ruudhaya", "wakahuula"),
            contributors.map { it.name },
        )
        assertEquals("https://github.com/wakahuula", contributors.last().profile)
    }
}
