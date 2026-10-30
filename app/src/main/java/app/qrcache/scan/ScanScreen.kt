package app.qrcache.scan

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.SystemClock
import android.provider.Settings
import android.view.HapticFeedbackConstants
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FlashlightOff
import androidx.compose.material.icons.rounded.FlashlightOn
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import app.qrcache.R
import app.qrcache.ui.components.EmptyState
import app.qrcache.ui.theme.Motion
import app.qrcache.ui.theme.QRCacheTheme
import app.qrcache.ui.theme.Radius
import app.qrcache.ui.theme.Spacing
import app.qrcache.util.findActivity
import com.google.mlkit.vision.common.InputImage
import java.io.IOException

private val ViewfinderSize = 260.dp

/** Scan tab: live camera, flashlight, scan from image, and the result sheet. */
@Composable
fun ScanScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val view = LocalView.current
    val debouncer = remember { ScanDebouncer() }
    var result by remember { mutableStateOf<ScanContent?>(null) }

    fun show(raw: String) {
        view.performHapticFeedback(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) HapticFeedbackConstants.CONFIRM
            else HapticFeedbackConstants.VIRTUAL_KEY,
        )
        result = ContentParser.parse(raw)
    }

    // The camera reports the same code many times a second; only react to new ones.
    fun onCode(raw: String) {
        val isNew = debouncer.accept(raw, SystemClock.elapsedRealtime())
        if (isNew && result == null) show(raw)
    }

    val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) scanImage(context, uri, onFound = ::show)
    }
    val onPickImage = {
        pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    Box(modifier.fillMaxSize().background(Color.Black)) {
        CameraPermissionGate(onPickImage = onPickImage) {
            CameraScanner(success = result != null, onCode = ::onCode, onPickImage = onPickImage)
        }
    }

    result?.let { content ->
        ResultSheet(content = content, onDismiss = { result = null })
    }
}

@Composable
private fun CameraScanner(success: Boolean, onCode: (String) -> Unit, onPickImage: () -> Unit) {
    val context = LocalContext.current
    val hasFlash = remember { context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH) }
    var torchOn by rememberSaveable { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        CameraPreview(torchOn = torchOn, onCode = onCode, modifier = Modifier.fillMaxSize())
        ViewfinderOverlay(success = success, modifier = Modifier.fillMaxSize())

        if (hasFlash) {
            IconButton(
                onClick = { torchOn = !torchOn },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(Spacing.s),
            ) {
                Icon(
                    if (torchOn) Icons.Rounded.FlashlightOn else Icons.Rounded.FlashlightOff,
                    contentDescription = stringResource(if (torchOn) R.string.scan_torch_off else R.string.scan_torch_on),
                    tint = Color.White,
                )
            }
        }

        Column(
            Modifier
                .align(Alignment.Center)
                .offset(y = ViewfinderSize / 2 + Spacing.xxl),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                stringResource(R.string.scan_hint),
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                textAlign = TextAlign.Center,
            )
        }

        ScanFromImageButton(
            onClick = onPickImage,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = Spacing.xl),
        )
    }
}

@Composable
private fun ScanFromImageButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        shape = CircleShape,
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = Color.White.copy(alpha = 0.18f),
            contentColor = Color.White,
        ),
    ) {
        Icon(Icons.Rounded.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.size(Spacing.s))
        Text(stringResource(R.string.scan_from_image), style = MaterialTheme.typography.titleMedium)
    }
}

/**
 * Shows [content] once camera permission is granted. Otherwise asks for it, and if the person
 * said no for good, offers system settings plus scanning from an image, which needs no camera.
 */
@Composable
private fun CameraPermissionGate(onPickImage: () -> Unit, content: @Composable () -> Unit) {
    val context = LocalContext.current
    var granted by remember { mutableStateOf(context.hasCameraPermission()) }
    var permanentlyDenied by rememberSaveable { mutableStateOf(false) }
    var askedOnce by rememberSaveable { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        granted = isGranted
        val activity = context.findActivity()
        permanentlyDenied = !isGranted && activity != null &&
            !activity.shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)
    }

    // Re-check when coming back from Settings, and ask once on first open.
    LifecycleResumeEffect(Unit) {
        granted = context.hasCameraPermission()
        if (!granted && !askedOnce) {
            askedOnce = true
            launcher.launch(Manifest.permission.CAMERA)
        }
        onPauseOrDispose { }
    }

    if (granted) {
        content()
    } else {
        PermissionNeeded(
            permanentlyDenied = permanentlyDenied,
            onAllow = {
                if (permanentlyDenied) context.openAppSettings() else launcher.launch(Manifest.permission.CAMERA)
            },
            onPickImage = onPickImage,
        )
    }
}

