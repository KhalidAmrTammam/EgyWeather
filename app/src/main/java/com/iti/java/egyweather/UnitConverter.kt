package com.iti.java.egyweather

object UnitConverter {
    fun convertTemperature(temp: Double, fromUnit: String, toUnit: String): Double {
        return when (toUnit) {
            "celsius" -> when (fromUnit) {
                "fahrenheit" -> (temp - 32) * 5/9
                "kelvin" -> temp - 273.15
                else -> temp
            }
            "fahrenheit" -> when (fromUnit) {
                "celsius" -> (temp * 9/5) + 32
                "kelvin" -> (temp - 273.15) * 9/5 + 32
                else -> temp
            }
            else -> temp
        }
    }

    fun convertWindSpeed(speed: Double, fromUnit: String, toUnit: String): Double {
        return when {
            fromUnit == "m/s" && toUnit == "mph" -> speed * 2.23694
            fromUnit == "mph" && toUnit == "m/s" -> speed / 2.23694
            else -> speed
        }
    }
}