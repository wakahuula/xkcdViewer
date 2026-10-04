package de.stoupas.xkcd.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import de.stoupas.xkcd.resources.*
import org.jetbrains.compose.resources.stringResource
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import de.stoupas.xkcd.comic.*
import de.stoupas.xkcd.favorites.*
import de.stoupas.xkcd.info.*
import de.stoupas.xkcd.settings.SettingsScreen
import de.stoupas.xkcd.platform.PlatformFailure
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString

@Composable
fun XkcdApp(dependencies: AppDependencies) {
    val settings by dependencies.settings.state.collectAsStateWithLifecycle()
    val nav = rememberNavController()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    fun settingsError() { scope.launch { snackbar.showSnackbar(getString(Res.string.settings_write_failed)) } }
    fun perform(action: suspend () -> Result<Unit>, saved: Boolean = false) {
        scope.launch {
            val result = action()
            if (result.isSuccess && saved) snackbar.showSnackbar(getString(Res.string.image_saved))
            else if (result.isFailure && result.exceptionOrNull() !is PlatformFailure.UserCancelled) {
                snackbar.showSnackbar(getString(if (saved) Res.string.image_save_failed else Res.string.platform_action_failed))
            }
        }
    }
    XkcdTheme(settings) {
        Box(Modifier.fillMaxSize()) {
            NavHost(navController = nav, startDestination = AppRoute.Home) {
                composable<AppRoute.Home> { entry ->
                    val vm = viewModel<ComicViewModel>(viewModelStoreOwner = entry) {
                        ComicViewModel(dependencies.comics, dependencies.settings, entry.savedStateHandle)
                    }
                    ComicScreen(vm, settings,
                        onFavorites = { nav.navigate(AppRoute.Favorites) { launchSingleTop = true } },
                        onSettings = { nav.navigate(AppRoute.Settings) { launchSingleTop = true } },
                        onShare = { comic -> perform({ dependencies.share.shareComic(comic) }) },
                        onSave = { comic, url -> perform({ dependencies.save.save(comic, url) }, saved = true) },
                        onExplain = { comic -> perform({ dependencies.browser.open("https://www.explainxkcd.com/wiki/index.php/${comic.num}") }) })
                }
                composable<AppRoute.ComicDetail> { entry ->
                    val route = entry.toRoute<AppRoute.ComicDetail>()
                    val vm = viewModel<ComicViewModel>(viewModelStoreOwner = entry) {
                        ComicViewModel(dependencies.comics, dependencies.settings, entry.savedStateHandle, route.id)
                    }
                    ComicScreen(vm, settings,
                        onFavorites = { nav.navigate(AppRoute.Favorites) { launchSingleTop = true } },
                        onSettings = { nav.navigate(AppRoute.Settings) { launchSingleTop = true } },
                        onShare = { comic -> perform({ dependencies.share.shareComic(comic) }) },
                        onSave = { comic, url -> perform({ dependencies.save.save(comic, url) }, saved = true) },
                        onExplain = { comic -> perform({ dependencies.browser.open("https://www.explainxkcd.com/wiki/index.php/${comic.num}") }) },
                        onBack = { nav.popBackStack() })
                }
                composable<AppRoute.Favorites> { entry ->
                    val vm = viewModel<FavoritesViewModel>(viewModelStoreOwner = entry) { FavoritesViewModel(dependencies.comics, dependencies.settings) }
                    FavoritesScreen(vm, dependencies.settings, { nav.navigate(AppRoute.ComicDetail(it)) }, { nav.popBackStack() }, ::settingsError)
                }
                composable<AppRoute.Settings> {
                    SettingsScreen(dependencies.settings, dependencies.cache, { nav.popBackStack() },
                        { nav.navigate(AppRoute.About) }, { nav.navigate(AppRoute.Contributors) }, { nav.navigate(AppRoute.Licenses) }, ::settingsError)
                }
                composable<AppRoute.Contributors> {
                    ContributorsScreen({ nav.popBackStack() }) { url -> perform({ dependencies.browser.open(url) }) }
                }
                composable<AppRoute.About> {
                    AboutScreen({ nav.popBackStack() }, { nav.navigate(AppRoute.Contributors) }, { nav.navigate(AppRoute.Licenses) }) { url -> perform({ dependencies.browser.open(url) }) }
                }
                composable<AppRoute.Licenses> { LicensesScreen(dependencies.licenses) { nav.popBackStack() } }
            }
            SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).navigationBarsPadding())
        }
    }
}
