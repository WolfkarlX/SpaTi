
package com.example.spaTi.util

import android.app.Dialog
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.provider.Settings.Secure.getString
import android.view.*
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.spaTi.R
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup


fun View.hide(){
    visibility = View.GONE
}

fun View.show(){
    visibility = View.VISIBLE
}

fun View.disable(){
    isEnabled = false
}

fun View.enabled(){
    isEnabled = true
}

fun Fragment.toast(msg: String?){
    Toast.makeText(requireContext(),msg,Toast.LENGTH_LONG).show()
}

fun Fragment.toastShort(msg: String?){
    Toast.makeText(requireContext(),msg,Toast.LENGTH_SHORT).show()
}

fun ChipGroup.addChip(
    text: String,
    isTouchTargeSize: Boolean = false,
    closeIconListener: View.OnClickListener? = null
) {
    val chip: Chip = LayoutInflater.from(context).inflate(R.layout.item_chip,null,false) as Chip
    chip.text = if (text.length > 9) text.substring(0,9) + "..." else text
    chip.isClickable = false
    chip.setEnsureMinTouchTargetSize(isTouchTargeSize)
    if (closeIconListener != null){
        chip.closeIcon = ContextCompat.getDrawable(context, com.google.android.material.R.drawable.ic_mtrl_chip_close_circle)
        chip.isCloseIconVisible = true
        chip.setOnCloseIconClickListener(closeIconListener)
    }
    addView(chip)
}

fun Context.createDialog(layout: Int, cancelable: Boolean): Dialog {
    val dialog = Dialog(this, android.R.style.Theme_Dialog)
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setContentView(layout)
    dialog.window?.setGravity(Gravity.CENTER)
    dialog.window?.setLayout(
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.WRAP_CONTENT
    )
    dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    dialog.setCancelable(cancelable)
    return dialog
}

val Int.dpToPx: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()

val Int.pxToDp: Int
    get() = (this / Resources.getSystem().displayMetrics.density).toInt()

fun String.isValidEmail() =
    isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()

fun getAge(day:Int, month: Int, year:Int): Int {
    val today = java.util.Calendar.getInstance()
    val currentYear = today.get(java.util.Calendar.YEAR)
    val currentMonth = today.get(java.util.Calendar.MONTH) + 1
    val currentDay = today.get(java.util.Calendar.DAY_OF_MONTH)

    var age = currentYear - year

    if (month > currentMonth || (month == currentMonth && day > currentDay)) {
        age--
    }

    return age
}

fun extractNumbersFromDate(input: String): Array<String> {
    // Split the input string by "/" and convert it to an array
    return input.split("/").toTypedArray()
}

fun validatePassword(context: Context, password: String): Pair<Boolean, String> {
    val minLength = 8

    val hasUppercase = Regex("[A-Z]")
    val hasLowercase = Regex("[a-z]")
    val hasDigit = Regex("\\d")
    val hasSpecialChar = Regex("[!@#\$%^&*(),.?\":{}|<>]")
    val consecutiveRepeat = Regex("(.)\\1{2,}")

    // Check each requirement and return a message if the password doesn't meet the criteria
    if (password.length < minLength) {
        return Pair(false, context.getString(R.string.short_password))
    }
    if (!hasUppercase.containsMatchIn(password)) {
        return Pair(false, context.getString(R.string.invalid_password_uppercase))
    }
    if (!hasLowercase.containsMatchIn(password)) {
        return Pair(false, context.getString(R.string.invalid_password_lowercase))
    }
    if (!hasDigit.containsMatchIn(password)) {
        return Pair(false, context.getString(R.string.invalid_password_digit))
    }
    if (!hasSpecialChar.containsMatchIn(password)) {
        return Pair(false, context.getString(R.string.invalid_password_special))
    }

    // Check for repeated characters (all characters are the same)
    if (password.toSet().size == 1) {
        return Pair(false, context.getString(R.string.invalid_password_repeated))
    }

    if (consecutiveRepeat.containsMatchIn(password)) {
        return Pair(false, context.getString(R.string.invalid_password_consecutive_repeat))
    }

    // If all requirements are met
    return Pair(true, "Password is strong.")
}

