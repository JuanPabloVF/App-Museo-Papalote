package com.example.mipapalote

import org.junit.Test

import org.junit.Assert.*


/**
 * Test para validar la funcionalidad de alternar el estado de edición (isEditMode).
 * Caso positivo: El estado debe alternar correctamente entre true y false.
 * Caso negativo: El estado no debe permanecer fijo al alternar.
 */
class EditModeTest {

    @Test
    fun `isEditMode should toggle between true and false`() {
        var isEditMode = false // Estado inicial

        // Cambiar a modo edición
        isEditMode = !isEditMode
        assertTrue("isEditMode debería ser true después de activarlo", isEditMode)

        // Volver al modo de solo lectura
        isEditMode = !isEditMode
        assertFalse("isEditMode debería ser false después de desactivarlo", isEditMode)
    }

    @Test
    fun `isEditMode should not remain fixed when toggled`() {
        var isEditMode = false // Estado inicial

        // Primer cambio: false -> true
        isEditMode = !isEditMode
        assertTrue("isEditMode debería cambiar a true", isEditMode)

        // Segundo cambio: true -> false
        isEditMode = !isEditMode
        assertFalse("isEditMode debería cambiar a false", isEditMode)

        // Tercer cambio: false -> true
        isEditMode = !isEditMode
        assertTrue("isEditMode debería volver a true", isEditMode)
    }
}

/**
 * Test para validar la función de verificación de correos electrónicos.
 * Caso positivo: correos válidos deben pasar la validación.
 * Caso negativo: correos inválidos no deben pasar la validación.
 */

fun isValidEmail(email: String): Boolean {
    val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    return email.matches(emailRegex.toRegex())
}



class EmailValidationTest {

    @Test
    fun `isValidEmail should return true for valid email`() {
        // Ejemplo de correo válido
        val validEmail = "example@example.com"

        // Llamada a la función
        val result = isValidEmail(validEmail)

        // Validación
        assertTrue("El correo válido debe pasar la validación", result)
    }

    @Test
    fun `isValidEmail should return false for invalid email`() {
        // Ejemplo de correos inválidos
        val invalidEmails = listOf(
            "example.com",         // Falta "@"
            "example@",            // Falta dominio
            "@example.com",        // Falta nombre
            "example@.com",        // Dominio incompleto
            "example@com",         // Falta "."
            ""                     // Vacío
        )

        // Validar cada caso
        for (email in invalidEmails) {
            val result = isValidEmail(email)
            assertFalse("El correo inválido ($email) no debe pasar la validación", result)
        }
    }
}
