package de.stoupas.xkcd

import android.app.Activity
import android.content.Intent
import android.provider.DocumentsContract
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.Before
import org.junit.After

class AndroidDocumentExporterTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private lateinit var monitor: android.app.Instrumentation.ActivityMonitor
    @Before fun holdDocumentPickerResult() {
        monitor = instrumentation.addMonitor(android.content.IntentFilter(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            addDataType("image/png")
        }, null, true)
    }
    @After fun removeMonitor() { instrumentation.removeMonitor(monitor) }
    private val resolver = InstrumentationRegistry.getInstrumentation().context.contentResolver
    private val root = DocumentsContract.buildDocumentUri("de.stoupas.xkcd.test.documents", "root")
    private val bytes = byteArrayOf(1, 2, 3, 4)

    private fun document(prefix: String) = requireNotNull(
        DocumentsContract.createDocument(resolver, root, "image/png", "$prefix-${System.nanoTime()}")
    )
    private fun exists(uri: android.net.Uri) = resolver.query(uri, null, null, null, null)!!.use { it.moveToFirst() }
    private fun deliver(activity: MainActivity, uri: android.net.Uri?) {
        val state = android.os.Bundle()
        activity.activityResultRegistry.onSaveInstanceState(state)
        val keys = requireNotNull(state.getStringArrayList("KEY_COMPONENT_ACTIVITY_REGISTERED_KEYS"))
        val codes = requireNotNull(state.getIntegerArrayList("KEY_COMPONENT_ACTIVITY_REGISTERED_RCS"))
        val launched = requireNotNull(state.getStringArrayList("KEY_COMPONENT_ACTIVITY_LAUNCHED_KEYS"))
        val code = codes[keys.indexOf(launched.single())]
        activity.activityResultRegistry.dispatchResult(code, if (uri == null) Activity.RESULT_CANCELED else Activity.RESULT_OK, Intent().setData(uri))
    }

    @Test fun resultAfterActivityRecreationWritesCompleteDocument() = runBlocking {
        val uri = document("rotation")
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
        try {
            ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                scenario.moveToState(Lifecycle.State.CREATED)
                lateinit var exporter: AndroidDocumentExporter
                scenario.onActivity { exporter = AndroidDocumentExporter(it) }
                scenario.moveToState(Lifecycle.State.RESUMED)
                val operation = scope.async { exporter.export(bytes, "image/png", "test.png") }
                // Wait for the main-thread coroutine to register its outstanding result.
                withContext(Dispatchers.Main) { yield() }
                scenario.recreate()
                scenario.moveToState(Lifecycle.State.CREATED)
                scenario.onActivity { AndroidDocumentExporter(it) }
                scenario.moveToState(Lifecycle.State.RESUMED)
                scenario.onActivity {
                    deliver(it, uri)
                }
                assertTrue(withTimeout(5_000) { operation.await() }.isSuccess)
                assertArrayEquals(bytes, resolver.openInputStream(uri)!!.use { it.readBytes() })
            }
        } finally {
            scope.cancel()
            if (exists(uri)) DocumentsContract.deleteDocument(resolver, uri)
        }
    }

    @Test fun cancelledCallerDeletesLateDocumentThroughDocumentApi() = runBlocking {
        val uri = document("cancel")
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
        try {
            ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                scenario.moveToState(Lifecycle.State.CREATED)
                lateinit var exporter: AndroidDocumentExporter
                scenario.onActivity { exporter = AndroidDocumentExporter(it) }
                scenario.moveToState(Lifecycle.State.RESUMED)
                val operation = scope.launch { exporter.export(bytes, "image/png", "test.png") }
                withContext(Dispatchers.Main) { yield() }
                operation.cancelAndJoin()
                scenario.onActivity {
                    deliver(it, uri)
                }
                withTimeout(5_000) { while (exists(uri)) delay(50) }
            }
        } finally {
            scope.cancel()
            if (exists(uri)) DocumentsContract.deleteDocument(resolver, uri)
        }
    }

    @Test fun writeFailureDeletesDocumentAndReportsFailure() = runBlocking {
        val uri = document("fail")
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
        try {
            ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                scenario.moveToState(Lifecycle.State.CREATED)
                lateinit var exporter: AndroidDocumentExporter
                scenario.onActivity { exporter = AndroidDocumentExporter(it) }
                scenario.moveToState(Lifecycle.State.RESUMED)
                val operation = scope.async { exporter.export(bytes, "image/png", "test.png") }
                withContext(Dispatchers.Main) { yield() }
                scenario.onActivity { deliver(it, uri) }
                assertTrue(withTimeout(5_000) { operation.await() }.isFailure)
                assertFalse(exists(uri))
            }
        } finally {
            scope.cancel()
            if (exists(uri)) DocumentsContract.deleteDocument(resolver, uri)
        }
    }

    @Test fun dialogCancellationReleasesNextExport() = runBlocking {
        val uri = document("retry")
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
        try {
            ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                scenario.moveToState(Lifecycle.State.CREATED)
                lateinit var exporter: AndroidDocumentExporter
                scenario.onActivity { exporter = AndroidDocumentExporter(it) }
                scenario.moveToState(Lifecycle.State.RESUMED)
                val cancelled = scope.async { exporter.export(bytes, "image/png", "test.png") }
                withContext(Dispatchers.Main) { yield() }
                scenario.onActivity { deliver(it, null) }
                assertSame(de.stoupas.xkcd.platform.PlatformFailure.UserCancelled,
                    withTimeout(5_000) { cancelled.await() }.exceptionOrNull())
                val retry = scope.async { exporter.export(bytes, "image/png", "test.png") }
                withContext(Dispatchers.Main) { yield() }
                scenario.onActivity { deliver(it, uri) }
                assertTrue(withTimeout(5_000) { retry.await() }.isSuccess)
                assertArrayEquals(bytes, resolver.openInputStream(uri)!!.use { it.readBytes() })
            }
        } finally {
            scope.cancel()
            if (exists(uri)) DocumentsContract.deleteDocument(resolver, uri)
        }
    }
}
