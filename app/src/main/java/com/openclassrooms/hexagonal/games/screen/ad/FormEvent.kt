package com.openclassrooms.hexagonal.games.screen.ad

import android.net.Uri
import androidx.annotation.StringRes
import com.openclassrooms.hexagonal.games.R


/**
 * A sealed class representing different events that can occur on a form.
 */
sealed class FormEvent {
  
  /**
   * Event triggered when the title of the form is changed.
   *
   * @property title The new title of the form.
   */
  data class TitleChanged(val title: String) : FormEvent()
  
  /**
   * Event triggered when the description of the form is changed.
   *
   * @property description The new description of the form.
   */
  data class DescriptionChanged(val description: String) : FormEvent()

  /**
   * Event triggered when the photo of the form is changed.
   *
   * @property photo The new photo of the form.
   */
  data class PhotoChanged(val photo: Uri?) : FormEvent()


}

/**
 * A sealed class representing different errors that can occur on a form.
 *
 * Each error holds a resource ID for the corresponding error message string.
 */
sealed class FormError(@StringRes val messageRes: Int) {
  
  /**
   * Error indicating an issue with the form title.
   *
   * The actual error message can be retrieved using the provided resource ID (`R.string.error_title`).
   */
  data object TitleError : FormError(R.string.error_title)

  data object DescriptionOrPictureError : FormError(R.string.error_description_or_photo)
}
