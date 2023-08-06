package com.pablo.trafficalert.utils

object Constants {
    const val EMAIL_PATTERN = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$"
    const val PHONE_PATTERN = "^+27|0[6-8][0-9]{8}$"
    const val CERT_ERROR_MSG = "An internal error has occurred. [ java.security.cert.CertPathValidatorException:Trust anchor for certification path not found. ]"
}