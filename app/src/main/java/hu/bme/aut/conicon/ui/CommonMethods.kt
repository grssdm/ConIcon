package hu.bme.aut.conicon.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.content.res.AppCompatResources
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import hu.bme.aut.conicon.R
import java.text.SimpleDateFormat
import java.util.*

/**
 * This class contains all of the methods which are commonly used
 */
class CommonMethods {
    /**
     * Regex for the validation of the username
     */
    val regexUsername = "^(?=[a-zA-Z0-9._])(?!.*[_.]{2})[^_.].*[^_.]\$".toRegex()

    /**
     * This method makes the showing of EditText errors easier
     * @param context Context
     * @param til Field that caused the error
     * @param message Message of the error
     */
    fun showEditTextError(context: Context, til: TextInputLayout, message: String) {
        til.error = message
        til.defaultHintTextColor = AppCompatResources.getColorStateList(context, R.color.error)
        til.requestFocus()
    }

    /**
     * This method clears the EditText error
     * @param context Context
     * @param til Field that's error message should be deleted
     */
    fun removeEditTextError(context: Context, til: TextInputLayout) {
        til.error = ""
        til.defaultHintTextColor = AppCompatResources.getColorStateList(context, R.color.orange)
    }

    /**
     * Checks if the Password field and it's confirm field texts are the same
     * @param tietPassword The EditText of the password's field
     * @param tietConfirmPassword The EditText of the password's confirm field
     * @param tilConfirmPassword The password's confirm field
     * @param context Context
     * @return If the fields match, it returns true
     */
    fun checkPasswordsMatch(
        tietPassword: TextInputEditText,
        tietConfirmPassword: TextInputEditText,
        tilConfirmPassword: TextInputLayout,
        context: Context
    ) : Boolean {
        return when {
            tietPassword.text.toString() != tietConfirmPassword.text.toString() -> {
                showEditTextError(context, tilConfirmPassword, "The two passwords do not match!")
                false
            }
            else -> true
        }
    }

    /**
     * Checks if the length of the given EditText's text is enough for the Firebase to store
     * @param tiet The EditText of the field
     * @param til The field
     * @param context Context
     * @param minLength Minimum length of the text
     * @param maxLength Maximum length of the text
     * @return True if the text is between the given parameters
     */
    fun checkEditTextLength(
            tiet: TextInputEditText,
            til: TextInputLayout,
            context: Context,
            minLength: Int,
            maxLength: Int
    ) : Boolean {
        return when {
            tiet.text.toString().length in minLength..maxLength -> true
            else -> {
                showEditTextError(context, til, "This field's length must be between $minLength and $maxLength characters!")
                false
            }
        }
    }

    /**
     * Checks if the length of the given EditText's text matches to the given regex pattern
     * @param tiet The EditText of the field
     * @param til The field
     * @param context Context
     * @param regex Regular expression
     * @return True if the text matches
     */
    fun validateEditTextCharactersByRegex(
            tiet: TextInputEditText,
            til: TextInputLayout,
            context: Context,
            regex: Regex
    ) : Boolean {
        return when {
            tiet.text.toString().matches(regex) -> true
            else -> {
                showEditTextError(context, til, "This field contains not allowed characters!")
                false
            }
        }
    }

    /**
     * This method validates the EditText to match the rules which are given as parameters
     * @param context Context
     * @param tiet The EditText of the field
     * @param til The field
     * @param minLength Minimum length of the text
     * @param maxLength Maximum length of the text
     * @param regex Regular expression
     */
    fun validateEditText(context: Context, tiet: TextInputEditText, til: TextInputLayout, minLength: Int, maxLength: Int, regex: Regex) : Boolean {
        if (checkEditTextLength(tiet, til, context, minLength, maxLength)) {
            if (validateEditTextCharactersByRegex(tiet, til, context, regex)) {
                removeEditTextError(context, til)
                return true
            }
        }

        return false
    }

    /**
     * This method formats properly the given post date
     * @param date The Long form of the date to format
     */
    @SuppressLint("SimpleDateFormat")
    fun formatPostDate(date: Long) : String {
        val diff = Date().time - date

        return when {
            diff < 86400000 -> {
                SimpleDateFormat("HH:mm").format(date)
            }
            diff in 86400000..604799999 -> {
                SimpleDateFormat("EEEE").format(date)
            }
            diff in 604800000..31556951999 -> {
                SimpleDateFormat("MMMM d").format(date)
            }
            else -> {
                SimpleDateFormat("MMMM d, yyyy").format(date)
            }
        }
    }

    /**
     * This method formats properly the given conversation date
     * @param date The Long form of the date to format
     */
    @SuppressLint("SimpleDateFormat")
    fun formatConversationDate(date: Long) : String {
        val diff = Date().time - date

        return when {
            diff < 60000 -> {
                "now"
            }
            diff in 60000..3599999 -> {
                SimpleDateFormat("m").format(diff).plus("m")
            }
            diff in 3600000..86399999 -> {
                SimpleDateFormat("H").format(diff).plus("h")
            }
            diff in 86400000..604799999 -> {
                "${diff / 86400000}d"
            }
            else -> {
                "${diff / 604800000}w"
            }
        }
    }

    /**
     * This method formats properly the given message date
     * @param date The Long form of the date to format
     */
    @SuppressLint("SimpleDateFormat")
    fun formatMessageDate(date: Long) : String {
        val diff = Date().time - date

        return when {
            diff < 86400000 -> {
                SimpleDateFormat("HH:mm").format(date)
            }
            diff in 86400000..604799999 -> {
                SimpleDateFormat("EEEE, HH:mm").format(date)
            }
            diff in 604800000..31556951999 -> {
                SimpleDateFormat("MMMM d, HH:mm").format(date)
            }
            else -> {
                SimpleDateFormat("MMMM d, yyyy, HH:mm").format(date)
            }
        }
    }

    /**
     * This method starts the mobile's map application to show
     * the place whose coordinates were given as parameters
     * @param context Context
     * @param lat Latitude
     * @param lng Longitude
     */
    fun startMap(context: Context, lat: Double, lng: Double) {
        val uri = String.format(
            Locale.ENGLISH, "geo:%f,%f?z=%d&q=%f,%f (%s)",
            lat, lng, 16, lat, lng, "Photo was taken here"
        )
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        context.startActivity(intent)
    }
}