@Composable
private fun PermissionNeeded(permanentlyDenied: Boolean, onAllow: () -> Unit, onPickImage: () -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(QRCacheTheme.colors.background),
    ) {
        EmptyState(
            icon = Icons.Rounded.QrCodeScanner,
            title = stringResource(R.string.camera_permission_title),
            message = stringResource(R.string.camera_permission_message),
            actionLabel = stringResource(
                if (permanentlyDenied) R.string.camera_permission_settings else R.string.camera_permission_allow,
            ),
            onAction = onAllow,
            modifier = Modifier.align(Alignment.Center),
        )
        FilledTonalButton(
            onClick = onPickImage,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = Spacing.xl)
                .height(44.dp),
            shape = CircleShape,
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = QRCacheTheme.colors.surfaceSecondary,
                contentColor = QRCacheTheme.colors.accent,
            ),
        ) {
            Icon(Icons.Rounded.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.size(Spacing.s))
            Text(stringResource(R.string.scan_from_image), style = MaterialTheme.typography.titleMedium)
        }
    }
}

/** Dims everything outside the viewfinder and draws its corner brackets. */
@Composable
private fun ViewfinderOverlay(success: Boolean, modifier: Modifier = Modifier) {
    val colors = QRCacheTheme.colors
    val bracketColor by animateColorAsState(
        targetValue = if (success) colors.success else Color.White,
        animationSpec = Motion.spring(),
        label = "viewfinderColor",
    )
    val scale by animateFloatAsState(
        targetValue = if (success) 0.94f else 1f,
        animationSpec = Motion.spring(),
        label = "viewfinderScale",
    )
    Canvas(modifier) {
        val side = ViewfinderSize.toPx() * scale
        val topLeft = Offset((size.width - side) / 2, (size.height - side) / 2)
        val radius = Radius.viewfinder.toPx()

        val dim = Path().apply {
            fillType = PathFillType.EvenOdd
            addRect(Rect(Offset.Zero, size))
            addRoundRect(RoundRect(Rect(topLeft, Size(side, side)), CornerRadius(radius)))
        }
        drawPath(dim, Color.Black.copy(alpha = 0.45f))
        drawBrackets(topLeft, side, radius, bracketColor, 5.dp)
    }
}

/** Rounded corner brackets only, no full box. */
private fun DrawScope.drawBrackets(topLeft: Offset, side: Float, radius: Float, color: Color, strokeWidth: Dp) {
    val stroke = strokeWidth.toPx()
    val arm = side * 0.22f
    val l = topLeft.x
    val t = topLeft.y
    val r = l + side
    val b = t + side
    val arc = Size(radius * 2, radius * 2)

    val path = Path().apply {
        moveTo(l, t + arm)
        lineTo(l, t + radius)
        arcTo(Rect(Offset(l, t), arc), 180f, 90f, false)
        lineTo(l + arm, t)

        moveTo(r - arm, t)
        lineTo(r - radius, t)
        arcTo(Rect(Offset(r - radius * 2, t), arc), 270f, 90f, false)
        lineTo(r, t + arm)

        moveTo(r, b - arm)
        lineTo(r, b - radius)
        arcTo(Rect(Offset(r - radius * 2, b - radius * 2), arc), 0f, 90f, false)
        lineTo(r - arm, b)

        moveTo(l + arm, b)
        lineTo(l + radius, b)
        arcTo(Rect(Offset(l, b - radius * 2), arc), 90f, 90f, false)
        lineTo(l, b - arm)
    }
    drawPath(path, color, style = Stroke(width = stroke, cap = StrokeCap.Round))
}

private fun scanImage(context: Context, uri: Uri, onFound: (String) -> Unit) {
    val image = try {
        InputImage.fromFilePath(context, uri)
    } catch (_: IOException) {
        Toast.makeText(context, R.string.scan_image_error, Toast.LENGTH_SHORT).show()
        return
    }
    val scanner = qrScanner()
    scanner.process(image)
        .addOnSuccessListener { barcodes ->
            val raw = barcodes.firstNotNullOfOrNull { it.rawValue }
            if (raw != null) onFound(raw) else Toast.makeText(context, R.string.scan_no_code_in_image, Toast.LENGTH_SHORT).show()
        }
        .addOnFailureListener {
            Toast.makeText(context, R.string.scan_image_error, Toast.LENGTH_SHORT).show()
        }
        .addOnCompleteListener { scanner.close() }
}

private fun Context.hasCameraPermission() =
    ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

private fun Context.openAppSettings() {
    startActivity(
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
    )
}
