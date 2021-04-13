package com.hito.nikolay.devtesthh.extensions

import android.text.TextUtils
import java.util.regex.Pattern

// Checking the string for compliance with the email
fun String.isEmailValid(): Boolean {
    return !TextUtils.isEmpty(this) && android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

// Checking the string for compliance with the password
fun String.isPasswordValid(): Boolean {
    val passwordPattern = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+\$).{6,}\$")
    return !TextUtils.isEmpty(this) && passwordPattern.matcher(this).matches()
}