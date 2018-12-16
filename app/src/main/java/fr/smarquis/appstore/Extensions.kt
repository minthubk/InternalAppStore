package fr.smarquis.appstore

import android.content.Context
import android.content.Intent
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.signature.ObjectKey
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FileDownloadTask
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.lang.Exception

//region Firebase

fun FirebaseStorage.with(reference: String): Any? {
    try {
        return getReference(reference)
    } catch (e: Exception) {
    }
    try {
        return getReferenceFromUrl(reference)
    } catch (e: Exception) {
    }
    return null
}

fun Version.getActiveDownloadTask(): FileDownloadTask? {
    val ref = apkRef ?: return null
    return Firebase.storage.getReference(ref).activeDownloadTasks.firstOrNull()?.takeIf { !it.isComplete }
}

fun StorageReference.cancelActiveDownloadTasks() {
    for (activeDownloadTask in activeDownloadTasks) {
        if (activeDownloadTask.isComplete.not()) {
            activeDownloadTask.cancel()
        }
    }
}

fun FirebaseDatabase.store(): DatabaseReference = reference.child("store")

fun FirebaseDatabase.applications(): DatabaseReference = store().child("applications")

fun FirebaseDatabase.application(key: String): DatabaseReference = applications().child(key)

fun FirebaseDatabase.versions(): DatabaseReference = store().child("versions")

fun FirebaseDatabase.versions(applicationKey: String): DatabaseReference = versions().child(applicationKey)

fun FirebaseDatabase.version(applicationKey: String, versionKey: String): DatabaseReference = versions(applicationKey).child(versionKey)

//endregion

//region Intents

fun Intent?.isSafe(context: Context): Boolean {
    return this != null && resolveActivity(context.packageManager) != null
}

fun AppCompatActivity.safeStartActivity(intent: Intent?): Boolean {
    if (intent.isSafe(this)) {
        startActivity(intent)
        return true
    }
    return false
}

fun AppCompatActivity.safeStartActivityForResult(intent: Intent?, requestCode: Int): Boolean {
    if (intent.isSafe(this)) {
        startActivityForResult(intent, requestCode)
        return true
    }
    return false
}

//endregion

//region Apps

fun Application.imageTransitionName(): String = "image_$key"

fun Application.findImageReference(): Any {
    val reference = image ?: "applications/$key/image"
    return Firebase.storage.with(reference) ?: reference
}

val DEFAULT_APPLICATION_IMAGE_REQUEST_OPTIONS = RequestOptions()
        .override(Target.SIZE_ORIGINAL)
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .centerCrop()
        .placeholder(R.drawable.item_application_icon_placeholder)
        .autoClone()

fun Application.loadImageInto(imageView: ImageView, glide: RequestManager) {
    glide.load(findImageReference()).apply(DEFAULT_APPLICATION_IMAGE_REQUEST_OPTIONS.signature(ObjectKey(image.orEmpty()))).into(imageView)
}

fun Application.isMyself(context: Context) = context.packageName == packageName

//endregion
