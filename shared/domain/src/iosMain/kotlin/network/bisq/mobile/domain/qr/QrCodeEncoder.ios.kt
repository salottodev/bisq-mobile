@file:OptIn(ExperimentalForeignApi::class)

package network.bisq.mobile.domain.qr

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import network.bisq.mobile.domain.utils.toNSData
import platform.CoreGraphics.CGBitmapContextCreate
import platform.CoreGraphics.CGColorSpaceCreateDeviceGray
import platform.CoreGraphics.CGColorSpaceRelease
import platform.CoreGraphics.CGContextDrawImage
import platform.CoreGraphics.CGContextRelease
import platform.CoreGraphics.CGContextSetInterpolationQuality
import platform.CoreGraphics.CGImageAlphaInfo
import platform.CoreGraphics.CGImageRelease
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.kCGInterpolationNone
import platform.CoreImage.CIContext
import platform.CoreImage.CIFilter
import platform.CoreImage.createCGImage
import platform.CoreImage.filterWithName
import platform.Foundation.setValue
import kotlin.math.roundToInt

/** Immutable and thread-safe per Apple docs; creating one per encode is costly. */
private val ciContext by lazy { CIContext() }

/** CIQRCodeGenerator renders at 1 px/module with a margin; the gray buffer is trimmed to the symbol. */
actual fun encodeQrCode(data: String): QrMatrix? {
    if (data.isEmpty()) return null
    val nsData = data.encodeToByteArray().toNSData()
    val filter = CIFilter.filterWithName("CIQRCodeGenerator") ?: return null
    filter.setValue(nsData, forKey = "inputMessage")
    filter.setValue("L", forKey = "inputCorrectionLevel")
    val output = filter.outputImage ?: return null
    val (width, height) =
        output.extent.useContents { size.width.roundToInt() to size.height.roundToInt() }
    if (width <= 0 || height <= 0) return null

    val cgImage = ciContext.createCGImage(output, fromRect = output.extent) ?: return null
    val colorSpace = CGColorSpaceCreateDeviceGray()
    val pixels = ByteArray(width * height)
    try {
        pixels.usePinned { pinned ->
            val context =
                CGBitmapContextCreate(
                    pinned.addressOf(0),
                    width.toULong(),
                    height.toULong(),
                    8u,
                    width.toULong(),
                    colorSpace,
                    CGImageAlphaInfo.kCGImageAlphaNone.value,
                ) ?: return null
            try {
                CGContextSetInterpolationQuality(context, kCGInterpolationNone)
                CGContextDrawImage(context, CGRectMake(0.0, 0.0, width.toDouble(), height.toDouble()), cgImage)
            } finally {
                CGContextRelease(context)
            }
        }
    } finally {
        CGImageRelease(cgImage)
        CGColorSpaceRelease(colorSpace)
    }
    return qrMatrixFromGrayPixels(pixels, width, height)
}
