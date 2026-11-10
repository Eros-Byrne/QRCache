package app.qrcache.scan

import androidx.camera.core.ImageAnalysis
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode

/** What the scanner looks for: square QR codes, or the striped barcodes on products and tickets. */
enum class ScanMode { QR, BARCODE }

/** ML Kit scanner for one [ScanMode]. Restricting formats makes decoding faster. */
internal fun scannerFor(mode: ScanMode) = BarcodeScanning.getClient(
    when (mode) {
        ScanMode.QR -> BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE)
        ScanMode.BARCODE -> BarcodeScannerOptions.Builder().setBarcodeFormats(
            Barcode.FORMAT_EAN_13,
            Barcode.FORMAT_EAN_8,
            Barcode.FORMAT_UPC_A,
            Barcode.FORMAT_UPC_E,
            Barcode.FORMAT_CODE_128,
            Barcode.FORMAT_CODE_39,
            Barcode.FORMAT_CODE_93,
            Barcode.FORMAT_CODABAR,
            Barcode.FORMAT_ITF,
        )
    }.build(),
)

/**
 * Live camera preview that reports every decoded value for [mode] through [onCode].
 * Binds to the screen's lifecycle, so the camera stops when the app is in the background.
 */
@Composable
fun CameraPreview(
    mode: ScanMode,
    torchOn: Boolean,
    onCode: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnCode by rememberUpdatedState(onCode)
    val controller = remember { LifecycleCameraController(context) }

    DisposableEffect(lifecycleOwner) {
        controller.imageAnalysisBackpressureStrategy = ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
        controller.bindToLifecycle(lifecycleOwner)
        onDispose { controller.unbind() }
    }

    // Swapping only the analyzer keeps the preview running while the mode changes.
    DisposableEffect(mode) {
        val executor = ContextCompat.getMainExecutor(context)
        val scanner = scannerFor(mode)
        controller.setImageAnalysisAnalyzer(
            executor,
            MlKitAnalyzer(listOf(scanner), ImageAnalysis.COORDINATE_SYSTEM_ORIGINAL, executor) { result ->
                result.getValue(scanner)?.firstNotNullOfOrNull { it.rawValue }?.let(currentOnCode)
            },
        )
        onDispose {
            controller.clearImageAnalysisAnalyzer()
            scanner.close()
        }
    }

    LaunchedEffect(torchOn) {
        controller.enableTorch(torchOn)
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
                this.controller = controller
            }
        },
    )
}
